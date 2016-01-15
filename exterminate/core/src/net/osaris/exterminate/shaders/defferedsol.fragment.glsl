#version 130


varying vec3 v_normal;

uniform sampler2D u_diffuseTexture;
uniform sampler2D u_diffuseTextureRock;
uniform sampler2D u_diffuseTextureSand;
uniform sampler2D u_diffuseTextureDirt;
varying vec2 v_texCoords0;
varying vec3 v_color;
varying vec4 v_acolor;
varying float depth;
uniform float biome;



void main() {


		float dirt = v_acolor.y;
		float sand = max(0.0, 1.0-v_acolor.x-v_acolor.y-v_acolor.z);
		
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0) * v_acolor.x;
		diffuse += texture2D(u_diffuseTextureSand, v_texCoords0) * sand;
		diffuse += texture2D(u_diffuseTextureDirt, v_texCoords0) * dirt;
		diffuse += texture2D(u_diffuseTextureRock, v_texCoords0) * v_acolor.z;

		

		gl_FragData[0].rgb = (diffuse.xyz);
		gl_FragData[1].rgb = (v_normal)*0.5+0.5;
		gl_FragData[2].rgb = (v_color.xyz);
		gl_FragData[3].r = depth;


}
