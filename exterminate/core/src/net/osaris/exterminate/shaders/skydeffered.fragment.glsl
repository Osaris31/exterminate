#version 130

#ifdef GL_ES
precision float;
#endif
                                                               
varying vec3 v_texCoord;                                       
uniform samplerCube u_sampler;                                 
uniform samplerCube u_samplerNight;                                 
uniform float jour;                
uniform float minfog;                
uniform float invY;                                             

uniform vec4 u_fogColor;
uniform vec2 resolution;
uniform vec3 position;
           

float packColor(vec3 color) {
    return floor(color.r * 255.9) + floor(color.g * 255.9) * 256.0 + floor(color.b * 255.9) * 256.0 * 256.0;
}      
void main()                                                    
{                                                              
    vec4 diffuse = texture(u_sampler, v_texCoord)*jour+((texture(u_samplerNight, v_texCoord)-0.5)*0.75+0.36)*(1.0-jour);
    
  //  float coef = (1.0-abs(v_texCoord.y))/(1.0+abs(v_texCoord.x)*0.25)/(1.0+abs(v_texCoord.z)*0.25)*1.25;

                         // on limite pas en dessous de y=0 
    float coef = min(0.9-max(-0.1, min(0.0, v_texCoord.y-0.15)), max(1.0-max(0.0, (v_texCoord.y-0.15)*4.5)+0.1, 0.0));
  coef = coef*coef;
  
  
  coef = max(minfog, coef*(1.0-jour*0.5));//*(1.0+invY)*0.5
    
    diffuse.xyz=(diffuse.xyz*(1.0-coef)*0.85+u_fogColor.rgb*coef)*0.005;
    
    vec2 uv = (gl_FragCoord.xy / resolution.xy);
    uv.y = 1.0-uv.y;

  uv -= vec2(position.x, 1.0-position.y);
  uv.x*=resolution.x/resolution.y;
 
 float disc_radius = 0.05;
 float border_size = 0.03;
 
  float dist = sqrt(dot(uv, uv));
  float t = smoothstep(disc_radius+border_size, disc_radius-border_size, dist);
  diffuse.xyz+= vec3(1.0, 0.8, 0.6)*t;
    diffuse.x= clamp(diffuse.x, 0.0, 1.0);
    diffuse.y= diffuse.x;
    diffuse.z= diffuse.x;
    
    gl_FragData[0].rgb = diffuse.xyz;
   	gl_FragData[1].rgb = vec3(1.0, 0.0, 0.0);
	gl_FragData[2].rgb = vec3(1.0, 0.0, 0.0);
	gl_FragData[3].x = 0.0;
}
