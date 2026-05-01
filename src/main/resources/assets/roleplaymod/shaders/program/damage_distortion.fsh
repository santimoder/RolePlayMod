#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 uv = texCoord;

    float wave = sin((uv.y * 20.0) + Time * 10.0) * 0.005 * Intensity;
    uv.x += wave;

    float wave2 = cos((uv.x * 15.0) + Time * 8.0) * 0.004 * Intensity;
    uv.y += wave2;

    vec4 color = texture(DiffuseSampler, uv);

    fragColor = color;
}