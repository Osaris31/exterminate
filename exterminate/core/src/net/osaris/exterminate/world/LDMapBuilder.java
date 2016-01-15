package net.osaris.exterminate.world;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import net.osaris.exterminate.InGame;
import net.osaris.exterminate.world.biomes.Biome;

import org.lwjgl.BufferUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.UniMesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;

public class LDMapBuilder {
	
	public int nbTrianglesL = 0;
	public int nbVertexL = 0;
	public float posX;
	public float posZ;
	public boolean hasBlocks = false;
	public boolean hasEau = false;	
	public Mesh modelBlock;	
	public UniMesh modelEau;
	public UniMesh modelSol;	
	public static float world[][];

	final static int VERTICE_SIZE = 8;
	final static int SOL_VERTICE_SIZE = 12;
	final static int EAU_VERTICE_SIZE = 12;
	public final static int GRIDSIZE = 128;
	final static int APPROX = IslandManager.HM_GRIDSIZE/GRIDSIZE;
	public final static float HM_CELLSIZE = IslandManager.HM_CELLSIZE;

	public final static float SIZE = GRIDSIZE;


	private FloatBuffer vertexL = BufferUtils.createFloatBuffer(65536*SOL_VERTICE_SIZE);
	private ShortBuffer indicesL = BufferUtils.createShortBuffer(65536*6);
		

	private Vector3 tmp = new Vector3();
	
	public byte[][] cells;
	private Island island;
	
	public LDMapBuilder() {
	
		posX = 0;
		posZ = 0;
		
	}
	
	public void buildModels(float[][] world, Island islands) {
		
		this.world = world;
		posX = IslandManager.HM_GRIDSIZE*islands.x*HM_CELLSIZE;
		posZ = IslandManager.HM_GRIDSIZE*islands.z*HM_CELLSIZE;
		hasBlocks = false;
		hasEau = false;
		island = islands;
		buildModelSol();
		buildModelEau();

		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);

	}
	

	public void rebuildIndex(Island world) {
		
		this.world = world.world;
		island = world;
		modelSol = world.modelSol;
		modelEau = world.modelEau;
		posX = IslandManager.HM_GRIDSIZE*world.x*HM_CELLSIZE;
		posZ = IslandManager.HM_GRIDSIZE*world.z*HM_CELLSIZE;
		hasBlocks = false;
		hasEau = false;
		rebuildIndexSol();
		rebuildIndexEau();
		
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	

	public void buildModelEau() {
		float hauteurEau = IslandManager.hauteurEau();
		indicesL.clear();
		vertexL.clear();

		int mul=APPROX;

		int wc = GRIDSIZE+1;
		nbVertexL = (GRIDSIZE+1)*(GRIDSIZE+1);
		nbTrianglesL = GRIDSIZE*GRIDSIZE*2;
				
		int ii=0, jj=0;
		float y;
				  for(int j=0;j<wc;j++) {
						for(int i=0;i<wc;i++) {

					  ii=mul*i;
					  jj=mul*j;
					  
					  if(ii>0 && jj>0 && ii<world.length && jj<world.length) {
							y=world[ii][jj]*IslandManager.HAUTEUR;
					  }
					  else {
						  y=0;
					  }					  
					  
					  
					  vertexL.put((i)*HM_CELLSIZE*APPROX+posX);

					  vertexL.put(hauteurEau-1f);
					  
					  vertexL.put((j)*HM_CELLSIZE*APPROX+posZ);


			  vertexL.put(0);
			  vertexL.put(1); 
			  vertexL.put(0);

			  vertexL.put(posX+i*HM_CELLSIZE/16f*APPROX/8f);
			  vertexL.put(posZ+j*HM_CELLSIZE/16f*APPROX/8f);
			  
			  vertexL.put((float) Math.exp(-Math.max(0f, hauteurEau-y)*0.1f));

			  vertexL.put(0);
			  vertexL.put(0);
			  vertexL.put(1);
			  }
		  }


		int k=0;
		for(int i=0;i<wc-1;i++) {
			  for(int j=0;j<wc-1;j++) {
				  
				  ii=mul*i;
				  jj=mul*j;
				  
				  if(world[ii][jj]*IslandManager.HAUTEUR<hauteurEau || i==wc-2 || j==wc-2
						  || world[ii+mul][jj]*IslandManager.HAUTEUR<hauteurEau
						  || world[ii][jj+mul]*IslandManager.HAUTEUR<hauteurEau
						  || world[ii+mul][jj+mul]*IslandManager.HAUTEUR<hauteurEau) {
						
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
					 indicesL.put((short) (i+1+(j+1)*wc));
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
					 hasEau = true;
			  	}
			  }
		  }

		nbTrianglesL = k/3;
		
		
		if(hasEau) {
			UniMesh mesh = new UniMesh(true, true, nbVertexL, nbTrianglesL*3, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0), VertexAttribute.ColorUnpacked()));
			mesh.setVertices(vertexL, 0, nbVertexL*EAU_VERTICE_SIZE);
			mesh.setIndices(indicesL, 0, nbTrianglesL*3);

			modelEau = mesh;

			
		}
		
		
	}
	

	public void rebuildIndexEau() {
		float hauteurEau = IslandManager.hauteurEau();
		indicesL.clear();
		int ii=0, jj=0;
		int mul=APPROX;

		int wc = GRIDSIZE+1;
		nbVertexL = (GRIDSIZE+1)*(GRIDSIZE+1);
		nbTrianglesL = GRIDSIZE*GRIDSIZE*2;
		Chunk c;
		int rayon = InGame.RAYON;
		int k=0;
		for(int i=0;i<wc-1;i++) {
			  for(int j=0;j<wc-1;j++) {
				  float atX = (i)*HM_CELLSIZE*APPROX+posX;
				  float atZ = (j)*HM_CELLSIZE*APPROX+posZ;
				  
				  ii=mul*i;
				  jj=mul*j;
				  
					if(atX<(-rayon*Chunk.GRIDSIZE)+InGame.player.pos.x
					|| atX>((rayon-1)*Chunk.GRIDSIZE)+InGame.player.pos.x
					|| atZ<(-rayon*Chunk.GRIDSIZE)+InGame.player.pos.z
					|| atZ>((rayon-1)*Chunk.GRIDSIZE)+InGame.player.pos.z) {
						 
						  if(world[ii][jj]*IslandManager.HAUTEUR<hauteurEau  || i==wc-2 || j==wc-2
								  || world[ii+mul][jj]*IslandManager.HAUTEUR<hauteurEau
								  || world[ii][jj+mul]*IslandManager.HAUTEUR<hauteurEau
								  || world[ii+mul][jj+mul]*IslandManager.HAUTEUR<hauteurEau) {
						 indicesL.put((short) (i+1+j*wc));
						 indicesL.put((short) (i+j*wc));
						 indicesL.put((short) (i+(j+1)*wc));
						 k+=3;
						 indicesL.put((short) (i+1+(j+1)*wc));
						 indicesL.put((short) (i+1+j*wc));
						 indicesL.put((short) (i+(j+1)*wc));
						 k+=3;
						  }
						continue;
					}
				  				  
				  c = InGame.world.getChunk((i)*HM_CELLSIZE*APPROX+posX, (j)*HM_CELLSIZE*APPROX+posZ);
				  
				  if((c==World.EMPTY || !c.isValid || c.enCreation || c.disposed || c.modelDemande) &&

						 (world[ii][jj]*IslandManager.HAUTEUR<hauteurEau || i==wc-2 || j==wc-2
						  || world[ii+mul][jj]*IslandManager.HAUTEUR<hauteurEau
						  || world[ii][jj+mul]*IslandManager.HAUTEUR<hauteurEau
						  || world[ii+mul][jj+mul]*IslandManager.HAUTEUR<hauteurEau)) {
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
					 indicesL.put((short) (i+1+(j+1)*wc));
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
					 hasEau = true;
			  	}
			  }
		  }

		nbTrianglesL = k/3;

		modelEau.setIndices(indicesL, 0, nbTrianglesL*3);
		
	}
	
	
	public void rebuildIndexSol() {

		float hauteurEau = IslandManager.hauteurEau();
		float y;
		int wc = GRIDSIZE+1;
		indicesL.clear();
	
		int ii=0, jj=0;
		int mul=APPROX;
		
		nbVertexL = (GRIDSIZE+1)*(GRIDSIZE+1);
		nbTrianglesL = GRIDSIZE*GRIDSIZE*2;

		Chunk c, c2;
		int k=0;
		int rayon = InGame.RAYON;
		float atX, atZ;
		for(int i=0;i<wc-1;i++) {
			  for(int j=0;j<wc-1;j++) {

				  atX = (i)*HM_CELLSIZE*APPROX+posX;
				  atZ = (j)*HM_CELLSIZE*APPROX+posZ;
				  
					if(atX<(-(rayon-1)*Chunk.GRIDSIZE)+InGame.player.pos.x
					|| atX>((rayon-2)*Chunk.GRIDSIZE)+InGame.player.pos.x
					|| atZ<(-(rayon-1)*Chunk.GRIDSIZE)+InGame.player.pos.z
					|| atZ>((rayon-2)*Chunk.GRIDSIZE)+InGame.player.pos.z) {
						
						 indicesL.put((short) (i+1+j*wc));
						 indicesL.put((short) (i+j*wc));
						 indicesL.put((short) (i+(j+1)*wc));
						 k+=3;
						 indicesL.put((short) (i+1+(j+1)*wc));
						 indicesL.put((short) (i+1+j*wc));
						 indicesL.put((short) (i+(j+1)*wc));
						 k+=3;
						 
						continue;
					}
				  
					  c = InGame.world.getChunk(atX+8, atZ+8);
						 atX = ((float)i+1)*HM_CELLSIZE*APPROX+posX;
						  atZ = ((float)j+1)*HM_CELLSIZE*APPROX+posZ;
					  c2 = InGame.world.getChunk(atX+8, atZ+8);
					  
				  if((c==World.EMPTY || !c.isValid || c.enCreation || c.disposed || c.modelDemande ||
						  c2==World.EMPTY || !c2.isValid || c2.enCreation || c2.modelDemande)) {
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
					 indicesL.put((short) (i+1+(j+1)*wc));
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
				  }
			  }
		  }
		
		nbTrianglesL = k/3;
	
		
		modelSol.setIndices(indicesL, 0, nbTrianglesL*3);
	}
	
	
	
	public void buildModelSol() {

		modelSol =  getSolMesh();

	}
	
	
	
	public UniMesh getSolMesh() {
		float hauteurEau = IslandManager.hauteurEau();
				float y;
				int wc = GRIDSIZE+1;
				
				int ii=0, jj=0;
				int mul=APPROX;
				indicesL.clear();
				vertexL.clear();
			
		nbVertexL = (GRIDSIZE+1)*(GRIDSIZE+1);
		nbTrianglesL = GRIDSIZE*GRIDSIZE*2;
				  for(int j=0;j<GRIDSIZE+1;j++) {
						for(int i=0;i<GRIDSIZE+1;i++) {
					
			  vertexL.put((i)*HM_CELLSIZE*APPROX+posX);
			  
			  
			  ii=mul*i;
			  jj=mul*j;
			  
			  if(ii>=0 && jj>=0 && ii<world.length && jj<world.length) {
					y=world[ii][jj]*IslandManager.HAUTEUR;
			  }
			  else {
				  y=0;
			  }
			  vertexL.put(y-1.5f);
			  
			  vertexL.put((j)*HM_CELLSIZE*APPROX+posZ);

					//  2v = (f(x−1,y) − f(x+1,y), f(x,y−1) − f(x,y+1), 2)
			/*  if(y<hauteurEau-2f) {
				  tmp.set(0, 
						  -1, 
						  0).nor();				  
			  }
			  else*/ if(ii>0 && jj>0 && ii<world.length-1 && jj<world.length-1) {
				  tmp.set(world[ii-1][jj]*IslandManager.HAUTEUR-world[ii+1][jj]*IslandManager.HAUTEUR, 
						  2, 
						  world[ii][jj-1]*IslandManager.HAUTEUR-world[ii][jj+1]*IslandManager.HAUTEUR).nor();				  
			  }
			  else {
				  tmp.set(0,
						  1, 
						 0);		
			  }
				  
				  vertexL.put(tmp.x);
				  vertexL.put(tmp.y); 
				  vertexL.put(tmp.z);

			  vertexL.put(i*HM_CELLSIZE/16f*APPROX);
			  vertexL.put(j*HM_CELLSIZE/16f*APPROX);

			  
			  
			  
			 float ombre = 1;
			  if(y<hauteurEau-2) {
				  ombre = Math.min(ombre, 1.0f-Math.min(island.biome.coefOmbrageEau,  (hauteurEau-y-2)/30f));
			  }
			  ombre = Math.max(0, ombre);

			  if(y<hauteurEau-2f) {
				  vertexL.put(0f);
				  vertexL.put(0f);
				  vertexL.put(0.5f);
			  }
			  else if(y<hauteurEau+2f && Math.abs(tmp.x)<0.25 && Math.abs(tmp.z)<0.25) {
				  vertexL.put(0f);
				  vertexL.put(0f);
				  vertexL.put(0f);
			  }
			  else if(y<hauteurEau+1f) {
				  vertexL.put(0f);
				  vertexL.put(1f);
				  vertexL.put(0f);
			  }
			  else if(Math.abs(tmp.x)>0.75 || Math.abs(tmp.z)>0.75) {
				  if(island.biome==Biome.biomeGrass) {

					  vertexL.put(0);
					  vertexL.put(0);
					  vertexL.put(1);
				  }
				  else {
					  vertexL.put(0.8f);
					  vertexL.put(0.2f);
					  vertexL.put(0f);
					  
					  
				  }
				  if(island.biome==Biome.biomeGrass)		  ombre-=0.1f;
			  }
			  else if(Math.abs(tmp.x)>0.5 || Math.abs(tmp.z)>0.5) {
				  vertexL.put(0);
				  vertexL.put(0);
				  vertexL.put(1);
			  }
			  else if(Math.abs(tmp.x)>0.25 || Math.abs(tmp.z)>0.25) {
				  vertexL.put(1);
				  vertexL.put(0);
				  vertexL.put(0);
			  }
			  else if(y>hauteurEau+2.5f && y<hauteurEau+10f) {
				  vertexL.put(1);
				  vertexL.put(0);
				  vertexL.put(0);
			if(island.biome==Biome.biomeGrass)	  ombre = Math.min(0.85f, ombre);
				  
			  }
			  else {
				  vertexL.put(0);
				  vertexL.put(1f);
				  vertexL.put(0);
			  }

			  vertexL.put(ombre);
			  
			  
			  
			  }
		  }


		int k=0;
		for(int i=0;i<wc-1;i++) {
			  for(int j=0;j<wc-1;j++) {
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
					 indicesL.put((short) (i+1+(j+1)*wc));
					 indicesL.put((short) (i+1+j*wc));
					 indicesL.put((short) (i+(j+1)*wc));
					 k+=3;
			  }
		  }

		
		
		UniMesh mesh = new UniMesh(true, true, nbVertexL, nbTrianglesL*3, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0), VertexAttribute.ColorUnpacked()));
		mesh.setVertices(vertexL, 0, nbVertexL*SOL_VERTICE_SIZE);
		mesh.setIndices(indicesL, 0, nbTrianglesL*3);
		
			
		return mesh;
	}

}
