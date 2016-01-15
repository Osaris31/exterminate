/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package net.osaris.exterminate;


import net.osaris.exterminate.world.Chunk;
import net.osaris.exterminate.world.Ground;
import net.osaris.exterminate.world.biomes.Biome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.UniVertexBufferObject;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class Assets {
	
	public static boolean doneOnce = false;
	public static boolean loaded = false;

	public static AssetManager assets;

	public static BillboardParticleBatch billboardBatch;
	
	public static PFXPool[] effectPools = new PFXPool[256];
	private static ParticleEffect effetExplFeuMaster;
	private static ParticleEffect effetBouleFeuMaster;
	public static PFXPool effectFeuTrainee;
	public static PFXPool effectFeuBoom;
	public static PFXPool effectFeuFlamme;
	public static Texture fonttexture;
	public static BitmapFont font;
	public static ShaderProgram fontShader;


	public static Texture loadTexture (String file) {
		return new Texture(Gdx.files.internal(file));
	}

	public static void load () {
		
		
		
		loaded = false;

		System.out.println("Loading Assets");
		fonttexture = new Texture(Gdx.files.internal("data/skin/ext.png"), true); // true enables mipmaps
		fonttexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear); // linear filtering in nearest mipmap image
		font = new BitmapFont(Gdx.files.internal("data/skin/ext.fnt"), new TextureRegion(fonttexture), false);
		
		
		
		
		
		UniVertexBufferObject.initBufferPool();
		Ground.prepare();
		
		fontShader = new ShaderProgram(Gdx.files.internal("net/osaris/exterminate/shaders/font.vertex.glsl"), Gdx.files.internal("net/osaris/exterminate/shaders/font.fragment.glsl"));
		if (!fontShader.isCompiled()) {
		    Gdx.app.error("fontShader", "compilation failed:\n" + fontShader.getLog());
		}
		Assets.fontShader.begin();
		  Assets.fontShader.setUniformf("smoothing", 1f/11f);
		  Assets.fontShader.end();

		
		SoundManager.musics[0] = Gdx.audio.newMusic(Gdx.files.internal("data/music/starbound.mp3"));
		
//		SoundManager.ambient[0] = Gdx.audio.newMusic(Gdx.files.internal("data/music/ambient1.mp3"));
//		SoundManager.ambient[1] = Gdx.audio.newMusic(Gdx.files.internal("data/music/ambient2.mp3"));
		
		SoundManager.boom = Gdx.audio.newSound(Gdx.files.internal("data/sounds/boom.mp3"));
		SoundManager.blam = Gdx.audio.newSound(Gdx.files.internal("data/sounds/woosh.mp3"));
		SoundManager.launch = Gdx.audio.newSound(Gdx.files.internal("data/sounds/launch4.mp3"));
		SoundManager.launch2 = Gdx.audio.newSound(Gdx.files.internal("data/sounds/launch5.mp3"));
	//	SoundManager.sounds[0] = Gdx.audio.newSound(Gdx.files.internal("data/sounds/extreminate1.mp3"));
		SoundManager.sounds[10] = Gdx.audio.newSound(Gdx.files.internal("data/sounds/extreminate2.mp3"));
		SoundManager.sounds[11] = Gdx.audio.newSound(Gdx.files.internal("data/sounds/extreminate3.mp3"));
		SoundManager.sounds[12] = Gdx.audio.newSound(Gdx.files.internal("data/sounds/destroy.mp3"));

//		SoundManager.effect[SoundManager.VENT] = Gdx.audio.newMusic(Gdx.files.internal("data/sounds/vent.mp3"));
	
		
		// ParticleSystem is a singleton class, we get the instance instead of creating a new object:
		ParticleSystem particleSystem = ParticleSystem.get();
		billboardBatch = new BillboardParticleBatch();
		particleSystem.add(billboardBatch);
		
		
		if (assets == null)
			assets = new AssetManager();
		
		if(doneOnce) {
			assets.clear();			
		}

		BitmapFontLoader.BitmapFontParameter param = new BitmapFontLoader.BitmapFontParameter();
		param.magFilter = Texture.TextureFilter.Linear;
		param.minFilter = Texture.TextureFilter.Linear;
	//	font = new BitmapFont(Gdx.files.internal("data/ex.fnt"), Gdx.files.internal("data/ex.png"), false);
	
		
		ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
        ParticleEffectLoader loader = new ParticleEffectLoader(new InternalFileHandleResolver());
        assets.setLoader(ParticleEffect.class, loader);
        assets.load("data/effects/boullefeu", ParticleEffect.class, loadParam);
        assets.load("data/effects/feu", ParticleEffect.class, loadParam);
        assets.load("data/effects/feuflamme", ParticleEffect.class, loadParam);
        
        
        
        
    	assets.load("data/objs/shark.g3db", Model.class);
		assets.load("data/objs/missile2.g3db", Model.class);
		assets.load("data/objs/monster.g3db", Model.class);
		assets.load("data/objs/golem.g3db", Model.class);
	
        
        
        boolean wasgl3 = ExterminateGame.useGL3;
		InGame.loadShaders();
		if(wasgl3 && !ExterminateGame.useGL3) {
			InGame.loadShaders();
		}
		System.out.println("GL3: "+ExterminateGame.useGL3);
		System.out.println("Deffered shading: "+InGame.DEFFERED);
		Biome.initBiomesTextures();
		Biome.initBiomes();
			
		
		Chunk.init();
		doneOnce = true;
		loaded = true;
	}
	public static Model requin;
	public static Model missile;
	public static Model monster;
	public static Model golem;
	
	public static void init() {

		System.out.println("Initialising Assets");
	
		effetBouleFeuMaster = assets.get("data/effects/boullefeu");

		effectFeuTrainee = new PFXPool(effetBouleFeuMaster);
		

		effetExplFeuMaster = assets.get("data/effects/feu");

		effectFeuBoom = new PFXPool(effetExplFeuMaster);
		
		effectFeuFlamme = new PFXPool(assets.get("data/effects/feuflamme", ParticleEffect.class));
		
		effectPools[0] = effectFeuFlamme;
		effectPools[1] = effectFeuTrainee;
		effectPools[2] = effectFeuBoom;

		requin = assets.get("data/objs/shark.g3db", Model.class);
		missile = assets.get("data/objs/missile2.g3db", Model.class);
		monster = assets.get("data/objs/monster.g3db", Model.class);
		golem = assets.get("data/objs/golem.g3db", Model.class);



	}
	private static Vector3 tmpV = new Vector3();
	private static Vector3 tmpV2 = new Vector3();
	private static Vector3 tmpV3 = new Vector3();
	
	public static class PFXPool extends Pool<ParticleEffect> {
	    private ParticleEffect sourceEffect;

	    public PFXPool(ParticleEffect sourceEffect) {
	        this.sourceEffect = sourceEffect;
	    }

	    @Override
	    public void free(ParticleEffect pfx) {
	   //     ParticleSystem.get().remove(pfx);
	        if(pfx==null) return;
			Emitter emitter = pfx.getControllers().first().emitter;
	        if (emitter instanceof RegularEmitter) {
	            RegularEmitter reg = (RegularEmitter) emitter;
	            reg.setEmissionMode(RegularEmitter.EmissionMode.Disabled);
	        }
	       
	        super.free(pfx);
	    }

	    @Override
	    protected ParticleEffect newObject() {
	    	ParticleEffect newEffect = sourceEffect.copy();
	    	newEffect.init();
	    	ParticleSystem.get().add(newEffect);
	        return newEffect;
	    }
	    
	    @Override
	    public ParticleEffect obtain() {
	    	ParticleEffect newEffect = super.obtain();
	    	
	    //	ParticleSystem.get().add(newEffect);
	    	Emitter emitter = newEffect.getControllers().first().emitter;
	        if (emitter instanceof RegularEmitter) {
	            RegularEmitter reg = (RegularEmitter) emitter;
	            reg.setEmissionMode(RegularEmitter.EmissionMode.Enabled);
	        }
	        newEffect.reset();
	        
	        return newEffect;
	    }
	    
	}

}
