package net.osaris.exterminate.world;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import net.osaris.exterminate.world.biomes.Biome;

import org.lwjgl.BufferUtils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.UniMesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

public class ChunkBuilder {

	public static final boolean PACKED_NORMAL = true;

	public Chunk c;
	public boolean isValid = false;
	public int nbTrianglesL = 0;
	public int nbVertexL = 0;
	public float posX;
	public float posZ;
	public float[] heightmap = new float[(GRIDSIZE + 1) * (GRIDSIZE + 1)];
	public float[] sunVisibleAt = new float[GRIDSIZE * GRIDSIZE];
	public boolean hasEau = false;

	public int VERTICE_SIZE = PACKED_NORMAL ? 6 : 8;
	final static public int TEXTUREOFFSET = PACKED_NORMAL ? 4 : 6;
	final static public int OFFSET_SKIN = PACKED_NORMAL ? 6 : 8;

	public final static int SOL_VERTICE_SIZE = 7;
	public final static int GRIDSIZE = 16;
	public final static int GRIDSIZE4 = GRIDSIZE * 4;
	public final static int NBGRIDH = 32;
	public final static int HMAX = NBGRIDH * GRIDSIZE;
	public final static float SIZE = GRIDSIZE;

	public final Random rand = new Random();
	protected static final boolean debug = false;

	// TODO idée d'optim : utiliser unsafeDirectBuffer et squeezer le buffer de
	// UniVBO qui sert à rien
	protected final FloatBuffer vertex = BufferUtils
			.createFloatBuffer(524288 * VERTICE_SIZE);
	protected final IntBuffer indices = BufferUtils.createIntBuffer(524288 * 3);

	protected final FloatBuffer vertexL = BufferUtils
			.createFloatBuffer(65536 * SOL_VERTICE_SIZE);
	protected final ShortBuffer indicesL = BufferUtils
			.createShortBuffer(65536 * 3);
	protected final IntBuffer indicesBigSol = BufferUtils
			.createIntBuffer(GRIDSIZE * GRIDSIZE * 2 * 3);

	protected Vector3 tmp1 = new Vector3();
	protected Vector3 tmp2 = new Vector3();
	protected Vector3 courant = new Vector3();
	protected Vector3 repY = new Vector3();
	protected Vector3 repX = new Vector3(1, 0, 0);
	protected Vector2 tex = new Vector2();
	protected Vector3 tmpn = new Vector3();

	public final static float d = 0.70710678118f;
	public final static float t = 0.57734998051f;

	protected final Vector3 tmp = new Vector3();
	protected final Vector3 tmp4 = new Vector3();
	protected final Vector3 tmp5 = new Vector3();
	protected final Vector3 tmp6 = new Vector3();

	public ChunkBuilder() {

	}

	public void buildCells(Chunk chunk) {
		c = chunk;

		posX = c.posX;
		posZ = c.posZ;
		hasEau = chunk.hasEau;
		// c.enCreation = true;
		c.modelDemande = false;

		heightmap = c.heightmap;
		sunVisibleAt = c.sunVisibleAt;
		try {
			buildCells();

			c.enCreation = false;
			c.isVoronoiser = true;
		} catch (Exception e) {
			e.printStackTrace();
			c.isGenerer = false;
			c.enCreation = false;
			c.modelDemande = false;

		}
	}

	protected void buildCells() {

	}

	Vector3 tc2 = new Vector3();
	Vector3 tc3 = new Vector3();
	Vector3 tc4 = new Vector3();
	Vector3 tc5 = new Vector3();

	protected Vector3 n = new Vector3();

	public static float packFloat(float tx, float ty) {
		return Float.intBitsToFloat((fromFloat(ty) & 0xFFFF) << 16
				| fromFloat(tx));
	}

	// returns all higher 16 bits as 0 for all results
	public static int fromFloat(float fval) {
		int fbits = Float.floatToIntBits(fval);
		int sign = fbits >>> 16 & 0x8000; // sign only
		int val = (fbits & 0x7fffffff) + 0x1000; // rounded value

		if (val >= 0x47800000) // might be or become NaN/Inf
		{ // avoid Inf due to rounding
			if ((fbits & 0x7fffffff) >= 0x47800000) { // is or must become
														// NaN/Inf
				if (val < 0x7f800000) // was value but too large
					return sign | 0x7c00; // make it +/-Inf
				return sign | 0x7c00 | // remains +/-Inf or NaN
						(fbits & 0x007fffff) >>> 13; // keep NaN (and Inf) bits
			}
			return sign | 0x7bff; // unrounded not quite Inf
		}
		if (val >= 0x38800000) // remains normalized value
			return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
		if (val < 0x33000000) // too small for subnormal
			return sign; // becomes +/-0
		val = (fbits & 0x7fffffff) >>> 23; // tmp exp for subnormal calc
		return sign | ((fbits & 0x7fffff | 0x800000) // add subnormal bit
				+ (0x800000 >>> val - 102) // round depending on cut off
		>>> 126 - val); // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
	}

	public static float toFloatBits(float r, float g, float b, float a) {
		int color = ((int) (255 * a) << 24) | ((int) (255 * b) << 16)
				| ((int) (255 * g) << 8) | ((int) (255 * r));
		return Float.intBitsToFloat(color);
	}

	public static float toFloatBits(int r, int g, int b, int a) {
		int color = (a << 24) | (b << 16) | (g << 8) | r;
		return Float.intBitsToFloat(color);
	}

	/*
	 * public static float packNormal(Vector3 n) { n.nor(); return
	 * (float)(Math.floor((n.x+0.5) * 127.95) + Math.floor((n.y+0.5) * 127.95) *
	 * 256.0 + Math.floor((n.z+0.5) * 127.95) * 256.0 * 256.0); }
	 * 
	 * 
	 * public static float packTexture(float nx, float ny, float nz) { return
	 * (float)(Math.floor(nx * 255.9) + Math.floor(ny * 255.9) * 256.0 +
	 * Math.floor(nz) * 256.0 * 256.0); }
	 * 
	 * public static float packNumbers(float nx, float ny, float nz) { return
	 * (float)(Math.floor(nx * 255.9) + Math.floor(ny * 255.9) * 256.0 +
	 * Math.floor(nz * 255.9) * 256.0 * 256.0); }
	 */

	public static float packNormal(Vector3 n) {
		n.nor();

		byte b1 = 0;
		byte b2 = (byte) ((n.z) * 127f);
		byte b3 = (byte) ((n.y) * 127f);
		byte b4 = (byte) ((n.x) * 127f);

		return NumberUtils.intToFloatColor(((0xFF & b1) << 24)
				| ((0xFF & b2) << 16) | ((0xFF & b3) << 8) | (0xFF & b4));
	}

	public boolean onchunk = true;

	protected boolean monstering = false;

	public void rebuildModel(Chunk chunk) {
		// System.out.println("m"+c.x+" "+c.z);

		if (chunk != c) {
			System.out
					.println("On essais de build le model du mauvais chunk !");
			return;
		}

		if (c.disposed) {
			return;// disposed...
		}

		Mesh ancienMeshSol = c.modelSol;

		c.modelSol = buildModelSol();

		if (ancienMeshSol != null) {
			ancienMeshSol.dispose();
		}
		c.changed = true;
	}

	public void buildModel(Chunk chunk) {
		if (chunk != c) {
			System.out
					.println("On essais de build le model du mauvais chunk !");
			return;
		}

		if (c.disposed) {
			return;// disposed...
		}

		c.modelDemande = true;

		// System.out.println("build "+c.x +" "+
		// c.z+" "+c.disposed+" "+chunk.disposed);

		c.isValid = false;
		c.enCreation = true;
		c.modelDemande = true;
		if (c.modelEau != null) {
			c.modelEau.dispose();
		}
		if (c.modelSol != null) {
			c.modelSol.dispose();
		}
		c.modelEau = buildModelEau();

		c.modelSol = buildModelSol();
		c.hasEau = hasEau;

		c.modelDemande = false;
		c.enCreation = false;
		c.isValid = true;

		// allez on va prendre depuis les chunks voisins histoire de rattraper
		// notre retard en entité
		c.loadEntitiesFrom(-1, -1);
		c.loadEntitiesFrom(-1, 1);
		c.loadEntitiesFrom(1, -1);
		c.loadEntitiesFrom(1, 1);
		c.loadEntitiesFrom(-1, 0);
		c.loadEntitiesFrom(0, -1);
		c.loadEntitiesFrom(1, 0);
		c.loadEntitiesFrom(0, 1);

		mustDoGCIn--;
		if (mustDoGCIn <= 0) {
			// System.gc();
			mustDoGCIn = 50;
		}

		/*
		 * mustRebuildLDIn--; if(mustRebuildLDIn<=0 && Island.getIslandAt(posX,
		 * posZ)!=null) { Island.getIslandAt(posX, posZ).mustRebuildIndex =
		 * true; mustRebuildLDIn = 40; }
		 */

	}

	int mustDoGCIn = 50;
	int mustRebuildLDIn = 40;
	int nbChunk = 0;

	public Mesh buildModelEau() {

		makeModelEau();
		if (true)
			return null;

		float hauteurEau = IslandManager.hauteurEau();
		hasEau = false;

		int wc = GRIDSIZE + 1;
		nbVertexL = (GRIDSIZE + 1) * (GRIDSIZE + 1);
		nbTrianglesL = GRIDSIZE * GRIDSIZE * 2;

		vertexL.clear();

		for (int j = 0; j < GRIDSIZE + 1; j++) {
			for (int i = 0; i < GRIDSIZE + 1; i++) {

				vertexL.put(((i)) + posX);
				vertexL.put(hauteurEau);
				vertexL.put(((j)) + posZ);

				vertexL.put(0);
				vertexL.put(1);
				vertexL.put(0);

				vertexL.put(((i + posX)) / 16f / 4f);
				vertexL.put(((j + posZ)) / 16f / 4f);

				vertexL.put((float) Math.exp(-Math.max(0f, hauteurEau
						- heightmap[i * Chunk.GRIDSIZE1 + j]) * 0.1f));
				vertexL.put(0);
				vertexL.put(0);
				vertexL.put(1);

			}
		}

		indicesL.clear();

		int k = 0;
		for (int i = 0; i < wc - 1; i++) {
			for (int j = 0; j < wc - 1; j++) {
				if (heightmap[i * Chunk.GRIDSIZE1 + j] < hauteurEau
						|| heightmap[(i + 1) * Chunk.GRIDSIZE1 + j] < hauteurEau
						|| heightmap[i * Chunk.GRIDSIZE1 + j + 1] < hauteurEau
						|| heightmap[(i + 1) * Chunk.GRIDSIZE1 + j + 1] < hauteurEau) {

					indicesL.put((short) (i + 1 + j * wc));
					indicesL.put((short) (i + j * wc));
					indicesL.put((short) (i + (j + 1) * wc));
					k += 3;
					indicesL.put((short) (i + 1 + (j + 1) * wc));
					indicesL.put((short) (i + 1 + j * wc));
					indicesL.put((short) (i + (j + 1) * wc));
					k += 3;
					hasEau = true;
				}
			}
		}

		nbTrianglesL = k / 3;

		if (hasEau) {
			UniMesh mesh = new UniMesh(true, true, nbVertexL, nbTrianglesL * 3,
					new VertexAttributes(VertexAttribute.Position(),
							VertexAttribute.NormalPacked(),
							VertexAttribute.TexCoordsPacked(0),
							VertexAttribute.ColorPacked()));
			mesh.setVertices(vertexL, 0, nbVertexL * SOL_VERTICE_SIZE);
			mesh.setIndices(indicesL, 0, nbTrianglesL * 3);

			return mesh;

		} else {
			return null;
		}

	}

	public void makeModelEau() {
		float hauteurEau = IslandManager.hauteurEau();
		hasEau = false;

		int wc = GRIDSIZE + 1;
		nbVertexL = (GRIDSIZE + 1) * (GRIDSIZE + 1);
		nbTrianglesL = GRIDSIZE * GRIDSIZE * 2;

		vertexL.clear();

		for (int j = 0; j < GRIDSIZE + 1; j++) {
			for (int i = 0; i < GRIDSIZE + 1; i++) {

				vertexL.put(((i)) + posX);
				vertexL.put(hauteurEau);
				vertexL.put(((j)) + posZ);

				vertexL.put(packNormal(Vector3.Y));

				vertexL.put(((i + posX)) / 16f / 4f);
				vertexL.put(((j + posZ)) / 16f / 4f);

				vertexL.put(toFloatBits(
						(float) Math.exp(-Math.max(0f, hauteurEau
								- heightmap[i * Chunk.GRIDSIZE1 + j]) * 0.1f),
						0f, 0f, 1f));

			}
		}

		indicesBigSol.clear();

		int offsetIndices = 0;
		int offset = 0;

		int k = 0;
		for (int i = 0; i < wc - 1; i++) {
			for (int j = 0; j < wc - 1; j++) {
				if (heightmap[i * Chunk.GRIDSIZE1 + j] < hauteurEau
						|| heightmap[(i + 1) * Chunk.GRIDSIZE1 + j] < hauteurEau
						|| heightmap[i * Chunk.GRIDSIZE1 + j + 1] < hauteurEau
						|| heightmap[(i + 1) * Chunk.GRIDSIZE1 + j + 1] < hauteurEau) {

					if (c.waterMeshId == -1) {
						c.waterMeshId = Chunk.water.getMeshId(c);

						offsetIndices = GRIDSIZE * GRIDSIZE * 2 * 3
								* c.waterMeshId;
						offset = (GRIDSIZE + 1) * (GRIDSIZE + 1)
								* c.waterMeshId;
					}

					indicesBigSol.put(offset + (i + 1 + j * wc));
					indicesBigSol.put(offset + (i + j * wc));
					indicesBigSol.put(offset + (i + (j + 1) * wc));
					k += 3;
					indicesBigSol.put(offset + (i + 1 + (j + 1) * wc));
					indicesBigSol.put(offset + (i + 1 + j * wc));
					indicesBigSol.put(offset + (i + (j + 1) * wc));
					k += 3;
					hasEau = true;
				}
			}
		}

		nbTrianglesL = k / 3;

		if (hasEau) {
			Chunk.water.mesh.updateVertices(vertexL, offset * SOL_VERTICE_SIZE,
					nbVertexL * SOL_VERTICE_SIZE);
			Chunk.water.mesh.updateIndices(indicesBigSol, offsetIndices,
					nbTrianglesL * 3);
		}

	}

	public Mesh buildModelSol() {

		// return getSolMesh();
		makeSolMesh();
		return null;

	}

	// pour texturage en strechY
	private final float[] cumulDistX = new float[GRIDSIZE + 1];
	private final float[] cumulDistY = new float[GRIDSIZE + 1];

	protected Mesh getSolMesh() {
		float y;
		float hauteurEau = IslandManager.hauteurEau();
		int wc = GRIDSIZE + 1;
		nbVertexL = (GRIDSIZE + 1) * (GRIDSIZE + 1);
		nbTrianglesL = GRIDSIZE * GRIDSIZE * 2;

		for (int i = 0; i < GRIDSIZE + 1; i++) {
			cumulDistX[i] = 0;
			cumulDistY[i] = 0;
		}
		float diffY;

		vertexL.clear();

		for (int j = 0; j < GRIDSIZE + 1; j++) {

			for (int i = 0; i < GRIDSIZE + 1; i++) {

				vertexL.put((i) + posX + 0.5f);

				y = heightmap[i * Chunk.GRIDSIZE1 + j];

				vertexL.put(y);

				vertexL.put((j) + posZ + 0.5f);

				// 2v = (f(xâˆ’1,y) âˆ’ f(x+1,y), f(x,yâˆ’1) âˆ’ f(x,y+1), 2)

				vertexL.put(c.normalmap[(i * (GRIDSIZE + 1) + j) * 3]);
				vertexL.put(c.normalmap[(i * (GRIDSIZE + 1) + j) * 3 + 1]);
				vertexL.put(c.normalmap[(i * (GRIDSIZE + 1) + j) * 3 + 2]);
				/*
				 * } else { vertexL[(i+j*wc)*SOL_VERTICE_SIZE+6-3]=0;
				 * vertexL[(i+j*wc)*SOL_VERTICE_SIZE+7-3]=1;
				 * vertexL[(i+j*wc)*SOL_VERTICE_SIZE+8-3]=0; }
				 */

				// si un jour t'es motivé
				// on texturera selon la base de 1 unit MAIS en ponderant selon
				// la répartition des grosse diff.
				// PAR REXEMPLE : imagine la moitié de la diffY est concentrée
				// sur une case: et ben on concentre la moitié de la div texture
				// sur cette case. Walla.
				// là on fait ça à l'arrache et on ajustera selon la sommeDiffY
				// dans la boucle suivante (vu que là on l'a pas encore)

				vertexL.put(cumulDistX[j]);
				vertexL.put(cumulDistY[i]);

				if (i < GRIDSIZE) {
					diffY = Math.abs(y
							- heightmap[(i + 1) * Chunk.GRIDSIZE1 + j]);
					cumulDistX[j] += Math.sqrt(1f + diffY * diffY);
				}

				if (j < GRIDSIZE) {
					diffY = Math
							.abs(y - heightmap[i * Chunk.GRIDSIZE1 + j + 1]);
					cumulDistY[i] += Math.sqrt(1f + diffY * diffY);
				}

				// vertexL[(i+j*wc)*SOL_VERTICE_SIZE+9-3]=i/8f;
				// vertexL[(i+j*wc)*SOL_VERTICE_SIZE+10-3]=j/8f;

				float ombre = 1f;
				if (y <= 1f
						|| (y >= sunVisibleAt(i, j)
								&& y >= sunVisibleAt(i - 1, j)
								&& y >= sunVisibleAt(i, j - 1) && y >= sunVisibleAt(
								i - 1, j - 1))) {
					ombre = random[((i % GRIDSIZE4 + GRIDSIZE4) % GRIDSIZE4)
							* (GRIDSIZE4)
							+ ((j % GRIDSIZE4 + GRIDSIZE4) % GRIDSIZE4)];// -Math.random()*0.1f
				} else {
					ombre = c.biome.ombrage;
				}

				if (y < hauteurEau - 2) {
					ombre = Math.min(
							ombre,
							1.0f - Math.min(c.biome.coefOmbrageEau, (hauteurEau
									- y - 2) / 30f));
				}
				ombre = Math.max(0, ombre);

				// x = grass
				// pour y, c'est Dirt selon l'altitude.
				// z = rock
				// et x+y+z=0 donne du sable.

				byte mat = c.matmap[i * (GRIDSIZE + 1) + j];
				if (mat == Chunk.MAT_SOUSLEAU) {
					vertexL.put(0f);
					vertexL.put(0f);
					vertexL.put(0.5f);
				} else if (mat == Chunk.MAT_SABLE) {
					vertexL.put(0f);
					vertexL.put(0f);
					vertexL.put(0f);
				} else if (mat == Chunk.MAT_DIRT) {
					vertexL.put(0f);
					vertexL.put(1f);
					vertexL.put(0f);
				} else if (mat == Chunk.MAT_PENTE
						&& c.biome == Biome.biomeGrass) {
					vertexL.put(0);
					vertexL.put(0);
					vertexL.put(1f);
					ombre -= 0.1f;
				} else if (mat == Chunk.MAT_PENTE) {
					vertexL.put(0.8f);
					vertexL.put(0.f);
					vertexL.put(0.2f);
					ombre -= 0.1f;
				} else if (mat == Chunk.MAT_ROCK) {
					vertexL.put(0);
					vertexL.put(0);
					vertexL.put(1);
				} else if (mat == Chunk.MAT_GRASS) {
					vertexL.put(1);
					vertexL.put(0);
					vertexL.put(0);
				} else if (mat == Chunk.MAT_GRASS_PLAT) {
					vertexL.put(1);
					vertexL.put(0);
					vertexL.put(0);
					if (c.biome == Biome.biomeGrass)
						ombre = Math.min(0.82f, ombre);

				} else {
					vertexL.put(0);
					vertexL.put(1f);
					vertexL.put(0);
				}

				vertexL.put(ombre);

			}
		}

		for (int j = 0; j < GRIDSIZE + 1; j++) {
			for (int i = 0; i < GRIDSIZE + 1; i++) {

				vertexL.position((i + j * wc) * SOL_VERTICE_SIZE + 9 - 3);
				float c1 = vertexL.get();
				float c2 = vertexL.get();

				vertexL.position((i + j * wc) * SOL_VERTICE_SIZE + 9 - 3);
				vertexL.put(c1 / (cumulDistX[j] * (0.5f)));
				vertexL.put(c2 / (cumulDistY[i] * (0.5f)));
			}
		}

		indicesL.clear();
		for (int i = 0; i < wc - 1; i++) {
			for (int j = 0; j < wc - 1; j++) {
				if (c.hole[i * Chunk.GRIDSIZE + j] == 0) {
					indicesL.put((short) (i + 1 + j * wc));
					indicesL.put((short) (i + j * wc));
					indicesL.put((short) (i + (j + 1) * wc));
					indicesL.put((short) (i + 1 + (j + 1) * wc));
					indicesL.put((short) (i + 1 + j * wc));
					indicesL.put((short) (i + (j + 1) * wc));
				}
			}
		}

		UniMesh mesh = new UniMesh(true, true, nbVertexL, nbTrianglesL * 3,
				new VertexAttributes(VertexAttribute.Position(),
						VertexAttribute.NormalPacked(),
						VertexAttribute.TexCoordsPacked(0),
						VertexAttribute.ColorPacked()));
		mesh.setVertices(vertexL, 0, nbVertexL * SOL_VERTICE_SIZE);
		mesh.setIndices(indicesL, 0, nbTrianglesL * 3);

		return mesh;
	}

	protected void makeSolMesh() {
		float y;
		float hauteurEau = IslandManager.hauteurEau();
		int wc = GRIDSIZE + 1;
		nbVertexL = (GRIDSIZE + 1) * (GRIDSIZE + 1);
		nbTrianglesL = GRIDSIZE * GRIDSIZE * 2;

		for (int i = 0; i < GRIDSIZE + 1; i++) {
			cumulDistX[i] = 0;
			cumulDistY[i] = 0;
		}
		float diffY;

		vertexL.clear();

		for (int j = 0; j < GRIDSIZE + 1; j++) {

			for (int i = 0; i < GRIDSIZE + 1; i++) {

				vertexL.put((i) + posX + 0.5f);

				y = heightmap[i * Chunk.GRIDSIZE1 + j];

				vertexL.put(y);

				vertexL.put((j) + posZ + 0.5f);

				// 2v = (f(xâˆ’1,y) âˆ’ f(x+1,y), f(x,yâˆ’1) âˆ’ f(x,y+1), 2)

				n.set(c.normalmap[(i * (GRIDSIZE + 1) + j) * 3], c.normalmap[(i
						* (GRIDSIZE + 1) + j) * 3 + 1], c.normalmap[(i
						* (GRIDSIZE + 1) + j) * 3 + 2]);
				/*
				 * vertexL.put(c.normalmap[(i*(GRIDSIZE+1)+j)*3]);
				 * vertexL.put(c.normalmap[(i*(GRIDSIZE+1)+j)*3+1]);
				 * vertexL.put(c.normalmap[(i*(GRIDSIZE+1)+j)*3+2]);
				 */
				vertexL.put(packNormal(n));
				/*
				 * } else { vertexL[(i+j*wc)*SOL_VERTICE_SIZE+6-3]=0;
				 * vertexL[(i+j*wc)*SOL_VERTICE_SIZE+7-3]=1;
				 * vertexL[(i+j*wc)*SOL_VERTICE_SIZE+8-3]=0; }
				 */

				// si un jour t'es motivé
				// on texturera selon la base de 1 unit MAIS en ponderant selon
				// la répartition des grosse diff.
				// PAR REXEMPLE : imagine la moitié de la diffY est concentrée
				// sur une case: et ben on concentre la moitié de la div texture
				// sur cette case. Walla.
				// là on fait ça à l'arrache et on ajustera selon la sommeDiffY
				// dans la boucle suivante (vu que là on l'a pas encore)

				vertexL.put(cumulDistX[j]);
				vertexL.put(cumulDistY[i]);

				if (i < GRIDSIZE) {
					diffY = Math.abs(y
							- heightmap[(i + 1) * Chunk.GRIDSIZE1 + j]);
					cumulDistX[j] += Math.sqrt(1f + diffY * diffY);
				}

				if (j < GRIDSIZE) {
					diffY = Math
							.abs(y - heightmap[i * Chunk.GRIDSIZE1 + j + 1]);
					cumulDistY[i] += Math.sqrt(1f + diffY * diffY);
				}

				// vertexL[(i+j*wc)*SOL_VERTICE_SIZE+9-3]=i/8f;
				// vertexL[(i+j*wc)*SOL_VERTICE_SIZE+10-3]=j/8f;

				float ombre = 1f;
				if (y <= 1f
						|| (y >= sunVisibleAt(i, j)
								&& y >= sunVisibleAt(i - 1, j)
								&& y >= sunVisibleAt(i, j - 1) && y >= sunVisibleAt(
								i - 1, j - 1))) {
					ombre = random[((i % GRIDSIZE4 + GRIDSIZE4) % GRIDSIZE4)
							* (GRIDSIZE4)
							+ ((j % GRIDSIZE4 + GRIDSIZE4) % GRIDSIZE4)];// -Math.random()*0.1f
				} else {
					ombre = c.biome.ombrage;
				}

				if (y < hauteurEau - 2) {
					ombre = Math.min(
							ombre,
							1.0f - Math.min(c.biome.coefOmbrageEau, (hauteurEau
									- y - 2) / 30f));
				}
				ombre = Math.max(0, ombre);
				ombre = Math.min(1f, ombre);

				// x = grass
				// pour y, c'est Dirt selon l'altitude.
				// z = rock
				// et x+y+z=0 donne du sable.

				byte mat = c.matmap[i * (GRIDSIZE + 1) + j];
				if (mat == Chunk.MAT_SOUSLEAU) {
					vertexL.put(toFloatBits(0f, 0f, 0.5f, ombre));
				} else if (mat == Chunk.MAT_SABLE) {
					vertexL.put(toFloatBits(0f, 0f, 0f, ombre));
				} else if (mat == Chunk.MAT_DIRT) {
					vertexL.put(toFloatBits(0f, 1f, 0f, ombre));
				} else if (mat == Chunk.MAT_PENTE
						&& c.biome == Biome.biomeGrass) {
					ombre -= 0.1f;
					vertexL.put(toFloatBits(0f, 0f, 1f, ombre));
				} else if (mat == Chunk.MAT_PENTE) {
					ombre -= 0.1f;
					vertexL.put(toFloatBits(0.8f, 0f, 0.2f, ombre));
				} else if (mat == Chunk.MAT_ROCK) {
					vertexL.put(toFloatBits(0f, 0f, 1f, ombre));
				} else if (mat == Chunk.MAT_GRASS) {
					vertexL.put(toFloatBits(1f, 0f, 0f, ombre));
				} else if (mat == Chunk.MAT_GRASS_PLAT) {
					if (c.biome == Biome.biomeGrass)
						ombre = Math.min(0.82f, ombre);
					vertexL.put(toFloatBits(1f, 0f, 0f, ombre));

				} else {
					vertexL.put(toFloatBits(0f, 1f, 0f, ombre));
				}

				// vertexL.put(ombre);

			}
		}

		for (int j = 0; j < GRIDSIZE + 1; j++) {
			for (int i = 0; i < GRIDSIZE + 1; i++) {

				vertexL.position((i + j * wc) * SOL_VERTICE_SIZE + 7 - 3);
				float c1 = vertexL.get();
				float c2 = vertexL.get();

				vertexL.position((i + j * wc) * SOL_VERTICE_SIZE + 7 - 3);
				vertexL.put(c1 / (cumulDistX[j] * (0.5f)));
				vertexL.put(c2 / (cumulDistY[i] * (0.5f)));
			}
		}

		indicesBigSol.clear();

		if (c.solMeshId == -1) {
			c.solMeshId = Chunk.ground.getMeshId(c);
		}

		int offsetIndices = GRIDSIZE * GRIDSIZE * 2 * 3 * c.solMeshId;
		int offset = (GRIDSIZE + 1) * (GRIDSIZE + 1) * c.solMeshId;

		int i = 0, j = 0;
		for (i = 0; i < wc - 1; i++) {
			for (j = 0; j < wc - 1; j++) {
				// if(c.hole[i*Chunk.GRIDSIZE+j]==0) {
				indicesBigSol.put(offset + (i + 1 + j * wc));
				indicesBigSol.put(offset + (i + j * wc));
				indicesBigSol.put(offset + (i + (j + 1) * wc));
				indicesBigSol.put(offset + (i + 1 + (j + 1) * wc));
				indicesBigSol.put(offset + (i + 1 + j * wc));
				indicesBigSol.put(offset + (i + (j + 1) * wc));
			}
		}

		Chunk.ground.mesh.updateVertices(vertexL, offset * SOL_VERTICE_SIZE,
				nbVertexL * SOL_VERTICE_SIZE);
		Chunk.ground.mesh.updateIndices(indicesBigSol, offsetIndices,
				nbTrianglesL * 3);

	}

	protected float sunVisibleAt(int x, int z) {
		if (x < 0 || x >= GRIDSIZE || z < 0 || z >= GRIDSIZE) {

			return 0f;
		}
		return sunVisibleAt[x * Chunk.GRIDSIZE + z];
	}

	public static float[] random = new float[(GRIDSIZE4) * (GRIDSIZE4)];

	public static void init() {
		for (int i = 0; i < GRIDSIZE4; i++) {
			for (int j = 0; j < GRIDSIZE4; j++) {
				random[i + (GRIDSIZE4) * j] = (float) Math.random() * 0.1f + 0.95f;
			}
		}
	}

	public void dispatch(int i, int h, int j) {

	}

}
