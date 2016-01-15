
varying vec3 v_normal;
uniform sampler2D u_diffuseTexture;
varying vec2 v_texCoords0;
varying vec3 v_color;
varying float depth;



void main() {
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0);
		if(diffuse.a<0.3) discard;

		gl_FragData[0].rgb = (diffuse.xyz);
		gl_FragData[1].rgb = (v_normal)*0.5+0.5;
		gl_FragData[2].rgb = (v_color.xyz);
		gl_FragData[3].r = depth;


}


