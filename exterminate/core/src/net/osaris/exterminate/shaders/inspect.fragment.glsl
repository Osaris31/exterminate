#version 130


uniform sampler2D u_diffuseTexture;

varying vec2 v_texCoords0;

void main() {

         vec3 color1 = texture2D(u_diffuseTexture, v_texCoords0 ).xyz;

		gl_FragColor.rgb=1.0/(1.0-color1)*0.001;
}
