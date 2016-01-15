package net.osaris.exterminate.entities;

import net.osaris.exterminate.Assets;
import net.osaris.exterminate.CameraController;
import net.osaris.exterminate.InGame;
import net.osaris.exterminate.SoundManager;
import net.osaris.exterminate.world.World;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Vector3;

public class DoubleMissile extends LivingEntity {

	public DoubleMissile() {
		super();		
		hasPhysics = true;
		
		size.set(1f, 1f, 1f);
		model = new ModelInstance(Assets.missile);
		
		transform = model.transform;
	}

	ParticleEffect trainee = null;
	ParticleEffect trainee2 = null;
	ParticleEffect boom = null;
	LivingEntity victime = null;
	
	float timeDead = 0;
	float timeLived = 0;
	private boolean mustDie = false;

	@Override
	public void onCreate(long id, World world) {
		
		super.onCreate(id, world);
		
		SoundManager.playSound(SoundManager.launch2, this, 0.5f);
		SoundManager.playSound(SoundManager.launch, this, 0.5f);

		
		
		vitesse = 0f;
		
		trainee = Assets.effectFeuTrainee.obtain();
		
		trainee.setTransform(IDTM);
		trainee.translate(InGame.masterTranslate);
		trainee.getControllers().first().scale(0.25f, 0.25f, 0.25f);

		trainee.start();
		
		trainee2 = Assets.effectFeuTrainee.obtain();
		
		trainee2.setTransform(IDTM);
		trainee2.translate(InGame.masterTranslate);
		trainee2.getControllers().first().scale(0.25f, 0.25f, 0.25f);

		trainee2.start();
		hasPhysics = true;
		v.set(CameraController.camera.direction);
		v.nor();
		dir.set(v);
		vitesse = 0f;

	}
		
	
	@Override
	public void update(float deltaTime) {
		if(target ==null || target.removeMe) target = InGame.player.target;
		
		
		
		
		if(target!=null) {
			tmpv.set(target.pos).mulAdd(pos, -1);
			if(!(target instanceof Shark)) tmpv.y+=2f;
			tmpv.nor();
			
			lerp(v, tmpv, target instanceof Shark ? 20f : 5f, deltaTime);
			v.nor();
			dir.set(v);
			vitesse = 80f;
			
		}
		if(target==null) {
			v.set(CameraController.camera.direction);
			v.nor();
			dir.set(v);
			
		}
		vitesse = lerp(vitesse, 100f, 15f, deltaTime);

		
//		super.update(deltaTime);

		timeLived+=deltaTime;
		if(mustDie  || (timeLived>10f && !dead)) {
			
			
			dead = true;
			boom = Assets.effectFeuBoom.obtain();

			boom.setTransform(IDTM);
			boom.translate(pos);
			boom.translate(InGame.masterTranslate);
			boom.getControllers().first().scale(4f, 4f, 4f);
			boom.start();
		
			mustDie = false;
		}
		

		
		if(dead) {

			trainee.setTransform(IDTM);
			trainee.translate(pos);
			if(victime!=null) {
				tmpv.set(0f, 2f, 0f);
				trainee.translate(tmpv);
				trainee.translate(InGame.masterTranslate);
		//		boom.translate(Vector3.Y.cpy().scl(victime.sizeBox.y*0.5f));
			}
			
			
			
			timeDead+=deltaTime;

			if(timeLived>5f || timeDead>(victime!=null ? victime.fulltimeBeforeRespawn : 0.5f)) {
				if(trainee!=null) {
					Assets.effectFeuTrainee.free(trainee);
				}
				if(trainee2!=null) {
					Assets.effectFeuTrainee.free(trainee2);
				}
				if(boom!=null) {
					Assets.effectFeuBoom.free(boom);
				}
				removeMe = true;
			}
			
			return;
		}

	
		
		previousPos.set(pos);
		pos.mulAdd(v, deltaTime*vitesse);

		float puissanceDispo = 100;
		
		if(pos.y<getHauteurSol()) {

			SoundManager.playSound(SoundManager.blam, this, 1.0f);		
			SoundManager.playSound(SoundManager.boom, this, 1.0f);
			
			
		
			mustDie = true;
			pos = previousPos;
		}

		
		transform.idt();
		transform.translate(pos);
		transform.translate(InGame.masterTranslate);

		tmp.set(-dir.x, -dir.z);
		angleCorps = -90f - tmp.angle();
		transform.rotate(up, angleCorps);
		
		
		tmp.set(-dir.y, tmp.len());
		angleY =  - tmp.angle();
		
		transform.rotate(up, 180f);

		
		anglePenche-=deltaTime*180f;
		transform.rotate(Vector3.X, -angleY+90f);
//		transform.rotate(Vector3.X, 45f);
		baseRotationY += deltaTime*360f;
		transform.rotate(up, 90f);
		transform.translate(-5f, 0.0f, 00.0f);
		
		
		transform.rotate(Vector3.X, -baseRotationY);
	
		
		
		trainee.setTransform(IDTM);
//		trainee.translate(pos);
	//	trainee.translate(InGame.masterTranslate);
	//	trainee.rotate(dir, baseRotationY);
		tmpv.set(4f, 0f, 2.f).mul(transform);
		trainee.translate(tmpv);
		
		
		trainee2.setTransform(IDTM);
	//	trainee2.translate(pos);
	//	trainee2.translate(InGame.masterTranslate);
	//	trainee2.rotate(dir, baseRotationY);
		tmpv.set(4f, 0f, -2.f).mul(transform);
		trainee2.translate(tmpv);
		
		
	}

	
	@Override
	public void draw(ModelBatch modelBatch, Environment environment,
			boolean pourOmbre) {
		if(dead) return;
		
		if (model == null)
			return;
		
		transform.scale(size.x, size.y, size.z);
		// System.out.println(this);
		
		transform.translate(0f, 0f, 2.f);
		
		modelBatch.render(model, environment);

		transform.translate(0f, 0f, -4.f);
		
		modelBatch.render(model, environment);
		
		transform.translate(0f, 0f, 2.f);

	//	transform.rotate(Vector3.X, baseRotationY);

		transform.scale(1f / size.x, 1f / size.y, 1f / size.z);

	}
	
	
	static long lastBoom = 0;
	static long nbBoom = 0;
	
	static String[] messages = {"Exterminate!", "Destroy!", "EXTERMINATE!", "DESTROY!", "KILL THEM ALL!", "EXTERMINATION OF ALL LIFE!!!", "FATALITY!", "MORE! MORE! MORE!", "LIFE IS STUPID!", "THIS IS INDIEPOCALYPSE!", "Love, mum.", "You must be EXTERMINATED!"};
	
	@Override
	public void onCollide(Entity other) {
		if(!mustDie && !dead && other!=owner() && other.owner()!=owner() && other instanceof LivingEntity) {
			
			
			victime=(LivingEntity)other;
			
			
			if(other instanceof DoubleMissile || victime.removeMe) return;
			
			SoundManager.playSound(SoundManager.blam, this, 0.75f);		
			SoundManager.playSound(SoundManager.boom, this, 0.75f);
			victime.removeMe = true;
		

			Vector3 vers = v.cpy().scl(vitesse);
			victime.onAttackedBy(this, owner(), 35, vers);
			
			if(System.currentTimeMillis()-lastBoom<2000) {
				nbBoom++;
				if(nbBoom>=messages.length) nbBoom=messages.length-1; 
				InGame.score(1000+nbBoom*200, messages[(int) nbBoom]);

				
				lastBoom=Math.min(System.currentTimeMillis(), lastBoom+1000);
			}
			else {
				lastBoom = System.currentTimeMillis();
				nbBoom = 0;
				InGame.score(1000, "Exterminate!");
			}
			InGame.nbKills++;
			
			mustDie  = true;

			victime.auSol = false;
			victime.v.y+=6f;
			
			pos = victime.pos;
			pos.y+=2f;
			victime = null;
		}
	}
	

	
	public void onFinish() {
			
	}

}
