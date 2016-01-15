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
uniform float invDistFog;
uniform float time;

varying float v_fog;

uniform vec3 DirectionalLightcolor;
uniform vec3 DirectionalLightdirection;
uniform mat4 u_worldTrans;

uniform mat4 u_shadowMapProjViewTrans;
varying vec3 v_shadowMapUv;

varying vec3 v_ambientLight;


void main() {
		v_texCoords0 = a_texCoord0;
		v_color = a_color;
		
		// a mettre en option caustiques
//		vec3 b_position = a_position;
  //       b_position.x = a_position.x+max(0.0, 30.0-a_position.y)*0.1*(cos(time*0.88+a_position.y+a_position.z)*0.7+cos(time*0.71+2.3+a_position.y*1.23+a_position.z*0.875)*0.85+cos(time*0.3+a_position.y*0.478+a_position.z*0.4125))*0.1;
    //     b_position.z = a_position.z+max(0.0, 30.0-a_position.y)*0.1*(cos(time*0.83+a_position.x+a_position.y)*0.7+cos(time*0.77+2.6+a_position.x*1.23+a_position.y*0.875)*0.85+cos(time*0.32+a_position.x*0.478+a_position.y*0.4125))*0.075;
		//vec4 pos = vec4(b_position, 1.0);
		
		
		
		vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	gl_Position = u_projViewTrans * pos;
		
		vec4 spos = u_shadowMapProjViewTrans * pos;
		v_shadowMapUv.xy = (spos.xy / spos.w) * 0.5 + 0.5;
		v_shadowMapUv.z = min(spos.z * 0.5 + 0.5, 0.998);
		
//		vec3 normal = normalize(u_normalMatrix * a_normal);
		v_normal = a_normal;

   //     vec3 flen = u_cameraPosition.xyz - pos.xyz;
   //     float dist = sqrt(dot(flen, flen));
    //    float fog = (1.0-exp(-dist * invDistFog))/max(1.0-flen.y/dist, 1.0)*2.0;
        
        vec3 rayDir = pos.xyz - u_cameraPosition.xyz;
        float distance = sqrt(dot(rayDir, rayDir));
  //      rayDir = rayDir/distance;
  //      float b1 = 1.0+invDistFog;
  //      float b2 = invDistFog;
  //      float c = 3.0;
  //      float fog = c * exp(-u_cameraPosition.y*b1) * (1.0-exp( -distance*rayDir.y*b2 ))/rayDir.y;
      float fog = 1. * (1.0-exp( -distance*invDistFog ));
        
        v_fog = min(fog*1.0, 0.9);
        // pour brouillard au sol, *(40.0/max(40.0, a_position.y))

        	vec3 ambientLight = u_ambientLight;

				v_ambientLight = ambientLight;
				v_lightDiffuse = vec3(0.0);
			

				vec3 lightDir = -DirectionalLightdirection;
				float NdotL = clamp(dot(a_normal, lightDir), 0.0, 1.0);
				vec3 value = DirectionalLightcolor * NdotL;
				v_lightDiffuse += value;

	//	v_color.a = v_color.a + (1.0-v_color.a)*(0.5-v_lightDiffuse.x);
		
}
