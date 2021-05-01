struct Light {
    vec3 position;
    vec3 colour;
    float radius;
};

in vec2 s_texCoord;

out vec4 o_fragColour;

uniform sampler2D u_gbufferAlbedo;
uniform sampler2D u_gbufferNormal;
uniform sampler2D u_gbufferPosition;

uniform vec3 u_cameraPos;
uniform float u_ambientLight;
uniform Light u_lights[MAX_LIGHTS];
uniform int u_numLights;

uniform vec3 u_fogColour;

uniform sampler2D u_directionalLightShadowMap;
uniform mat4 u_directionalLightProjection;
uniform mat4 u_directionalLightView;
uniform vec3 u_directionalLightDirection;
uniform vec3 u_directionalLightColour;
uniform float u_directionalLightIntensity;

uniform float u_renderDistance;

void main() {
	float alpha = texture(u_gbufferAlbedo, s_texCoord).a;
	vec3 albedo = texture(u_gbufferAlbedo, s_texCoord).rgb;
	vec3 normal = texture(u_gbufferNormal, s_texCoord).rgb;
	vec3 fragPos = texture(u_gbufferPosition, s_texCoord).rgb;
	
	vec3 ambient = albedo * u_ambientLight;
	vec3 lighting = vec3(0.0, 0.0, 0.0);
	vec3 viewDirection = normalize(u_cameraPos - fragPos);
	
	// Calculate directional light
	{
        vec3 diffuse = clamp(dot(normal, u_directionalLightDirection), 0.0, 1.0) * albedo * u_directionalLightColour;
        lighting += clamp(diffuse * u_directionalLightIntensity, 0.0, 1.0);
	}
	
	// Calculate point lights
	for (int i = 0; i < u_numLights; i++) {
		float dist = distance(fragPos, u_lights[i].position);
		
		if (dist < u_lights[i].radius) {
			vec3 lightDirection = normalize(u_lights[i].position - fragPos);
			vec3 diffuse = clamp(dot(normal, lightDirection), 0.0, 1.0) * albedo * u_lights[i].colour;
			float atten = 1.0 - (dist / u_lights[i].radius);
			lighting += clamp(diffuse * atten, 0.0, 1.0);
		}
	}
	
    vec3 lit = clamp(lighting, ambient, vec3(1.0));
	
	// Apply fog
	float fogFactor = (distance(u_cameraPos, fragPos) / u_renderDistance);
	vec3 fogged = mix(lit, u_fogColour, fogFactor);
	
	o_fragColour = vec4(clamp(fogged, 0.0, 1.0), alpha);
}