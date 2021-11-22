#vertexShader

#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 inTexCoords;
layout (location=2) in vec3 normals;

out vec2 psTexCoords;

void main()
{
    gl_Position = vec4(position, 1.0f);
    psTexCoords = inTexCoords;
}

#fragmentShader

#version 330

in  vec2 psTexCoords;
out vec4 fragColor;

uniform sampler2D texture_sampler;

void main()
{
    fragColor = texture(texture_sampler, psTexCoords);
}
