#version 130


uniform sampler2D u_diffuseTexture;

varying vec2 v_texCoords0;
uniform vec3 position;
uniform float intense;

void main() {

         vec3 color1 = texture2D(u_diffuseTexture, v_texCoords0 ).xyz;
         vec3 color2 = texture2D(u_diffuseTexture, v_texCoords0+vec2(0.0, 0.001)).xyz;
         vec3 color3 = texture2D(u_diffuseTexture, v_texCoords0+vec2(0.002, 0.0)).xyz;
         vec3 color4 = texture2D(u_diffuseTexture, v_texCoords0+vec2(0.002, 0.001)).xyz;
         vec3 color10 = texture2D(u_diffuseTexture, v_texCoords0+vec2(0.0, 0.0015)).xyz;
         vec3 color20 = texture2D(u_diffuseTexture, v_texCoords0+vec2(0.0, 0.0015)).xyz;
         vec3 color30 = texture2D(u_diffuseTexture, v_texCoords0+vec2(0.003, 0.0)).xyz;
         vec3 color40 = texture2D(u_diffuseTexture, v_texCoords0+vec2(0.003, 0.0015)).xyz;

gl_FragColor.rgb=color1*0.25+color2*0.25+color3*0.25+color4*0.25+color10*0.25+color20*0.25+color30*0.25+color40*0.25;
gl_FragColor.a = intense*gl_FragColor.b*0.35;
//gl_FragColor.rgb=max(gl_FragColor.rgb, vec3(1.0,0.95, 0.9));
gl_FragColor.rgb=vec3(1.0,1.0, 1.0);
}