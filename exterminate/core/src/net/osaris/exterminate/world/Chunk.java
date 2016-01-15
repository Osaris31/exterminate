package net.osaris.exterminate.world;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.osaris.exterminate.entities.Entity;
import net.osaris.exterminate.entities.LivingEntity;
import net.osaris.exterminate.entities.Monster;
import net.osaris.exterminate.entities.Shark;
import net.osaris.exterminate.world.biomes.Biome;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Chunk implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6926285133876235545L;

	public static final int NBB = 2;
	public int solMeshId = -1;
	public int waterMeshId = -1;

	public transient Biome biome;

	public boolean hasEau = false;

	public boolean isValid = false;
	public boolean isGenerer = false;
	public boolean isVoronoiser = false;
	public boolean enCreation = false;
	public boolean modelDemande = false;

	// final static Cell EMPTYCELL = new Cell(0.5f, 0.5f, 0.5f);

	final static int SOL_VERTICE_SIZE = 12;
	public final static int GRIDSIZE = 16;
	public final static int GRIDSIZE1 = 17;
	public final static int NBGRIDH = 32;
	public final static int HMAX = NBGRIDH * GRIDSIZE;

	public final static float SIZE = GRIDSIZE;
	// amplitude maximale de rand
	public final static float MAXRAND = 0.5f; // 0.7 marche !
	public final static Random rand = new Random();
	public static final byte MAT_SOUSLEAU = 0;
	public static final byte MAT_SABLE = 1;
	public static final byte MAT_DIRT = 2;
	public static final byte MAT_PENTE = 3;
	public static final byte MAT_ROCK = 4;
	public static final byte MAT_GRASS = 5;
	public static final byte MAT_GRASS_PLAT = 6;

	public static final byte VERSION = 3;
	public static final byte VERSION_ENTITY = 2;

	public int nbTrianglesL = 0;
	public int nbVertexL = 0;

	public int x = -11111111;
	public int z = -11111111;
	public float posX;
	public float posZ;
	public transient Mesh modelEau;
	public transient Mesh modelSol;

	static ByteBuffer buf_heightmap = ByteBuffer.allocate((GRIDSIZE + 1)
			* (GRIDSIZE + 1) * 4);
	static ByteBuffer buf_normalmap = ByteBuffer.allocate((GRIDSIZE + 1)
			* (GRIDSIZE + 1) * 3 * 4);
	static ByteBuffer buf_sunVisibleAt = ByteBuffer.allocate((GRIDSIZE)
			* (GRIDSIZE) * 4);
	static ByteBuffer buf_hole = ByteBuffer.allocate((GRIDSIZE) * (GRIDSIZE));
	static ByteBuffer buf_matmap = ByteBuffer.allocate((GRIDSIZE + 1)
			* (GRIDSIZE + 1));

	static byte[] byte_heightmap = new byte[(GRIDSIZE + 1) * (GRIDSIZE + 1) * 4];
	static byte[] byte_normalmap = new byte[(GRIDSIZE + 1) * (GRIDSIZE + 1) * 3
			* 4];
	static byte[] byte_sunVisibleAt = new byte[(GRIDSIZE) * (GRIDSIZE) * 4];

	public Set<Entity> entities = Collections
			.synchronizedSet(new HashSet<Entity>());
	public Set<LivingEntity> ennemisProches = Collections
			.synchronizedSet(new HashSet<LivingEntity>());
	private Set<LivingEntity> ennemisToRemove = Collections
			.synchronizedSet(new HashSet<LivingEntity>());

	private Vector2 tmpV2a = new Vector2();
	private Vector2 tmpV2b = new Vector2();

	public void removeFarEnemies() {
		ennemisToRemove.clear();
		for (LivingEntity entity : ennemisProches) {
			tmpV2a.set(entity.pos.x, entity.pos.z);
			tmpV2b.set(posX + SIZE * 0.5f, posZ + SIZE * 0.5f);
			if (tmpV2a.dst2(tmpV2b) > 64 * 64) {
				ennemisToRemove.add(entity);
			}
		}
		ennemisProches.removeAll(ennemisToRemove);
	}

	@Override
	public int hashCode() {
		return (x << 8) + z;
	}

	@Override
	public String toString() {
		return x + " ; " + z;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chunk other = (Chunk) obj;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	private void spawnRandomEntities() {
		
		if(Math.random()<0.95) return;
		
		Vector3 where = new Vector3(posX+(float)Math.random()*16f, 10f, posZ+(float)Math.random()*16f);
		where.y = getHauteurInterpole(where.x, where.z);
		LivingEntity entity = null;
		
		if(where.y>IslandManager.hauteurEau()) {
			entity = new Monster();
		}
		else {
			entity = new Shark();
			if(where.y<42f && Math.random()<0.5f) ((Shark)entity).jumping = true;
			where.y = 40f;
		}

		entity.pos.set(where);
		entity.mustTPTo = where.cpy();
		entity.life = 100;
		
		entity.onCreate(0, world);

	}

	public void loadEntitiesFrom(int i, int j) {
		Chunk other = world.getChunk(posX + i * GRIDSIZE, posZ + j * GRIDSIZE); // marche
																				// pas
																				// à
																				// la
																				// création
																				// car
																				// ils
																				// sont
																				// pas
																				// valides.

		if (other != World.EMPTY) {
			for (Entity entity : other.entities) {
				if (entity.pos.dst2(posX + 8f, entity.pos.y, posZ + 8f) < 20f * 20f) {
					entities.add(entity);
				}
			}
			for (LivingEntity entity : other.ennemisProches) {
				if (entity.pos.dst2(posX + 8f, entity.pos.y, posZ + 8f) < 30f * 30f) {
					ennemisProches.add(entity);
				}
			}
		}
	}

	public float[] heightmap = new float[(GRIDSIZE + 1) * (GRIDSIZE + 1)];
	public byte[] hole = new byte[(GRIDSIZE) * (GRIDSIZE)];
	public float[] sunVisibleAt = new float[(GRIDSIZE) * (GRIDSIZE)];
	public float[] normalmap = new float[(GRIDSIZE + 1) * (GRIDSIZE + 1) * 3];
	public byte[] matmap = new byte[(GRIDSIZE + 1) * (GRIDSIZE + 1)];

	public boolean doitEtreRegenerer = false;

	public void generate() {
		rand.setSeed(seed + x * 153 + z * 457);
		hasEau = false;

		for (int i = 0; i < GRIDSIZE; i++) {
			for (int j = 0; j < GRIDSIZE; j++) {
				sunVisibleAt[i * GRIDSIZE + j] = 0;
			}
		}

		posX = x * GRIDSIZE;
		posZ = z * GRIDSIZE;

		Island island = world != null ? world.island.getIslandAt(posX, posZ)
				: null;
		if (island == null) {
			biome = Biome.biomeGrass;
		} else {
			biome = island.biome;
		}
		buildHeighmap(true);

		Random r = new Random();
		r.setSeed(1253 + x * 156 + z * 453);

		biome.generate(this, rand, world);

		spawnRandomEntities();

		isGenerer = true;

	}

	public void buildHeighmap(boolean fromIsland) {

		float hauteurEau = IslandManager.hauteurEau();
		float y = 0;
		for (int i = 0; i < GRIDSIZE + 1; i++) {
			for (int j = 0; j < GRIDSIZE + 1; j++) {
				if (fromIsland) {
					y = world.island.getGenerationHauteur(posX + i, posZ + j);
					heightmap[i * GRIDSIZE1 + j] = y;
					tmp.set(world.island.getGenerationHauteur(posX + i - 1,
							posZ + j)
							- world.island.getGenerationHauteur(posX + i + 1,
									posZ + j),
							2,
							world.island.getGenerationHauteur(posX + i, posZ
									+ j - 1)
									- world.island.getGenerationHauteur(posX
											+ i, posZ + j + 1)).nor();
					normalmap[(i * (GRIDSIZE + 1) + j) * 3] = tmp.x;
					normalmap[(i * (GRIDSIZE + 1) + j) * 3 + 1] = tmp.y;
					normalmap[(i * (GRIDSIZE + 1) + j) * 3 + 2] = tmp.z;

				} else {
					y = heightmap[i * GRIDSIZE1 + j];
				}

				if (y < hauteurEau - 2f) {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_SOUSLEAU;
				} else if (y < hauteurEau + 2f && Math.abs(tmp.x) < 0.25
						&& Math.abs(tmp.z) < 0.25) {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_SABLE;
				} else if (y < hauteurEau + 1f) {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_DIRT;
				} else if (Math.abs(tmp.x) > 0.75 || Math.abs(tmp.z) > 0.75) {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_PENTE;
				} else if (Math.abs(tmp.x) > 0.5 || Math.abs(tmp.z) > 0.5) {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_ROCK;
				} else if (Math.abs(tmp.x) > 0.25 || Math.abs(tmp.z) > 0.25) {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_GRASS;
				} else if (y > hauteurEau + 2.5f && y < hauteurEau + 10f) {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_GRASS_PLAT;
				} else {
					matmap[(i * (GRIDSIZE + 1) + j)] = MAT_DIRT;
				}

			}
		}
	}

	final Vector3 tmp = new Vector3();

	public boolean changed = true;

	public World world;
	int seed;

	public Chunk(int x, int z, int seed, World world) {

		biome = Biome.biomeGrass;
		this.x = x;
		this.z = z;
		this.seed = seed;
		posX = x * GRIDSIZE;
		posZ = z * GRIDSIZE;

		this.world = world;
	}

	static Vector3 tc1 = new Vector3();
	static Vector3 tc2 = new Vector3();

	public boolean regenPourVoisin = false;

	public byte getMatAt(float x, float y, float z) {
		int icellx = Math.min(GRIDSIZE - 1,
				(int) Math.max(0, Math.floor(x - posX)));
		int icellz = Math.min(GRIDSIZE - 1,
				(int) Math.max(0, Math.floor(z - posZ)));

		return matmap[icellx * (GRIDSIZE + 1) + icellz];
	}

	public final static double DEFAULT_RAND = 0.8f;

	private static final byte TRUE = (byte) 1;

	public static void init() {

	}

	public boolean disposed = false;

	public float getHauteurInterpole(float x2, float z2) {
		return getHauteurInterpole(x2, z2, false);
	}

	public float getHauteurBasique(int ax, int az) {
		return Math.min(heightmap[ax * GRIDSIZE1 + az], heightmap[(ax + 1)
				* GRIDSIZE1 + az + 1]);
	}

	public float getHauteurInterpole(float x2, float z2, boolean nohole) {
		// position dans le chunk
		float pX = x2 - this.posX - 0.5f; // a cause du -0.5f de meshsol
		float pZ = z2 - this.posZ - 0.5f;

		if (disposed)
			return 0;

		// allez on va interpoler.
		float difX = (float) (pX - Math.floor(pX));
		float difZ = (float) (pZ - Math.floor(pZ));
		int ax = (int) Math.floor(pX);
		int az = (int) Math.floor(pZ);

		if (ax < 0 || az < 0 || ax >= GRIDSIZE || az >= GRIDSIZE) {
			// System.out.println("x "+x+" "+z+" "+x2+" "+z2+" "+posX+" "+posZ+" ");
			// return world!=null ? world.getHauteurAt(x2, z2, nohole, true) :
			// 0;

			if (ax < 0) {
				ax = Math.max(0, ax);
				difX = 0;
			}
			if (az < 0) {
				az = Math.max(0, az);
				difZ = 0;
			}
			if (ax >= GRIDSIZE) {
				ax = Math.min(GRIDSIZE - 1, ax);
				difX = 1;
			}
			if (az >= GRIDSIZE) {
				az = Math.min(GRIDSIZE - 1, az);
				difZ = 1;
			}
		}

		if (!nohole && hole[ax * GRIDSIZE + az] == TRUE) {
			return 0;
		}

		return ((heightmap[ax * GRIDSIZE1 + az]) * (1f - difX) + (heightmap[(ax + 1)
				* GRIDSIZE1 + az])
				* (difX))
				* (1f - difZ)
				+ ((heightmap[ax * GRIDSIZE1 + az + 1]) * (1f - difX) + (heightmap[(ax + 1)
						* GRIDSIZE1 + az + 1])
						* (difX)) * (difZ);
	}

	public void getNormal(float x2, float z2, Vector3 tmpv2) {
		if (!isValid || x2 - posX < 0 || z2 - posZ < 0 || x2 - posX >= GRIDSIZE
				|| z2 - posZ >= GRIDSIZE) {
			tmpv2.set(0, 1, 0);
			return;
		}

		int pos = ((int) (Math.floor(x2 - posX) * (GRIDSIZE + 1) + Math
				.floor(z2 - posZ))) * 3;

		tmpv2.set(normalmap[pos], normalmap[pos + 1], normalmap[pos + 2]);
	}

	public void save() {

	}

	public static Ground ground;
	public static Ground water;

	public void dispose() {
		disposed = true;
//		System.out.println(solMeshId+" "+x+" "+z);
		if (isValid && this != World.EMPTY) {

			if (entities.size() > 0) {
				for (Entity entity : entities) {
					if (entity.onChunk == this)
						entity.removeMe = true;
				}
			}

			normalmap = null;
			hole = null;
			sunVisibleAt = null;
			matmap = null;

			if (ground != null && ground.forChunk[solMeshId]==this)
				ground.dispose(solMeshId);
			if (water != null && water.forChunk[solMeshId]==this)
				water.dispose(waterMeshId);

			if (hasEau && modelEau != null)
				modelEau.dispose();
			hasEau = false;
			modelEau = null;

			if (modelSol != null)
				modelSol.dispose();
			modelSol = null;

			isValid = false;
		}
	}

	public boolean isPosInChunk(Vector3 pos) {
		return pos.x - posX >= 0 && pos.z - posZ >= 0
				&& pos.x - posX < GRIDSIZE && pos.z - posZ < GRIDSIZE;
	}

	public boolean isPosInChunk(float posx, float posz) {
		return Math.floor(posx - posX) >= 0 && Math.floor(posz - posZ) >= 0
				&& Math.floor(posx - posX) < GRIDSIZE
				&& Math.floor(posz - posZ) < GRIDSIZE;
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
	}

}
