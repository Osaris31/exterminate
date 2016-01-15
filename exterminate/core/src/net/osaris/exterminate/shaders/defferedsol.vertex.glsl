attribute vec3 a_position;
attribute vec4 a_color;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

varying vec3 v_normal;
varying vec2 v_texCoords0;
varying vec3 v_color;
varying float depth;
uniform mat4 u_worldTrans;

uniform mat4 u_projViewTrans;
uniform vec3 u_cameraPosition;
uniform vec3 DirectionalLightdirection;
//uniform vec3 DirectionalLightcolor;
varying vec4 v_acolor;

void main() {
		v_texCoords0 = a_texCoord0;
		vec4 pos = u_worldTrans * vec4(a_position, 1.0);
		v_acolor = a_color;
	gl_Position = u_projViewTrans * pos;
		
		vec3 normal = normalize(a_normal);
		v_normal = normal;

//		vec3 viewVec = normalize(u_cameraPosition.xyz - pos.xyz);
		depth = gl_Position.z/(gl_Position.w+1.0-sign(gl_Position.z)); // hack pour eviter le div par zero
//	vec3 viewVec = u_cameraPosition.xyz - pos.xyz;
//	depth = sqrt(dot(viewVec, viewVec));

		vec3 lightDir = -DirectionalLightdirection;
		// diffuse
		v_color.g = clamp(dot(normal, lightDir), 0.0, 1.0);

	//	float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewVec)));
		//specular
	//	v_color.b = a_color.x * v_color.g * pow(halfDotView, a_color.y*10.0);//u_shininess
		v_color.b = 0.0;//u_shininess

		//ambiant
		v_color.r = 1.0;

		float vcolora = a_color.a;// + (1.0-a_color.a)*(0.5-v_color.g*DirectionalLightcolor.x);
		
		v_color.g*=vcolora;
		v_color.r*=vcolora;
		
		v_color.r=clamp(v_color.r*0.5, 0.0, 1.0);
		v_color.g=clamp(v_color.g*0.5, 0.0, 1.0);
		v_color.b=clamp(v_color.b*0.5, 0.0, 1.0);
}
