package net.osaris.exterminate.shaders;

import net.osaris.exterminate.InGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;

public class ModelShader extends DefaultShader {
	public static class Config extends DefaultShader.Config {
		public boolean depthBufferOnly = false;
		public float defaultAlphaTest = 0.5f;

		public Config () {
			super();
			defaultCullFace = GL20.GL_BACK;
			vertexShader = Gdx.files.classpath("net/osaris/exterminate/shaders/default.vertex.glsl").readString();
			fragmentShader = Gdx.files.classpath("net/osaris/exterminate/shaders/default.fragment.glsl").readString();
		}

		public Config (String vertexShader, String fragmentShader) {
			super(vertexShader, fragmentShader);
		}
	}

	public ModelShader(Renderable renderable) {
		super(renderable);
	}
	

	public ModelShader(Renderable renderable, Config config) {
		super(renderable, config);
	}
	@Override
	public void begin (Camera camera, RenderContext context) {
		super.begin(camera, context);
		program.setUniformf("invDistFog", InGame.invDistFog);
		program.setUniformf("u_fogColor", InGame.solFog);
		// Gdx.gl20.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
		// Gdx.gl20.glPolygonOffset(2.f, 100.f);
	}

	@Override
	public void end () {
		super.end();
		// Gdx.gl20.glDisable(GL20.GL_POLYGON_OFFSET_FILL);
	}

}

