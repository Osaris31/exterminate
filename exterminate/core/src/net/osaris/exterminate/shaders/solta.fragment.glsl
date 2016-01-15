#version 130



varying vec3 v_normal;

varying vec4 v_color;

varying vec2 v_texCoords0;

//uniform vec4 u_diffuseColor;

uniform float biome;
uniform sampler2DArray u_diffuseTexture;

varying vec3 v_lightDiffuse;


uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;


float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 160581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));	
}

float getShadow() 
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 160581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy), bitShifts));//+(1.0/255.0));	
}

float getShadow4() 
{
	return (//getShadowness(vec2(0.0,0.0)) + 
			getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}

//float getShadow64() 
//{
//float sum = 0.0;
//float x, y;

//for (y = -3.5; y <= 3.5; y += 1.0)
//  for (x = -3.5; x <= 3.5; x += 1.0)
//    sum += getShadowness(vec2(x*u_shadowPCFOffset, y*u_shadowPCFOffset));


//	return sum / 64.0;
//}
float getShadow1() 
{
float sum = 0.0;
float x, y;

for (y = -1.5; y <= 1.5; y += 1.0)
  for (x = -1.5; x <= 1.5; x += 1.0)
    sum += getShadowness(vec2(x*u_shadowPCFOffset*2.0, y*u_shadowPCFOffset*2.0));


	return sum / 16.0;
}

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
		
		float shadow = getShadow();
		
		diffuse*=(v_color.a);

		gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + shadow * v_lightDiffuse));
	//	gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + 1.0 * v_lightDiffuse));
	//			gl_FragColor += texture2D(u_shadowTexture, v_shadowMapUv.xy);
	


		gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
		
		gl_FragColor.a = 1.0;


}
