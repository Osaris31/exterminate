#version 130

#ifdef GL_ES
precision float;
#endif
                                                               
varying vec3 v_texCoord;                                       
uniform samplerCube u_sampler;                                 
uniform float jour;                
uniform float minfog;                
uniform float invY;                                             

uniform vec4 u_fogColor;
                 
void main()                                                    
{                                                              
    gl_FragColor = texture(u_sampler, v_texCoord);
    
  //  float coef = (1.0-abs(v_texCoord.y))/(1.0+abs(v_texCoord.x)*0.25)/(1.0+abs(v_texCoord.z)*0.25)*1.25;

                         // on limite pas en dessous de y=0 
    float coef = min(0.9-max(-0.1, min(0.0, v_texCoord.y-0.15)), max(1.0-max(0.0, (v_texCoord.y-0.15)*4.5)+0.1, 0.0));
  coef = coef*coef;
  
  
  coef = max(minfog, coef*(1.0-jour*0.5));//*(1.0+invY)*0.5
    
    gl_FragColor.xyz = gl_FragColor.xyz*(1.0-coef)*0.85+u_fogColor.rgb*coef;
             
    gl_FragColor.a = 1.0;         
}                                       
