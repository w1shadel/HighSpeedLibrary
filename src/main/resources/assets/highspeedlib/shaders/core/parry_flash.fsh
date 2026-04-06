#version 150

uniform sampler2D Sampler0;
uniform float Intensity;
uniform vec4 ColorModulator;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 center = vec2(0.5, 0.5);
    vec2 p = texCoord - center;
    float r = length(p);

    // 【色収差：放射状(Radial)への変更とノイズ対策】 
    // 横方向のみではなく、中心から放射状ににじませることで自然な歪みに
    // 画面端(r > 0.3)から段階的に減衰させ、エッジクランプによるノイズ（青い縞）を排除
    float edgeFade = 1.0 - smoothstep(0.3, 0.48, r);
    float aberration = 0.04 * Intensity * edgeFade;
    
    // 放射方向にずらす
    vec2 rCoord = clamp(center + p * (1.0 + aberration), 0.001, 0.999);
    vec2 bCoord = clamp(center + p * (1.0 - aberration), 0.001, 0.999);

    float red = texture(Sampler0, rCoord).r;
    float green = texture(Sampler0, texCoord).g;
    float blue = texture(Sampler0, bCoord).b;
    vec3 sceneColor = vec3(red, green, blue) * ColorModulator.rgb;

    // 【円形ベースの四隅強調エフェクト】
    float edgeGlow = smoothstep(0.3, 0.9, r * 1.35); 

    // 【全体フラッシュ + 四隅】
    float flashAmount = (0.05 + edgeGlow * 0.5) * Intensity;
    
    vec3 finalColor = sceneColor + vec3(flashAmount);
    
    fragColor = vec4(finalColor, ColorModulator.a);
}
