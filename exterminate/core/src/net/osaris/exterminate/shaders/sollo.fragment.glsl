#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision float;
#else
#define MED
#define LOWP
#define HIGH
#endif


varying vec3 v_normal;

varying vec4 v_color;

varying MED vec2 v_texCoords0;

//uniform vec4 u_diffuseColor;

uniform sampler2D u_diffuseTexture;
uniform sampler2D u_diffuseTextureRock;
uniform sampler2D u_diffuseTextureSand;
uniform sampler2D u_diffuseTextureDirt;

varying vec3 v_lightDiffuse;



varying vec3 v_ambientLight;

uniform vec4 u_fogColor;
varying float v_fog;

void main() {
		vec3 normal = v_normal;
		
		float dirt = v_color.y;
		float sand = max(0.0, 1.0-v_color.x-v_color.y-v_color.z);
		
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0) * v_color.x;
		diffuse += texture2D(u_diffuseTextureSand, v_texCoords0) * sand;
		diffuse += texture2D(u_diffuseTextureDirt, v_texCoords0) * dirt;
		diffuse += texture2D(u_diffuseTextureRock, v_texCoords0) * v_color.z;

		diffuse*=(v_color.a);

		gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + 1.0 * v_lightDiffuse));

	


		gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
		
		gl_FragColor.a = 1.0;


}
