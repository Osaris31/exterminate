package net.osaris.exterminate.entities;

import java.util.Hashtable;

import net.osaris.exterminate.Assets;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;

public class Monster extends AnimatedEntity {
	
	private static Hashtable<String, Animation> animations=null;
		
	@Override
	public Hashtable<String, Animation> getAnimations() {
			return animations;
		}

	static Animation[] anims = {new Animation("idle_Standby",0,30)};

	
	public Monster() {
		hasPhysics = true;
		vitesse = (float) (1f+Math.random()*4f);
	//	gravity = 100f;
		if(animations==null) {
			animations = new Hashtable<String, Shark.Animation>();
			for(Animation anim : anims) {
				animations.put(anim.id, anim);
			}
		}
		
		dir.setToRandomDirection();
		targetDir.setToRandomDirection();
		targetDir.y=0;
		targetDir.nor();
		
	
				size.set(2f, 2f, 2f);
		model = new ModelInstance(Assets.golem);
		
		transform = model.transform;
		baseRotationY = 15f;
		
		
		animation = new AnimationController(model);
	    animation.allowSameAnimation=true;
		animation.setAnimation( getNomAnimation(), -1);
	}


	@Override
	public Animation[] getAnims() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getNomAnimation() {
		return "Take 001";
	}


	@Override
	public void update(float delta) {
		lerp(dir, targetDir, 5f, delta);
		v.x = dir.x*vitesse;
		v.z = dir.z*vitesse;

		tmpv.set(pos).mulAdd(targetDir, 5f);
		if(getHauteurSol(tmpv)<50f) {
			targetDir.x = -targetDir.x;				
			tmpv.set(pos).mulAdd(targetDir, 5f);
			if(getHauteurSol(tmpv)<50f) {
				targetDir.z = -targetDir.z;				
				tmpv.set(pos).mulAdd(targetDir, 5f);
				if(getHauteurSol(tmpv)<50f) {
					targetDir.x = -targetDir.x;				
				}
			}
		}

		super.update(delta);
	}

	@Override
	public void postUpdate() {

	}
}
