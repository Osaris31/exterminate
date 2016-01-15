uniform sampler2D u_diffuseTexture;
uniform sampler2D u_dudvTexture;
uniform sampler2D u_diffuseRefletTexture;
uniform float soleil;
varying vec2 v_texCoords0;
varying float ombre;
varying vec4 reflet;
varying vec4 v_color;
varying vec4 pos;
varying float intreflet;
uniform float time;
varying vec2 TexCoords;
varying float distance;
uniform vec4 u_fogColor;
varying float v_fog;
void main ()
{
   vec4 tmpvar_1;
  tmpvar_1 = (texture2D (u_diffuseTexture, v_texCoords0) * ombre);
  gl_FragColor = tmpvar_1;
   vec4 tmpvar_2;
  tmpvar_2 = texture2D (u_diffuseRefletTexture, clamp (((
    ((pos * vec4((1.0/(pos.w)))) + vec4(1.0, 1.0, 1.0, 1.0))
   * vec4(0.5, 0.5, 0.5, 0.5)) + (
    normalize(((texture2D (u_dudvTexture, (TexCoords + 
      (texture2D (u_dudvTexture, (TexCoords + vec2((time * 0.005)))) * 0.035)
    .xy)) * 2.0) - 1.0))
   * 
    max (0.007, (0.04 - (0.0002 * distance)))
  )), 0.001, 0.999).xy);
  float cse_3;
  cse_3 = (intreflet * 0.35);
  gl_FragColor.xyz = mix (tmpvar_2.xyz, gl_FragColor.xyz, (0.8 - cse_3));
  gl_FragColor.xyz = (((
    (gl_FragColor.xyz * (1.0 - ((reflet.x * intreflet) * 0.5)))
   * 
    (1.0 - cse_3)
  ) + (
    (vec3((soleil * 0.25)) * intreflet)
   * 0.5)) + ((reflet.xyz * 
    (tmpvar_1.x * 5.5)
  ) * (0.3 + intreflet)));
  gl_FragColor.w = ((0.95 - (soleil * 0.1)) - ((
    (v_color.x * 0.9)
   * 
    (1.0 - reflet.x)
  ) * (1.0 - 
    (intreflet * 0.5)
  )));
  gl_FragColor.xyz = mix (gl_FragColor.xyz, u_fogColor.xyz, v_fog);
}
