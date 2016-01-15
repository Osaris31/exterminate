attribute vec3 a_position;


uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;
#endif

attribute vec2 a_boneWeight0;
attribute vec2 a_boneWeight1;
uniform mat4 u_bones[17];

varying float v_depth;

void main() {
	#ifdef blendedTextureFlag
		v_texCoords0 = a_texCoord0;
	#endif // blendedTextureFlag
	
		mat4 skinning = mat4(0.0);
			skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];		
			skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];		
		
		
		vec4 pos = u_projViewTrans * u_worldTrans * skinning * vec4(a_position, 1.0);
		
			
	v_depth = pos.z * 0.5 + 0.5;

	gl_Position = pos;
}