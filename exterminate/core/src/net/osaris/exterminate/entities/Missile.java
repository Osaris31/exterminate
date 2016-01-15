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

public class Missile extends LivingEntity {

	public Missile() {
		super();		
		hasPhysics = true;
		
		size.set(1f, 1f, 1f);
		model = new ModelInstance(Assets.missile);
		
		transform = model.transform;
	}

	ParticleEffect trainee = null;
	ParticleEffect boom = null;
	LivingEntity victime = null;
	
	float timeDead = 0;
	float timeLived = 0;
	private boolean mustDie = false;

	@Override
	public void onCreate(long id, World world) {
		
		super.onCreate(id, world);
		
		SoundManager.playSound(SoundManager.launch2, this, 0.4f);

		
		
		vitesse = 80f;
	
		trainee = Assets.effectFeuTrainee.obtain();
		
		trainee.setTransform(IDTM);
		trainee.translate(InGame.masterTranslate);
		trainee.getControllers().first().scale(0.25f, 0.25f, 0.25f);

		trainee.start();
		hasPhysics = true;
		v.set(CameraController.camera.direction);
		v.nor();
		dir.set(v);
		vitesse = 80f;

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
			vitesse = 80f;
		}
		
		
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
		
		if(pos.y<getHauteurSol()+1f) {

			
			if(owner()==InGame.player && pos.dst(InGame.player.pos)>50f) {
				SoundManager.playSound(SoundManager.blam, this, 1.0f);		
			}
			else if(owner()==InGame.player) {
				SoundManager.playSound(SoundManager.boom, this, 0.4f);
			}
			
		
			mustDie = true;
			pos = previousPos;
		}
		trainee.setTransform(IDTM);
		trainee.translate(pos);
		trainee.translate(InGame.masterTranslate);
		
		
		
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
	//	baseRotationY = -90f;
		transform.rotate(up, 90f);
		transform.translate(-5f, 0.0f, 00.0f);
	}

	
	@Override
	public void draw(ModelBatch modelBatch, Environment environment,
			boolean pourOmbre) {
		if(dead) return;
		super.draw(modelBatch, environment, pourOmbre);
	}
	
	
	@Override
	public void onCollide(Entity other) {
		if(!mustDie && !dead && other!=owner() && other.owner()!=owner() && other instanceof LivingEntity) {
			
			
			
			if(other instanceof Missile) return;
			
			
			SoundManager.playSound(SoundManager.boom, this, 0.4f);
			
			victime=(LivingEntity)other;
			

			Vector3 vers = v.cpy().scl(vitesse);
			victime.onAttackedBy(this, owner(), 35, vers);
			
			
			mustDie  = true;

			victime.auSol = false;
			victime.v.y+=6f;
			
			pos = victime.pos;
			pos.y+=2f;
			victime.removeMe = true;
			victime = null;
		}
	}
	

	
	public void onFinish() {
			
	}

}
