#ifdef GL_ES
precision float;
#endif

uniform float time;
uniform float angle;
uniform float angley;
uniform float jour;
uniform vec2 resolution;
//normalized sin
float sinn(float x)
{
	return sin(x)/2.+.5;
}

float CausticPatternFn(vec2 pos)
{
float posx = pos.x + angle;

	return (sinn(posx*40.+time)
		+pow(sinn(-posx*130.+time),1.)
		+pow(sinn(posx*30.+time),2.)
		+pow(sinn(posx*50.+time),2.)
		+pow(sinn(posx*80.+time),2.)
		+pow(sinn(posx*90.+time),2.)
		+pow(sinn(posx*12.+time),2.)
		+pow(sinn(posx*6.+time),2.)
		+pow(sinn(-posx*13.+time),5.))/2.;
}

vec2 CausticDistortDomainFn(vec2 pos)
{
	pos.x*=(pos.y*.20+.5);
	pos.x*=1.+sin(time/1.)/10.;
	return pos;
}

void main( void ) 
{
	vec2 pos = gl_FragCoord.xy/resolution.xy;
	pos-=.5;
	vec2  CausticDistortedDomain = CausticDistortDomainFn(pos);
	float CausticShape = clamp(7.-length(CausticDistortedDomain.x*20.),0.,1.);
	float CausticPattern = CausticPatternFn(CausticDistortedDomain);
	float Caustic;
	Caustic += CausticShape*CausticPattern;
	Caustic *= (pos.y*angley+.5)/4.;

	float f = length(pos+vec2(-.5,.5))*length(pos+vec2(.5,.5))*(1.+Caustic)/1.;
	
	
	gl_FragColor = vec4(0.07*jour,0.12*jour,0.1*jour,1.0)*(f*0.75+0.25)*angley+vec4(0.05*jour,0.25*jour,0.3*jour,0.6)*(1.0-angley);

}