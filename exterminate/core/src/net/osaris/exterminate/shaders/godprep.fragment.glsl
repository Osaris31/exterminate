#version 130


uniform sampler2D u_diffuseTexture;

varying vec2 v_texCoords0;
uniform vec3 position;

uniform vec2 resolution;


float when_eq(float x, float y) {
  return 1.0 - abs(sign(x - y));
}

void main() {


         float gbuf = texture2D(u_diffuseTexture, v_texCoords0 ).x;


    vec2 uv = (v_texCoords0);
    uv.y = 1.0-uv.y;

  uv -= vec2(position.x, 1.0-position.y);
  uv.x*=resolution.x/resolution.y;
 
 float disc_radius = 0.045;
 float border_size = 0.06;
 
  float dist = sqrt(dot(uv, uv));
  float t = smoothstep(disc_radius+border_size, disc_radius-border_size, dist);

         gl_FragColor.r = ((0.07)+t)*when_eq(gbuf, 0.0);
         gl_FragColor.g = 0.0;
         gl_FragColor.b = when_eq(gbuf, 0.0);
         
}
