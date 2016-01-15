//!\\ Specific Licence for this file: this water is an important part of the graphic style of my game, do not use it on your own.

uniform sampler2D u_diffuseTexture;
uniform sampler2D u_normal2Texture;
uniform sampler2D u_dudvTexture;
uniform sampler2D u_diffuseRefletTexture;
uniform sampler2D u_gbufferDiffuse;
uniform sampler2D u_gbufferDepth;
uniform sampler2D u_gbufferAttribs;
uniform float soleil;
varying vec2 v_texCoords0;
varying float ombre;
varying vec4 pos;
varying vec4 possurface;
uniform float time;
varying vec2 TexCoords;
uniform vec2 resolution;
varying float distance;
uniform vec4 u_fogColor;
varying float v_fog;
varying float intenseRefl;
uniform mat4 InvProjectionMatrix;
uniform vec3 DirectionalLightcolor;
uniform vec3 DirectionalLightdirection;
uniform vec3 u_ambientLight;
uniform vec3 u_cameraPosition;
uniform sampler2D u_shadowTexture;
varying vec3 v_shadowMapUv;
varying float coefombrage;
void main ()
{
   vec2 cse_1;
  cse_1 = (gl_FragCoord.xy / resolution);
   float tmpvar_2;
  tmpvar_2 = texture2D (u_gbufferDepth, cse_1).x;
   vec4 tmpvar_3;
  tmpvar_3.w = 1.0;
  tmpvar_3.xy = ((cse_1 * 2.0) - 1.0);
  tmpvar_3.z = tmpvar_2;
   vec4 tmpvar_4;
  tmpvar_4 = (InvProjectionMatrix * tmpvar_3);
   vec3 tmpvar_5;
  tmpvar_5 = ((tmpvar_4 / tmpvar_4.w).xyz - possurface.xyz);
   float tmpvar_6;
  tmpvar_6 = exp((-(
    sqrt(dot (tmpvar_5, tmpvar_5))
  ) * 0.15));
   vec4 tmpvar_7;
  tmpvar_7 = (texture2D (u_dudvTexture, (TexCoords + vec2((time * 0.01)))) * 0.035);
   float tmpvar_8;
  tmpvar_8 = ((1.0 - (tmpvar_6 * 0.9)) * exp((
    -(distance)
   * 0.1)));
   vec2 tmpvar_9;
  float cse_10;
  cse_10 = (time * 0.5);
  tmpvar_9.x = (1.0 + cos((
    (possurface.x + cse_10)
   + 
    (tmpvar_7.x * 30.0)
  )));
  tmpvar_9.y = (1.0 + sin((
    (possurface.z + cse_10)
   + 
    (tmpvar_7.y * 30.0)
  )));
   vec2 tmpvar_11;
  tmpvar_11 = (tmpvar_9 * (0.03 * tmpvar_8));
   vec2 tmpvar_12;
  tmpvar_12 = ((cse_1 * (1.0 - 
    (tmpvar_8 * 0.06)
  )) + tmpvar_11);
   vec3 tmpvar_13;
  tmpvar_13 = (texture2D (u_gbufferAttribs, tmpvar_12).xyz * 2.0);
   float tmpvar_14;
  tmpvar_14 = texture2D (u_gbufferDepth, tmpvar_12).x;
   vec4 tmpvar_15;
  tmpvar_15.w = 1.0;
  tmpvar_15.xy = ((cse_1 * 2.0) - 1.0);
  tmpvar_15.z = tmpvar_14;
   vec4 tmpvar_16;
  tmpvar_16 = (InvProjectionMatrix * tmpvar_15);
   vec3 tmpvar_17;
  tmpvar_17 = ((tmpvar_16 / tmpvar_16.w).xyz - possurface.xyz);
   float tmpvar_18;
  tmpvar_18 = min (tmpvar_6, exp((
    -(sqrt(dot (tmpvar_17, tmpvar_17)))
   * 0.15)));
   vec3 tmpvar_19;
  tmpvar_19 = ((texture2D (u_gbufferDiffuse, tmpvar_12).xyz * (
    (u_ambientLight * tmpvar_13.x)
   + 
    (tmpvar_13.y * DirectionalLightcolor)
  )) * ((tmpvar_18 * 0.25) + 0.7));
   float tmpvar_20;
  tmpvar_20 = (((
    float((dot (texture2D (u_shadowTexture, v_shadowMapUv.xy), vec4(1.0, 0.00392157, 1.53787e-05, 6.22737e-09)) >= v_shadowMapUv.z))
   * coefombrage) + 1.0) - coefombrage);
   vec3 y_21;
  y_21 = (vec3(0.9, 0.85, 0.8) * ((tmpvar_18 * 0.5) + 0.5));
   float a_22;
  a_22 = (0.2 + (tmpvar_18 * 0.2));
   vec3 tmpvar_23;
  tmpvar_23 = (mix (texture2D (u_diffuseTexture, v_texCoords0).xyz, y_21, a_22) * ombre);
  gl_FragColor.xyz = tmpvar_23;
   vec3 tmpvar_24;
  tmpvar_24 = normalize(((
    mix (texture2D (u_normal2Texture, (TexCoords + tmpvar_7.xy)).xyz, texture2D (u_normal2Texture, ((TexCoords + tmpvar_7.xy) + vec2(0.5, 0.5))).xyz, ((cos(
      ((((
        ((cos((
          ((time * 1.8) + possurface.x)
         + possurface.z)) * 0.7) + (cos((
          (((time * 1.7) + 2.5) + (possurface.x * 0.23))
         + 
          (possurface.z * 0.875)
        )) * 0.85))
       + 
        cos((((time * 1.3) + (possurface.x * 0.478)) + (possurface.z * 0.4125)))
      ) * 0.15) + 0.5) * 3.0)
    ) * 0.5) + 0.5))
   * 2.0) - 1.0));
   vec4 tmpvar_25;
  tmpvar_25 = texture2D (u_diffuseRefletTexture, clamp (((
    ((pos * vec4((1.0/(pos.w)))) + vec4(1.0, 1.0, 1.0, 1.0))
   * vec4(0.5, 0.5, 0.5, 0.5)) + (
    normalize(((texture2D (u_dudvTexture, (TexCoords + tmpvar_7.xy)) * 2.0) - 1.0))
   * 
    max (0.007, (0.04 - (0.001 * distance)))
  )), 0.001, 0.999).xy);
  vec3 tmpvar_26;
  tmpvar_26 = normalize((u_cameraPosition - possurface.xyz));
  float tmpvar_27;
  tmpvar_27 = (1.0 - (tmpvar_26.y * 0.8));
   float tmpvar_28;
  tmpvar_28 = max (max (0.0, dot (
    normalize(((tmpvar_24.xzy * 0.5) + vec3(0.0, 0.5, 0.0)))
  , 
    normalize((-(DirectionalLightdirection) + tmpvar_26))
  )), max (0.0, dot (
    normalize(((tmpvar_24.xzy * 0.5) + vec3(0.0, 0.5, 0.0)))
  , 
    normalize((vec3(0.0, 1.0, 0.0) + tmpvar_26))
  )));
  float cse_29;
  cse_29 = -(DirectionalLightdirection.y);
  float cse_30;
  cse_30 = (intenseRefl * 0.4);
  float cse_31;
  cse_31 = (possurface.y * 0.4);
  vec4 tmpvar_32;
  tmpvar_32.xw = vec2(1.2, 0.0);
  tmpvar_32.y = (0.75 - (DirectionalLightdirection.y * 0.3));
  tmpvar_32.z = (0.5 - (DirectionalLightdirection.y * 0.5));
   float tmpvar_33;
  tmpvar_33 = sqrt(tmpvar_18);
   vec4 tmpvar_34;
  tmpvar_34 = ((1.0 - tmpvar_33) * ((
    ((((0.025 * 
      pow (tmpvar_28, u_cameraPosition.y)
    ) * (
      (0.5 + cse_30)
     + cse_31)) * soleil) * cse_29)
   + 
    ((((0.09 * 
      pow (tmpvar_28, ((16.0 + u_cameraPosition.y) + ((cse_29 * 10.0) * (cse_29 * 10.0))))
    ) * (
      (0.5 + cse_30)
     + cse_31)) * soleil) * cse_29)
  ) * tmpvar_32));
   float a_35;
  a_35 = (0.8 - ((tmpvar_27 * 0.6) * (1.0 - 
    (tmpvar_24.z * 0.7)
  )));
  gl_FragColor.xyz = mix (tmpvar_25.xyz, gl_FragColor.xyz, a_35);
  gl_FragColor.xyz = (((
    (gl_FragColor.xyz * (1.0 - ((tmpvar_34.x * tmpvar_27) * 0.5)))
   * 
    (1.0 - (tmpvar_27 * 0.35))
  ) + (
    (vec3((soleil * 0.25)) * tmpvar_27)
   * 0.5)) + ((tmpvar_34.xyz * 
    (tmpvar_23.x * 5.5)
  ) * (0.3 + tmpvar_27)));
   float tmpvar_36;
  tmpvar_36 = clamp ((0.95 - (
    ((tmpvar_18 * 0.95) * (1.0 - tmpvar_34.x))
   * 
    (1.0 - (tmpvar_27 * 0.1))
  )), 0.0, 1.0);
  gl_FragColor.w = tmpvar_36;
   float x_37;
  x_37 = (0.8 + (tmpvar_18 * 0.17));
   float tmpvar_38;
  tmpvar_38 = (max (x_37, gl_FragColor.w) - gl_FragColor.w);
  gl_FragColor.xyz = ((gl_FragColor.xyz * (1.0 - tmpvar_38)) + ((tmpvar_19 * tmpvar_38) * (
    (tmpvar_20 * 0.35)
   + 0.65)));
  gl_FragColor.w = (gl_FragColor.w + tmpvar_38);
  gl_FragColor.xyz = mix (gl_FragColor.xyz, u_fogColor.xyz, v_fog);
  gl_FragColor.y = (gl_FragColor.y * (0.98 + (tmpvar_18 * 0.02)));
  gl_FragColor.z = (gl_FragColor.z * (0.96 + (tmpvar_18 * 0.04)));
}
