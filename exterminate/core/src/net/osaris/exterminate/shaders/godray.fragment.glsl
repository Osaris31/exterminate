#version 130
//!\\ Specific Licence for this file: those specifics godrays are an important part of the graphic style of my game, do not use it on your own.


uniform sampler2D u_diffuseTexture;
uniform sampler2D u_perlinTexture;

varying vec2 v_texCoords0;
uniform vec3 position;



float when_eq(float x, float y) {
  return 1.0 - abs(sign(x - y));
}

void main() {
	

vec2 lightPositionOnScreen = vec2(position.x, position.y);

	vec3 godray = vec3(0.0, 0.0, 0.0);
     	vec2 textCoo = v_texCoords0;
     vec3 depart = texture2D(u_diffuseTexture, textCoo ).xyz;
    		vec3 perlin = (texture2D(u_perlinTexture, textCoo*4.0 ).xyz-vec3(0.5, 0.5, 0.5))*0.001;
			textCoo +=perlin.xy;
	vec2 deltaTextCoord = vec2( textCoo - lightPositionOnScreen.xy );
    	
    	float illuminationDecay = 1.0;

float density = 1.0;
float weight = 0.5+perlin.x;//-0.25*depart.z;
float decay = 0.97+perlin.y;//-0.05*depart.z;

	deltaTextCoord *= (1.0+perlin.z) /  float(80) * density;
	
    	for(int i=0; i < 80 ; i++)
        {
                 textCoo -= deltaTextCoord;
                 vec3 gbuf = texture2D(u_diffuseTexture, textCoo ).xyz;
                
                 vec3 sample = gbuf.x*vec3(1.0, 0.95, 0.9);//vec3(1.0, 0.71, 0.55)
			
                 sample *= illuminationDecay * (weight);

                 godray += sample;

                 illuminationDecay *= decay;//+0.1*(1.0-decay)*gbuf.z;
         }
         gl_FragColor.rgb = godray*0.55;
         
         
         // pour inspecter le gbuffer
  //       vec3 gbuf = texture2D(u_diffuseTexture, v_texCoords0 ).xyz;
  //        gl_FragColor.rgb =  gl_FragColor.rgb*0.00000001+gbuf;
          
          
          gl_FragColor.a = 1.0;
}
