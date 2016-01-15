
attribute vec3 a_position;    
attribute vec2 a_texCoord0;
uniform mat4 u_worldView;
      
varying vec2 v_texCoords0;
      

void main()                  
{                            
         gl_Position =  u_worldView * vec4(a_position, 1.0);
		v_texCoords0 = a_texCoord0;
		v_texCoords0.y = 1.0-v_texCoords0.y;
}
      