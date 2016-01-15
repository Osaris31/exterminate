#version 130




varying vec3 v_normal;

varying vec4 v_color;

varying vec2 v_texCoords0;

//uniform vec4 u_diffuseColor;

uniform float biome;
uniform sampler2DArray u_diffuseTexture;

varying vec3 v_lightDiffuse;



varying vec3 v_ambientLight;

uniform vec4 u_fogColor;
varying float v_fog;

void main() {
		vec3 normal = v_normal;
		
		float dirt = v_color.y;
		float sand = max(0.0, 1.0-v_color.x-v_color.y-v_color.z);
		
		vec4 diffuse = texture(u_diffuseTexture, vec3(v_texCoords0, biome)) * v_color.x;
		diffuse += texture(u_diffuseTexture, vec3(v_texCoords0, biome+1.0)) * sand;
		diffuse += texture(u_diffuseTexture, vec3(v_texCoords0, biome+2.0)) * dirt;
		diffuse += texture(u_diffuseTexture, vec3(v_texCoords0, biome+3.0)) * v_color.z;

		diffuse*=(v_color.a);

		gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + 1.0 * v_lightDiffuse));

	


		gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
		
		gl_FragColor.a = 1.0;


}
