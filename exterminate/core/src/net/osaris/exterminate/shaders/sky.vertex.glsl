attribute vec4 a_position;                                     
uniform mat4 u_M;                                              
uniform mat4 u_VP;                                             
uniform float invY;                                             
varying vec3 v_texCoord;     
                                  
void main()                                                    
{                                        

                     
   gl_Position = u_VP * u_M * a_position;
   v_texCoord = a_position.xyz;                                
   
   gl_Position.y = gl_Position.y;
   v_texCoord.y =  invY*v_texCoord.y;                                
   
}
