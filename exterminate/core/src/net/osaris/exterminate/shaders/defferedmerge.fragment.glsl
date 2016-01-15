#version 130


uniform sampler2D u_diffuseTexture;
//uniform sampler2D u_normalTexture;
uniform sampler2D u_attribsTexture;
uniform sampler2D u_depthTexture;

varying vec2 v_texCoords0;


uniform vec3 DirectionalLightcolor;
uniform float invDistFog;
uniform vec3 u_ambientLight;
uniform vec3 u_lightSpecular;
uniform vec3 u_cameraPosition;
uniform vec4 u_fogColor;

uniform mat4 InvProjectionMatrix;


uniform mat4 u_shadowMapProjViewTrans;
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
uniform float u_shadowResolution;

float getShadowness(vec3 v_shadowMapUv, vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 160581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));	
}

float getShadow(vec3 v_shadowMapUv) 
{
	return (//getShadowness(v_shadowMapUv, vec2(0.0,0.0)) + 
			getShadowness(v_shadowMapUv, vec2(u_shadowPCFOffset, u_shadowPCFOffset))*fract(v_shadowMapUv.x*u_shadowResolution)*fract(v_shadowMapUv.y*u_shadowResolution) +
			getShadowness(v_shadowMapUv, vec2(0, u_shadowPCFOffset))*(1.0-fract(v_shadowMapUv.x*u_shadowResolution))*fract(v_shadowMapUv.y*u_shadowResolution) +
			getShadowness(v_shadowMapUv, vec2(u_shadowPCFOffset, 0))*fract(v_shadowMapUv.x*u_shadowResolution)*(1.0-fract(v_shadowMapUv.y*u_shadowResolution)) +
			getShadowness(v_shadowMapUv, vec2(0, 0))*(1.0-fract(v_shadowMapUv.x*u_shadowResolution))*(1.0-fract(v_shadowMapUv.y*u_shadowResolution)));
}



float getShadowBof(vec3 v_shadowMapUv) 
{
	return (//getShadowness(v_shadowMapUv, vec2(0.0,0.0)) + 
			getShadowness(v_shadowMapUv, vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(v_shadowMapUv, vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(v_shadowMapUv, vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
			getShadowness(v_shadowMapUv, vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}


vec3 positionFromDepth(float depth) {
    vec4 pos = vec4( v_texCoords0 * 2.0 - 1.0, depth, 1.0 );
    pos = InvProjectionMatrix * pos;
    pos.w = 1.0/pos.w;
    pos *= pos.w;
 
    return pos.xyz;
}

float when_eq(float x, float y) {
  return 1.0 - abs(sign(x - y));
}

void main() {
		vec3 diffuse = texture(u_diffuseTexture, v_texCoords0).xyz;
		vec3 color = texture(u_attribsTexture, v_texCoords0).xyz*2.0;

		float depth = texture(u_depthTexture, v_texCoords0).x;
		vec3 pos = positionFromDepth(depth);
//		vec3 normal = texture(u_normalTexture, v_texCoords0).xyz*2.0-1.0;
		
        vec3 rayDir = pos.xyz - u_cameraPosition.xyz;
        float distance = sqrt(dot(rayDir, rayDir));
  //      float distance = depth;
        float fog = 1. * (1.0-exp( -distance*invDistFog ));
		
		vec4 spos = u_shadowMapProjViewTrans * vec4(pos, 1.0);
	vec3 v_shadowMapUv;
	vec2 shpos = (spos.xy / spos.w);
		v_shadowMapUv.xy = shpos * 0.5 + 0.5;
		v_shadowMapUv.z = min(spos.z * 0.5 + 0.5, 0.998);

	float coefombrage = 0.7-min(shpos.x*shpos.x+shpos.y*shpos.y, 0.7);
		gl_FragColor.rgb = mix((diffuse.rgb * (u_ambientLight * color.r + (getShadow(v_shadowMapUv)*coefombrage+1.0-coefombrage) * color.g * DirectionalLightcolor)) + u_lightSpecular*color.b
									, u_fogColor.rgb, min(fog*1.0, 0.9));
		gl_FragColor.a = 1.0;



}
