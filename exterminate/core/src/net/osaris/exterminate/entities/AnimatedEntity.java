package net.osaris.exterminate.entities;

import java.util.Hashtable;

import net.osaris.exterminate.InGame;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;

public abstract class AnimatedEntity extends LivingEntity {
	public AnimationController animation;
	public AnimationController action;
	public abstract Animation[] getAnims();
	public abstract Hashtable<String, Animation> getAnimations();
	
	String currentAnimation ="";
	float currentVitesse = 0;
	
	
	@Override
	public void setAnimation(String id, float vitesse) {

		
		if(id.equals(currentAnimation) && vitesse==currentVitesse) {
			return;
		}
		animate = true;

		currentAnimation = id;
		currentVitesse = vitesse;
		
		
		Animation anim = getAnimations().get(id);

	      animation.animate(getNomAnimation(), anim.debut, anim.duree, -1, vitesse, new AnimationListener() {
	    
			@Override
			public void onLoop(AnimationDesc animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEnd(AnimationDesc animation) {
				// TODO Auto-generated method stub
				
			}
		}, 0.25f);
	}

	public void action(String id, float vitesse, float transition) {
		if(id.equals(currentAnimation) && vitesse==currentVitesse) {
			return;
		}
		animate = true;
		

		currentAnimation = id;
		currentVitesse = vitesse;
		
		
		Animation anim = getAnimations().get(id);
		
	      animation.action(getNomAnimation(), anim.debut, anim.duree, 1, vitesse, new AnimationListener() {
			
			@Override
			public void onLoop(AnimationDesc animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEnd(AnimationDesc animation) {
				// TODO Auto-generated method stub
				
			}
		}, transition);
	}
	
	public void action(String id, float vitesse) {
		action(id, vitesse, 0.25f);
	}

	public abstract String getNomAnimation();


	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		if(animate) {	
			animation.update(deltaTime);
//			action.update(deltaTime);
		}
		else if(InGame.frame%10==Math.abs(id)%10) {	
			animation.update(deltaTime*10f);
//			action.update(deltaTime*10f);
		}
	/*      if(Math.random()<0.001) {
	    	  setAnimation(((Animation)getAnimations().values().toArray()[(int) (Math.random()*getAnimations().values().size())]).id);
	      }*/
		
		postUpdate();

	}
	
	public abstract void postUpdate();
	
	public static class Animation {
	
		public String id;
		public Animation(String id, float debut, float fin) {
			super();
			this.id = id;
			this.debut = debut/30f;
			this.duree = (fin-debut)/30f;
		}
		public float debut;
		public float duree;
		
	}
}
