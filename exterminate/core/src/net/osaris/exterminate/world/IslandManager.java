
package net.osaris.exterminate.world;


import java.util.Random;

import net.osaris.exterminate.InGame;
import net.osaris.exterminate.heightmap.WorldGenerator;
import net.osaris.exterminate.heightmap.WorldInfo;
import net.osaris.exterminate.world.biomes.Biome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.UniMesh;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;

public final class IslandManager
{

	public final static float HAUTEUR = 5f;
	public final static int HM_GRIDSIZE = 512;
	public final static float HM_CELLSIZE = 4*512/HM_GRIDSIZE;
	public final static float HM_TOTALSIZE = HM_CELLSIZE*HM_GRIDSIZE;
	
	public int centerX = 0;
	public int centerZ = 0;

	
	public final static int RADIUS = 3;
    private static final float hills = 3;

	protected final int SEED = 355;//+657432;//+40213;//+13

	private World world;
	
	public float getHauteur(float posX, float posZ) {
		return getHauteur(posX, posZ, false, false);
	}
	
	public float getHauteur(float posX, float posZ, boolean nohole, boolean chunkDejaTeste) {
		Chunk c=world.getChunk(posX-0.5f, posZ-0.5f);//a cause du décalage de meshsol
		if(!chunkDejaTeste && c!=World.EMPTY) {
			return c.getHauteurInterpole(posX, posZ, nohole);//c'est dans la fonction qu'on -0.5f
		}
		
		
//		return getSimpleHauteur(posX, posZ);
		return (getSimpleHauteur(posX, posZ)*2f+getSimpleHauteur(posX+1, posZ-1)+getSimpleHauteur(posX+1, posZ+1)+getSimpleHauteur(posX-1, posZ-1)+getSimpleHauteur(posX-1, posZ+1))/6f;
	}
	
	public float getGenerationHauteur(float posX, float posZ) {
		return (getSimpleHauteur(posX, posZ)*2f+getSimpleHauteur(posX+1, posZ-1)+getSimpleHauteur(posX+1, posZ+1)+getSimpleHauteur(posX-1, posZ-1)+getSimpleHauteur(posX-1, posZ+1))/6f;
	}
	
	public float getSimpleHauteur(float posX, float posZ) {
		//System.out.println(posX + " " + posZ);
		if(getIslandAt(posX, posZ)==null || getIslandAt(posX, posZ).world==null) {
			return 0;
		}
		float world[][] = getIslandAt(posX, posZ).world;
		
		
		float decX = (float) (posX/HM_CELLSIZE - Math.floor(posX/HM_CELLSIZE));
		float decZ = (float) (posZ/HM_CELLSIZE - Math.floor(posZ/HM_CELLSIZE));
//		System.out.println(decX);
		int xPos1InMap = (int) ((Math.floor(posX/HM_CELLSIZE)%HM_GRIDSIZE+HM_GRIDSIZE)%HM_GRIDSIZE);
		int xPos2InMap = (int) ((Math.floor(posX/HM_CELLSIZE)%HM_GRIDSIZE+HM_GRIDSIZE)%HM_GRIDSIZE+1);
		int zPos1InMap = (int) ((Math.floor(posZ/HM_CELLSIZE)%HM_GRIDSIZE+HM_GRIDSIZE)%HM_GRIDSIZE);
		int zPos2InMap = (int) ((Math.floor(posZ/HM_CELLSIZE)%HM_GRIDSIZE+HM_GRIDSIZE)%HM_GRIDSIZE+1);
		
		return ((world[xPos1InMap][zPos1InMap]*(1-decX)+world[xPos2InMap][zPos1InMap]*decX)*(1-decZ)
				+(world[xPos1InMap][zPos2InMap]*(1-decX)+world[xPos2InMap][zPos2InMap]*decX)*decZ)*IslandManager.HAUTEUR;
	}
	
	private static Island islands[][] = new Island[RADIUS][RADIUS];

	public static int AllIndices;
	private static Mesh fullFirstMesh;

	public static boolean canRebuild = false;
	private boolean mustBuildModel = true;
	public boolean mustRebuildIndex = true;
	public Renderable renderableSol;
	public Mesh modelEau;
	public Mesh modelSol;

	private static boolean isServer = false;
	public Island getIslandAt(float posX, float posZ) {


		int tX = (int) Math.floor(posX/HM_TOTALSIZE);
		int tZ = (int) Math.floor(posZ/HM_TOTALSIZE);
		
		int x = (((tX % RADIUS) + RADIUS) % RADIUS);
		int y = (((tZ % RADIUS) + RADIUS) % RADIUS);

		if(islands[x][y]!=null && islands[x][y].x==tX && islands[x][y].z==tZ) {
			return islands[x][y];			
		}
		else {
			return null;
		}

	}
	
	public static Island getHeightMapInGrid(int i, int j) {
		
		
		
		
		if(islands[i][j]==null) {
			return null;
		}


		
		if(islands[i][j].mustBuildModel && !isServer ) {
		     
            ldMap.buildModels(islands[i][j].world, islands[i][j]);
    		
    		islands[i][j].modelSol = ldMap.modelSol;
    		islands[i][j].modelEau = ldMap.modelEau;
    		
    		islands[i][j].mustBuildModel = false;
		}
		
		if(canRebuild && islands[i][j].rebuildin>=0) {
			islands[i][j].rebuildin--;
		}
		if(islands[i][j].mustRebuildIndex && islands[i][j].rebuildin<=0 && !isServer) {
			islands[i][j].rebuildin = 10;
            ldMap.rebuildIndex(islands[i][j]);
            islands[i][j].mustRebuildIndex = false;
			canRebuild =false;
		}
		
		
		return islands[i][j];
	}
	
	static float hauteurEau = 0;

    public void generate()
    {
    	
       	centerX = -100000;
       	centerZ = -100000;
           	
        System.out.println("Generation...");
        long time_start = System.currentTimeMillis();
        
    	WorldGenerator generator = new WorldGenerator(HM_GRIDSIZE*2, SEED+1+1*100);
        WorldInfo world_info = generator.generate(0, 0, hills, Biome.biomeGrass); // Mettre hills = 0 pour biome mesa !
        hauteurEau = world_info.sea_level_meters;

        if(!isServer) {
            nowOn(InGame.player.pos, true);
            
            ldMap.buildModels(islands[0][0].world, islands[0][0]);
            
    		fullFirstMesh = ldMap.getSolMesh();
    		AllIndices = ((UniMesh)fullFirstMesh).getIndiceHandle();
       }
        else {
            nowOn(new Vector3(0, 0, 0), false);        	
        }
         		
		
/*		islands[0][0].renderableSol = ldMap.renderableSol;
		islands[0][0].modelEau = ldMap.modelEau;     
		islands[0][0].modelSol = ldMap.modelSol;     
		islands[0][0].mustBuildModel = false;
       */
    	
        long time_stop = System.currentTimeMillis();
        System.out.println("Terminé. " + (time_stop - time_start) / 1000F + "");

    }
	public final Random rand = new Random();

    public Biome getRandBiome(int i, int j) {
    	rand.setSeed(i*45187+j*4137);
		return Biome.biomes[(int)(rand.nextFloat()*Biome.NB_BIOMES)];
	}	
    public void generateServer()
    {
    	isServer = true;
        generate();
    }
	
    public static Pixmap noise; 
    public IslandManager(World world)
    {
    	
    	noise = new Pixmap(
				Gdx.files.internal("data/pics/noise.png"));
	 
    	
    	this.world = world;
    }


    public static final int GRID_UNITS_PER_PATCH = 16;

    private static final Vector3 vec1 = new Vector3();
    private static final Vector3 vec2 = new Vector3();
    private static final Vector3 vec3 = new Vector3();
    
    private static final LDMapBuilder ldMap = new LDMapBuilder();





    public void export() {
    	float world[][] = islands[1][1].world;
		
		Pixmap pixmap = new Pixmap(world.length, world.length, Format.RGBA8888);
    	float maxh = 0;
    	
       	for(int i=0;i<world.length;i++) {
        	for(int j=0;j<world.length;j++) {
        		maxh = Math.max(maxh, world[i][j]);
        	}    		
    	}

    	
    	for(int i=0;i<world.length;i++) {
        	for(int j=0;j<world.length;j++) {
        		pixmap.setColor(world[i][j]/maxh, world[i][j]/maxh, world[i][j]/maxh, 1);
        		if(world[i][j]<hauteurEau) {
        			pixmap.setColor(world[i][j]/maxh, world[i][j]/maxh, 0.8f, 1);
            		
        		}
        		pixmap.drawPixel(i, j);
        	}    		
    	}
    	FileHandle f = new FileHandle("map.png");
    	PixmapIO.writePNG(f, pixmap);
    	System.out.println("map exported. "+f.path());
    }

	public static float hauteurEau() {
		//System.out.println(HAUTEUR*hauteurEau); 38.4
		return HAUTEUR*hauteurEau;
	}

	
	
	public void nowOn(Vector3 pos, boolean immediate) {
		int tX = (int) Math.floor(pos.x/HM_TOTALSIZE);
		int tZ = (int) Math.floor(pos.z/HM_TOTALSIZE);
		
		if(centerX!=tX || centerZ!=tZ) {
			centerX = tX;
			centerZ = tZ;

			int x=0, z=0;
			
    		int i = (((x % RADIUS) + RADIUS) % RADIUS);
    		int j = (((z % RADIUS) + RADIUS) % RADIUS);

        	
        	if((islands[i][j]==null || islands[i][j].x!=x || islands[i][j].z!=z) && immediate) {
        		WorldGenerator generator1 = new WorldGenerator(HM_GRIDSIZE*2, SEED+i+j*100);
        		Biome biome = getRandBiome(x, z);
        		if(x==0 && z==0) {
        			biome = Biome.biomeGrass;
        		}
        		WorldInfo world_info0 = generator1.generate(x, z, (int)rand.nextFloat()*(4f-biome.minHills)+biome.minHills, biome);
                islands[i][j] = new Island(hauteurEau, world_info0.heightmap);        	
                islands[i][j].biome = biome;
                islands[i][j].x = x;
                islands[i][j].z = z;
                }


			
			  new Thread(new Runnable() {
					
					@Override
					public void run() {
						for(int x=centerX-1;x<centerX+2;x++) {
				            for(int z=centerZ-1;z<centerZ+2;z++) {
				            	
				        		int i = (((x % RADIUS) + RADIUS) % RADIUS);
				        		int j = (((z % RADIUS) + RADIUS) % RADIUS);

				            	
				            	if(islands[i][j]==null || islands[i][j].x!=x || islands[i][j].z!=z) {
				            		WorldGenerator generator1 = new WorldGenerator(HM_GRIDSIZE*2, SEED+i+j*100);
				            		Biome biome = getRandBiome(x, z);
				            		biome = Biome.biomeGrass;
				            		WorldInfo world_info0 = generator1.generate(x, z, (int)rand.nextFloat()*(4f-biome.minHills)+biome.minHills, biome);
				                    islands[i][j] = new Island(hauteurEau, world_info0.heightmap);        	
				                    islands[i][j].biome = biome;
				                    islands[i][j].x = x;
				                    islands[i][j].z = z;
				                    }
				
				            }
				        }
					}

			  }).start();

		}
		

	}
}
