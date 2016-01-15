package net.osaris.exterminate;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import net.osaris.exterminate.entities.EntityManager;
import net.osaris.exterminate.entities.Player;
import net.osaris.exterminate.entities.Shark;
import net.osaris.exterminate.shaders.DefShaderProvider;
import net.osaris.exterminate.shaders.ModelShaderProvider;
import net.osaris.exterminate.world.Chunk;
import net.osaris.exterminate.world.ChunkBuilder;
import net.osaris.exterminate.world.IslandManager;
import net.osaris.exterminate.world.Skybox;
import net.osaris.exterminate.world.World;
import net.osaris.exterminate.world.WorldAround;
import net.osaris.exterminate.world.WorldSolo;
import net.osaris.exterminate.world.biomes.Biome;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.UniMesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.FontBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.particles.MyParticules;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.glutils.DeferredGBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.PostProcessorListener;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Fxaa;
import com.bitfire.postprocessing.effects.Vignette;
import com.bitfire.postprocessing.filters.Blur.BlurType;
import com.bitfire.utils.ShaderLoader;

public class InGame implements Screen, PostProcessorListener {
	public static int SHADOWS = 2;
	// SpriteBatch batch;
	private FrameBuffer fboWaterRefletion = null;
	private FrameBuffer fbogod = null;
	private FrameBuffer fbogodprep = null;
	private static DeferredGBuffer fboDeffered = null;

	public static PerspectiveCamera cam;
	public static OrthographicCamera cam2D;
	public static Vector3 masterTranslate = new Vector3();

	private static CameraController cameraController;
	public static Environment environment;
	public static Environment environment2D;
	private ModelBatch modelBatch;
	private ModelBatch modelEntityBatch;
	private ModelBatch modelEntityDefBatch;

	public static boolean BLOOM = false;
	public static boolean POSTPRO = true;
	public static boolean DEFFERED = true;

	public static Player player;
	public static int frame;

	public static EntityManager entityManager;

	static DirectionalShadowLight shadowLight;
	ModelBatch shadowBatch;
	static Skybox skybox;

	private float currentSunMul = 0f;

	public static final int FBO_SIZE = 256;
	private static final int WATER = 0;
	private static final int GROUND = 1;

	public static int RAYON = ExterminateGame.rendu;

	public static ShaderProgram shaderSun;
	public static ShaderProgram shaderInspect;
	public static ShaderProgram shaderEauBase;
	public static ShaderProgram shaderSousEau;
	public static ShaderProgram shaderFlare;
	public static ShaderProgram groundShader;
	public static ShaderProgram solLowShader;
	public static ShaderProgram depthShader;
	public static ShaderProgram depthShaderStatic;
	public static ShaderProgram shaderDefferedSol;
	public static ShaderProgram shaderGodray;
	public static ShaderProgram shaderGodapply;
	public static ShaderProgram shaderGodprep;
	public static ShaderProgram shaderDefferedMerge;
	public static ShaderProgram shaderEauDef;

	public static Chunk onChunk = null;

	public static BitmapFont font;
	public static BitmapFont font3D;

	public static float fogAmount = 10;

	public static float invDistFog = (1f / 1200f * fogAmount);
	public static float invDistFogEau = (1f / 1600f * fogAmount);

	private static float invDistFogBase = (1f / 1200f);
	private static float invDistFogEauBase = (1f / 1600f);

	public final static Matrix3 invIDTM = new Matrix3()
			.set(new Matrix4().idt()).inv().transpose();
	public final static Matrix3 invtmp = new Matrix3().set(new Matrix4().idt())
			.inv().transpose();

	final static Vector3 solAmbiant = new Vector3(0.4f, 0.4f, 0.4f);
	final static Vector3 solShadow = new Vector3(0.8f, 0.8f, 0.8f);
	public final static Color solFog = new Color(0.53f, 0.55f, 0.59f, 1f);
	final static Vector3 solFogBase = new Vector3(0.705f, 0.776f, 0.855f);
	final static Vector3 solLightColor = new Vector3(0.85f, 0.85f, 0.85f);
	final static Vector3 specLightColor = new Vector3(0.9f, 0.85f, 0.8f);
	final static Vector3 solLightDir = new Vector3(0.f, -30f, -20.3f).nor();
	private static boolean sun = true;
	private static final int NB_CHUNK_MODELISE_PAR_FRAME = 3 + InGame.RAYON / 11;

	public static SpriteBatch batcher;
	public static FontBatch batcherFont;
	public static FontBatch batcherFont3D;
	public static PostProcessor postProcessor;
	private static Bloom bloom;
	private static Vignette vignette;

	public static TextureRegion cursor;
	public static TextureRegion cursor2;

	public static TextureRegion voile;
	public static TextureRegion fondtext;
	public static Texture normalMap;
	public static Texture logo;
	public static Texture normalMap2;
	public static Texture dudvMap;
	public static Texture perlinMap;
	public static TextureRegion toolbarButton;
	public static TextureRegion toolbarButtonS;

	public static TextureRegion target;
	public static TextureRegion healthBar;

	public static Texture loadTexture(String file) {
		return loadTexture(file, true);
	}

	public static Texture loadTexture(String file, boolean mipmaps) {
		Texture texture = new Texture(Gdx.files.internal("data/pics/" + file),
				mipmaps);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return texture;
	}

	@Override
	public void beforeRenderToScreen() {

		// Gdx.gl20.glEnable( GL20.GL_BLEND );
		// Gdx.gl20.glBlendFunc( GL20.GL_SRC_COLOR, GL20.GL_SRC_ALPHA );

	}

	PointLight pointLight = new PointLight();

	public static ShaderProgram shaderContrast;

	public static IslandManager island;

	public void init() {
		if (world != null) {
			world.dispose();
		}
		world = null;
		island = null;
		entityManager = null;
		System.gc();
		world = new WorldSolo();

		island = new IslandManager(world);

		world.loadEntities();
		island.generate();
		world.init();
		world.island = island;
		WorldAround.initBuilder();
		ChunkBuilder.init();
		cameraController = new CameraController(cam, player);
		Gdx.input.setInputProcessor(cameraController);
		Gdx.input.setCursorCatched(true);
	}

	public InGame() {

		try {

			ShaderLoader.BasePath = "shaders/";

			// HeightMap.export();

			// batch = new SpriteBatch();

			shapeRenderer = new ShapeRenderer();

			// font = Menu.skin.getFont("default-font");
			// font.setColor(Color.WHITE);
			font3D = Assets.font;
			font = Assets.font;

			// modelEntityBatch = new CullingModelBatch(new
			// ModelShaderProvider());
			modelEntityBatch = new ModelBatch(new ModelShaderProvider());
			modelEntityDefBatch = new ModelBatch(new DefShaderProvider());
			modelBatch = new ModelBatch(new ModelShaderProvider());
			Texture texture = loadTexture("healthb.png", false);
			cursor = new TextureRegion(texture, 17, 113, 63, 63);
			cursor2 = new TextureRegion(texture, 110, 114, 63, 63);
			voile = new TextureRegion(texture, 213, 3, 14, 14);
			fondtext = new TextureRegion(texture, 0, 68, 256, 23);
			toolbarButton = new TextureRegion(texture, 0, 128, 128, 128);
			toolbarButtonS = new TextureRegion(texture, 128, 128, 128, 128);

			healthBar = new TextureRegion(texture, 0, 0, 112, 96);

			target = new TextureRegion(texture, 134, 5, 84, 84);

			environment2D = new Environment();

			environment2D.set(new ColorAttribute(ColorAttribute.AmbientLight,
					0.4f, 0.4f, 0.4f, 1f));
			environment2D.add((new DirectionalLight()).set(1.0f, 1.0f, 1.0f,
					1f, 1f, -1f));

			environment = new Environment();

			environment.set(new ColorAttribute(ColorAttribute.AmbientLight,
					0.4f, 0.4f, 0.4f, 1f));
			environment.set(new ColorAttribute(ColorAttribute.Fog, solFog));
			if (SHADOWS >= 2 && DEFFERED) {
				ExterminateGame.precisionOmbre = 4;
			} else if (SHADOWS >= 1) {
				ExterminateGame.precisionOmbre = 2;
			} else {
				ExterminateGame.precisionOmbre = 1;
			}
			(shadowLight = new DirectionalShadowLight(
					ExterminateGame.precisionOmbre * 512,
					ExterminateGame.precisionOmbre * 512,
					ExterminateGame.precisionOmbre * 16f,
					ExterminateGame.precisionOmbre * 16f, 1f, 200f)).set(
					solShadow.x, solShadow.y, solShadow.z, solLightDir);
			environment.add(shadowLight);
			shadowLight.getDepthMap().texture.bind(1);
			Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D,
					GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
			Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D,
					GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);

			// environment.add((new DirectionalLight()).set(1.0f, 1.0f, 1.0f,
			// solLightDir));
			// environment.add(pointLight);
			// environment.shadowMap = shadowLight; // remet si tu veux de
			// l'ombre sur les entités.

			shadowBatch = new ModelBatch(new DepthShaderProvider());

			// hand.materials.add(handMat);

			Pixmap[] texturesSkyBoxNight = Skybox.makeSkybox("skybox.png");

			skybox = new Skybox(texturesSkyBoxNight);

			mesh = new Mesh(true, 4, 6, VertexAttribute.Position(),
					VertexAttribute.ColorUnpacked(),
					VertexAttribute.TexCoords(0));
			mesh.setVertices(new float[] { 0, 0, 0, 1, 1, 1, 1, 0, 1, width(),
					0, 0, 1, 1, 1, 1, 1, 1, width(), height(), 0, 1, 1, 1, 1,
					1, 0, 0, height(), 0, 1, 1, 1, 1, 0, 0 });
			mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

			meshOne = new Mesh(true, 4, 6, VertexAttribute.Position(),
					VertexAttribute.ColorUnpacked(),
					VertexAttribute.TexCoords(0));
			meshOne.setVertices(new float[] { 0, -0.5f, 0, 1, 1, 1, 1, 0, 1, 1,
					-0.5f, 0, 1, 1, 1, 1, 1, 1, 1, 0.5f, 0, 1, 1, 1, 1, 1, 0,
					0, 0.5f, 0, 1, 1, 1, 1, 0, 0 });
			meshOne.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

			meshCenter = new Mesh(true, 4, 6, VertexAttribute.Position(),
					VertexAttribute.ColorUnpacked(),
					VertexAttribute.TexCoords(0));
			meshCenter.setVertices(new float[] { -0.5f, -0.5f, 0, 1, 1, 1, 1,
					0, 1, 0.5f, -0.5f, 0, 1, 1, 1, 1, 1, 1, 0.5f, 0.5f, 0, 1,
					1, 1, 1, 1, 0, -0.5f, 0.5f, 0, 1, 1, 1, 1, 0, 0 });
			meshCenter.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

			meshFBO = new Mesh(true, 4, 6, VertexAttribute.Position(),
					VertexAttribute.ColorUnpacked(),
					VertexAttribute.TexCoords(0));
			meshFBO.setVertices(new float[] { 0, 0, 0, 1, 1, 1, 1, 0, 1,
					FBO_SIZE, 0, 0, 1, 1, 1, 1, 1, 1, FBO_SIZE, FBO_SIZE, 0, 1,
					1, 1, 1, 1, 0, 0, FBO_SIZE, 0, 1, 1, 1, 1, 0, 0 });
			meshFBO.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

			fboWaterRefletion = new FrameBuffer(Format.RGB888, 256,
					ExterminateGame.fsaa > 1f ? 256 : 128, true);
			if (ExterminateGame.useGL3) {
				fbogodprep = new FrameBuffer(Format.RGB888, 1024, 512, true);
				fbogod = new FrameBuffer(Format.RGB888, 1024, 512, true);
			}
			normalMap = loadTexture("seanormal.jpg");
			normalMap.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			normalMap2 = loadTexture("seanormal.png");
			normalMap2.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			logo = loadTexture("logo.png");
			// normalMap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			dudvMap = loadTexture("dudv.jpg");
			perlinMap = loadTexture("perlin.png");
			perlinMap.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			dudvMap.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			// dudvMap.setFilter(TextureFilter.Linear, TextureFilter.Linear);

			if (Gdx.gl30 != null) {
				Gdx.gl30.glGenQueries(4, query, 0);
				queryDone = false;
			} else {
				if (sun) {
					sun = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		init();

	}


	public static ShapeRenderer shapeRenderer;

	private Mesh mesh;
	public static Mesh meshOne;
	public static Mesh meshCenter;
	private Mesh meshFBO;
	public static FPSLogger FPSLogger = new FPSLogger();
	private float targetMul;
	private boolean inWater;
	private Vector2 tmpv2 = new Vector2();


	public static ParticleSystem particleSystem = ParticleSystem.get();

	boolean resized = false;

	private boolean queryDone = false;
	private float intenseGodray;
	private float flareForce;
	private Vector3 tmpv = new Vector3();
	public static Vector2 sizev2 = new Vector2();

	public static void loadShaders() {
		System.out.println("shader sun...");
		// shaderSun = new
		// ShaderProgram(Gdx.files.internal("net/osaris/exterminate/shaders/sun.vertex.glsl").readString(),
		// Gdx.files
		// .internal("net/osaris/exterminate/shaders/sun.fragment.glsl").readString());

		System.out.println("shader eau...");
		shaderEauBase = new ShaderProgram(Gdx.files.internal(
				"net/osaris/exterminate/shaders/eau.vertex.glsl").readString(),
				Gdx.files.internal(
						"net/osaris/exterminate/shaders/eau.fragment.glsl")
						.readString());
		if (!shaderEauBase.isCompiled()) {
			String log = shaderEauBase.getLog();
			System.out.println(log);
			System.exit(0);
		}
		System.out.println("shader inspect...");
		shaderInspect = new ShaderProgram(Gdx.files.internal(
				"net/osaris/exterminate/shaders/godray.vertex.glsl").readString(),
				Gdx.files.internal(
						"net/osaris/exterminate/shaders/inspect.fragment.glsl")
						.readString());
		if (!shaderInspect.isCompiled()) {
			String log = shaderInspect.getLog();
			System.out.println(log);
			System.exit(0);
		}
		System.out.println("shader souseau...");
		shaderSousEau = new ShaderProgram(Gdx.files.internal(
				"net/osaris/exterminate/shaders/souseau.vertex.glsl")
				.readString(), Gdx.files.internal(
				"net/osaris/exterminate/shaders/souseau.fragment.glsl")
				.readString());
		if (!shaderSousEau.isCompiled()) {
			String log = shaderSousEau.getLog();
			System.out.println(log);
			System.exit(0);
		}
		System.out.println("shader flare...");
		shaderFlare = new ShaderProgram(Gdx.files.internal(
				"net/osaris/exterminate/shaders/sun.vertex.glsl").readString(),
				Gdx.files.internal(
						"net/osaris/exterminate/shaders/sun.fragment.glsl")
						.readString());
		if (!shaderFlare.isCompiled()) {
			System.err.println(shaderFlare.getLog());
			System.exit(0);
		}



		System.out.println("shader sol...");
		groundShader = new ShaderProgram(
				Gdx.files.internal(
						"net/osaris/exterminate/shaders/sol.vertex.glsl")
						.readString(),
				Gdx.files
						.internal(
								ExterminateGame.useGL3 ? "net/osaris/exterminate/shaders/sol.fragment.glsl"
										: "net/osaris/exterminate/shaders/sol.fragment.glsl")
						.readString());
		if (!groundShader.isCompiled()) {
			System.err.println(groundShader.getLog());
			System.exit(0);
		}

		System.out.println("shader sol low...");
		solLowShader = new ShaderProgram(
				Gdx.files
						.internal(
								ExterminateGame.useGL3 ? "net/osaris/exterminate/shaders/sollo.vertex.glsl"
										: "net/osaris/exterminate/shaders/sollogl2.vertex.glsl")
						.readString(),
				Gdx.files
						.internal(
								ExterminateGame.useGL3 ? "net/osaris/exterminate/shaders/sollo.fragment.glsl"
										: "net/osaris/exterminate/shaders/sollo.fragment.glsl")
						.readString());
		if (!solLowShader.isCompiled()) {
			System.err.println(solLowShader.getLog());
			System.exit(0);
		}

		System.out.println("shader depth...");
		depthShader = new ShaderProgram(Gdx.files.internal(
				"net/osaris/exterminate/shaders/depth.vertex.glsl")
				.readString(), Gdx.files.internal(
				"net/osaris/exterminate/shaders/depth.fragment.glsl")
				.readString());
		if (!depthShader.isCompiled()) {
			System.err.println(depthShader.getLog());
			System.exit(0);
		}

		if (ExterminateGame.useGL3) {

			System.out.println("shader defferedsol...");
			shaderDefferedSol = new ShaderProgram(Gdx.files.internal(
					"net/osaris/exterminate/shaders/defferedsol.vertex.glsl")
					.readString(), Gdx.files.internal(
					"net/osaris/exterminate/shaders/defferedsol.fragment.glsl")
					.readString());
			if (!shaderDefferedSol.isCompiled()) {
				System.err.println(shaderDefferedSol.getLog());
				ExterminateGame.useGL3 = false;
				DEFFERED = false;
			}

			System.out.println("shader godray...");
			shaderGodray = new ShaderProgram(Gdx.files.internal(
					"net/osaris/exterminate/shaders/godray.vertex.glsl")
					.readString(), Gdx.files.internal(
					"net/osaris/exterminate/shaders/godray.fragment.glsl")
					.readString());
			if (!shaderGodray.isCompiled()) {
				System.err.println(shaderGodray.getLog());
				ExterminateGame.useGL3 = false;
				DEFFERED = false;
			}

			System.out.println("shader godapply...");
			shaderGodapply = new ShaderProgram(Gdx.files.internal(
					"net/osaris/exterminate/shaders/godray.vertex.glsl")
					.readString(), Gdx.files.internal(
					"net/osaris/exterminate/shaders/godapply.fragment.glsl")
					.readString());
			if (!shaderGodapply.isCompiled()) {
				System.err.println(shaderGodapply.getLog());
				ExterminateGame.useGL3 = false;
				DEFFERED = false;
			}

			System.out.println("shader godprep...");
			shaderGodprep = new ShaderProgram(Gdx.files.internal(
					"net/osaris/exterminate/shaders/godray.vertex.glsl")
					.readString(), Gdx.files.internal(
					"net/osaris/exterminate/shaders/godprep.fragment.glsl")
					.readString());
			if (!shaderGodprep.isCompiled()) {
				System.err.println(shaderGodprep.getLog());
				ExterminateGame.useGL3 = false;
				DEFFERED = false;
			}

			System.out.println("shader defferedmerge...");
			shaderDefferedMerge = new ShaderProgram(
					Gdx.files
							.internal(
									"net/osaris/exterminate/shaders/defferedmerge.vertex.glsl")
							.readString(),
					Gdx.files
							.internal(
									"net/osaris/exterminate/shaders/defferedmerge.fragment.glsl")
							.readString());
			if (!shaderDefferedMerge.isCompiled()) {
				System.err.println(shaderDefferedMerge.getLog());
				ExterminateGame.useGL3 = false;
				DEFFERED = false;
			}

			System.out.println("shader eaudef...");
			shaderEauDef = new ShaderProgram(Gdx.files.internal(
					"net/osaris/exterminate/shaders/eaudef.vertex.glsl")
					.readString(), Gdx.files.internal(
					"net/osaris/exterminate/shaders/eaudef.fragment.glsl")
					.readString());
			if (!shaderEauDef.isCompiled()) {
				System.err.println(shaderEauDef.getLog());
				ExterminateGame.useGL3 = false;
				DEFFERED = false;
			}

		} else {
			DEFFERED = false;

		}

	}

	public static float forceVent = 1f;
	public static float maxScore = 1f;
	public static float totalTime = 0f;
	public static int nbKills = 0;
	public static boolean dead = false;

	private void doPhysics(float delta) {

		MyParticules.setTimeStepS(delta);

		world.nowAt(player.pos);

		updateSun(16f);

		if (!CameraController.PAUSED) {

			cameraController.update(delta);
			entityManager.update(Gdx.graphics.getDeltaTime());

			SoundManager.update(delta);
			
			if(WorldAround.ready && !dead) {
				score-=delta*speedEnergy;
				speedEnergy+=delta*10f;
				totalTime+=delta;
				maxScore = Math.max(maxScore, score);
			}
	//		score = 1000;
			if(score<0) {
				score=0;
				dead = true;
			}
			
			if(dead && player.timeBeforeRespawn>0.1f) 			Gdx.input.setCursorCatched(false);


		}

		world.vazyGenere = NB_CHUNK_MODELISE_PAR_FRAME;
		world.vazyRegenere = true;
		IslandManager.canRebuild = true;

		time += delta;
		if (time > 100f) {
			time -= time;
		}
	
	}
	
	@Override
	public void render(float delta) {
		// FPSLogger.log();


		doPhysics(delta);
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		Gdx.gl.glClearColor(0.05f, 0.0505f, 0.06f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		// Gdx.gl.glClear(GL20.GL_ALPHA_BITS);
		Gdx.gl.glColorMask(true, true, true, true);
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

		if (!WorldAround.ready) {
			cam2D.zoom = 1f;
			cam2D.update();
			batcher.setProjectionMatrix(cam2D.combined);

			batcher.begin();
			batcher.enableBlending();
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			batcher.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			font.setColor(Color.WHITE);

			batcher.end();

			batcherFont.begin();
			font.getData().setScale(1f);

			font.draw(batcherFont, "Generating world...", width()*0.5f-400f, height() * 0.5f + 20f, 800f, Align.center, true);

			batcherFont.end();

			draw2D(delta);

			return;
		}


		n++;

		prepareWaterReflection();

		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);

		/***
		 * Première passe : ombres
		 */
		if (n % 2 == 0 || SHADOWS > 1) { // juste une fois sur 2 OKLM
			if (hauteur > 0 && SHADOWS > 0) {
				tmpv.set(cam.position);
				shadowLight.begin(tmpv, solLightDir);
				shadowBatch.begin(shadowLight.getCamera());
				// System.out.println(shadowLight.getCamera().position);
				Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

				// System.out.println(delta);
				if ((CameraController.cameraMode != CameraController.FPS)
						|| player.dead) {
					player.draw(shadowBatch, environment, true);
				}

				// shadowBatch.render(test, environment);
				entityManager.draw(shadowBatch, environment, true);

				shadowBatch.end();

				shadowLight.end();

			} else {
				tmpv.set(cam.position);
				shadowLight.begin(tmpv, solLightDir);
				shadowBatch.begin(shadowLight.getCamera());
				shadowBatch.end();
				shadowLight.end();
			}
		}

		/***
		 * Deuxième passe : Glowmap
		 */
		float sunForce = 0;
		flareForce = 0;
		sunDirection.set(sunPosition).add(cam.position);
		cam.project(sunDirection);
		sunForce = (float) Math.min(Math.max(0.04,
				0.1 - 0.05 * sunDirection.dst(width() / 2, height() / 2, 0)
						/ width()), 0.1);
		// sunForce = 0.04f;
		if (sunDirection.z < 1)
			sunForce = 0;
		flareForce = sunForce * 15f;
		sunForce *= width() * 0.5f;

		sunForce *= sunForce;
		sunDirection.z = 0;

		intenseGodray = 0.0f;
		float posX = sunDirection.x / width();
		float posY = sunDirection.y / width();
		if (DEFFERED && hauteur > -0.05f && sunForce > 0.0f && posX > -2.
				&& posY > -2. && posX < 3. && posY < 3.) {
			intenseGodray = 1.0f * (1.0f - hauteur * 0.8f) / fogAmount;
			if (posX < -0.) {
				intenseGodray *= -(-2 - posX) * 0.5;
			}
			if (posY < -0.) {
				intenseGodray *= -(-2 - posY) * 0.5;
			}
			if (posX > 1.) {
				intenseGodray *= (3 - posX) * 0.5;
			}
			if (posY > 1.) {
				intenseGodray *= (3 - posY) * 0.5;
			}
			if (hauteur < 0.0f) {
				intenseGodray *= (0.05f + hauteur) / 0.05f;
			}
		}
		if (intenseGodray < 0.02f) {
			intenseGodray = 0.0f;
		}
		// System.out.println(intenseGodray);

		/*******************************************************************************************************
		 * Troisième passe : principale
		 */
		// Gdx.gl.glColorMask(true, true, true, true);
		// Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

		// deffered
		if (DEFFERED) {

			fboDeffered.begin();

			Gdx.gl.glDepthMask(true);
			Gdx.gl.glClearDepthf(1f);

			// LA COULEUR DU ROUGE EST IMPORTANTE!!!!!!! pour le godray.
			Gdx.gl.glClearColor(0.0f, 0.45f, 0.6f, 1f);// g à 1 car c'est le
														// depth //lol. ou pas.
														// fait un bleu, car vu
														// de sous l'eau on
														// verra cette couleur.
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glStencilMask(0xFF); // on ecrit sur tout les bits
			Gdx.gl.glClearStencil(0);

			Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
			Gdx.gl.glDepthMask(false);
			// skybox.render(cam, jour, false, skybox.skyboxShaderDeffered);
			Gdx.gl.glDepthMask(true);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

			Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);

			Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
			Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF); // passe tout le
															// temps
			Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);// remplace
																			// (par
																			// 1)
																			// si
																			// passe
			Gdx.gl.glStencilMask(0xFF); // on ecrit sur tout les bits

			deferredPopulateGBuffer();
			Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);// remplace
																			// (par
																			// 1)
																			// si
																			// passe

			fboDeffered.end();

			if (POSTPRO) {
				postProcessor.captureNoClear();
			}

			Gdx.gl.glDepthMask(false);
			Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
			Gdx.gl.glStencilFunc(GL20.GL_GEQUAL, 1, 0xFF); // on light ceux qui
															// sont marqués
			deferredStepMerge();

			Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 0, 0xFF); // on sky box les
															// autres

			skybox.render(cam, jour, false, Skybox.skyboxShader);

			Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);

			Gdx.gl.glDepthMask(true);

		} else {

			if (POSTPRO) {
				postProcessor.capture();
			}

			skybox.render(cam, jour, false);

		}

		// eau LD

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);

		if (!DEFFERED) {
			drawChunks(modelBatch, GROUND, RAYON);
			modelEntityBatch.begin(cam);
			// Gdx.gl.glDisable(GL20.GL_CULL_FACE);

			if (InGame.wallhack) {

				shapeRenderer.setProjectionMatrix(cam.combined);
				shapeRenderer.setAutoShapeType(true);
				shapeRenderer.setColor(Color.RED);

				InGame.shapeRenderer.begin();

			}

			if ((CameraController.cameraMode != CameraController.FPS)
					|| player.dead)
				player.draw(modelEntityBatch, environment, false);

			entityManager.draw(modelEntityBatch, environment, false);
			if (InGame.wallhack) {
				InGame.shapeRenderer.flush();
				InGame.shapeRenderer.end();
			}

			modelEntityBatch.end();
		}

/*		updateSun(16f);
		drawChunks(modelBatch, EAU, RAYON);
		updateSun(10f);*/
		drawChunks(modelBatch, WATER, RAYON);
//		updateSun(16f);

		Assets.fontShader.begin();
		Assets.fontShader.setUniformf("smoothing",
				1f / 16f / ExterminateGame.fsaa);
		Assets.fontShader.end();
		batcherFont3D.setProjectionMatrix(InGame.cam.combined);
		Gdx.gl.glCullFace(GL20.GL_FRONT);

		batcherFont3D.enableBlending();
		batcherFont3D.begin();
		InGame.font3D.setUseIntegerPositions(false);
		InGame.font3D.getData().setScale(0.007f);
		InGame.font3D.setColor(28 / 255f, 198 / 255f, 1f, 0.6f);
		InGame.font3D.setColor(0 / 255f, 0 / 255f, 0f, 0.8f);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		InGame.font3D.getData().setScale(0.024f);
		// draw3DText("test <3", 412.01f-0.25f, 39.8f, 1142.5f, 90f);

		batcherFont3D.end();

		Assets.fontShader.begin();
		Assets.fontShader.setUniformf("smoothing", 1f / 11f);
		Assets.fontShader.end();

		entityManager.drawOther();

		modelBatch.begin(cam);

		ParticleSystem particleSystem = ParticleSystem.get();

		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();

		// modelBatch.begin(cam);
		modelBatch.render(particleSystem);
		// modelBatch.end();

		modelBatch.end();

		// fin eau LD

		// renderSkeleton(boy.model);

		/**
		 * Soleil + flare flareForce est entre 1.5 et 1.0. en gros.
		 */
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		if (ExterminateGame.useGL3) {
			if (queryDone) {
				Gdx.gl30.glGetQueryObjectuiv(query[0],
						GL30.GL_QUERY_RESULT_AVAILABLE, queryResultA);
				if (queryResultA.get(0) != GL20.GL_FALSE) {
					Gdx.gl30.glGetQueryObjectuiv(query[0],
							GL30.GL_QUERY_RESULT, queryResult1);
					queryDone = false;
				}
				Gdx.gl30.glGetQueryObjectuiv(query[1],
						GL30.GL_QUERY_RESULT_AVAILABLE, queryResultA);
				if (queryResultA.get(0) != GL20.GL_FALSE)
					Gdx.gl30.glGetQueryObjectuiv(query[1],
							GL30.GL_QUERY_RESULT, queryResult2);
				Gdx.gl30.glGetQueryObjectuiv(query[2],
						GL30.GL_QUERY_RESULT_AVAILABLE, queryResultA);
				if (queryResultA.get(0) != GL20.GL_FALSE)
					Gdx.gl30.glGetQueryObjectuiv(query[2],
							GL30.GL_QUERY_RESULT, queryResult3);
				Gdx.gl30.glGetQueryObjectuiv(query[3],
						GL30.GL_QUERY_RESULT_AVAILABLE, queryResultA);
				if (queryResultA.get(0) != GL20.GL_FALSE)
					Gdx.gl30.glGetQueryObjectuiv(query[3],
							GL30.GL_QUERY_RESULT, queryResult4);
			}
		} else {
			if (sun) {
				/*
				 * GL15.glGetQueryObject(query[0],
				 * GL15.GL_QUERY_RESULT_AVAILABLE, queryResultA);
				 * if(queryResultA.get(0)!=0) {
				 * 
				 * GL15.glGetQueryObject(query[0], GL15.GL_QUERY_RESULT,
				 * queryResult1); GL15.glGetQueryObject(query[1],
				 * GL15.GL_QUERY_RESULT, queryResult2);
				 * GL15.glGetQueryObject(query[2], GL15.GL_QUERY_RESULT,
				 * queryResult3); GL15.glGetQueryObject(query[3],
				 * GL15.GL_QUERY_RESULT, queryResult4);
				 * 
				 * }
				 */
			}

		}
		int nbPassed = 4;

		if (queryResult1.get(0) == 0) {
			nbPassed--;
		}
		if (queryResult2.get(0) == 0) {
			nbPassed--;
		}
		if (queryResult3.get(0) == 0) {
			nbPassed--;
		}
		if (queryResult4.get(0) == 0) {
			nbPassed--;
		}

		if (nbPassed == 0) {
			targetMul = 0;
		} else if (nbPassed == 1) {
			targetMul = 0.5f;
		} else if (nbPassed == 2) {
			targetMul = 0.6f;
		} else if (nbPassed == 3) {
			targetMul = 0.7f;
		} else {
			targetMul = 1.0f;
		}

		currentSunMul += Math.max(Math.min(
				(targetMul - currentSunMul) * Math.min(delta * 10f, 1f), 0.1f),
				-0.1f);
		if (currentSunMul < 0.1f && targetMul == 0)
			currentSunMul = 0;
		flareForce *= currentSunMul;

		//
		if (sun && flareForce > 0 && sunForce > 0
				&& sunDirection.x / width() > -0.2
				&& sunDirection.y / height() > -0.2
				&& sunDirection.x / width() < 1.2
				&& sunDirection.y / height() < 1.2) {
			if (!shaderFlare.isCompiled()) {
				System.out.println(shaderFlare.getLog());
			}

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_SRC_ALPHA);
			shaderFlare.begin();

			shaderFlare.setUniformMatrix("u_worldView", cam2D.combined);
			tmpv.set(sunDirection.x / width(), sunDirection.y / height(), 0);
			shaderFlare.setUniformf("position", tmpv);
			shaderFlare
					.setUniformf("forcesoleil", 1.0f - intenseGodray * 0.75f);
			shaderFlare.setUniformf("force", (flareForce)
					/ (1f + fogAmount / 10f));// +mm/1000f
			shaderFlare.setUniformf("hauteur", hauteur);// +mm/1000f
			shaderFlare.setUniformf("resolution", sizev2);
			mesh.render(shaderFlare, GL20.GL_TRIANGLES);
			shaderFlare.end();
		}

		/**
		 * Requete d'occlusion
		 */

		if (ExterminateGame.useGL3) {
			// if(!queryDone) n++;
			if (n % 3 == 0 && !queryDone) {
				Gdx.gl.glDisable(GL20.GL_BLEND);
				Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

				shapeRenderer.setProjectionMatrix(cam.combined);
				shapeRenderer.setTransformMatrix(IDTM);

				shapeRenderer.setAutoShapeType(true);
				shapeRenderer.setColor(Color.RED);

				Gdx.gl.glColorMask(false, false, false, false);
				Gdx.gl30.glBeginQuery(GL30.GL_ANY_SAMPLES_PASSED, query[0]);
				// attention à la distance de rendu...
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.box(sun3Dpos.x * 25f + cam.position.x - 25,
						sun3Dpos.y * 25f + cam.position.y - 25, sun3Dpos.z
								* 25f + cam.position.z, 15, 15, 15);
				shapeRenderer.end();
				Gdx.gl30.glEndQuery(GL30.GL_ANY_SAMPLES_PASSED);

				Gdx.gl30.glBeginQuery(GL30.GL_ANY_SAMPLES_PASSED, query[1]);
				// attention à la distance de rendu...
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.box(sun3Dpos.x * 25f + cam.position.x + 25,
						sun3Dpos.y * 25f + cam.position.y - 25, sun3Dpos.z
								* 25f + cam.position.z, 15, 15, 15);
				shapeRenderer.end();
				Gdx.gl30.glEndQuery(GL30.GL_ANY_SAMPLES_PASSED);

				Gdx.gl30.glBeginQuery(GL30.GL_ANY_SAMPLES_PASSED, query[2]);
				// attention à la distance de rendu...
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.box(sun3Dpos.x * 25f + cam.position.x - 25,
						sun3Dpos.y * 25f + cam.position.y + 25, sun3Dpos.z
								* 25f + cam.position.z, 15, 15, 15);
				shapeRenderer.end();
				Gdx.gl30.glEndQuery(GL30.GL_ANY_SAMPLES_PASSED);

				Gdx.gl30.glBeginQuery(GL30.GL_ANY_SAMPLES_PASSED, query[3]);
				// attention à la distance de rendu...
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.box(sun3Dpos.x * 25f + cam.position.x + 25,
						sun3Dpos.y * 25f + cam.position.y + 25, sun3Dpos.z
								* 25f + cam.position.z, 15, 15, 15);
				shapeRenderer.end();
				Gdx.gl30.glEndQuery(GL30.GL_ANY_SAMPLES_PASSED);

				queryDone = true;
				Gdx.gl.glColorMask(true, true, true, true);
			}
		}

		else if (n % 5 == 0 && sun) {

		}

		/**
		 * Water
		 */

		if (cam.position.y - masterTranslate.y < IslandManager.hauteurEau() - 1f) {
			inWater = true;
		} else {
			inWater = false;
		}

		// System.out.println(cam.position);

		if (inWater) {
			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
			if (!shaderSousEau.isCompiled()) {
				System.out.println(shaderSousEau.getLog());
			}

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shaderSousEau.begin();

			shaderSousEau.setUniformMatrix("u_worldView", cam2D.combined);

			shaderSousEau.setUniformf("jour", jour);
			// shaderSousEau.setUniformf("position",new
			// Vector2(sunDirection.x/width()-0.5f, 0) );
			shaderSousEau.setUniformf("angle",
					-(float) Math.toRadians(CameraController.angleX) * 0.25f);// +mm/1000f
			shaderSousEau.setUniformf("angley",
					(float) Math.cos(Math.toRadians(CameraController.angleY)));// +mm/1000f
			shaderSousEau.setUniformf("time", time);// +mm/1000f
			// shaderSousEau.setUniformf("rouge",0);//+mm/1000f
			tmpv2.set(width() * ExterminateGame.fsaa, height()
					* ExterminateGame.fsaa);
			shaderSousEau.setUniformf("resolution", tmpv2);
			mesh.render(shaderSousEau, GL20.GL_TRIANGLES);
			shaderSousEau.end();
		}

		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);

		if (POSTPRO) {
			postProcessor.render();
		}

		if (intenseGodray > 0) {
			godray(intenseGodray);
		}

		/*********************************************************************************************************
		 * *********************************************************************
		 * ********************************** 2D
		 */
		draw2D(delta);
		/**
		 * 		

		 */
		POSTPRO = true;
//		DEFFERED = true;
	/*	
if(false) {
	try{
	Gdx.gl.glDisable(GL20.GL_BLEND);
	Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	shaderInspect.begin();

	shaderInspect.setUniformMatrix("u_worldView", cam2D.combined);
	fboRefletEau.getColorBufferTexture().bind(0);
	shaderInspect.setUniformi("u_diffuseTexture", 0);
	mesh.render(shaderInspect, GL20.GL_TRIANGLES);

	shaderInspect.end();
	}
	catch(Exception e) {
		e.printStackTrace();
	}
	
	
}*/
		

		// ensure default texture unit #0 is active
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

	}

	private void deferredStepMerge() {
		shaderDefferedMerge.begin();

		shaderDefferedMerge.setUniformMatrix("u_worldView", cam2D.combined);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, fboDeffered.colorTexture);
		shaderDefferedMerge.setUniformi("u_diffuseTexture", 0);
		/*
		 * Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0+1);
		 * Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, fboDeffered.normalTexture);
		 * shaderDefferedMerge.setUniformi("u_normalTexture", 1);
		 */
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 2);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, fboDeffered.attribsTexture);
		shaderDefferedMerge.setUniformi("u_attribsTexture", 2);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 3);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, fboDeffered.depthTexture);
		shaderDefferedMerge.setUniformi("u_depthTexture", 3);

		shaderDefferedMerge.setUniformMatrix("InvProjectionMatrix",
				cam.invProjectionView);
		tmpv2.set(width() * ExterminateGame.fsaa, height()
				* ExterminateGame.fsaa);
		// shaderDefferedMerge.setUniformf("ScreenSize", tmpv2);

		shaderDefferedMerge.setUniformf("u_cameraPosition", cam.position);

		shaderDefferedMerge.setUniformf("u_ambientLight", solAmbiant);
		// blockShader.setUniformf("u_fogColor",solFog);
		shaderDefferedMerge.setUniformf("DirectionalLightcolor", solLightColor);
		shaderDefferedMerge.setUniformf("u_lightSpecular", specLightColor);
		// shaderDefferedMerge.setUniformf("u_shadowPCFOffset",1.f / (float)(2f
		// * shadowLight.getDepthMap().texture.getWidth()));
		// shaderDefferedMerge.setUniformMatrix("u_shadowMapProjViewTrans",
		// shadowLight.getProjViewTrans());
		shaderDefferedMerge.setUniformf("u_fogColor", solFog);
		shaderDefferedMerge.setUniformf("invDistFog", invDistFog);

		shadowLight.getDepthMap().texture.bind(1);
		shaderDefferedMerge.setUniformi("u_shadowTexture", 1);
		shaderDefferedMerge.setUniformf("u_shadowPCFOffset",
				1.f / (shadowLight.getDepthMap().texture.getWidth()));
		shaderDefferedMerge.setUniformf("u_shadowResolution",
				shadowLight.getDepthMap().texture.getWidth());
		shaderDefferedMerge.setUniformMatrix("u_shadowMapProjViewTrans",
				shadowLight.getProjViewTrans());

		mesh.render(shaderDefferedMerge, GL20.GL_TRIANGLES);
		shaderDefferedMerge.end();
	}

	private void godray(float intense) {
		// if(CameraController.creafly) return;
		fbogod.begin();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(false);
		shaderGodprep.begin();
		// tmpv.set(sunDirection.x/width(), sunDirection.y/height(), 0);
		// shaderGodprep.setUniformf("position",tmpv);

		shaderGodprep.setUniformMatrix("u_worldView", cam2D.combined);
		tmpv.set(sunDirection.x / width(), sunDirection.y / height(), 0);
		shaderGodprep.setUniformf("position", tmpv);
		shaderGodprep.setUniformf("resolution", sizev2);

		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, fboDeffered.depthTexture);
		shaderGodprep.setUniformi("u_diffuseTexture", 0);

		mesh.render(shaderGodprep, GL20.GL_TRIANGLES);
		shaderGodprep.end();

		fbogod.end();

		fbogodprep.begin();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		shaderGodray.begin();
		tmpv.set(sunDirection.x / width(), sunDirection.y / height(), 0);
		shaderGodray.setUniformf("position", tmpv);

		shaderGodray.setUniformMatrix("u_worldView", cam2D.combined);
		perlinMap.bind(3);
		shaderGodray.setUniformi("u_perlinTexture", 3);
		fbogod.getColorBufferTexture().bind(0);
		shaderGodray.setUniformi("u_diffuseTexture", 0);

		mesh.render(shaderGodray, GL20.GL_TRIANGLES);
		shaderGodray.end();

		fbogodprep.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);

		// Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_SRC_ALPHA);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		// Gdx.gl.glBlendEquationSeparate(GL30.GL_MAX, GL20.GL_FUNC_ADD);
		// Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA,
		// GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ZERO);

		shaderGodapply.begin();

		shaderGodapply.setUniformMatrix("u_worldView", cam2D.combined);

		// intense=1f;
		shaderGodapply.setUniformf("intense", intense);
		fbogodprep.getColorBufferTexture().bind(0);
		shaderGodapply.setUniformi("u_diffuseTexture", 0);
		mesh.render(shaderGodapply, GL20.GL_TRIANGLES);

		shaderGodapply.end();

		Gdx.gl.glBlendEquationSeparate(GL20.GL_FUNC_ADD, GL20.GL_FUNC_ADD);
		Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA,
				GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ZERO);

		Gdx.gl.glDepthMask(true);

	}


	private void deferredPopulateGBuffer() {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);

		shaderDefferedSol.begin();
		shaderDefferedSol.setUniformMatrix("u_worldTrans", IDTMtranslated);
		shaderDefferedSol.setUniformi("u_diffuseTexture", 1);
		shaderDefferedSol.setUniformi("u_diffuseTextureSand", 2);
		shaderDefferedSol.setUniformi("u_diffuseTextureRock", 3);
		shaderDefferedSol.setUniformi("u_diffuseTextureDirt", 4);
		shaderDefferedSol.setUniformMatrix("u_projViewTrans", cam.combined);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		// Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, Biome.textureSol);
		// shaderDefferedSol.setUniformi("u_diffuseTexture", 0);

		// shaderDefferedSol.setUniformf("u_cameraPosition", cam.position);
		// shaderDefferedSol.setUniformf("DirectionalLightcolor",solLightColor);
		shaderDefferedSol.setUniformf("DirectionalLightdirection", solLightDir);

		// solShader.setUniformMatrix("u_normalMatrix", cam.combined);
		// shaderDefferedSol.setUniformf("u_cameraPosition", cam.position);
		Biome biome = null;
		for (int i = 0; i < IslandManager.RADIUS; i++) {
			for (int j = 0; j < IslandManager.RADIUS; j++) {
				if (IslandManager.getHeightMapInGrid(i, j) != null
						&& IslandManager.getHeightMapInGrid(i, j).modelSol != null) {

					// shaderDefferedSol.setUniformf("biome",
					// island.getHeightMapInGrid(i, j).biome.type*4f);
					if (IslandManager.getHeightMapInGrid(i, j).biome != biome) {
						biome = IslandManager.getHeightMapInGrid(i, j).biome;
						biome.textureHerbe.bind(1);
						biome.textureSand.bind(2);
						biome.textureRock.bind(3);
						biome.textureDirt.bind(4);
					}
					Mesh sol = IslandManager.getHeightMapInGrid(i, j).modelSol;

					sol.render(shaderDefferedSol, GL20.GL_TRIANGLES);

				}
			}
		}

		biome = null;
		Chunk.ground.render(shaderDefferedSol, true);

		shaderDefferedSol.end();

		// modelEntityBatch.getShaderProvider()

		modelEntityDefBatch.begin(cam);

		if ((CameraController.cameraMode != CameraController.FPS)
				|| player.dead)
			player.draw(modelEntityDefBatch, environment, false);

		Gdx.gl.glCullFace(GL20.GL_FRONT);
		entityManager.draw(modelEntityDefBatch, environment, false);

		modelEntityDefBatch.end();

		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
	}


	DoubleBuffer worldClip = BufferUtils.createDoubleBuffer(4);

	private void prepareWaterReflection() {
		fboWaterRefletion.begin();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		skybox.render(cam, jour, true);

		if (ExterminateGame.useGL3) {

			Gdx.gl.glEnable(GL11.GL_CLIP_PLANE0);
			worldClip.put(0).put(1).put(0).put(IslandManager.hauteurEau())
					.flip();
			GL11.glClipPlane(GL11.GL_CLIP_PLANE0, worldClip);

			drawGroundLowLOD(true);

			Gdx.gl.glDisable(GL11.GL_CLIP_PLANE0);
		}

		fboWaterRefletion.end();

	}
	
	public static float score = 5000;
	public static float speedEnergy = 250;
	public static float lastScore = 0;
	public static String lastMessage = "";
	public static float timeLastMessage = 0;
	
	public static void score(float much, String message) {
		score+=much;
		lastScore = much;
		lastMessage = message;
		timeLastMessage = 2f;
	}

	public static float lastAttackedMachineSince = 0;
	public static float fontSize = 0.82f;
	public static float percentLife = 1f;
	public static float targetRotation = 0f;
	public static float percentStamina = 1f;
	public static float logoshawn = 0f;

	private void draw2D(float delta) {
		
		if (!CameraController.show2D)
			return;
		
		
		float size = height() * 0.09f;

		cam2D.zoom = 1f;
		cam2D.update();
		batcher.setProjectionMatrix(cam2D.combined);

		batcher.begin();
		batcher.enableBlending();

		
		percentLife = lerp(percentLife,
				Math.max(0, player.life / player.maxLife), 10f, delta);

		if (WorldAround.ready && !dead) {
			for(int i=0;i<player.ammo;i++) {
				batcher.draw(healthBar, width()-size * 3.3f+i*size*0.3f, size * 0.6f+i*size*0.01f,
						size * 0.4f, size * 0.25f);
				
			}
	
			batcher.draw(cursor2, width()/2-size*0.2f, height()/2-size*0.2f,
					size*0.4f, size*0.4f);
	
			float targetSize = size;
			if(player.target!=null) {
				tmpv.set(player.target.pos);
				tmpv.y+=player.target instanceof Shark ? 2f : 2f;
				tmpv.add(masterTranslate);
				cam.project(tmpv);
				if(tmpv.z<1f) {
					float tx = tmpv.x;
					float ty = tmpv.y;
					targetRotation+=delta*50f;
					targetSize = 0.5f*Math.min(targetSize*1.5f, 40f/player.target.pos.dst(player.pos)*size)*1f+(float)Math.cos(targetRotation*0.1f+5f)*5f;
					
					batcher.draw(cursor2, tx, ty, 0.5f, 0.5f, 1, 1, targetSize*1.5f, targetSize*1.5f, -0*3.4f);
					targetSize = 0.5f*Math.min(targetSize*1.5f, 40f/player.target.pos.dst(player.pos)*size)*1f+(float)Math.cos(targetRotation*0.1f+5f)*2f;
					batcher.draw(target, tx, ty, 0.5f, 0.5f, 1, 1, targetSize, targetSize, targetRotation);
					
				}
				else {
					player.target = null;
				}
			}
			if(WorldAround.ready) logoshawn+=delta;
			if(logoshawn<3f) {
				batcher.setColor(0.8f, 1f, 1f, 0.8f);
				batcher.draw(logo, width()*0.5f-size*7f, height()*0.5f-size*0.8f, size*14f, size*2f);
				batcher.setColor(1f, 1f, 1f, 1f);
			}
		}

		// batcher.draw(fboRefletEau.getColorBufferTexture(), 0, 720, 512,
		// -720);

		batcherFont.begin();

		font.getData().setScale(0.6f);

		font.setColor(0.99f, 0.99f, 0.99f, 0.9f);
		if (showInfo) {

			font.draw(batcherFont,
					"FPS : " + Gdx.graphics.getFramesPerSecond(), 10f,
					height() - 10f);

			font.draw(batcherFont, "Pos : " + (int) player.pos.x + " ; "
					+ (int) player.pos.y + " ; " + (int) player.pos.z, 150f,
					height() - 10f);
			font.draw(batcherFont, "Chunk : " + player.onChunk, 150f,
					height() - 35f);
			font.draw(batcherFont, "Entities : "
					+ entityManager.managedEntities.size(), 150f,
					height() - 60f);
			if (player.onChunk != null)
				font.draw(batcherFont, "Entities on chunk : "
						+ player.onChunk.entities.size(), 150f, height() - 85f);

		}

		font.getData().setScale(InGame.fontSize*1.45f);

		
		font.setColor(21f/255f, 197f/255f, 255f/255f, 1f);
		if(!dead && !CameraController.PAUSED) font.draw(batcherFont, "Energy: " + (int)score, 40f, 70f);

		if(timeLastMessage>0) {
			timeLastMessage-=delta*1.5f;
			font.setColor(21f/255f, 197f/255f, 255f/255f, 1f-Math.max(0, 1f-timeLastMessage)*Math.max(0, 1f-timeLastMessage));

			
			font.draw(batcherFont, lastMessage+"\n+"+ (int)lastScore, width()*0.5f-400f, 240f, 800f, Align.center, true);
			
		}
		
		if(dead && !CameraController.PAUSED) {
			font.setColor(21f/255f, 197f/255f, 255f/255f, 1f);
			font.draw(batcherFont, "YOU'VE BEEN EXTERMINATED!\n"+"\nKills: "+nbKills+"\n"
					+"\nTime alive: "+((int)totalTime/60)+"min "+(int)totalTime%60+"sec"+"\n"
					+"\nHighest Energy: "+(int)maxScore+"\n\nPress SPACE to restart", width()*0.5f-350f, height()*0.75f, 700f, Align.center, true);
		
		}
		
		if(CameraController.PAUSED) {
			font.setColor(21f/255f, 197f/255f, 255f/255f, 1f);
			font.draw(batcherFont, "Game Paused", width()*0.5f-350f, height()*0.55f, 700f, Align.center, true);
			font.draw(batcherFont, "M: Sound On/Off", 35f, height()*0.95f);
			font.draw(batcherFont, "Ctrl+X: Exit Game", 35f, height()*0.95f-50f);
			font.draw(batcherFont, "F11: Fullscreen", 35f, height()*0.95f-100f);
			font.draw(batcherFont, "Copyright Osaris Games - www.osaris.net", 35f,120f);
			font.draw(batcherFont, "Music by Synthr - www.synthr.net", 35f, 70f);
		
		}
		
		batcher.end();

		batcherFont.end();

		if (CameraController.PAUSED) {
			batcherFont.begin();

			/*
			 * Menu.
			 */

			batcher.begin();
			batcher.enableBlending();

			// batcher.draw(voile, 0, 0, width(), height());

			batcher.end();

			batcherFont.end();
		}

		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

	}

	float vitelerp = 0;


	int n = 0;

	Vector3 tmpVec1 = new Vector3();
	Vector3 tmpVec2 = new Vector3();
	Vector3 tmpVec3 = new Vector3();
	Matrix4 tmpMat4 = new Matrix4();
	Matrix4 effectTransform = new Matrix4();

	public final static Matrix4 IDTM = new Matrix4().idt();
	public final static Matrix4 IDTMtranslated = new Matrix4().idt();

	public static void lerp(Vector3 vec, Vector3 target, float speed,
			float deltaTime) {

		vec.x -= (vec.x - target.x)
				* Math.min(1.0f / 30f, Math.max(1.0f / 120f, deltaTime))
				* speed;
		vec.y -= (vec.y - target.y)
				* Math.min(1.0f / 30f, Math.max(1.0f / 120f, deltaTime))
				* speed;
		vec.z -= (vec.z - target.z)
				* Math.min(1.0f / 30f, Math.max(1.0f / 120f, deltaTime))
				* speed;
	}

	public static float lerp(float vec, float target, float speed,
			float deltaTime) {

		return vec - (vec - target)
				* Math.min(1.0f / 30f, Math.max(1.0f / 120f, deltaTime))
				* speed;
	}

	public static float time = 0;
	private float soleilEau;
	public static float couDeVent = 0f;

	private void drawChunks(ModelBatch modelBatch2, int type, int rayon) {
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);

		ShaderProgram shaderWater = shaderEauBase;

		if (DEFFERED) {
			shaderWater = shaderEauDef;
		}

		if (type == WATER) {
			Gdx.gl.glEnable(GL20.GL_CULL_FACE);

			if (inWater) {
				Gdx.gl.glCullFace(GL20.GL_FRONT);

			} else {
				Gdx.gl.glCullFace(GL20.GL_BACK);

			}

			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			shaderWater.begin();
			shaderWater.setUniformMatrix("u_worldTrans", IDTMtranslated);

			if (DEFFERED) {
				shaderWater.setUniformMatrix("InvProjectionMatrix",
						cam.invProjectionView);
				shaderWater.setUniformf("resolution", sizev2);

				Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 2);
				Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D,
						fboDeffered.colorTexture);
				shaderWater.setUniformi("u_gbufferDiffuse", 2);

				Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 7);
				Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D,
						fboDeffered.attribsTexture);
				shaderWater.setUniformi("u_gbufferAttribs", 7);

				Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 6);
				Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D,
						fboDeffered.depthTexture);
				shaderWater.setUniformi("u_gbufferDepth", 6);

				// normalMap.bind(4);
				// shaderEau.setUniformi("u_normalTexture", 4);
				normalMap.bind(5);
				shaderWater.setUniformi("u_normal2Texture", 5);

				shadowLight.getDepthMap().texture.bind(4);
				shaderWater.setUniformi("u_shadowTexture", 4);
				// shaderEau.setUniformf("u_shadowPCFOffset",1.f / (float)(2f *
				// shadowLight.getDepthMap().texture.getWidth()));
				shaderWater.setUniformMatrix("u_shadowMapProjViewTrans",
						shadowLight.getProjViewTrans());

			} else {
				shaderWater.setUniformf("eau", 1.0f);// +mm/1000f

			}

			Biome.textureEau.bind(0);
			shaderWater.setUniformi("u_diffuseTexture", 0);

			fboWaterRefletion.getColorBufferTexture().bind(1);
			shaderWater.setUniformi("u_diffuseRefletTexture", 1);
			/*	
			*/

			dudvMap.bind(3);
			shaderWater.setUniformi("u_dudvTexture", 3);

			shaderWater.setUniformMatrix("u_worldView", cam.combined);
			// tmpv.set(cam.position).sub(masterTranslate);
			// shaderEau.setUniformf("u_cameraPosition", tmpv);
			shaderWater.setUniformf("u_cameraPosition", cam.position);

			shaderWater.setUniformf("u_ambientLight", solAmbiant);
			shaderWater.setUniformf("DirectionalLightcolor", solLightColor);
			shaderWater.setUniformf("DirectionalLightdirection", solLightDir);

			shaderWater.setUniformf("u_fogColor", solFog);
			shaderWater.setUniformf("invDistFog", invDistFogEau);

			shaderWater.setUniformf("time", (time * 1000f) / 300f);// +mm/1000f
			shaderWater.setUniformf("jour", jour);// +mm/1000f
			shaderWater.setUniformf("soleil", inWater ? 0.0f : soleilEau);// +mm/1000f

			float vague = 0.05f;
			shaderWater.setUniformf("vague", vague);// +mm/1000f

			for (int i = 0; i < IslandManager.RADIUS; i++) {
				for (int j = 0; j < IslandManager.RADIUS; j++) {
					if (IslandManager.getHeightMapInGrid(i, j) != null
							&& IslandManager.getHeightMapInGrid(i, j).modelEau != null) {
						IslandManager.getHeightMapInGrid(i, j).modelEau.render(
								shaderWater, GL20.GL_TRIANGLES);
					}
				}
			}

			if (Chunk.water.mesh != null)
				Chunk.water.mesh.render(shaderWater, GL20.GL_TRIANGLES);

		}

		if (type == GROUND && modelBatch2 != shadowBatch) {

			drawGroundLowLOD(false);

			groundShader.begin();
			groundShader.setUniformMatrix("u_worldTrans", IDTMtranslated);

			// if(!ExterminateGame.useGL3) {
			groundShader.setUniformi("u_diffuseTexture", 1);
			groundShader.setUniformi("u_diffuseTextureSand", 2);
			groundShader.setUniformi("u_diffuseTextureRock", 3);
			groundShader.setUniformi("u_diffuseTextureDirt", 4);
			// }
			shadowLight.getDepthMap().texture.bind(0);
			groundShader.setUniformi("u_shadowTexture", 0);
			groundShader.setUniformMatrix("u_projViewTrans", cam.combined);
			// solShader.setUniformMatrix("u_normalMatrix", cam.combined);
			groundShader.setUniformf("u_cameraPosition", cam.position);
			// solShader.setUniformf("time",(time*1000f)/300f );//+mm/1000f

			groundShader.setUniformf("u_ambientLight", solAmbiant);
			groundShader.setUniformf("u_fogColor", solFog);
			groundShader.setUniformf("DirectionalLightcolor", solLightColor);
			groundShader.setUniformf("DirectionalLightdirection", solLightDir);
			// solShader.setUniformf("u_shadowPCFOffset",1.f / (float)(2f *
			// shadowLight.getDepthMap().texture.getWidth()));
			groundShader.setUniformMatrix("u_shadowMapProjViewTrans",
					shadowLight.getProjViewTrans());
			groundShader.setUniformf("invDistFog", invDistFog);

			Chunk.ground.render(groundShader, true);

			if (ExterminateGame.useGL3) {
				// Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0+1);
				// Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY,
				// Biome.textureSol);
				// solShader.setUniformi("u_diffuseTexture", 1);

			}

		}

		if (type == WATER) {
			shaderWater.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
		if (type == GROUND && modelBatch2 != shadowBatch) {
			groundShader.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}

		if (type == GROUND) {
			// System.out.println(t+"/"+a);
		}

	}

	private void drawGroundLowLOD(boolean invY) {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(invY ? GL20.GL_FRONT : GL20.GL_BACK);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		solLowShader.begin();
		solLowShader.setUniformMatrix("u_worldTrans", IDTMtranslated);

		Biome biome = null;
		// if(!ExterminateGame.useGL3) {
		solLowShader.setUniformi("u_diffuseTexture", 1);
		solLowShader.setUniformi("u_diffuseTextureSand", 2);
		solLowShader.setUniformi("u_diffuseTextureRock", 3);
		solLowShader.setUniformi("u_diffuseTextureDirt", 4);
		// }

		solLowShader.setUniformMatrix("u_projViewTrans", cam.combined);
		// solShader.setUniformMatrix("u_normalMatrix", cam.combined);
		solLowShader.setUniformf("u_cameraPosition", cam.position);

		solLowShader.setUniformf("u_ambientLight", solAmbiant);
		solLowShader.setUniformf("u_fogColor", solFog);
		solLowShader.setUniformf("invY", invY ? -1f : 1f);
		solLowShader.setUniformf("hauteurEau", IslandManager.hauteurEau());
		solLowShader.setUniformf("invDistFog", invDistFog);
		solLowShader.setUniformf("DirectionalLightcolor", solLightColor);
		solLowShader.setUniformf("DirectionalLightdirection", solLightDir);



		for (int i = 0; i < IslandManager.RADIUS; i++) {
			for (int j = 0; j < IslandManager.RADIUS; j++) {
				if (IslandManager.getHeightMapInGrid(i, j) != null
						&& IslandManager.getHeightMapInGrid(i, j).modelSol != null) {
					if (IslandManager.getHeightMapInGrid(i, j).biome != biome) {
						biome = IslandManager.getHeightMapInGrid(i, j).biome;
						biome.textureHerbe.bind(1);
						biome.textureSand.bind(2);
						biome.textureRock.bind(3);
						biome.textureDirt.bind(4);
					}

					if (ExterminateGame.useGL3) {
						// solLowShader.setUniformf("biome",
						// island.getHeightMapInGrid(i, j).biome.type*4f);
					}

					int tmp = 0;

					Mesh sol = IslandManager.getHeightMapInGrid(i, j).modelSol;

					if (invY) {
						tmp = ((UniMesh) sol).getIndiceHandle();
						((UniMesh) sol).setIndices(IslandManager.AllIndices);
					}

					sol.render(solLowShader, GL20.GL_TRIANGLES);
					if (invY) {
						((UniMesh) sol).setIndices(tmp);
					}
				}
			}
		}

		solLowShader.end();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);

	}

	static Vector3 tmp1 = new Vector3();
	static Vector3 tmp2 = new Vector3();

	Vector3 sun3Dpos = new Vector3(0, 1, 1);

	int query[] = new int[4];
	IntBuffer queryResult1 = ByteBuffer.allocateDirect(8).asIntBuffer();
	IntBuffer queryResult2 = ByteBuffer.allocateDirect(8).asIntBuffer();
	IntBuffer queryResult3 = ByteBuffer.allocateDirect(8).asIntBuffer();
	IntBuffer queryResult4 = ByteBuffer.allocateDirect(8).asIntBuffer();
	IntBuffer queryResultA = ByteBuffer.allocateDirect(8).asIntBuffer();

	public static Vector3 sunDirection = new Vector3(0, 1, 1);
	public static Vector3 sunPosition = new Vector3(0, -1, 0).nor(); //
	public static Vector3 sunAxe = new Vector3(-0.3f, 0.f, -0.2f).nor(); //

	public static float timeInDay = 0;
	static float jour = 0.0f;
	float hauteur = 0.0f;
	public static float vitesseDay = 1f * 1f / 60f;

	void updateSun(float time) {
		// System.out.println(boy.inventory.amount[5]);
		timeInDay = time;
		Biome biome = player.onChunk != null ? player.onChunk.biome
				: Biome.biomeGrass;

		// System.out.println(-sunPosition.y);

		sunPosition.set(sunAxe).scl(0.25f).add(0, 1, 0); // minuit
		sunPosition.rotate(sunAxe, -timeInDay * 360f / 24f);

		hauteur = -sunPosition.y;

		if (sunPosition.z < 0 && hauteur > 0) {
			float max = 0.25f;
			float tauxBrouillard = 0f;

			if (hauteur < max) {
				fogAmount = 1f + (hauteur) / max * tauxBrouillard;
			} else {
				fogAmount = 1f
						+ (float) (1.0 - (Math.abs(hauteur) - max) / (1f - max))
						* tauxBrouillard;
			}
		} else {
			fogAmount = 1f;
		}
		forceVent = 1.1f;
		fogAmount = Math.min(fogAmount + (forceVent - 1f) * 3f,
				20f + forceVent * 2f);

		invDistFog = (invDistFogBase * fogAmount);
		invDistFogEau = (invDistFogEauBase * fogAmount);

		// sunPosition.rotate( time*0.25f, 1.0f, 0.5f, 0.0f);
		solLightDir.set(sunPosition);
		// System.out.println(solLightDir);
		sun3Dpos.set(sunPosition).scl(-90);

		jour = 1.0f;

		if (hauteur > 0.1f) {
			jour = 1.0f;
		} else if (hauteur > -0.1f) {
			jour = ((hauteur + 0.1f) / 0.2f);
		} else {
			jour = 0.0f;
		}

		if (hauteur < 0.0f) {
			solLightDir.y = -solLightDir.y;
		}
		if (POSTPRO) {
			vignette.setIntensity(biome.vignette);
			bloom.setBloomIntesity(biome.bloom * 1.05f);

		}

		if (hauteur > 0.0f) {
			float light = (Math.min(hauteur, 0.2f) + 0.3f) * 0.4f + 0.5f;
			solShadow.set(light * 0.8f, light * 0.8f, light * 0.8f);
			// solShadow.set(hauteur, hauteur, hauteur);
			solLightColor.set(light, light, light);
			specLightColor.set(light * 1.1f, light, light * 0.9f);
			solAmbiant.set(0.6f, 0.6f, 0.6f);

			soleilEau = 1.0f;

			/*
			 * if(biome.type==Biome.ICE) { solAmbiant.set(0.55f, 0.55f, 0.55f);
			 * }
			 */

			// on met tout le monde d'accord
			// solAmbiant.set(0.50f, 0.50f, 0.50f);
		} else {

			float light = Math.max(0.5f + (hauteur) * 1f, 0.15f);
			solShadow.set(light * 0.8f, light * 0.8f, light * 0.8f);
			solLightColor.set(light, light, light);
			specLightColor.set(light, light, light);

			float ambient = Math.max(0.6f + (hauteur) * 1f, 0.3f);

			solAmbiant.set(ambient, ambient, ambient);

			if (hauteur > -0.05f) {
				soleilEau = (hauteur + 0.05f) * 20f;
			} else {
				soleilEau = 0f;
			}
			if (POSTPRO) {
				bloom.setBloomIntesity(Math.max(biome.bloom + (hauteur) * 4f,
						0.0f));
				vignette.setIntensity(Math.min(biome.vignette - (hauteur) * 4f,
						biome.vignette * 2f));
			}
		}

		tmp1.set(solFogBase).scl(inWater ? 0.2f : (jour * 0.85f + 0.15f));
		if (forceVent > 1f) {
			float cc = (forceVent - 1f) / 12f;
			tmp1.scl(1f - cc).add(cc * 0.65f, cc * 0.75f, cc * 0.95f);
		}
		solFog.set(tmp1.x, tmp1.y, tmp1.z, 1f);
		solFog.set(50f / 255f, 50f / 255f, 50f / 255f, 1f);
//		solAmbiant.set(2f,2f,2f);
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight,
				solAmbiant.x, solAmbiant.y, solAmbiant.z, 1f));
		environment.set(new ColorAttribute(ColorAttribute.Fog, solFog));

		shadowLight.set(solShadow.x, solShadow.y, solShadow.z, solLightDir);

		float picAt = 0.9f;
		if (POSTPRO) {

			if (rouge > picAt && rouge <= 1f) {

				vignette.setIntensity(vignette.getIntensity() + (1f - rouge)
						/ (1.0f - picAt));
				vignette.setRouge((1f - rouge) / (1.0f - picAt));
			} else if (rouge > 0 && rouge <= picAt) {
	
				vignette.setIntensity(vignette.getIntensity() + rouge / picAt);
				vignette.setRouge(rouge / picAt);
			} else if (rouge > 0) {
				vignette.setRouge(0);
			} else {
				vignette.setRouge(0);
			}

			bloomAttackTarget = 1f;
			bloomAttack = lerp(bloomAttack, bloomAttackTarget, 11f, time);

			// boy.dead = true;
			if (dead) {
				player.timeBeforeRespawn+=Gdx.graphics.getDeltaTime();
				rouge+=Gdx.graphics.getDeltaTime();
				player.timeBeforeRespawn = Math.min(player.timeBeforeRespawn, 3f);
				rouge = Math.min(rouge, 0.5f);
				bloom.setBaseIntesity((0.7f + 0.28f / fogAmount)*(3f-player.timeBeforeRespawn)/3f);// bloomAttack*
			} else {
				bloom.setBaseIntesity(0.7f + 0.28f / fogAmount);// bloomAttack*
			}

			if (CameraController.snipe) {
				vignette.setIntensity(1f);
			}

			/*
			 * vignette.setLutIndexVal( 0, 4 ); vignette.setLutIndexVal( 1, 5 );
			 * vignette.setLutIntensity(0.2f);
			 */
			vignette.setSaturation(1.05f);
			vignette.setSaturationMul(1.1f);
			vignette.setContrast(-0.1f
					* jour
					- (DEFFERED ? (intenseGodray * 0.15f + flareForce * 0.1f)
							: 0.0f));
			vignette.setExposure(0.4f);

		}
	}

	public float bloomAttack = 0f;
	public float bloomAttackTarget = 0f;

	public static void rebuildPostPro() {
		if (POSTPRO) {
			sizev2.set(width() * ExterminateGame.fsaa, height()
					* ExterminateGame.fsaa);

			if (postProcessor != null) {
				postProcessor.dispose();
			}

			if (fboDeffered != null) {
				fboDeffered.dispose();
			}

			// ShaderLoader.Pedantic = false;
			postProcessor = new PostProcessor(true, true, false, true);

			PostProcessor.EnableQueryStates = false;
			// create the effects you want
			bloom = new Bloom((int) (Gdx.graphics.getWidth() * 0.25f),
					(int) (Gdx.graphics.getHeight() * 0.25f));
			bloom.setBlurType(BlurType.Gaussian5x5b);
			int vpW = Gdx.graphics.getWidth();
			int vpH = Gdx.graphics.getHeight();
			vignette = new Vignette(vpW, vpH, true);
			/*
			 * vignette.setLutTexture( loadTexture("gradient-mapping.png") );
			 * vignette.setLutIndexVal( 0, 4 ); vignette.setLutIndexVal( 1, 16
			 * ); vignette.setLutIndexOffset(0.5f);
			 * vignette.setLutIntensity(1.00f);
			 */
			vignette.setSaturation(1.0f);
			vignette.setSaturationMul(1.0f);

			postProcessor.addEffect(vignette);
			postProcessor.addEffect(bloom);
			if (ExterminateGame.fxaa) {
				Fxaa fxaa = new Fxaa(Gdx.graphics.getWidth(),
						Gdx.graphics.getHeight());
				postProcessor.addEffect(fxaa);
				fxaa.setEnabled(true);
			}

			vignette.setIntensity(0.25f);
			bloom.setBloomIntesity(0.85f);

			bloom.setEnabled(true);
			vignette.setEnabled(true);
			// bloom.enableBlending( GL20.GL_SRC_COLOR,
			// GL20.GL_ONE_MINUS_SRC_COLOR );

			if (DEFFERED)
				fboDeffered = new DeferredGBuffer(
						(int) (width() * ExterminateGame.fsaa),
						(int) (height() * ExterminateGame.fsaa), true, false,
						postProcessor);
		}

	}

	float h = 1.5f;

	@Override
	public void resize(int width, int height) {

		ExterminateGame.resize();

		System.out.println("resized to +" + width);

		rebuildPostPro();

		cameraController = new CameraController(cam, player);
		Gdx.input.setInputProcessor(cameraController);

		cam = new PerspectiveCamera(80f, width(), height());
		cam.position.set(0f, 20f, 0f);
		cam.up.set(0, 1f, 0);
		cam.direction.set(0, 0, 1f);
		cam.near = 0.1f;
		cam.far = 3000f;
		cam.update();



		cam2D = new OrthographicCamera(width(), height());
		cam2D.setToOrtho(false);
		cam2D.near = 0f;
		cam2D.update();

		CameraController.camera = cam;
		mesh.setVertices(new float[] { 0, 0, 0, 1, 1, 1, 1, 0, 1, width(), 0,
				0, 1, 1, 1, 1, 1, 1, width(), height(), 0, 1, 1, 1, 1, 1, 0, 0,
				height(), 0, 1, 1, 1, 1, 0, 0 });

		Assets.billboardBatch.setCamera(cam);

		batcher = ExterminateGame.batcher;
		batcherFont = ExterminateGame.batcherFont;
		batcherFont.setShader(Assets.fontShader);

		batcherFont3D = new FontBatch(6);
		batcherFont3D.setShader(Assets.fontShader);

		try {
			cameraController.update(Gdx.graphics.getDeltaTime());
		} catch (Exception e) {

		}

	}

	@Override
	public void show() {

		Gdx.input.setInputProcessor(cameraController);
		CameraController.longTimeNoMove = false;
		CameraController.longTime = 0f;

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	private final static Pool<Vector3> vectorPool = new Pool<Vector3>() {
		@Override
		protected Vector3 newObject() {
			return new Vector3();
		}
	};

	private final static Vector3 tmpV = new Vector3();
	public static boolean showInfo = false;
	public static boolean wallhack = false;
	public static float rouge = 0f;

	public static World world;
	public static String save = "default";



	public static float width() {
		return Gdx.graphics.getWidth();
	}

	public static float height() {
		return Gdx.graphics.getHeight();
	}

}
