package net.osaris.exterminate.world.biomes;

import java.util.Random;

import net.osaris.exterminate.world.Chunk;
import net.osaris.exterminate.world.World;

public class BiomeMesa extends Biome {

	public BiomeMesa(int type) {
		super(type);
		
		randArbre = 0.05f;
		
		minHills = 0;
		
		coefOmbrageEau = 0.63f;
		forcevent = 1f;
		vignette = 0.25f;
		bloom = 0.985f;
		coefOmbrage = 0.85f;
		ombrage = 0.85f;
		
		if(!isServer) {

			addTexture("data/pics/canyon.jpg");
			addTexture("data/pics/sand.jpg");
			addTexture("data/pics/dirt.jpg");
			addTexture("data/pics/beach.jpg");

		}
	}
	

	@Override
	public void generate(Chunk c, Random rand, World world) {
		
	}
	
}
