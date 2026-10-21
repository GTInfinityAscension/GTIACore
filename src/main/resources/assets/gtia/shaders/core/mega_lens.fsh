#version 150

// EN: Point-mass gravitational lens around a black hole. A ray seen at angle theta from the hole comes from the source
// angle beta = theta - thetaE^2 / theta (thetaE = Einstein radius): outside the ring the sky is pulled outwards, inside
// it the far side folds round, and the Einstein ring shows where both images meet. Only what lies behind the hole
// (deeper than FrontDepth) is bent; the effect fades out at Reach.
// PT: Lente gravitacional de massa pontual em volta de um buraco negro. Um raio visto no ângulo theta vem do ângulo
// beta = theta - thetaE^2 / theta (thetaE = raio de Einstein): fora do anel o céu é puxado para fora, dentro dele o lado
// de trás se dobra, e o anel de Einstein aparece onde as duas imagens se encontram. Só o que está atrás do buraco
// (mais fundo que FrontDepth) entorta; o efeito some em Reach.
// ZH: 黑洞周围的点质量引力透镜。在角度 theta 处看到的光线来自源角 beta = theta - thetaE^2 / theta（thetaE 为爱因斯坦半径）：
// 环外天空被向外拉，环内背面折回，两个像相遇处出现爱因斯坦环。只有位于黑洞后方（深于 FrontDepth）的物体被弯曲；效果在 Reach 处消失。
uniform sampler2D Scene;
uniform sampler2D SceneDepth;
uniform vec2 Center;
uniform float Einstein;
uniform float Horizon;
uniform float Strength;
uniform float Aspect;
uniform float FrontDepth;
uniform float Reach;

in vec2 uv;

out vec4 fragColor;

void main() {
    vec2 d = uv - Center;
    d.x *= Aspect;
    float r = length(d);
    vec4 here = texture(Scene, uv);
    if (Strength <= 0.0 || r > Reach || texture(SceneDepth, uv).r < FrontDepth) {
        fragColor = vec4(here.rgb, 1.0);
        return;
    }
    float te = Einstein;
    float beta = r - te * te / max(r, 1e-4);
    // Fade the bending out towards Reach so the edge of the pass never shows.
    float k = Strength * smoothstep(Reach, Reach * 0.55, r);
    float rs = mix(r, beta, k);
    vec2 dir = d / max(r, 1e-4);
    vec2 off = dir * rs;
    off.x /= Aspect;
    vec2 st = clamp(Center + off, 0.0, 1.0);
    // Things nearer than the hole are never pulled into the lens.
    if (texture(SceneDepth, st).r < FrontDepth) st = uv;
    vec3 col = texture(Scene, st).rgb;
    // Light piles up at the Einstein ring.
    float ring = exp(-pow((r - te) / (te * 0.06), 2.0)) * 0.45 * k;
    col += vec3(1.0, 0.9, 0.75) * ring + col * ring;
    if (r < Horizon) col = vec3(0.0);
    fragColor = vec4(col, 1.0);
}
