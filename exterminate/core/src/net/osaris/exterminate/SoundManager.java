package net.osaris.exterminate;

import net.osaris.exterminate.entities.Entity;
import net.osaris.exterminate.world.WorldAround;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
	
	public static float masterSound = 0.3f;
	public static float masterMusic = 0.3f;
	
	public static final int VENT = 0;
	public static final int WATER = 1;
	
	private static final int NB_EFFECTS = 3;
	
	public static float ambientVolume = 1f;
	public static Music currentAmbiant = null;
	public static Music currentMusic = null;
	
	public static Music[] musics = new Music[2];
	public static Music[] ambient = new Music[2];
	
	public static Music effect[] = new Music[NB_EFFECTS];
	public static float effectVolume[] = {0, 0};
	
	
	public static Sound sounds[] = new Sound[256];
	public static float lastPlayed[] = new float[256];
	public static Sound boom;
	public static Sound launch;
	public static Sound launch2;
	public static Sound blam;
	public static boolean soundEnabled = false;
	public static boolean musicEnabled = false;
	
	
	public final static int LAST_PAS = 2;
	

	public static long playSound(int soundId, Entity from, float volume) {
		if(lastPlayed[soundId]<1f) return -1;
		lastPlayed[soundId] = 0;
		
		return playSound(sounds[soundId], from, volume);
	}

	public static long playSound(Sound son, Entity from, float volume) {
		if(soundEnabled ) {
			if(from!=null) {
				return son.play(volume*masterSound*(float)(0.5f+Math.exp(-from.pos.dst(InGame.player.pos)*0.025f)), (float)Math.random()*0.15f+1f, 0);				
			}
			else {
				return son.play(volume*masterSound, (float)Math.random()*0.15f+1f, 0);
			}
		}
		return -1;
	}
	
	public static long playSound(int soundId, Entity from, float volume, int son2, float volume2) {
		if(lastPlayed[soundId]<0.5f) return -1;
		lastPlayed[soundId] = 0;
		
		return playSound(sounds[soundId], from, volume, sounds[son2], volume2);
	}

	public static long playSound(Sound son, Entity from, float volume, Sound son2, float volume2) {
		if(!soundEnabled) return -1;
		
		if(son2==null || Math.random()<0.5f) {
			return playSound(son, from, volume);
		}
		else {
			return playSound(son2, from, volume2);
		}
	}
	
	public static void update(float delta) {
		
    	SoundManager.soundEnabled=SoundManager.masterSound>0.005f;
    	SoundManager.musicEnabled=SoundManager.masterMusic>0.005f;

    	if(currentMusic==null && WorldAround.ready) {
			currentMusic = musics[0];
			lastPlayed[0] = 0f;
			currentMusic.setVolume(0);
	  		currentMusic.setLooping(true);
			currentMusic.play();

    	}
    	if(lastPlayed[0]<1 && currentMusic!=null) {
			currentMusic.setVolume(masterMusic*0.5f*lastPlayed[0]);
  		
    	}
    	
		if(!soundEnabled) return;
		
/*		if(currentAmbiant==null || !currentAmbiant.isPlaying()) {
			currentAmbiant = ambient[1];
			currentAmbiant.setLooping(true);
			currentAmbiant.setVolume(masterSound);
			currentAmbiant.play();
		}
		else {
			currentAmbiant.setVolume(masterSound);			
		}
		
		handleEffect(VENT, CameraController.fly, InGame.player.vitesse/50f, 3f, delta);
		
		handleEffect(WATER, InGame.world.chunkCourant.hasEau, 0.1f, 1f, delta);*/
		
		for(int i=0;i<128;i++) {
			lastPlayed[i]+=delta;
		}

		
		
	}

	private static void handleEffect(int type, boolean condition, float volume, float vitesse, float delta) {

		if(condition) {
			effectVolume[type] = lerp(effectVolume[type], Math.min(1f, volume), vitesse, delta);
		}
		else {
			effectVolume[type] = lerp(effectVolume[type], 0f, vitesse, delta);
		}
		if(effectVolume[type]>0.01 && !effect[type].isPlaying() ) {
			effect[type].setVolume(effectVolume[type]*masterSound);			
			effect[type].setLooping(true);
			effect[type].play();
		}
		if(effectVolume[type]<0.01 && effect[type].isPlaying()) {
			effect[type].pause();
		}
		if(effect[type].isPlaying()) {
			effect[type].setVolume(effectVolume[type]*masterSound);
		}
	}

	public static float lerp(float vec, float target, float speed, float deltaTime) {
		
		return vec-(vec-target)*Math.min(1.0f/60f, Math.max(1.0f/120f, deltaTime))*speed;
	}

	public static void pauseAllSounds() {
		if(currentAmbiant!=null && currentAmbiant.isPlaying()) {
			currentAmbiant.pause();
		}
		
		for(int i=0;i<NB_EFFECTS;i++){
			if(effect[i]!=null && effect[i].isPlaying()) {
				effect[i].pause();
			}
		}
		
		for(int i=0;i<256;i++){
			if(sounds[i]!=null) {
				sounds[i].stop();
			}
		}
	}

	public static void pauseAllMusiques() {
		if(currentMusic!=null) {
			currentMusic.stop();
			currentMusic=null;
		}
	}

	public static void stopAllSounds() {
		if(currentAmbiant!=null) {
			currentAmbiant.stop();
		}
		
		for(int i=0;i<NB_EFFECTS;i++){
			if(effect[i]!=null && effect[i].isPlaying()) {
				effect[i].stop();
			}
		}
		
		for(int i=0;i<256;i++){
			if(sounds[i]!=null) {
				sounds[i].stop();
			}
		}
	}
}
