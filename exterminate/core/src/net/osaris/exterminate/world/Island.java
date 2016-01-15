
// Source File Name:   HeightMap.java

package net.osaris.exterminate.world;

import net.osaris.exterminate.world.biomes.Biome;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.UniMesh;
import com.badlogic.gdx.math.Vector3;

// Referenced classes of package net.osaris.landscape:
//            LandscapeLeaf, World

public final class Island {
	public Biome biome;

	public int centerX = 0;
	public int centerZ = 0;

	// en coordonnée ile : -1, 0, 1, ...
	public int x = 0;
	public int z = 0;

	public final static int RADIUS = 3;
	protected final int SEED = 355;// +657432;//+40213;//+13

	boolean mustBuildModel = true;
	public boolean mustRebuildIndex = true;
	public UniMesh modelEau;
	public UniMesh modelSol;

	int rebuildin = 0;

	public Island(float sea_level_meters, float world[][]) {
		this.world = world;

	}

	public static final int GRID_UNITS_PER_PATCH = 16;

	private static final Vector3 vec1 = new Vector3();
	private static final Vector3 vec2 = new Vector3();
	private static final Vector3 vec3 = new Vector3();
	public final float world[][];

	private static final LDMapBuilder ldMap = new LDMapBuilder();

	public void export() {
		float world[][] = this.world;

		Pixmap pixmap = new Pixmap(world.length, world.length, Format.RGBA8888);
		float maxh = 0;

		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				maxh = Math.max(maxh, world[i][j]);
			}
		}

		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				pixmap.setColor(world[i][j] / maxh, world[i][j] / maxh,
						world[i][j] / maxh, 1);
				if (world[i][j] < IslandManager.hauteurEau) {
					pixmap.setColor(world[i][j] / maxh, world[i][j] / maxh,
							0.8f, 1);

				}
				pixmap.drawPixel(i, j);
			}
		}
		FileHandle f = new FileHandle("map.png");
		PixmapIO.writePNG(f, pixmap);
		System.out.println("map exported. " + f.path());
	}

}
