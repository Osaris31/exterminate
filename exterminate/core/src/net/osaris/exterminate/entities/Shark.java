package net.osaris.exterminate.entities;

import java.util.Hashtable;

import net.osaris.exterminate.Assets;
import net.osaris.exterminate.InGame;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;

public class Shark extends AnimatedEntity {
	
	private static Hashtable<String, Animation> animations=null;
	
	@Override
	public Hashtable<String, Animation> getAnimations() {
			return animations;
		}

	static Animation[] anims = {new Animation("idle_Standby",0,40)};

	
	public Shark() {
		hasPhysics = true;
		vitesse = (float) (4f+Math.random()*6f);
		
		if(animations==null) {
			animations = new Hashtable<String, Shark.Animation>();
			for(Animation anim : anims) {
				animations.put(anim.id, anim);
			}
		}
		
		
		targetDir.setToRandomDirection();
		targetDir.y=0;
		targetDir.nor();
		
		size.set(0.016f, 0.016f, 0.016f);
		model = new ModelInstance(Assets.requin);
		
		transform = model.transform;
		
		
		animation = new AnimationController(model);
	    animation.allowSameAnimation=true;
		animation.setAnimation( getNomAnimation(), -1);
	}
	
	public float lastjump = 0;
	public float timeinjump = 5f;
	public boolean jumping = false;
	
	@Override
	public void update(float delta) {
				
		if(jumping) {
			timeinjump+=delta*1.f;
			if(timeinjump>=5.f) {
				if(Math.random()<0.2f && pos.dst(InGame.player.pos)<100f) {
					timeinjump = 0;
				}
				else {
					timeinjump=4.75f;
				}
			}	
		}

		
		lerp(dir, targetDir, 5f, delta);
		
		v.x = dir.x*vitesse;
		v.z = dir.z*vitesse;
//		anglePenche = 0f;
		if(jumping) {
			baseRotationY = 0f;
			v.x = dir.x*vitesse*0f;
			v.z = dir.z*vitesse*0f;
			float sousEau = 1f;
			float saut = 1.5f;
			float chute = 4f;
			if(timeinjump<sousEau) {
				angleY = 0;
					float ltime = timeinjump/sousEau;
				
//				anglePenche = -85f+ltime*10f;
				anglePenche = lerp(anglePenche, -85f+ltime*10f, 1f, delta);
				reelPosy = ltime*15f+30f;
			}
			else if(timeinjump<sousEau+saut) {
				float ltime = (timeinjump-sousEau)/saut;
	//			anglePenche += (float)Math.sin(ltime*3.14f)*delta*70f;
				anglePenche += (float)Math.sin(ltime*3.14f)*delta*80f;
				angleY += (float)Math.sin(ltime*3.14f)*delta*80f;
				
				reelPosy = 30f+15f + (float)Math.sin(ltime*3.14f)*10f;
				
			}
			else {
				float ltime = (timeinjump-sousEau-saut)/chute;
				reelPosy =  Math.max(30f+15f -ltime*30f, 30f);
				anglePenche = lerp(anglePenche, 0, 1f, delta);
				angleY = lerp(angleY, 0, 1f, delta);
//				anglePenche = -80f+(timeinjump-3f)*90f;
				
			}
		}
		else {
			reelPosy = Math.max(47.5f, reelPosy);
			anglePenche = lerp(anglePenche, 0, 1f, delta);
			angleY = lerp(angleY, 0, 1f, delta);

		}

		tmpv.set(pos).mulAdd(targetDir, 5f);
		if(getHauteurSol(tmpv)>reelPosy && getHauteurSol(tmpv)>40f) {
			targetDir.x = -targetDir.x;				
			tmpv.set(pos).mulAdd(targetDir, 5f);
			if(getHauteurSol(tmpv)>reelPosy) {
				targetDir.z = -targetDir.z;				
				tmpv.set(pos).mulAdd(targetDir, 5f);
				if(getHauteurSol(tmpv)>reelPosy) {
					targetDir.x = -targetDir.x;				
				}
			}
		}
		
		super.update(delta);
	}

	@Override
	public Animation[] getAnims() {
		return null;
	}


	@Override
	public String getNomAnimation() {
			return "Take 001";
	}

	@Override
	public void postUpdate() {
		// TODO Auto-generated method stub
		
	}
}
