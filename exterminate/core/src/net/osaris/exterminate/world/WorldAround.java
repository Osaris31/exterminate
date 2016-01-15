package net.osaris.exterminate.world;

import net.osaris.exterminate.InGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class WorldAround {

	static int lastx;
	static int lasty;
	static int lastz;
	
	
	// les quatres chunks autour du joueur.
	public static Chunk current;
	public static boolean ready = false;
	public static Chunk[][] voisins = new Chunk[3][3];
	
	public static float timeNotReady = 0;
	private static Vector3 tmpv = new Vector3();
	
	public static void nowOn(Vector3 pos) {

		if(!ready || current==null || !current.isPosInChunk(pos)) {
			ready = true;
			
			for(int i=0;i<3;i++) {
				for(int j=0;j<3;j++) {
					voisins[i][j] = InGame.world.getChunk(pos.x+(i-1)*Chunk.GRIDSIZE, pos.z+(j-1)*Chunk.GRIDSIZE, true);
					if(voisins[i][j]==World.EMPTY) {
//						System.out.println("manque "+Math.floor((pos.x+(i-1)*Chunk.GRIDSIZE)/Chunk.GRIDSIZE) +" "+ +Math.floor((pos.z+(j-1)*Chunk.GRIDSIZE)/Chunk.GRIDSIZE));
						ready = false;
					}
				}				
			}
			
			
			if(ready && !saved) {
				ready = false;
				saved = true;
				// pour éviter le lag disque
				voisins[0][0].save();
				ready = true;
			}
			

			
			if(!ready) {
				timeNotReady+=Gdx.graphics.getDeltaTime();
				
				if(timeNotReady>5) {
					if(InGame.player.world instanceof WorldSolo) {
						((WorldSolo)InGame.player.world).chunkCourant = null;
					}
					timeNotReady=0;
					for(int i=0;i<3;i++) {
						for(int j=0;j<3;j++) {
							voisins[i][j] = InGame.world.getChunk(pos.x+(i-1)*Chunk.GRIDSIZE, pos.z+(j-1)*Chunk.GRIDSIZE, true);
							if(voisins[i][j]==World.EMPTY) {
								InGame.world.unloadChunk(pos.x+(i-1)*Chunk.GRIDSIZE, pos.z+(j-1)*Chunk.GRIDSIZE, true);
//								System.out.println("demande "+Math.floor((pos.x+(i-1)*Chunk.GRIDSIZE)/Chunk.GRIDSIZE) +" "+ +Math.floor((pos.z+(j-1)*Chunk.GRIDSIZE)/Chunk.GRIDSIZE));
							}
						}				
					}
				}
			}

			
			current = voisins[1][1];
		}
	}
	
	public static Chunk getChunkAt(Vector3 pos0) {
		Vector3 pos = tmpv ;
		tmpv.set((float)Math.floor(pos0.x), (float)Math.floor(pos0.x), (float)Math.floor(pos0.z));
		if(pos.x>=current.posX && pos.z>=current.posZ && pos.x<current.posX+Chunk.GRIDSIZE && pos.z<current.posZ+Chunk.GRIDSIZE) {
			return current;
		}
		else if(pos.x>=current.posX-Chunk.GRIDSIZE && pos.z>=current.posZ-Chunk.GRIDSIZE && pos.x<current.posX+Chunk.GRIDSIZE*2 && pos.z<current.posZ+Chunk.GRIDSIZE*2) {
			return voisins[(int) (Math.floor((pos.x-current.posX+Chunk.GRIDSIZE)/Chunk.GRIDSIZE))][(int) (Math.floor((pos.z-current.posZ+Chunk.GRIDSIZE)/Chunk.GRIDSIZE))];
		}
		else {
			return InGame.world.getChunk(pos.x, pos.z);
		}
	}
	
	public static Chunk getChunkAt(float posx, float posz) {
		posx = (float)Math.floor(posx);
		posz = (float)Math.floor(posz);
		if(current!=null && posx>=current.posX && posz>=current.posZ && posx<current.posX+Chunk.GRIDSIZE && posz<current.posZ+Chunk.GRIDSIZE) {
			return current;
		}
		else if(current!=null && posx>=current.posX-Chunk.GRIDSIZE && posz>=current.posZ-Chunk.GRIDSIZE && posx<current.posX+Chunk.GRIDSIZE*2 && posz<current.posZ+Chunk.GRIDSIZE*2) {
			return voisins[(int) (Math.floor((posx-current.posX)/Chunk.GRIDSIZE)+1)][(int) (Math.floor((posz-current.posZ)/Chunk.GRIDSIZE)+1)];
		}
		else {
			return InGame.world.getChunk(posx, posz);
		}
	}
	
	// hauteur au centre de la case.
	public static float getRawHeight(float posx, float posz) {
		Chunk c = getChunkAt(posx, posz);
		
		return c.heightmap[(int)Math.floor(posx-c.posX)*Chunk.GRIDSIZE1+(int)Math.floor(posz-c.posZ)];
	}
	
	
	
/*	public static void copyCaracs(float posx, float posy, float posz, float posTox, float posToy, float posToz, boolean clearX, boolean clearY, boolean clearZ) {
		Chunk c = getChunkAt(posx, posz);
		Chunk c1 = getChunkAt(posTox, posToz);
		
		if(c.getCellAt(posx, posy, posz, tmpv )==BlockType.UNDEF) {
			c.generateEmptyCellAt(posx, posy, posz);
			c.getCellAt(posx, posy, posz, tmpv );
		}
		
		
		if(clearX) tmpv.x=0.5f;
		if(clearY) tmpv.y=0.5f;
		if(clearZ) tmpv.z=0.5f;
		c.setCellAt(posx, posy, posz, tmpv);
		c.doitEtreRegenerer = true;

		c1.setCellAt(posTox, posToy, posToz, tmpv);
		c1.doitEtreRegenerer = true;
	}
	
	public static void setCaracs(float posx, float posy, float posz, float caracTox, float caracToy, float caracToz) {
		Chunk c = getChunkAt(posx, posz);
		
		tmpv.x=caracTox;
		tmpv.y=caracToy;
		tmpv.z=caracToz;
		c.setCellAt(posx, posy, posz, tmpv);
		c.doitEtreRegenerer = true;

	}
	public static void setRandCaracs(float posx, float posy, float posz, int caracTox, int caracToy, int caracToz) {
		Chunk c = getChunkAt(posx, posz);
		
		if(caracTox==-1) {
			tmpv.x=0.5f;
			tmpv.y=rand1[caracToy][caracToz];
			tmpv.z=rand2[caracToy][caracToz];
		}
		else if(caracToy==-1) {
			tmpv.x=rand1[caracTox][caracToz];
			tmpv.y=0.5f;
			tmpv.z=rand2[caracTox][caracToz];
		}
		else if(caracToz==-1) {
			tmpv.x=rand1[caracTox][caracToy];
			tmpv.y=rand2[caracTox][caracToy];
			tmpv.z=0.5f;
		}

		c.setCellAt(posx, posy, posz, tmpv);
		c.doitEtreRegenerer = true;

	}*/
/*	
	public static void copyCaracs9X(float posx, float posy, float posz, int dir) {
		for(int i=-1;i<2;i++) {
			for(int j=-1;j<2;j++) {
				copyCaracs(posx, posy+i, posz+j, posx+dir, posy+i, posz+j, true, false, false);
			}
		}
	}
	
	public static void copyCaracs9Y(float posx, float posy, float posz, int dir) {
		for(int i=-1;i<2;i++) {
			for(int j=-1;j<2;j++) {
				copyCaracs(posx+i, posy, posz+j, posx+i, posy+dir, posz+j, false, true, false);
			}
		}
	}
	
	public static void copyCaracs9Z(float posx, float posy, float posz, int dir) {
		for(int i=-1;i<2;i++) {
			for(int j=-1;j<2;j++) {
				copyCaracs(posx+i, posy+j, posz, posx+i, posy+j, posz+dir, false, false, true);
			}
		}
	}
	*/
/*	public static void setCaracs9X(float posx, float posy, float posz) {
		for(int i=-1;i<2;i++) {
			for(int j=-1;j<2;j++) {
				setRandCaracs(posx, posy+i, posz+j, -1, modulo((posy)+i, RS), modulo((posz)+j, RS));
	//			setRandCaracs(posx+dir, posy+i, posz+j, -1, modulo((posy)+i, RS), modulo((posz)+j, RS));
			}
		}
	}
	
	public static void setCaracs9Y(float posx, float posy, float posz) {
		for(int i=-1;i<2;i++) {
			for(int j=-1;j<2;j++) {
				setRandCaracs(posx+i, posy, posz+j, modulo((posx)+i, RS), -1, modulo((posz)+j, RS));
	//			setRandCaracs(posx+i, posy+dir, posz+j, modulo((posx)+i, RS), -1, modulo((posz)+j, RS));
			}
		}
	}
	
	public static void setCaracs9Z(float posx, float posy, float posz) {
		for(int i=-1;i<2;i++) {
			for(int j=-1;j<2;j++) {
				setRandCaracs(posx+i, posy+j, posz, modulo((posx)+i, RS), modulo((posy)+j, RS), -1);
	//			setRandCaracs(posx+i, posy+j, posz+dir, modulo((posx)+i, RS), modulo((posy)+j, RS), -1);
			}
		}
	}*/
	
	public static int modulo(float nombre, int mod) {
		return (((((int)nombre)%mod)+mod)%mod);
	}
	
	public static int RS = 16;
	public static float[][] rand1 = new float[RS][RS];
	public static float[][] rand2 = new float[RS][RS];
	private static boolean saved;
	
	public static void initBuilder()
	{
		for(int i=0;i<RS;i++) {
			for(int j=0;j<RS;j++) {
				rand1[i][j] = (float) (Math.random()*0.5f-0.25f);
				rand2[i][j] = (float) (Math.random()*0.5f-0.25f);
			}			
		}
		
		current = null;
		ready = false;
		saved = false;
		voisins = new Chunk[3][3];
		
	}
}
