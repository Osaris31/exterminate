package net.osaris.exterminate.world.biomes;

import java.util.Random;

import net.osaris.exterminate.ExterminateGame;
import net.osaris.exterminate.world.Chunk;
import net.osaris.exterminate.world.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class Biome {

	public final static int GRASS = 1;
	public final static int ICE = 0;
	public final static int MESA = 2;
	
	public static Biome biomeGrass;
	
	public static boolean isServer = false;
	
	public final static int NB_BIOMES = 3;
	public static Biome[] biomes = new Biome[NB_BIOMES];
	
	public int type;
	public float forcevent;
	public float bloom;
	public float vignette;
	public float coefOmbrage;		
	public float ombrage;		
	public int minHills;
	public boolean plat = false;

	public static Texture textureEau;
	public Texture textureHerbe;		
	public Texture textureSand;		
	public Texture textureDirt;		
	public Texture textureRock;		
	public float coefOmbrageEau;
	public float randArbre = 0.65f;
	
	public Biome(int type) {
		this.type = type;	
	}

	public static int textureBlocks;
	
//	public static int textureSol;
	
	public static int textureGrass;
	public static int textureGlassid;
	
	public static void initBiomesTextures() {

		textureEau = new Texture(Gdx.files.internal("data/pics/water.png"), true);
		
		textureEau.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		textureEau.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
		
	}

	public static void initBiomes() {


		
		biomeGrass = new BiomeMesa(Biome.MESA);
		biomes[Biome.MESA] = biomeGrass;
		biomes[Biome.GRASS] = biomeGrass;

	}

	public void generate(Chunk c, Random rand, World world) {		
			
	}
	
	int ltid=0;
	
	public void addTexture(String file) {
			Texture texture = new Texture(Gdx.files.internal(file), true);
			
			texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			if(ExterminateGame.useGL3) {
				texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
			}
			else {
				texture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear);				
			}
			
			if(ltid==0) {
				textureHerbe = texture;
			}
			else if(ltid==1) {
				textureSand = texture;
			}
			else if(ltid==2) {
				textureDirt = texture;
			}
			else if(ltid==3) {
				textureRock = texture;
			}

		ltid++;
	}




	
}
