#version 130

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

uniform float invY;
uniform float hauteurEau;


varying vec3 v_lightDiffuse;

uniform vec3 u_ambientLight;


uniform vec3 u_cameraPosition;
uniform mat4 u_worldTrans;

uniform float invDistFog;
varying float v_fog;

uniform vec3 DirectionalLightcolor;
uniform vec3 DirectionalLightdirection;

varying vec3 v_ambientLight;

void main() {
		v_texCoords0 = a_texCoord0;
		v_color = a_color;
		vec4 pos = u_worldTrans * vec4(a_position, 1.0);
		
		
//	gl_ClipDistance[0] = dot(ModelMatrix * a_position, Plane);
gl_ClipDistance[0] = (pos.y+hauteurEau*invY);


		pos.y=(pos.y-hauteurEau)*invY+hauteurEau;

		
		
	gl_Position = u_projViewTrans * pos;
		
//#ifdef __GLSL_CG_DATA_TYPES
//gl_ClipVertex = gl_Position;
//#endif
		
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

        	vec3 ambientLight = u_ambientLight;

				v_ambientLight = ambientLight;
				v_lightDiffuse = vec3(0.0);
			

				vec3 lightDir = -DirectionalLightdirection;
				float NdotL = clamp(dot(a_normal, lightDir), 0.0, 1.0);
				vec3 value = DirectionalLightcolor * NdotL;
				v_lightDiffuse += value;

	//	v_color.a = v_color.a + (1.0-v_color.a)*(0.5-v_lightDiffuse.x);
		
}
