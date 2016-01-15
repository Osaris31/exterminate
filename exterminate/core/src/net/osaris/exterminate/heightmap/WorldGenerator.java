

package net.osaris.exterminate.heightmap;

import net.osaris.exterminate.world.IslandManager;
import net.osaris.exterminate.world.biomes.Biome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;


public final class WorldGenerator
{
	   public static Pixmap noise; 

	public WorldGenerator(int meters_per_world, int seed)
    {
        this.seed = seed;
        grid_units = meters_per_world / 2;
        this.meters_per_world = meters_per_world;
    }


    public final WorldInfo generate(int x0, int z0, float hills, Biome biome)
    {
    	noise = IslandManager.noise;
        int res = 512;
        int nres = noise.getHeight();
        Color c = new Color();
        float heightmap[][] = new float[res+1][res+1];
        
        	for(int i=0;i<heightmap.length;i++) {
            	for(int j=0;j<heightmap[i].length;j++) {
            		
            		double x = x0*res+i;
            		double z = z0*res+j;
            		
            		int hm = IslandManager.noise.getPixel((int)(x%nres+nres)%nres, (int)(z%nres+nres)%nres);
            		c.set(hm);
//            		System.out.println(c.r);
            		double f = c.r*c.r*c.r*100.0;
            		heightmap[i][j] = (float)Math.max(0, f);
            	}
       		
        	}
               
        return new WorldInfo(meters_per_world, 10f, heightmap);
    }


    private static final long serialVersionUID = 1L;
     private final int meters_per_world;
    private final int grid_units;
  
    private final int seed;
}
