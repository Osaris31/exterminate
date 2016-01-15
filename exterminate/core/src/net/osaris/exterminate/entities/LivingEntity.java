package net.osaris.exterminate.entities;

import net.osaris.exterminate.CameraController;
import net.osaris.exterminate.InGame;
import net.osaris.exterminate.world.IslandManager;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public abstract class LivingEntity extends Entity {
	public ModelInstance model;

	public int actionTodoOneTime = 0;

	public float vitesseWalk = 3.0f;
	public float vitesseBoost = 0.0f;
	public float vitesseRun = 4.5f;
	public Vector3 up = new Vector3(0, 1, 0);

	public float life = 100f;
	public float angleX = 0f;
	public float angleY = 0f;

	public Vector3 handPos = new Vector3();
	public Vector3 multiPos;

	public Vector3 targetPos = new Vector3();

	public boolean dead = false;
	public boolean backward = false;
	public boolean cantDie = false;

	public float fulltimeBeforeRespawn = 3f;
	public float timeBeforeRespawn = fulltimeBeforeRespawn;

	public float timeSinceAttack = 0f;
	public LivingEntity attaquelancee;

	public boolean attacking = false;

	public float offsety = 0;

	public Vector3 droite = new Vector3();

	public float angleCorps;

	public boolean ami = false;
	public double chanceFuite = 0f;
	public boolean fuit = false;

	public float maxLife = 100f;

	private float regenSpeed = 3f;

	private float timeBeforeRegen;

	protected float anglePenche = 0;

	protected float baseRotationY = 0f;

	@Override
	public void draw(ModelBatch modelBatch, Environment environment,
			boolean pourOmbre) {
		if (model == null)
			return;
		if (InGame.wallhack && !pourOmbre) {
			InGame.shapeRenderer.setTransformMatrix(model.transform);

			InGame.shapeRenderer.box(-sizeBox.x * 0.5f, 0, sizeBox.z * 0.5f,
					sizeBox.x, sizeBox.y, sizeBox.z);

		}
		//
		transform.scale(size.x, size.y, size.z);
		// System.out.println(this);
		model.transform.rotate(Vector3.X, -baseRotationY);
		transform.rotate(Vector3.Z, angleY);
		modelBatch.render(model, environment);
		transform.rotate(Vector3.Z, -angleY);

		model.transform.rotate(Vector3.X, baseRotationY);

		transform.scale(1f / size.x, 1f / size.y, 1f / size.z);
		//
		// transform.rotate(Vector3.X, -anglePenche);

	}

	@Override
	public void update(float deltaTime) {
		if (target != null && (target.dead || target.pos.dst(pos) > 100f)) {
			target = null;
		}
		if (timeBeforeRegen < 0f) {
			life = Math.min(maxLife, life + deltaTime * regenSpeed);
		} else {
			timeBeforeRegen -= deltaTime;
		}

		dir.nor();

		// attacking = attaquelancee!=null && attaquelancee.update(deltaTime);

		if (!attacking) {
			timeSinceAttack += deltaTime;
		} else {
			timeSinceAttack = 0f;
		}

		if (life < 0) {
			life = 0f;
			dead = true;
		}

		if (dead) {
			v.x = 0;
			v.z = 0;

			timeBeforeRespawn -= deltaTime;
			if (timeBeforeRespawn < 0) {
				if (cantDie) {
					timeBeforeRespawn = fulltimeBeforeRespawn;
					dead = false;
					life = maxLife;
				} else {
					// respawnAround(this);
					removeMe = true;
				}
			}
		}

		vchoc.mulAdd(vchoc, Math.max(-1, -deltaTime * 3.0f));
		if (vchoc.len() < 0.5f) {
			vchoc.setZero();
		}

		float timetodo = Math.min(deltaTime, 0.25f);
		/*
		 * while(timetodo>0) { appliquerVitesse(Math.min(timetodo, 0.1f));
		 * timetodo-=0.1f; }
		 */
		appliquerVitesse(timetodo * 0.5f);
		appliquerVitesse(timetodo * 0.5f);
		// appliquerVitesse(timetodo*0.34f);
		transform.idt();

		// model.transform.translate(0, CameraController.cameraMode==0 ? 0 :
		// offsety, 0);
		transform.translate(pos);
		transform.translate(InGame.masterTranslate);

		tmp.set(-dir.x, -dir.z);
		angleCorps = -90f - tmp.angle();
		transform.rotate(up, angleCorps);

		if(this instanceof Shark) transform.translate(0, 0.5f, -5.5f);
		transform.rotate(Vector3.X, anglePenche);

	}

	public void onAttackedBy(LivingEntity attaque, LivingEntity other,
			float force, Vector3 blastDir) {
		if (attaque != null) {
			attaque.target = this;
		}

		if (Math.random() / (force / 30f) < chanceFuite) {
			fuit = true;
		}

		life -= force;
		if (other != null && other != this) {
			target = other;
			other.target = this;
		}

		timeBeforeRegen = 4f;

		if (blastDir != null) {
			this.blam(blastDir);
		}
	}

	public boolean sousEau = false;

	public float reelPosy;
	private Vector3 previousv = new Vector3();

	public void appliquerVitesse(float deltaTime) {

		pos.y = reelPosy;

		float hauteurSolInitiale = getHauteurSol();

		previousPos.set(pos);

		boolean glisse = false;

		// on applique la vitesse
		float pente = 0;

		if (pos.y < IslandManager.hauteurEau() - 2f) {
			sousEau = true;
		} else {
			sousEau = false;
		}

		// on applique quand même la gravite sinon on est dans la ****
		if (!(this instanceof Player)
				|| (!CameraController.fly && !CameraController.creafly)) {
			if (!sousEau) {
				v.y -= gravity * deltaTime;
			} else {
				v.y -= gravity * deltaTime * 0.1f;
				v.y = Math.max(-4f, v.y);
			}
		}

		// je te propose, que la vitesse, si on est au sol, on l'ajuste en
		// fonction de la normal.
		if (!sousEau && pos.y <= world.getHauteurAt(pos.x, pos.z)) {
			onChunk.getNormal(pos.x, pos.z, tmpv2);
			// System.out.println(tmpv2);
			float a = tmpv2.dot(dir);
			if (a < -0.5) {
				pente = (-a - 0.5f) * 2.0f;
			}
			if (tmpv2.y < 0.5) {
				vchoc.add(tmpv2.x * 5f, -tmpv2.y, tmpv2.z * 5f);
				v.y = 0;
				tombe = true;
				glisse = true;
			}
		}

		previousv.set(v);
		pos.mulAdd(v, deltaTime * (1f - pente));
		pos.mulAdd(vchoc, deltaTime);
		pos.add(moveByCollisions);
		moveByCollisions.setZero();

		pos.y += 1f;
		float nouvelleHauteurSol = getHauteurSol();
		tmpv.set(pos);
		tmpv.y = nouvelleHauteurSol + 0.1f;// le +0.1f est important sinon on ne
											// monte pas les slab.
		pos.y -= 1f;

		nouvelleHauteurSol = getHauteurSol();
		if (!tombe
				&& !glisse
				&& nouvelleHauteurSol > hauteurSolInitiale
						- (v.y < 0 ? 0.5f : 0.0f)
				&& nouvelleHauteurSol <= hauteurSolInitiale + 1.4f
				&& !CameraController.fly && !CameraController.creafly
				&& pos.y > nouvelleHauteurSol - 0.5f
				&& pos.y <= nouvelleHauteurSol + 1f) {
			pos.y = nouvelleHauteurSol;
			// System.out.println(pos.y+" nouveau ok");

		}

		if (v.y < 0f) {
			v.y = Math.max(v.y,
					Math.min(0f, (pos.y - previousPos.y) / deltaTime));
		}

		v.y = Math.min(v.y, 100f);
		v.y = Math.max(v.y, -60f);

		// System.out.println(pos.y+" vs "+getHauteurSol()+" apres monter");

		// gravity
		if (pos.y > getHauteurSol() - 0.2 && pos.y < getHauteurSol() + 0.2
				&& !glisse) {
			if (v.y <= 0) {
				pos.y = getHauteurSol();
				if (v.y < -30.0f && !auSol) {
				}
				tombe = false;
				v.y = 0;
			} else {

			}
			auSol = true;
		} else if (!(this instanceof Player)
				|| (!CameraController.fly && !CameraController.creafly)) {

			if (pos.y > getHauteurSol() + 1.0)
				tombe = true;

			auSol = false;
		}

		// au cas où.
		if (pos.y < world.getHauteurAt(pos.x, pos.z)) { // ||
														// (pos.y<getHauteurSol()
														// &&
														// pos.y>getHauteurSol()-0.1f
														// && v.y<=0)
			pos.y = world.getHauteurAt(pos.x, pos.z);
			auSol = true;
			tombe = false;
		}

		reelPosy = pos.y;
		/*
		 * if(!canWalk(pos) && canWalk(previousPos)) { pos.set(previousPos); }
		 */

		pos.y = lerp(smoothPosY, reelPosy, 15f, deltaTime);

		smoothPosY = pos.y;

		/*
		 * if(this instanceof Boy) {
		 * 
		 * pos.y=getHauteurSol(); System.out.println(pos.y); }
		 */
		/*
		 * if(!tombe && pos.y<World.getHauteurAt(pos.x, pos.z)+0.8f) { // ||
		 * (pos.y<getHauteurSol() && pos.y>getHauteurSol()-0.1f && v.y<=0)
		 * pos.y=World.getHauteurAt(pos.x, pos.z); }
		 */

		// calcul de la penchitude
		if (sizeBox.z > 1.2f) {
			tmpv2.set(-dir.x, 0.0f, -dir.z).nor();
			tmpv.set(pos).mulAdd(tmpv2, sizeBox.z * 0.5f);
			float y1 = getHauteurSol(tmpv);

			tmpv.set(pos).mulAdd(tmpv2, -sizeBox.z);
			float y2 = getHauteurSol(tmpv);

			anglePenche = lerp(
					anglePenche,
					-(float) Math.toDegrees(Math.atan((y2 - y1) / sizeBox.z)) * 0.9f,
					10f, deltaTime);

		}
//		pos.y = 50f;

		handPos.set(pos);
		handPos.mulAdd(droite, 0.9f);
		handPos.y += 2.5f;
	}

	public float smoothPosY = 0f;

	static Vector3 axe = new Vector3();


	public boolean intersect(Vector3 origin, Vector3 direction, Vector3 tmp) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setAnimation(String anim, float f) {
		// TODO Auto-generated method stub

	}

}
