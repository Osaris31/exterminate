
attribute vec3 a_position;


uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;



varying float v_depth;

void main() {


		vec4 pos = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
		
			
	v_depth = pos.z * 0.5 + 0.5;

	gl_Position = pos;
}
