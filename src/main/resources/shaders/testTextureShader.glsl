#vertexShader

#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 inTexCoords;
layout (location=2) in vec3 normals;

uniform mat4 u_projectionMatrix;
uniform mat4 u_viewMatrix;
uniform mat4 u_worldMatrix;

out vec2 TexCoords;
out vec3 vertexNormals;
out vec3 vertexPos;

void main()
{
    vec4 pos = u_viewMatrix * u_worldMatrix * vec4(position, 1.0);
    gl_Position =  u_projectionMatrix * pos;
    TexCoords = inTexCoords;
    vertexNormals = normalize( u_viewMatrix * vec4(normals, 0.0)).xyz;
    vertexPos = pos.xyz;
}


#fragmentShader

#version 330

in vec2 TexCoords;
in vec3 vertexNormal;
in vec3 vertexPos;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct PointLight
{
    vec3 colour;
    // Light position is assumed to be in view coordinates
    vec3 position;
    float intensity;    // 0 .. 1
    Attenuation att;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLight;

vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;


void setupColours(Material material, vec2 TexCoords)
{
    if (material.hasTexture == 1)
    {
        ambientC = texture(texture_sampler, TexCoords);
        diffuseC = ambientC;
        speculrC = ambientC;
    }
    else
    {
        ambientC = material.ambient;
        diffuseC = material.diffuse;
        speculrC = material.specular;
    }
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec4 diffuseColour = vec4(0, 0, 0, 0);
    vec4 specColour = vec4(0, 0, 0, 0);

    // Diffuse Light
    vec3 light_direction = light.position - position;
    vec3 to_light_source  = normalize(light_direction);
    float diffuseFactor = max(dot(normal, to_light_source ), 0.0);
    diffuseColour = diffuseC * vec4(light.colour, 1.0) * light.intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_source = -to_light_source;
    vec3 reflected_light = normalize(reflect(from_light_source, normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = speculrC * specularFactor * material.reflectance * vec4(light.colour, 1.0);

    // Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return (diffuseColour + specColour) / attenuationInv;
}

void main()
{
    setupColours(material, TexCoords);

    vec4 diffuseSpecularComp = calcPointLight(pointLight, vertexPos, vertexNormal);

    fragColor = ambientC * vec4(ambientLight, 1) + diffuseSpecularComp;
}