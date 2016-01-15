#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_color;
attribute vec4 a_color;

attribute vec3 a_position;  
attribute vec2 a_texCoord0;  
varying vec2 v_texCoords0;
varying vec2 TexCoords;
varying float ombre;
uniform float time;
uniform float eau;
uniform float jour;
uniform float soleil;
uniform float vague;
varying vec4 reflet;
varying vec4 pos;
varying float intreflet;
uniform vec3 u_cameraPosition;
uniform mat4 u_worldView;
uniform mat4 u_worldTrans;

uniform float invDistFog;
varying float v_fog;
varying float distance;

uniform vec3 DirectionalLightcolor;
uniform vec3 DirectionalLightdirection;
uniform vec3 u_ambientLight;

      void main()                  
      {                            
        vec4 b_position = u_worldTrans * vec4(a_position, 1.0);
		v_color = a_color;
         TexCoords = a_texCoord0;
         //v_texCoords0.x+=time*0.003;
         
         float timea=time*0.4;
         v_texCoords0 = vec2((a_position.x*0.02+cos(timea*0.055+a_position.z*0.06)*0.14+cos(timea*0.05+a_position.x*0.07+1.0)*0.15), (a_position.z*0.02+sin(timea*0.06+a_position.x*0.05)*0.16+sin(timea*0.04+a_position.z*0.04+0.5)*0.13));
          b_position.y = b_position.y-1.5+(cos(time*0.8+a_position.x+a_position.z)*0.7+cos(time*0.7+2.5+a_position.x*0.23+a_position.z*0.875)*0.85+cos(time*1.3+a_position.x*0.478+a_position.z*0.4125))*(vague+0.15-v_color.x*0.15)+0.5;
         float intenseRefl = (-sin(time*0.8+a_position.x+a_position.z)*0.7-0.875*sin(time*0.7+2.5+a_position.x*0.23+a_position.z*0.875)*0.85-0.4125*sin(time*1.3+a_position.x*0.478+a_position.z*0.4125))*0.5;
    
        vec3 rayDir = b_position.xyz - u_cameraPosition.xyz;
        distance = sqrt(dot(rayDir, rayDir));
  //      rayDir = rayDir/distance;
  //      float b1 = 1.0+invDistFog;
  //      float b2 = invDistFog;
  //      float c = 3.0;
  //      float fog = c * exp(-u_cameraPosition.y*b1) * (1.0-exp( -distance*rayDir.y*b2 ))/rayDir.y;
      float fog = 1. * (1.0-exp( -distance*invDistFog ));
        
        v_fog = min(fog*1.0, 0.9);                 
         // pour avoir la normale, on derive la position selon x puis y... ouh putin c'est chaud
      //   vec3 normal;
      //   normal.x = 0.0;
      //   normal.y = -1.0;
      //   normal.z = 0.0;
         
         gl_Position =  u_worldView * b_position;
         
         vec4 newPos = vec4(b_position.x, b_position.y, b_position.z, b_position.w);
		pos = u_worldView * newPos;
 
         

         ombre = min(1.0, DirectionalLightcolor.x+u_ambientLight.x)-(1.0-jour)*0.125;
         
         vec3 r = normalize(DirectionalLightdirection); //coordonnée du soleil
         
         // ray dir est la direction du reflet du rayon qui arrive depuis la camera.
         // donc pos-campos avec inversion de y
 			vec3 ray_dir = normalize(u_cameraPosition.xyz - b_position.xyz); //+2.0*normalize(normal.y);


		intreflet = 1.0-ray_dir.y*0.8;
		
		ray_dir.y=-ray_dir.y;

		float specular1 = 0.0;
		if(eau>0.0) {
		 specular1 += (0.2 * pow(max(0.0, dot(r, ray_dir)), u_cameraPosition.y*0.25))*(0.5+intenseRefl*0.4+b_position.y*0.4)*soleil*(-DirectionalLightdirection.y);
		ray_dir.y=-ray_dir.y;
		 specular1 += (0.4 * pow(max(0.0, dot(r, ray_dir)), 4.0))*(0.5+intenseRefl*0.4+b_position.y*0.4)*soleil*(-DirectionalLightdirection.y);
		}
		
		//vec3(1.5, 1.2, 0.6) *
         reflet = 1.15*specular1*vec4(1.1, (0.4-DirectionalLightdirection.y*0.6), (0.1-DirectionalLightdirection.y*0.9), 0.0);//DirectionalLightdirection est negatif mec
      }
      