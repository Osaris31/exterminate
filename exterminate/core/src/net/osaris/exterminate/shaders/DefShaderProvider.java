package net.osaris.exterminate.shaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public class DefShaderProvider extends BaseShaderProvider {
	public final DefShader.Config config;

	public DefShaderProvider (final DefShader.Config config) {
		this.config = (config == null) ? new DefShader.Config() : config;
	}

	public DefShaderProvider (final String vertexShader, final String fragmentShader) {
		this(new DefShader.Config(vertexShader, fragmentShader));
	}

	public DefShaderProvider (final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}

	public DefShaderProvider () {
		this(null);
	}

	@Override
	protected Shader createShader (final Renderable renderable) {
		return new DefShader(renderable, config);
	}
}
