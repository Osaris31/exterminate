#ifdef GL_ES
	precision float;
#endif


/// <summary>
/// Uniform variables.
/// <summary>
uniform vec2 TexelSize;
uniform sampler2D u_texture;

uniform int Orientation;
uniform int BlurAmount;
uniform float BlurScale;
uniform float BlurStrength;


/// <summary>
/// Varying variables.
/// <summary>
varying vec2 vTexCoord;


/// <summary>
/// Gets the Gaussian value in the first dimension.
/// </summary>
/// <param name="x">Distance from origin on the x-axis.</param>
/// <param name="deviation">Standard deviation.</param>
/// <returns>The gaussian value on the x-axis.</returns>
float Gaussian (float x, float deviation)
{
	return (1.0 / sqrt(2.0 * 3.141592 * deviation)) * exp(-((x * x) / (2.0 * deviation)));	
}


/// <summary>
/// Fragment shader entry.
/// <summary>
void main ()
{
	// Locals
	float halfBlur = float(BlurAmount) * 0.5;
	vec4 colour = vec4(0.0);
	vec4 texColour = vec4(0.0);
	
	// Gaussian deviation
	float deviation = halfBlur * 0.35;
	deviation *= deviation;
	float strength = 1.0 - BlurStrength;
	
	if ( Orientation == 0 )
	{
		// Horizontal blur
		for (int i = 0; i < 10; ++i)
		{
			if ( i >= BlurAmount )
				break;
			
			float offset = float(i) - halfBlur;
			texColour = texture2D(u_texture, vTexCoord + vec2(offset * TexelSize.x * BlurScale, 0.0)) * Gaussian(offset * strength, deviation);
			colour += texColour;
		}
	}
	else
	{
		// Vertical blur
		for (int i = 0; i < 10; ++i)
		{
			if ( i >= BlurAmount )
				break;
			
			float offset = float(i) - halfBlur;
			texColour = texture2D(u_texture, vTexCoord + vec2(0.0, offset * TexelSize.y * BlurScale)) * Gaussian(offset * strength, deviation);
			colour += texColour;
		}
	}
	
	// Apply colour
	gl_FragColor = clamp(colour, 0.0, 1.0);
	gl_FragColor.w = 1.0;
}