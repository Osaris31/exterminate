package net.osaris.exterminate.world;

import java.nio.IntBuffer;

import net.osaris.exterminate.ExterminateGame;
import net.osaris.exterminate.InGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

public class Skybox implements Disposable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static ShaderProgram skyboxShader;
	public static ShaderProgram skyboxShaderDeffered;


	private static final int SKYBOX_TEXTURE_UNIT = 0;
	private static final int SKYBOX_TEXTURE_ACTIVE_UNIT = GL20.GL_TEXTURE0
			+ SKYBOX_TEXTURE_UNIT;


	private static final float ONE = 1f;

	private final static int QUAD_LENGTH = 4;

	// x, y, z
	private final static float[] VERTICES = { //
		-ONE, -ONE, -ONE, //
			ONE, -ONE, -ONE, //
			-ONE, ONE, -ONE, //
			ONE, ONE, -ONE, //
			-ONE, -ONE, ONE, //
			ONE, -ONE, ONE, //
			-ONE, ONE, ONE, //
			ONE, ONE, ONE };

	private final static float[] VERTICESDEMI = { //
		-ONE, -0.1f, -ONE, //
			ONE, -0.1f, -ONE, //
			-ONE, ONE, -ONE, //
			ONE, ONE, -ONE, //
			-ONE, -0.1f, ONE, //
			ONE, -0.1f, ONE, //
			-ONE, ONE, ONE, //
			ONE, ONE, ONE };

	// QUADS drawn with TRIANGLE_FAN
	private final static int[] INDICES = { 5, 1, 7, 3, // positive x
			4, 0, 6, 2, // 0, 4, 2, 6, // negative x
			4, 5, 6, 7, // positive y
			1, 0, 3, 2, // negative y
			0, 1, 4, 5, // positive z
			3, 2, 7, 6 // negative z
	};

	private final static short[] ORDERED_INDICES = { 0, 1, 2, 3 };

	// ===========================================================
	// Fields
	// ===========================================================

	public static int textureId;
	public static int textureNightId;

	private Mesh[] meshes;

	public Matrix4 model;
	boolean useDemi = false;
	private Vector3 tmpv = new Vector3();

	// ===========================================================
	// Constructors
	// ===========================================================

	public Skybox(Pixmap[] pixmaps) {
		if(skyboxShader==null) {

			skyboxShader = new ShaderProgram(Gdx.files.internal("net/osaris/exterminate/shaders/sky.vertex.glsl").readString(), Gdx.files
					.internal(ExterminateGame.useGL3 ? "net/osaris/exterminate/shaders/sky.fragmentta.glsl" : "net/osaris/exterminate/shaders/sky.fragment.glsl").readString());
			if (skyboxShader.isCompiled() == false) {
				throw new RuntimeException("Could not load skybox shader: "
						+ skyboxShader.getLog());
			}

			if(ExterminateGame.useGL3 ) {
				skyboxShaderDeffered = new ShaderProgram(Gdx.files.internal("net/osaris/exterminate/shaders/sky.vertex.glsl").readString(), Gdx.files
						.internal("net/osaris/exterminate/shaders/skydeffered.fragment.glsl").readString());
				if (skyboxShaderDeffered.isCompiled() == false) {
					throw new RuntimeException("Could not load skybox deffered shader: "
							+ skyboxShaderDeffered.getLog());
				}
				
			}
		}
		

		this.meshes = new Mesh[6];
		for (int i = 0; i < 6; i++) {
			Mesh mesh = new Mesh(true, QUAD_LENGTH * 3, QUAD_LENGTH,
					VertexAttribute.Position());
			mesh.setVertices(getVertices(i));
			mesh.setIndices(ORDERED_INDICES);
			meshes[i] = mesh;
		}

		// Generate a texture object
		IntBuffer buffer = BufferUtils.newIntBuffer(2);
		buffer.position(0);
		buffer.limit(buffer.capacity());
		Gdx.gl20.glGenTextures(2, buffer);
		textureId = buffer.get(0);
		textureNightId = buffer.get(1);

		// Bind the texture object
		Gdx.gl20.glActiveTexture(SKYBOX_TEXTURE_ACTIVE_UNIT);
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, textureId);

		// Set the filtering mode
		// cf. http://www.opengl.org/sdk/docs/man/xhtml/glTexParameter.xml
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP,
				GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP,
				GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);

		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP,
				GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP,
				GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
		// Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP,
		// GL20.GL_TEXTURE_WRAP_R, GL20.GL_CLAMP_TO_EDGE);

		// Load cube faces
		for (int i = 0; i < 6; i++) {
			glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, pixmaps[i]);
		}


		// Create model matrix for rendering
		model = new Matrix4();

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void dispose() {
		for (Mesh mesh : meshes) {
			mesh.dispose();
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void render(Camera camera, float jour, boolean invY) {
		this.render(camera, jour, invY, skyboxShader);
	}
	
	public void render(Camera camera, float jour, boolean invY, ShaderProgram shader) {

		
		Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
		shader.begin();
		

		// Bind the texture
		Gdx.gl20.glActiveTexture(SKYBOX_TEXTURE_ACTIVE_UNIT);
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, textureId);

		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);


		model.setToTranslation(camera.position);
//		model.translate(0, -0.06f*(1.0f-jour), 0);
	//	model.scale(0, -1f, 0);
		if(InGame.vitesseDay<0.02) {
			model.rotate(Vector3.Y, InGame.timeInDay/24f*360f);				
		}
		
	//	model.rotate(camera.up.cpy().crs(camera.direction), -InGame3D.decAngleYInfini );
	//	model.rotate(camera.up, -InGame3D.decAngleInfini );	
	//	model.rotate( 0.0f, 0.0f, 1.0f, angleZ );*/
//		
		shader.setUniformMatrix("u_M", model);
		
		shader.setUniformMatrix("u_VP", camera.combined);
		shader.setUniformi("u_sampler", SKYBOX_TEXTURE_UNIT);
		if(shader==skyboxShaderDeffered) {
			System.out.println("aller :p");
			tmpv.set(InGame.sunDirection.x/InGame.width(), InGame.sunDirection.y/InGame.height(), 0);
			shader.setUniformf("position",tmpv);

			
			shader.setUniformf("resolution", InGame.sizev2);
		}
		
		shader.setUniformf("jour", jour);
		shader.setUniformf("invY", invY ? -1.0f : 1.0f);
		shader.setUniformf("minfog", 1.0f-1.0f/Math.max(1f, InGame.fogAmount*0.7f));
		shader.setUniformf("u_fogColor",InGame.solFog);

		for (Mesh mesh : meshes) {
			mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
		}
		shader.end();
		
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
	}
	
	private void glTexImage2D(int textureCubeMapIndex, Pixmap pixmap) {
		// cf. http://www.opengl.org/sdk/docs/man/xhtml/glTexImage2D.xml
		Gdx.gl20.glTexImage2D(textureCubeMapIndex, 0,
				pixmap.getGLInternalFormat(), pixmap.getWidth(),
				pixmap.getHeight(), 0, pixmap.getGLFormat(),
				pixmap.getGLType(), pixmap.getPixels());
	}

	private float[] getVertices(int indexNb) {
		float[] vertices = new float[3 * QUAD_LENGTH];
		for (int i = 0; i < QUAD_LENGTH; i++) {
			int offset = INDICES[QUAD_LENGTH * indexNb + i];
			vertices[3 * i] = useDemi ? VERTICESDEMI[3 * offset] : VERTICES[3 * offset]; // X
			vertices[3 * i + 1] = useDemi ? VERTICESDEMI[3 * offset +1] : VERTICES[3 * offset + 1]; // Y
			vertices[3 * i + 2] = useDemi ? VERTICESDEMI[3 * offset +2] : VERTICES[3 * offset + 2]; // Z
		}
		return vertices;
	}

	public int getCubeMapTextureUnit() {
		return SKYBOX_TEXTURE_UNIT;
	}
	
	
	public static Pixmap[] makeSkybox(String string) {
		Pixmap big = new Pixmap( // right
				Gdx.files.internal("data/textures/" + string));

		Pixmap[] texturesSkyBoxNight = new Pixmap[6];
		int res = big.getHeight() / 3;

		texturesSkyBoxNight[0] = new Pixmap(res, res, Format.RGB888);
		texturesSkyBoxNight[0].drawPixmap(big, 0, 0, res, res, res, res);
		texturesSkyBoxNight[1] = new Pixmap(res, res, Format.RGB888);
		texturesSkyBoxNight[1].drawPixmap(big, 0, 0, res * 3, res, res, res);
		texturesSkyBoxNight[2] = new Pixmap(res, res, Format.RGB888);
		texturesSkyBoxNight[2].drawPixmap(big, 0, 0, res, 0, res, res);
		texturesSkyBoxNight[3] = new Pixmap(res, res, Format.RGB888);
		texturesSkyBoxNight[3].drawPixmap(big, 0, 0, res, res * 2, res, res);
		texturesSkyBoxNight[4] = new Pixmap(res, res, Format.RGB888);
		texturesSkyBoxNight[4].drawPixmap(big, 0, 0, res * 2, res, res, res);
		texturesSkyBoxNight[5] = new Pixmap(res, res, Format.RGB888);
		texturesSkyBoxNight[5].drawPixmap(big, 0, 0, 0, res, res, res);

		texturesSkyBoxNight[0] = flipPixmap(texturesSkyBoxNight[0]);
		texturesSkyBoxNight[1] = flipPixmap(texturesSkyBoxNight[1]);
		texturesSkyBoxNight[2] = rotatePixmap(texturesSkyBoxNight[2]);
		texturesSkyBoxNight[3] = rotatePixmap(texturesSkyBoxNight[3]);
		texturesSkyBoxNight[4] = flipPixmap(texturesSkyBoxNight[4]);
		texturesSkyBoxNight[5] = flipPixmap(texturesSkyBoxNight[5]);

		return texturesSkyBoxNight;
	}

	public static Pixmap flipPixmap(Pixmap src) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		Pixmap flipped = new Pixmap(width, height, src.getFormat());

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				flipped.drawPixel(x, y, src.getPixel(width - x - 1, y));
			}
		}
		src.dispose();
		return flipped;
	}

	private static Pixmap rotatePixmap(Pixmap srcPix) {
		final int width = srcPix.getWidth();
		final int height = srcPix.getHeight();
		Pixmap rotatedPix = new Pixmap(height, width, srcPix.getFormat());

		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				rotatedPix.drawPixel(x, y, srcPix.getPixel(y, x));
			}
		}

		srcPix.dispose();
		return rotatedPix;
	}



}
