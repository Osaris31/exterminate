attribute vec3 a_position;
//uniform mat4 u_projViewTrans;

varying vec4 v_color;
attribute vec4 a_color;
attribute vec3 a_normal;
//uniform mat3 u_normalMatrix;
varying vec3 v_normal;

attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;


uniform mat4 u_projViewTrans;


varying vec3 v_lightDiffuse;

uniform vec3 u_ambientLight;


uniform vec3 u_cameraPosition;

varying float v_fog;

uniform vec3 DirectionalLightcolor;
uniform vec3 DirectionalLightdirection;

uniform mat4 u_shadowMapProjViewTrans;
varying vec3 v_shadowMapUv;

varying vec3 v_ambientLight;


void main() {
		v_texCoords0 = a_texCoord0;
		v_color = a_color;
		vec4 pos = vec4(a_position, 1.0);
		pos.y = 38.4-(pos.y-38.4);
	gl_Position = u_projViewTrans * pos;
		
		vec4 spos = u_shadowMapProjViewTrans * pos;
		v_shadowMapUv.xy = (spos.xy / spos.w) * 0.5 + 0.5;
		v_shadowMapUv.z = min(spos.z * 0.5 + 0.5, 0.998);
		
//		vec3 normal = normalize(u_normalMatrix * a_normal);
		v_normal = a_normal;

        vec3 flen = u_cameraPosition.xyz - pos.xyz;
        float fog = dot(flen, flen) * 0.000000181;
        v_fog = min(fog*1.5, 0.9);

        	vec3 ambientLight = u_ambientLight;

				v_ambientLight = ambientLight;
				v_lightDiffuse = vec3(0.0);
			

				vec3 lightDir = -DirectionalLightdirection;
				float NdotL = clamp(dot(a_normal, lightDir), 0.0, 1.0);
				vec3 value = DirectionalLightcolor * NdotL;
				v_lightDiffuse += value;

	//	v_color.a = v_color.a + (1.0-v_color.a)*(0.5-v_lightDiffuse.x);
		
}
