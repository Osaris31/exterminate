package net.osaris.exterminate.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.osaris.exterminate.ExterminateGame;
import net.osaris.exterminate.InGame;
import net.osaris.exterminate.entities.Player;

import com.badlogic.gdx.math.Vector3;

public class WorldSolo extends World {


	public List<Chunk> chunkACreer;
	public List<Chunk> chunkAVoroneiser;
	public boolean dontEvenThinkAboutPausing = false;
	public boolean verifieMec = false;

	public static int SIZE = InGame.RAYON*2+6;//(int) (HeightMap.HM_CELLSIZE*512/Chunk.GRIDSIZE);

	private static int seed = 543;
	
	@Override
	public void init() {
		Chunks = new Chunk[SIZE][SIZE];
	//	EMPTY.generate();

		island = InGame.island;
		chunkACreer = new ArrayList<Chunk>();
		chunkAVoroneiser = new ArrayList<Chunk>();
		
		nowAt(InGame.player.pos);
		
		//World.load(Gdx.files.internal("data/chunks.map").read());
		final WorldSolo world = this;
		
		threadBuilder = new Thread(new Runnable() {
			
				@Override
				public void run() {
					
					while(!mustStop ) {
						dontEvenThinkAboutPausing = false;
						
						
						while(!chunksToSave.isEmpty()) {
							Chunk c = chunksToSave.remove(0);
							if(c!=null) {
								c.save();
								chunksToDispose.add(c);
							}
						}
						
					
							while(!chunkACreer.isEmpty()) {
								try {
									
									Chunk c = chunkACreer.get(0);
									if(c!=null && chunkACreer.contains(c)) {
										
										chunkACreer.remove(c);
									
										c.generate();
									}
									
								}
								catch(Exception e) {
									System.out.println(e);
									e.printStackTrace(System.out);
								}
								
							}
					
					while(!chunkAVoroneiser.isEmpty()) {
						Chunk c1 = null, c2=null, c3=null, c4=null;
						
				//		System.out.println(chunkAVoroneiser.size()+" chunk à voronoiser");
						
						try {
							synchronized(world) {
								if(!chunkAVoroneiser.isEmpty()) {
									c1 = chunkAVoroneiser.get(0);
									chunkAVoroneiser.remove(c1);
								}
								
								if(!chunkAVoroneiser.isEmpty()) {
									c2 = chunkAVoroneiser.get(0);
									chunkAVoroneiser.remove(c2);
								}
								
								if(!chunkAVoroneiser.isEmpty()) {
									c3 = chunkAVoroneiser.get(0);
									chunkAVoroneiser.remove(c3);
								}
								
/*								if(!chunkAVoroneiser.isEmpty()) {
									c4 = (Chunk) chunkAVoroneiser.get(0);
									chunkAVoroneiser.remove(c4);
								}*/
							}
							
							if(c1!= null && c1.isGenerer) {
								builder1.buildCells(c1);
							}
							else {
								c1 = null;
							}
							if(c2!= null && c2.isGenerer) {
								builder2.buildCells(c2);
							}
							else {
								c2 = null;
							}
							if(c3!= null && c3.isGenerer) {
								builder3.buildCells(c3);
							}
							else {
								c3 = null;
							}
	/*						if(c4!= null && c4.isGenerer) {
								builder4.buildCells(c4);
							}
							else {
								c4 = null;
							}*/
						
							
							synchronized(world) {
							
								chunkAModeliser1 = c1;
								chunkAModeliser2 = c2;
								chunkAModeliser3 = c3;
								chunkAModeliser4 = c4;
						//		System.out.println("ATTTTTEEEEEEENNNNNNNND!!!!!!!");
								// on attent qu'il soit modélisé avant de continuer.
								if(chunkAModeliser1!=null || chunkAModeliser2!=null || chunkAModeliser3!=null || chunkAModeliser4!=null) world.wait();								
						//		System.out.println("ok vazy!");
							}


							
						}
						catch(Exception e) {
							System.out.println(e);
							e.printStackTrace(System.out);
						}
					}

					synchronized(world) {
						if(!dontEvenThinkAboutPausing && chunkACreer.isEmpty() && chunkAVoroneiser.isEmpty()) {
//							System.out.println("fini!");
							isThreadBuilderPaused = true;
							
							try {
								world.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}								
					}
					isThreadBuilderPaused = false;
					// important pour les chunks déchargés, mais c'est fait ailleurs tkt mec
					/*  float rayon = (InGame.RAYON+2)*Chunk.GRIDSIZE;
					  Island.getIslandAt(InGame.boy.pos.x, InGame.boy.pos.z).mustRebuildIndex = true;
					  try {
						  Island.getIslandAt(InGame.boy.pos.x+rayon, InGame.boy.pos.z).mustRebuildIndex = true;
						  Island.getIslandAt(InGame.boy.pos.x-rayon, InGame.boy.pos.z).mustRebuildIndex = true;
						  Island.getIslandAt(InGame.boy.pos.x, InGame.boy.pos.z+rayon).mustRebuildIndex = true;
						  Island.getIslandAt(InGame.boy.pos.x, InGame.boy.pos.z-rayon).mustRebuildIndex = true;
						  Island.getIslandAt(InGame.boy.pos.x+rayon, InGame.boy.pos.z+rayon).mustRebuildIndex = true;
						  Island.getIslandAt(InGame.boy.pos.x+rayon, InGame.boy.pos.z-rayon).mustRebuildIndex = true;
						  Island.getIslandAt(InGame.boy.pos.x-rayon, InGame.boy.pos.z+rayon).mustRebuildIndex = true;
						  Island.getIslandAt(InGame.boy.pos.x-rayon, InGame.boy.pos.z-rayon).mustRebuildIndex = true;						  
					  }
					  catch(Exception e) {
						  //pas grave, les iles existes pas encore
					  }*/
 
					}
				}
			});
		
		threadBuilder.start();			
		
	}

	
	@Override
	public void loadEntities() {

		InGame.entityManager = entityManager;
		
		
		
		InGame.player = new Player();
		
		entityManager.manage(InGame.player);
	


	}

	private static List<Chunk> chunksToDispose = Collections.synchronizedList(new ArrayList<Chunk>());
	private ArrayList<Chunk> chunksToSave = new ArrayList<Chunk>();

	@Override
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

			
		}
		
	//	if(c!=null) System.out.println(x+" "+c.x);
		
		return (c!=null && c.isValid && c.x==ax && c.z==ay && !c.disposed) ? c : EMPTY;
	}

	@Override
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
	//	long t = System.nanoTime();
	//	Runtime runtime = Runtime.getRuntime();
	//    long memory = runtime.totalMemory() - runtime.freeMemory();
		
		boolean notif = false;

				
				if(chunkAModeliser1!=null) {
					
					if(chunkAModeliser1.isValid) builder1.rebuildModel(chunkAModeliser1);
					else if(!chunkAModeliser1.isValid && !chunkAModeliser1.modelDemande) builder1.buildModel(chunkAModeliser1);
					rebuild(island.getIslandAt(chunkAModeliser1.posX, chunkAModeliser1.posZ));
					
					chunkAModeliser1 = null;
					notif = true;
				}
				 
				if(chunkAModeliser2!=null) {
					
					if(chunkAModeliser2.isValid) builder2.rebuildModel(chunkAModeliser2);
					else if(!chunkAModeliser2.isValid && !chunkAModeliser2.modelDemande) builder2.buildModel(chunkAModeliser2);
					rebuild(island.getIslandAt(chunkAModeliser2.posX, chunkAModeliser2.posZ));
					
					chunkAModeliser2 = null;
					notif = true;


				}

				/*	if(chunkAModeliser4!=null) {
						
						if(!chunkAModeliser4.isValid && !chunkAModeliser4.modelDemande) builder4.buildModel(chunkAModeliser4);
						rebuild(island.getIslandAt(chunkAModeliser4.posX, chunkAModeliser4.posZ));
						
						chunkAModeliser4 = null;
						notif = true;
					}
					 
					if(System.nanoTime()-t>1000000) System.out.println("2 temps:"+(System.nanoTime()-t)/1000000);
						 
					if(System.nanoTime()-t>1000000) System.out.println("3 temps:"+(System.nanoTime()-t)/1000000);*/
			 
					if(chunkAModeliser3!=null) {
						
						if(chunkAModeliser3.isValid) builder3.rebuildModel(chunkAModeliser3);
						else if(!chunkAModeliser3.isValid && !chunkAModeliser3.modelDemande) builder3.buildModel(chunkAModeliser3);
						rebuild(island.getIslandAt(chunkAModeliser3.posX, chunkAModeliser3.posZ));
						
						chunkAModeliser3 = null;

						notif = true;

					}
				 
				
				
				
				
				if(notif) {
										
				try{
					this.notifyAll();
					
				}
				catch(Exception e) {
					System.out.println(e);
				}

			}
		 
	/*		if(System.nanoTime()-t>1000000 || notif) {
				System.out.println("4 temps:"+(System.nanoTime()-t)/1000000);

			}*/
		 

		
	}

	private void rebuild(Island islandAt) {
		if(islandAt!=null) islandAt.mustRebuildIndex = true;
	}

	@Override
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

	@Override
	public void nowAt(Vector3 pos) {
		if(mustMAJRayon) {
			mustMAJRayon=false;
			majRayon();
		}
		
		while(!chunksToDispose.isEmpty()) {
			chunksToDispose.remove(0).dispose();
		}
		
		WorldAround.nowOn(pos);

		island.nowOn(pos, false);

		if(island.getIslandAt(InGame.player.pos.x, InGame.player.pos.z)==null) {
			// ile pas encore prete, on se casse mec
//			System.out.println("le problème c'est l'ile gros!");
			return;
		}
		
		modelise();
		
		
		if(chunkACreer.isEmpty() && chunkAVoroneiser.isEmpty() && 
				(chunkCourant==null || !chunkCourant.isGenerer
				|| getChunk(chunkCourant.posX+(InGame.RAYON-3)*Chunk.GRIDSIZE, chunkCourant.posZ)==EMPTY
				|| getChunk(chunkCourant.posX-(InGame.RAYON-3)*Chunk.GRIDSIZE, chunkCourant.posZ)==EMPTY
				|| getChunk(chunkCourant.posX, chunkCourant.posZ+(InGame.RAYON-3)*Chunk.GRIDSIZE)==EMPTY
				|| getChunk(chunkCourant.posX, chunkCourant.posZ-(InGame.RAYON-3)*Chunk.GRIDSIZE)==EMPTY)) {
//			System.out.println("refait moi ça!");
			chunkCourant = null;
		}

		
		if(chunkCourant==getOrCreateChunk((int)Math.floor(pos.x/Chunk.GRIDSIZE), (int)Math.floor(pos.z/Chunk.GRIDSIZE)) && !verifieMec) {
			return;
		}
		else if(chunkCourant==getOrCreateChunk((int)Math.floor(pos.x/Chunk.GRIDSIZE), (int)Math.floor(pos.z/Chunk.GRIDSIZE)) && verifieMec) {
			
			if(chunkACreer.isEmpty() && chunkAVoroneiser.isEmpty()) {
				verifieMec = false;				
			}
			 // réveil le thread
			  synchronized (this) {
				  if(isThreadBuilderPaused) {
					  notifyAll();
				  }
			  }
			  return;
		}
		else {
			chunkCourant=getOrCreateChunk((int)Math.floor(pos.x/Chunk.GRIDSIZE), (int)Math.floor(pos.z/Chunk.GRIDSIZE));
			
			// pour virer ce qui est derriere.
			 float rayon = (InGame.RAYON+2)*Chunk.GRIDSIZE;
			 
			 island.getIslandAt(InGame.player.pos.x, InGame.player.pos.z).mustRebuildIndex = true;
			 try {
				  island.getIslandAt(InGame.player.pos.x+rayon, InGame.player.pos.z).mustRebuildIndex = true;
				  island.getIslandAt(InGame.player.pos.x-rayon, InGame.player.pos.z).mustRebuildIndex = true;
				  island.getIslandAt(InGame.player.pos.x, InGame.player.pos.z+rayon).mustRebuildIndex = true;
				  island.getIslandAt(InGame.player.pos.x, InGame.player.pos.z-rayon).mustRebuildIndex = true;
				  island.getIslandAt(InGame.player.pos.x+rayon, InGame.player.pos.z+rayon).mustRebuildIndex = true;
				  island.getIslandAt(InGame.player.pos.x+rayon, InGame.player.pos.z-rayon).mustRebuildIndex = true;
				  island.getIslandAt(InGame.player.pos.x-rayon, InGame.player.pos.z+rayon).mustRebuildIndex = true;
				  island.getIslandAt(InGame.player.pos.x-rayon, InGame.player.pos.z-rayon).mustRebuildIndex = true;						  
			  }
			  catch(Exception e) {
				  //pas grave, les iles existes pas encore
			  }
			
		}
		int cc=0;
		
//		System.out.println(chunkCourant.isValid+" "+chunkCourant.isGenerer+" "+chunkCourant.modelDemande+" "+chunkCourant.isVoronoiser);
			
		  //TODO fix crash
		  try {
			// on fait quoi, on vérifie d'abord que tout les chunks du rayon+1 sont générés ?
			chunkACreer.clear();
			  for(int i=chunkCourant.x-InGame.RAYON-1;i<chunkCourant.x+InGame.RAYON+1;i+=1) {
				  for(int j=chunkCourant.z-InGame.RAYON-1;j<chunkCourant.z+InGame.RAYON+1;j+=1) {
					
					  Chunk c = getOrCreateChunk(i, j);
					  if(!c.isGenerer) {
						  chunkACreer.add(c);
						  cc++;
						  dontEvenThinkAboutPausing = true;
						  verifieMec = true;
					  }
				  }
			  }
			  
		//	  System.out.println(cc+" chunks à génerer.");
			  
			  cc=0;
			  chunkAVoroneiser.clear();
			  for(int i=chunkCourant.x-InGame.RAYON;i<chunkCourant.x+InGame.RAYON;i+=1) {
				  for(int j=chunkCourant.z-InGame.RAYON;j<chunkCourant.z+InGame.RAYON;j+=1) {
					
					  Chunk c = getOrCreateChunk(i, j);
					  if(!c.isVoronoiser) {
						  chunkAVoroneiser.add(c);
						  cc++;
						  dontEvenThinkAboutPausing = true;
						  verifieMec = true;
					  }
				  }
			  }
			  
			  Collections.sort(chunkAVoroneiser, new ChunkComparator());
			  
		  }
		  catch(Exception e) {
//			  e.printStackTrace();
			  try {
				  Collections.sort(chunkAVoroneiser, new ChunkComparator());
				  
			  }
			  catch(Exception dfe) {
				  
			  }
		
		  }
			  
		//  System.out.println(cc+" chunks à voronoiser.");
		  
		  // on dispose les chunks en n+3
		  
			 
		 // réveil le thread
			  synchronized (this) {
				  if(isThreadBuilderPaused) {
					  notifyAll();
				  }
		  }
	}
	
	private void majRayon() {
		
		float rayon = (InGame.RAYON+2)*Chunk.GRIDSIZE;
		  island.getIslandAt(InGame.player.pos.x, InGame.player.pos.z).mustRebuildIndex = true;
		  try {
			  island.getIslandAt(InGame.player.pos.x+rayon, InGame.player.pos.z).mustRebuildIndex = true;
			  island.getIslandAt(InGame.player.pos.x-rayon, InGame.player.pos.z).mustRebuildIndex = true;
			  island.getIslandAt(InGame.player.pos.x, InGame.player.pos.z+rayon).mustRebuildIndex = true;
			  island.getIslandAt(InGame.player.pos.x, InGame.player.pos.z-rayon).mustRebuildIndex = true;
			  island.getIslandAt(InGame.player.pos.x+rayon, InGame.player.pos.z+rayon).mustRebuildIndex = true;
			  island.getIslandAt(InGame.player.pos.x+rayon, InGame.player.pos.z-rayon).mustRebuildIndex = true;
			  island.getIslandAt(InGame.player.pos.x-rayon, InGame.player.pos.z+rayon).mustRebuildIndex = true;
			  island.getIslandAt(InGame.player.pos.x-rayon, InGame.player.pos.z-rayon).mustRebuildIndex = true;						  
		  }
		  catch(Exception e) {
			  //pas grave, les iles existes pas encore
		  }
		
		chunkACreer.clear();
		  for(int i=chunkCourant.x-InGame.RAYON-1;i<chunkCourant.x+InGame.RAYON+1;i+=1) {
			  for(int j=chunkCourant.z-InGame.RAYON-1;j<chunkCourant.z+InGame.RAYON+1;j+=1) {
				
				  Chunk c = getChunk(i, j);
				  setChunk(i, j, null);
				  c.dispose();
			  }
		  }
		  
		  chunkAVoroneiser.clear();
		  WorldAround.initBuilder();
			Ground.prepare();

		InGame.RAYON = ExterminateGame.rendu;
		SIZE = InGame.RAYON*2+6;
	//	if(Chunks.length<SIZE) {
			Chunks = new Chunk[SIZE][SIZE];
			
	//	}
	}

	@Override
	public void save() {
	

	}

	@Override
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

}
