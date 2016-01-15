package net.osaris.exterminate.world;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import net.osaris.exterminate.ExterminateGame;
import net.osaris.exterminate.InGame;
import net.osaris.exterminate.world.biomes.Biome;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.UniMesh;
import com.badlogic.gdx.graphics.UniVertexBufferObject;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Ground {
	
	public UniMesh mesh;
	
	public boolean usedSol[];
	public Chunk forChunk[];
	public int firstUnusedSol = 0;
	public int lastUsedSol = 0;
	
	public void makeMesh() {
		int rayon = Math.max(ExterminateGame.rendu, ExterminateGame.MAX_VIEW_DISTANCE)+5;
		 int nbTriangles=ChunkBuilder.GRIDSIZE*ChunkBuilder.GRIDSIZE*2*rayon*2*rayon*2;
		 int nbVertex=(ChunkBuilder.GRIDSIZE+1)*(ChunkBuilder.GRIDSIZE+1)*rayon*2*rayon*2;
		 
		FloatBuffer vertexL = org.lwjgl.BufferUtils.createFloatBuffer(nbVertex*ChunkBuilder.SOL_VERTICE_SIZE);
		IntBuffer indicesL = org.lwjgl.BufferUtils.createIntBuffer(nbTriangles*3);
		
		mesh = new UniMesh(true, true, nbVertex, nbTriangles*3, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.NormalPacked(), VertexAttribute.TexCoords(0), VertexAttribute.ColorPacked()));
		mesh.setVertices(vertexL, 0, nbVertex*ChunkBuilder.SOL_VERTICE_SIZE);
		mesh.setIndices(indicesL, 0, nbTriangles*3);
		mesh.setCapacity(0);

		usedSol = new boolean[rayon*2*rayon*2];
		forChunk = new Chunk[rayon*2*rayon*2];
		
		for(int i=0;i<rayon*2*rayon*2;i++) {
			usedSol[i] = false;
		}

		UniVertexBufferObject.showMem();
	}

	public synchronized int getMeshId(Chunk who) {
		
		
		while(usedSol[firstUnusedSol]) {
			firstUnusedSol++;
			
			if(firstUnusedSol>=usedSol.length) {
				cleanUp();
			}
		}
		lastUsedSol = Math.max(lastUsedSol, firstUnusedSol);
		
		usedSol[firstUnusedSol] = true;
		forChunk[firstUnusedSol] = who;

		
//		System.out.println(firstUnusedSol+" pour "+who);

		return firstUnusedSol;
	}

	private void cleanUp() {
	//	System.out.println("je clean :/");
		for(int i=0;i<usedSol.length;i++) {
			if(!usedSol[i]) {
				firstUnusedSol = i;
				return;
			}
		}
		for(int i=0;i<usedSol.length;i++) {
			
			boolean trouve = false;
			
			for(int j=0;j<InGame.world.Chunks.length;j++) {
				for(int k=0;k<InGame.world.Chunks.length;k++) {
					if(InGame.world.Chunks[j][k]!=null && InGame.world.Chunks[j][k]==forChunk[i]) trouve = true;
				}				
			}
			if(!trouve) {
				firstUnusedSol = i;
				usedSol[i] = false;
				forChunk[i] = null;
				return;
			}
		}
	//	System.out.println("c'est chaud là :/");
		//cas désespéré...
		firstUnusedSol--;
		usedSol[firstUnusedSol]=false;
	}

	public synchronized void render(ShaderProgram shader, boolean texture) {
		boolean singleBiome = true;
		 if(mesh==null || InGame.player.onChunk==null) return;
			int rayon = Math.max(ExterminateGame.rendu, ExterminateGame.MAX_VIEW_DISTANCE)+5;
			 int nbTriangles=ChunkBuilder.GRIDSIZE*ChunkBuilder.GRIDSIZE*2;
	 
		for(int i=0;i<usedSol.length;i++) {
			if(usedSol[i] && forChunk[i].biome!=InGame.player.onChunk.biome) {
				// bah tiens, si il est trop loin, c'est le moment de le dispose.
				if(forChunk[i].isValid && (forChunk[i]!=forChunk[i].world.getChunk(forChunk[i].posX, forChunk[i].posZ)
						|| forChunk[i].x>InGame.player.onChunk.x+rayon+2
						|| forChunk[i].x<InGame.player.onChunk.x-rayon-2
						|| forChunk[i].z>InGame.player.onChunk.z+rayon+2
						|| forChunk[i].z<InGame.player.onChunk.z-rayon-2
						
						)) {
					
					forChunk[i].dispose();
					
					usedSol[i] = false;
					forChunk[i] = null;
				}
				else if(forChunk[i].disposed) {
					usedSol[i] = false;
					forChunk[i] = null;
					
				}
//				else {
//					System.out.println("loin : "+forChunk[i].x+" "+forChunk[i].z+" "+forChunk[i].isValid);
					singleBiome = false;					
//				}
			}
		}
//		System.out.println(singleBiome);
		  if(texture && singleBiome) {
				 Biome biome = InGame.player.onChunk.biome;
					biome.textureHerbe.bind(1);
					biome.textureSand.bind(2);
					biome.textureRock.bind(3);
					biome.textureDirt.bind(4);		
				mesh.render(shader,  GL20.GL_TRIANGLES);
		  }
		  else if(!texture) {
				mesh.render(shader,  GL20.GL_TRIANGLES);			  
		  }
		  else {
			  Biome biome = null;
				for(int i=0;i<usedSol.length;i++) {
					
					if(usedSol[i]) {
						
						if(forChunk[i].biome!=biome) {
							biome = forChunk[i].biome;
							biome.textureHerbe.bind(1);
							biome.textureSand.bind(2);
							biome.textureRock.bind(3);
							biome.textureDirt.bind(4);		
						}
						mesh.render(shader,  GL20.GL_TRIANGLES, nbTriangles*3*i*4, nbTriangles*3, true);			  

					}
				}
		  }
	}

	public synchronized void dispose(int solMeshId) {
		if(solMeshId<0) return;
		
/*		if(forChunk[solMeshId]!=null) {
			if(Math.abs(forChunk[solMeshId].x-InGame.player.onChunk.x)<10 && Math.abs(forChunk[solMeshId].z-InGame.player.onChunk.z)<10) {
				System.out.println("!!!!!!"+solMeshId);
				System.out.println(forChunk[solMeshId]);
			//	Thread.dumpStack();			
			}

		}*/
		
		usedSol[solMeshId] = false;
		forChunk[solMeshId] = null;
		
		while(lastUsedSol>0 && !usedSol[lastUsedSol]) {
			lastUsedSol--;
		}
		mesh.setCapacity((lastUsedSol+1)*ChunkBuilder.GRIDSIZE*ChunkBuilder.GRIDSIZE*2*3*4);
		firstUnusedSol = Math.min(firstUnusedSol, solMeshId);
		
	}

	public static void prepare() {
		if(Chunk.ground!=null) {
			Chunk.ground.mesh.dispose();
		}
		if(Chunk.water!=null) {
			Chunk.water.mesh.dispose();
		}
		
		
		Chunk.ground = new Ground();
		Chunk.ground.makeMesh();
		Chunk.water = new Ground();
		Chunk.water.makeMesh();
	}

	
	
}
