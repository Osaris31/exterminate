

#ifdef GL_ES
precision mediump float;
#endif

uniform float force;
uniform float forcesoleil;
uniform float hauteur;
uniform vec2 resolution;
uniform vec3 position;

float smoothstepwesh(float edge0, float edge1, float x) {
	float t;
    t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

float point(vec2 p, vec2 p2)
{
	return pow(1.0 / sqrt((p2.x-p.x)*(p2.x-p.x)+(p2.y-p.y)*(p2.y-p.y)) * 0.02, 0.75);	
}

float bokeh(vec2 p, float r, float smodoth)
{
	vec2 q = abs(p);
	float d = dot(q, vec2(0.866024,  0.5));
	float s = max(d, q.y) - r;
	return smoothstepwesh(smodoth, -smodoth, s);
}
vec3 adjust(vec3 color) {
	return color*color;
}
void main( void ) {

	vec2 p = (gl_FragCoord.xy / resolution.xy - vec2(0.5,0.5)) * vec2(1.0, resolution.y/resolution.x);
	vec3 color;
	vec2 center = vec2(0.0, 0.0);
	
	
	
	vec2 flare = vec2(position.x-0.5, (position.y-0.5)*(resolution.y/resolution.x) );//mouse * 2.0 - 1.0;
	
	
	
	vec2 flaredir = normalize(center - flare);
	color = vec3(0.0, 0.0, 0.0);
	//sun
	vec3 sun = vec3(0.0, 0.0, 0.0);
	sun += pow(1.2*vec3(1.0, 0.6+0.3*hauteur, 0.3+0.5*hauteur) * bokeh(p -flare ,0.05, 0.1) * 1.0, vec3(2.0, 2.0, 2.0));
	sun += 1.2*vec3(1.0, 0.71+0.15*hauteur, 0.55+0.25*hauteur) * point(p, flare);
	
	//ghosts
	vec3 ghosts = vec3(0.0, 0.0, 0.0);
	ghosts += adjust(vec3(1.0, 0.9, 0.8) * bokeh(p + flare * 0.3,0.05, 0.004)) * 0.2;
	ghosts += adjust(vec3(1.0, 0.8, 0.7) * bokeh(p + flare * - 0.2,0.03, 0.003)) * 0.3;
	ghosts += adjust(vec3(1.0, 1.0, 0.9) * bokeh(p + flare * 0.6,0.06, 0.010)) * 0.2;
	ghosts += adjust(vec3(1.0, 1.0, 0.9) * bokeh(p + flare * 1.5,0.09, 0.015)) * 0.1;
	
	float yy = 5.0-max(abs(flare.x),abs(flare.y));
	ghosts = pow(ghosts, vec3(yy, yy, yy))*1.75;
//	ghosts = pow(ghosts, vec3(2.0, 2.0, 2.0)) * 1.75;
	
	color = adjust(sun)*forcesoleil + ghosts;
	
	gl_FragColor = vec4(sqrt(color)*(force-0.2), 1.0-(force-1.1) );
	gl_FragColor.a = 1.0;

}