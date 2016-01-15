package net.osaris.exterminate.entities;


import net.osaris.exterminate.CameraController;
import net.osaris.exterminate.InGame;
import net.osaris.exterminate.SoundManager;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Player extends LivingEntity {


//private static Entity.Constructor constructeur = null;

private static final float BASE_SIZE_Y = 3.3f;



public int actionToSend;

public float ammo = 9.5f;


public Player() {
	
		hasPhysics = true;

		cantDie = true;
	/*	if(constructeur==null) {
			btBoxShape box = new btBoxShape(sizeBox.cpy().scl(0.5f));

			constructeur = new Entity.Constructor(box, 1f);
		}
		*/
        
		
		model = null;
		if(model!=null) {
			transform=model.transform;
		}
		else {
			transform = new Matrix4();
		}
		
		ami = true;


		size = new Vector3(0.0255f, 0.0255f, 0.0255f);
		
	
	 	
	 	mustTPTo = new Vector3(0, 60, -0);
	 	pos.set(mustTPTo);
	 // 	mustTPTo = new Vector3(460, 111, 1112);
		  	
	  	managedDraw = false;
	 
	  	fulltimeBeforeRespawn = 0f;
	  	
	  	timeBeforeRespawn = 0f;
	  	dir.set(-1f, 0, 1);
	}



	
	public int interaction = 0;



	private Entity lastOther;
	
	@Override
	public void update(float deltaTime) {
		lastSound-=deltaTime;
		if(ammo<9.5f){
			ammo+=deltaTime;
		}
		ammo = Math.min(ammo, 8.5f);
		
		if(reelPosy<40f && !InGame.dead) {
			vchoc.y+=deltaTime*(40f-reelPosy+2f)*150f;
		}
		if(reelPosy<getHauteurSol()+0.5f && !InGame.dead) {
			vchoc.y=30f;
		}

		super.update(deltaTime);

	
		CameraController.fly = !InGame.dead;
		
		acquireTarget(10f, 150f);

	}	

	


	public void acquireTarget(float distMin, float distMax) {

		float refAngle = 180;
		target = null;
		tmpv.set(CameraController.camera.direction).nor();

		for (Entity ennemi : InGame.entityManager.managedEntities.values()) {
			tmpv2.set(ennemi.pos).add(Vector3.Y).sub(CameraController.cameraRealPosition).nor();
			
			float angle = (float) Math.abs(Math.acos(tmpv.dot(tmpv2)));
			
			if ((ennemi instanceof Monster || ennemi instanceof Shark) && !((LivingEntity) ennemi).dead
					&& ennemi.pos.y>48f	&& ennemi.pos.dst2(CameraController.cameraRealPosition) < distMax * distMax
					&& angle < 0.4f) {
				float distp = Math.max(ennemi.pos.dst(CameraController.cameraRealPosition)-20f, 0f)*0.01f;
				if (angle+distp < refAngle) {
					refAngle =angle+distp;
					target = (LivingEntity) ennemi;

				}
			}
		}
	}
	
	static String[] messages = {"Boom!", "Blam!", "Make it fly!", "Higher!", "It's a bird!", "KILL the bird!!!", "NOW KILL IT!", "Or just play with it?", "Ok, right", "Enough!", "Exterminate! Please!", "It must be EXTERMINATED!"};
	static long lastBoom = 0;
	static long reallastBoom = 0;
	static long nbBoom = 0;
	
	@Override
	public void onCollide(Entity other) {
		
		if(System.currentTimeMillis()-reallastBoom<200 || InGame.dead) return;
		reallastBoom = System.currentTimeMillis();
		
		other.vchoc.set(v).scl(5f);
		other.v.y=50f;
		other.vchoc.y=0f;
		other.targetDir.set(dir);
		if(other instanceof Shark) {
			((Shark) other).jumping = false;
		}
		
		
		if(System.currentTimeMillis()-lastBoom<10000 && other==lastOther) {
			nbBoom++;
			if(nbBoom>=messages.length) nbBoom=messages.length-1; 
			InGame.score(500+nbBoom*100, messages[(int) nbBoom]);

			
			lastBoom=Math.min(System.currentTimeMillis(), lastBoom+1000);
		}
		else {
			lastBoom = System.currentTimeMillis();
			nbBoom = 0;
			InGame.score(500, "Boom!");
			lastOther = other;
		}

		
//		SoundManager.playSound(SoundManager.blam, this, 1.0f);		
		SoundManager.playSound(SoundManager.boom, this, 0.4f);

	}



boolean pair = false;
	public void attack() {
		if(ammo>0.5f && !InGame.dead) {
			ammo-=1f;
		}
		else {
			return;
		}
		
/*		Missile missile = new Missile();
		missile.ownerId = this.id;
		missile.target = target;
		missile.pos.set(pos);
		missile.pos.mulAdd(droite, 4f);
		missile.mustTPTo = new Vector3(missile.pos);
		missile.onCreate(0, world);
		
		missile = new Missile();
		missile.ownerId = this.id;
		missile.target = target;
		missile.pos.set(pos);
		missile.pos.mulAdd(droite, -4f);
		missile.mustTPTo = new Vector3(missile.pos);
		missile.onCreate(0, world);*/
		pair=!pair;
		DoubleMissile missile = new DoubleMissile();
		missile.ownerId = this.id;
		missile.target = target;
		missile.pos.set(pos);
		missile.pos.y-=1f;
		missile.pos.mulAdd(droite, pair ? 2f : -2f);
		missile.pos.mulAdd(CameraController.camera.direction, -10f);
		missile.mustTPTo = new Vector3(missile.pos);
		missile.onCreate(0, world);
		if(lastSound<0f) {
			SoundManager.playSound(10+(int) (Math.random()*3), this, 0.35f);
			lastSound=1f;
		}
	}
float lastSound = 0f;
	
}
