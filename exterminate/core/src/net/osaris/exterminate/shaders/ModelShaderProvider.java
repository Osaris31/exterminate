package net.osaris.exterminate.shaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public class ModelShaderProvider extends BaseShaderProvider {
	public final ModelShader.Config config;

	public ModelShaderProvider (final ModelShader.Config config) {
		this.config = (config == null) ? new ModelShader.Config() : config;
	}

	public ModelShaderProvider (final String vertexShader, final String fragmentShader) {
		this(new ModelShader.Config(vertexShader, fragmentShader));
	}

	public ModelShaderProvider (final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}

	public ModelShaderProvider () {
		this(null);
	}

	@Override
	protected Shader createShader (final Renderable renderable) {
		return new ModelShader(renderable, config);
	}
}
