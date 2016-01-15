package net.osaris.exterminate.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.osaris.exterminate.ExterminateGame;
import net.osaris.exterminate.InGame;
import net.osaris.exterminate.entities.EntityManager;

import com.badlogic.gdx.math.Vector3;

public class World {
	/*
	 * 
	 * Taille maxi chargé = 64*64, on dec de +currentpos en storage.;
	 * 
	 */
//	public static World instance;
	public static final ChunkBuilder builder1 = new ChunkBuilder();
	public static final ChunkBuilder builder2 = new ChunkBuilder();
	public static final ChunkBuilder builder3 = new ChunkBuilder();
	public static final ChunkBuilder builder4 = new ChunkBuilder();
	public static final ChunkBuilder rebuilder = new ChunkBuilder();
	public Thread threadBuilder;
	
	public EntityManager entityManager = new EntityManager(this);

//	public List<Chunk> chunkACreer;
	public List<Chunk> chunkAVoroneiser;
	public Chunk chunkAModeliser1 = null; // UN SEUL car on a un seul MOdelBuilder !!!!
	public Chunk chunkAModeliser2 = null; // UN SEUL car on a un seul MOdelBuilder !!!!
	public Chunk chunkAModeliser3 = null; // UN SEUL car on a un seul MOdelBuilder !!!!
	public Chunk chunkAModeliser4 = null; // UN SEUL car on a un seul MOdelBuilder !!!!
	public boolean isThreadBuilderPaused = false;
//	public boolean dontEvenThinkAboutPausing = false;
//	public boolean verifieMec = false;
	public String name = "world";
	
	public static Chunk EMPTY = new Chunk(0, 0, 0, null);
	
	public int SIZE = ExterminateGame.MAX_VIEW_DISTANCE*2+6; //fixé en multi.

			//Math.max(InGame.RAYON, 10)*2+6;//(int) (HeightMap.HM_CELLSIZE*512/Chunk.GRIDSIZE);

	public Chunk[][] Chunks = new Chunk[SIZE][SIZE];
	protected boolean mustStop = false;

	private static int seed = 543;
	
	public void init() {

		
	}

	
	public void loadEntities() {
			
		InGame.entityManager = entityManager;



	}
	private static List<Chunk> chunksToDispose = Collections.synchronizedList(new ArrayList<Chunk>());
	private ArrayList<Chunk> chunksToSave = new ArrayList<Chunk>();

	public Chunk getChunk(float bx, float by) {
		return getChunk(bx, by, false);
	}

	public Chunk getChunk(Vector3 where) {
		return getChunk(where.x, where.z, false);
	}

	public Chunk getChunk(float bx, float by, boolean canGenere) {
		
	/*	if(vazyGenere>0) {
			vazyGenere --;
			modelise();
		}*/
		int ax = (int) Math.floor(bx/Chunk.GRIDSIZE);
		int ay = (int) Math.floor(by/Chunk.GRIDSIZE);
		
		int x = (((ax % SIZE) + SIZE) % SIZE);
		int y = (((ay % SIZE) + SIZE) % SIZE);		
		
		Chunk c = Chunks[x][y];
		
		
		if(canGenere && c!=null && c.isValid && c.x==ax && c.z==ay && c.doitEtreRegenerer ) {// && vazyRegenere
			/*		c.doitEtreRegenerer = false;
			vazyRegenere = false;
			rebuilder.buildCells(c);
			rebuilder.rebuildModel(c);*/
			c.doitEtreRegenerer = false;
			  synchronized (this) {
				chunkAVoroneiser.add(c);
				  
				  Collections.sort(chunkAVoroneiser, new ChunkComparator());
			 // réveil le thread
				  if(isThreadBuilderPaused) {
					  notifyAll();
				  }
			  }
	//		c.buildHeighmap(false);
		}
		
	//	if(c!=null) System.out.println(x+" "+c.x);
		
		return (c!=null && c.isValid && c.x==ax && c.z==ay && !c.disposed) ? c : EMPTY;
	}

	public void unloadChunk(float bx, float by, boolean ask) {
		
	/*	if(vazyGenere>0) {
			vazyGenere --;
			modelise();
		}*/
		int ax = (int) Math.floor(bx/Chunk.GRIDSIZE);
		int ay = (int) Math.floor(by/Chunk.GRIDSIZE);
		
		int x = (((ax % SIZE) + SIZE) % SIZE);
		int y = (((ay % SIZE) + SIZE) % SIZE);		
		
		if(x>Chunks.length || y>Chunks.length) {
			return;
		}
		Chunk c = Chunks[x][y];
		if(c!=null) {
			c.dispose();
		}
		Chunks[x][y] = null;
	}

	public void setChunk(float bx, float by, Chunk c) {
		
	/*	if(vazyGenere>0) {
			vazyGenere --;
			modelise();
		}*/
		int ax = (int) Math.floor(bx/Chunk.GRIDSIZE);
		int ay = (int) Math.floor(by/Chunk.GRIDSIZE);
		
		int x = (((ax % SIZE) + SIZE) % SIZE);
		int y = (((ay % SIZE) + SIZE) % SIZE);		
		
		c = Chunks[x][y] = c;

	}

	private synchronized void modelise() {

		
	}

	private void rebuild(Island islandAt) {
		if(islandAt!=null) islandAt.mustRebuildIndex = true;
	}

	public Chunk getOrCreateChunk(int ax, int ay) {
		int x = (((ax % SIZE) + SIZE) % SIZE);
		int y = (((ay % SIZE) + SIZE) % SIZE);
		
		Chunk c = Chunks[x][y];
		
		if(c!=null) {
			if(c.x==ax && c.z==ay && !c.disposed) {
				return c;				
			}
			else {
				chunksToSave.add(c);
				Chunks[x][y] = new Chunk(ax, ay, seed, this);
				
			
				return Chunks[x][y];
			}
		}
		else {
			Chunks[x][y] = new Chunk(ax, ay, seed, this);

			return Chunks[x][y];
		}
	}

	public float getHauteurAt(float x, float z) {
		return getHauteurAt(x, z, false, false);
	}
	
	public float getHauteurAt(float x, float z, boolean nohole, boolean chunkDejaTeste) {
		
		Chunk chunk = getChunk(x-0.5f, z-0.5f);// a cause de bon tu vois quoi
		
		if(chunkDejaTeste || !chunk.isGenerer || chunk==EMPTY || !chunk.isPosInChunk(x-0.5f, z-0.5f)) {
			return island.getHauteur(x, z, nohole, chunkDejaTeste);
		}
		
		return chunk.getHauteurInterpole(x, z, nohole);

	}

	public Chunk chunkCourant = EMPTY;
	public int vazyGenere = 0;
	public boolean vazyRegenere = false;
	public boolean mustMAJRayon = false;
	
	public void nowAt(Vector3 pos) {
	
	}
	
	
	public class ChunkComparator implements Comparator<Chunk> {

		@Override
		public int compare(Chunk c1, Chunk c2) {
			if((c1.x-chunkCourant.x)*(c1.x-chunkCourant.x)+(c1.z-chunkCourant.z)*(c1.z-chunkCourant.z)
					< (c2.x-chunkCourant.x)*(c2.x-chunkCourant.x)+(c2.z-chunkCourant.z)*(c2.z-chunkCourant.z)) {
				return -1;
			}
			else if((c1.x-chunkCourant.x)*(c1.x-chunkCourant.x)+(c1.z-chunkCourant.z)*(c1.z-chunkCourant.z)
					> (c2.x-chunkCourant.x)*(c2.x-chunkCourant.x)+(c2.z-chunkCourant.z)*(c2.z-chunkCourant.z)) {
				return 1;
			}
			else {
				return 0;
			}
			
		}


	}

	public void save() {
	

	}

	
	public IslandManager island;

	public void dispose() {
		 for(int i=0;i<SIZE;i+=1) {
			  for(int j=0;j<SIZE;j+=1) {
				
				  Chunk c = Chunks[i][j];
				  if(c!=null) {
					c.dispose();
				  }
			  }
		  }
		 mustStop = true;
	}

	long currentPid = 0;
	public long newPid() {
			currentPid++;
			return currentPid;
		
	}

	
}
