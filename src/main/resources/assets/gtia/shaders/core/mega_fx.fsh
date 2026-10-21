#version 150

// Megastructure effects (GTIA-D-316). Modes:
//   0 star photosphere   1 (unused)   2 prominence ribbon   3 jet   4 force field / shell   5 accretion disk
//   6 event horizon   7 glow billboard (corona, bloom, photon ring)   8 particle   9 beam   10 planet
// Additive modes write premultiplied colour with alpha 1 (blend: src alpha, one).
uniform float Time;
uniform float Mode;
uniform vec4 ColorA;
uniform vec4 ColorB;
uniform vec4 Params;
uniform vec4 Params2;
uniform vec4 Shape;

in vec3 objPos;
in vec3 objNormal;
in vec3 viewPos;
in vec3 viewNormal;
in vec3 viewVel;
in vec2 uv;
in float fade;

out vec4 fragColor;

// ------------------------------------------------------------------ noise

float hash(vec3 p) {
    p = fract(p * 0.3183099 + vec3(0.71, 0.113, 0.419));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

vec3 hash3(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 74.7)), dot(p, vec3(269.5, 183.3, 246.1)), dot(p, vec3(113.5, 271.9, 124.6)));
    return fract(sin(p) * 43758.5453);
}

float noise(vec3 x) {
    vec3 i = floor(x), f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(i), hash(i + vec3(1, 0, 0)), f.x), mix(hash(i + vec3(0, 1, 0)), hash(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash(i + vec3(0, 0, 1)), hash(i + vec3(1, 0, 1)), f.x), mix(hash(i + vec3(0, 1, 1)), hash(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float s = 0.0, a = 0.5;
    for (int i = 0; i < 5; i++) {
        s += a * noise(p);
        p = p * 2.03 + vec3(1.7, 9.2, 3.1);
        a *= 0.5;
    }
    return s;
}

// Animated Worley noise: x = distance to the nearest feature point, y = to the second (convective cells).
vec2 worley(vec3 p, float t) {
    vec3 i = floor(p), f = fract(p);
    float d1 = 8.0, d2 = 8.0;
    for (int z = -1; z <= 1; z++)
        for (int y = -1; y <= 1; y++)
            for (int x = -1; x <= 1; x++) {
                vec3 g = vec3(x, y, z);
                vec3 h = hash3(i + g);
                vec3 o = 0.5 + 0.42 * sin(t * (0.6 + h) + 6.2831853 * h);
                float d = length(g + o - f);
                if (d < d1) {
                    d2 = d1;
                    d1 = d;
                } else if (d < d2) d2 = d;
            }
    return vec2(d1, d2);
}

float facing() {
    return clamp(abs(dot(normalize(viewNormal), normalize(-viewPos))), 0.0, 1.0);
}

// Hexagonal tiling: xy = position in the cell, zw = cell id.
vec4 hexCell(vec2 p) {
    const vec2 s = vec2(1.0, 1.7320508);
    vec4 c = floor(vec4(p, p - vec2(0.5, 1.0)) / s.xyxy) + 0.5;
    vec4 h = vec4(p - c.xy * s, p - (c.zw + 0.5) * s);
    return dot(h.xy, h.xy) < dot(h.zw, h.zw) ? vec4(h.xy, c.xy) : vec4(h.zw, c.zw + 0.5);
}

float hexEdge(vec2 h) {
    h = abs(h);
    return max(dot(h, vec2(0.5, 0.8660254)), h.x);
}

// ------------------------------------------------------------------ modes

vec3 star() {
    // EN: Params = (brightness, granule density, spot coverage, seed). Limb darkening I(mu) = 1 - 0.6(1 - mu)
    // - 0.2(1 - mu)^2 (solar quadratic law); the limb also reddens (ColorB), as the light comes from higher, cooler gas.
    // PT: Params = (brilho, densidade de grânulos, cobertura de manchas, semente). Escurecimento de limbo pela lei
    // quadrática solar; o limbo também avermelha (ColorB), porque a luz vem de gás mais alto e mais frio.
    // ZH: Params =（亮度、米粒密度、黑子覆盖率、种子）。临边昏暗采用太阳二次定律；临边也偏红（ColorB），因为光来自更高更冷的气体。
    vec3 n = normalize(objPos);
    float mu = facing();
    // Granules (two octaves, the fine one boiling faster) inside supergranule cells, both with dark lanes.
    vec2 w = worley(n * Params.y, Time * 0.35);
    vec2 w2 = worley(n * Params.y * 2.3 + vec3(5.1), Time * 0.6);
    vec2 sg = worley(n * Params.y * 0.28 + vec3(2.7), Time * 0.05);
    float granule = smoothstep(0.0, 0.5, w.y - w.x) * 0.65 + smoothstep(0.0, 0.5, w2.y - w2.x) * 0.35;
    float network = smoothstep(0.0, 0.12, sg.y - sg.x);
    float coarse = fbm(n * 3.1 + vec3(Time * 0.015));
    float limb = 1.0 - 0.6 * (1.0 - mu) - 0.2 * (1.0 - mu) * (1.0 - mu);
    vec3 col = mix(ColorB.rgb, ColorA.rgb, pow(mu, 0.3));
    col *= limb * (0.8 + 0.24 * granule) * (0.9 + 0.1 * network) * (0.9 + 0.2 * coarse);
    // Sunspots: umbra and penumbra where a slow noise peaks, in two belts either side of the equator.
    float belt = smoothstep(0.75, 0.15, abs(abs(n.y) - 0.3));
    float s = noise(n * 6.0 + vec3(Params.w * 13.1, Time * 0.004, 0.0)) * belt;
    float cut = 1.0 - Params.z * 0.35;
    float umbra = smoothstep(cut + 0.1, cut + 0.14, s), pen = smoothstep(cut + 0.02, cut + 0.1, s);
    col *= 1.0 - 0.3 * pen - 0.5 * umbra;
    // Faculae: bright network near the limb; the disc centre runs hot towards white.
    col += ColorA.rgb * 0.35 * smoothstep(0.55, 0.85, coarse) * (1.0 - network * 0.5) * pow(1.0 - mu, 1.5);
    // The disc centre runs hot towards white, as an overexposed photosphere does.
    col = mix(col, vec3(1.0, 0.97, 0.88), pow(mu, 2.0) * 0.45);
    return col * Params.x;
}

vec4 field() {
    // Params = (brightness, hexes across a radian, pulse speed, pattern: 0 hex, 1 lattice, 2 lit city cells)
    vec3 n = normalize(objNormal);
    float mu = facing();
    float rim = pow(1.0 - mu, 2.6);
    vec3 w = abs(n);
    w = w / (w.x + w.y + w.z);
    vec3 p = objPos * Params.y;
    vec4 hx = hexCell(p.yz), hy = hexCell(p.zx), hz = hexCell(p.xy);
    float e = hexEdge(hx.xy) * w.x + hexEdge(hy.xy) * w.y + hexEdge(hz.xy) * w.z;
    float cid = hash(vec3(hx.zw, 1.0)) * w.x + hash(vec3(hy.zw, 2.0)) * w.y + hash(vec3(hz.zw, 3.0)) * w.z;
    float line;
    int pattern = int(Params.w + 0.5);
    if (pattern == 1) {
        vec3 g = abs(fract(p * 0.5) - 0.5);
        line = smoothstep(0.42, 0.49, max(max(g.x * w.z + g.x * w.y, g.y * w.x + g.y * w.z), g.z * w.x + g.z * w.y));
    } else line = smoothstep(0.40, 0.49, e);
    float cell = step(0.86, fract(cid * 7.0 + Time * 0.07)) * (0.5 + 0.5 * sin(Time * 2.0 + cid * 40.0));
    // A pulse sweeps over the field from a slowly wandering pole.
    vec3 pole = normalize(vec3(sin(Time * 0.11), cos(Time * 0.07), sin(Time * 0.05 + 1.0)));
    float ph = fract(Time * Params.z);
    float pulse = exp(-pow((dot(n, pole) - (1.0 - 2.0 * ph)) * 9.0, 2.0)) * (1.0 - ph);
    float shimmer = 0.85 + 0.3 * noise(objPos * 2.0 + vec3(Time * 0.6));
    float i = line * (0.3 + 1.4 * rim) + rim * 0.3 + cell * 0.18 + pulse * (0.5 * line + 0.25);
    vec3 col = mix(ColorA.rgb, ColorB.rgb, rim);
    if (pattern == 2) {
        // Shell worlds: dark plates with warm city lights, bright at the terminator rim.
        float lights = step(0.7, hash(floor(objPos * Params.y * 4.0))) * (1.0 - line);
        col = mix(ColorA.rgb, vec3(1.0, 0.78, 0.45), lights);
        i = line * 0.35 + lights * 0.6 + rim * 0.5 + pulse * 0.2;
    }
    return vec4(col * i * shimmer * Params.x, 1.0);
}

vec4 jet() {
    // Params = (brightness, shock diamonds along the jet, flow speed, turbulence); ColorA hot, ColorB cool.
    // EN: Mach diamonds: standing shocks of an over- or under-expanded nozzle, bright bands that fade downstream.
    // PT: Diamantes de Mach: choques parados de um bocal super ou subexpandido, faixas claras que somem rio abaixo.
    // ZH: 马赫环：过膨胀或欠膨胀喷管的驻激波，向下游逐渐消失的亮带。
    float t = uv.y;
    float mu = facing();
    float body = pow(mu, 1.8);
    float turb = fbm(vec3(objPos.x * 0.18, t * 18.0 - Time * Params.z, objPos.z * 0.18));
    float diamonds = pow(0.5 + 0.5 * cos(t * Params.y * 6.2831853), 5.0) * exp(-t * 4.0);
    float heat = exp(-t * 3.0);
    // Hot white-blue at the nozzle, the tint further out, fading to the cool colour at the end.
    vec3 col = mix(ColorB.rgb, ColorA.rgb, heat);
    col = mix(col, vec3(1.0), pow(mu, 8.0) * heat * 0.6);
    float a = body * mix(1.0, 0.2 + 1.4 * turb, Params.w) * pow(1.0 - t, 1.2) * smoothstep(0.0, 0.02, t);
    a *= 0.7 + 1.6 * diamonds;
    return vec4(col * a * Params.x, 1.0);
}

vec4 beam() {
    // Params = (brightness, bands along the beam, band speed, flicker)
    float t = uv.y;
    float mu = facing();
    float core = pow(mu, 3.0);
    float bands = 0.65 + 0.35 * sin(t * Params.y - Time * Params.z);
    float n = noise(vec3(uv.x * 6.0, t * 30.0 - Time * Params.z * 0.5, 0.0));
    vec3 col = mix(ColorA.rgb, vec3(1.0), core * 0.75);
    float ends = smoothstep(0.0, 0.03, t) * smoothstep(1.0, 0.97, t);
    float a = (core * 1.4 + pow(mu, 0.9) * 0.35) * bands * mix(1.0, 0.7 + 0.6 * n, Params.w) * ends;
    return vec4(col * a * Params.x, 1.0);
}

vec4 prominence() {
    // Ribbon across uv.x, along an arc in uv.y; Params = (brightness, knot density, flow speed, seed)
    float across = 1.0 - abs(uv.x * 2.0 - 1.0);
    float knots = fbm(vec3(uv.y * Params.y - Time * Params.z, uv.x * 2.0, Params.w * 7.0));
    float feet = smoothstep(0.0, 0.12, uv.y) * smoothstep(1.0, 0.88, uv.y);
    float a = pow(across, 1.6) * (0.3 + 1.2 * knots) * feet;
    return vec4(mix(ColorB.rgb, ColorA.rgb, knots) * a * Params.x, 1.0);
}

vec4 disk() {
    // EN: Thin disk, T ~ r^(-3/4); Keplerian shear (angular speed ~ r^(-3/2)); relativistic beaming makes the side
    // that comes towards the camera brighter, by about D^3 with D = 1 + beta cos(angle).
    // PT: Disco fino, T ~ r^(-3/4); cisalhamento kepleriano (velocidade angular ~ r^(-3/2)); o feixe relativístico
    // deixa mais claro o lado que vem para a câmera, por cerca de D^3 com D = 1 + beta cos(ângulo).
    // ZH: 薄盘，T ~ r^(-3/4)；开普勒剪切（角速度 ~ r^(-3/2)）；相对论集束使朝向相机的一侧更亮，约为 D^3，D = 1 + beta cos(角度)。
    // Params = (inner radius / outer, brightness, spin, beaming); ColorA inner (hot), ColorB outer.
    vec2 c = (uv - 0.5) * 2.0;
    float r = length(c);
    float rin = Params.x;
    if (r < rin || r > 1.0) discard;
    float ang = atan(c.y, c.x) - Time * Params.z * pow(max(r, 0.05), -1.5);
    float n = fbm(vec3(cos(ang) * r * 5.0, sin(ang) * r * 5.0, r * 14.0));
    float lanes = 0.6 + 0.4 * sin(r * 70.0 + n * 6.0);
    float temp = pow(rin / r, 0.75);
    vec3 col = mix(ColorB.rgb, ColorA.rgb, temp) + vec3(1.0) * pow(temp, 5.0) * 0.6;
    float beta = dot(normalize(viewVel + vec3(1e-5)), normalize(-viewPos)) * Params.w * inversesqrt(max(r, 0.05)) * 0.5;
    float boost = pow(max(0.05, 1.0 + beta), 3.0);
    float edge = smoothstep(rin, rin * 1.12, r) * smoothstep(1.0, 0.7, r);
    float i = edge * (0.35 + 0.9 * n) * lanes * boost * sqrt(temp) * Params.y;
    return vec4(col * i, 1.0);
}

vec4 glow() {
    // Billboard: objPos.xy in -1..1. Params = (brightness, inner radius, falloff, streamers);
    // Params2 = (disc bloom, photon ring radius, ring width, seed).
    vec2 p = objPos.xy;
    float r = length(p);
    if (r > 1.0) discard;
    float rs = Shape.z > 0.0 ? fade : Params.y;
    if (r < rs && Params2.x <= 0.0) discard;
    float d = max(r - rs, 0.0) / max(1.0 - rs, 1e-3);
    // Over the star's own disc only the faint disc bloom shows: the photosphere carries its own detail.
    float g = r < rs ? 0.0 : exp(-d * Params.z) * smoothstep(1.0, 0.8, r);
    float ang = atan(p.y, p.x);
    float st = fbm(vec3(cos(ang) * 3.0 + Params2.w, sin(ang) * 3.0, d * 1.2 - Time * 0.04));
    float rays = pow(abs(sin(ang * 5.0 + st * 4.0)), 6.0) * 0.5;
    g *= mix(1.0, 0.35 + 1.3 * st + rays, Params.w);
    float disc = r < rs ? Params2.x * (1.0 - 0.5 * r / max(rs, 1e-3)) : 0.0;
    float ring = Params2.y > 0.0 ? exp(-pow((r - Params2.y) / max(Params2.z, 1e-3), 2.0)) : 0.0;
    vec3 col = mix(ColorA.rgb, ColorB.rgb, clamp(d, 0.0, 1.0)) * g + ColorA.rgb * disc + ColorB.rgb * ring * 1.5;
    return vec4(col * Params.x, 1.0);
}

vec4 particle() {
    float r = length(objPos.xy);
    if (r > 1.0) discard;
    float a = pow(1.0 - r, 2.0) * fade;
    vec3 col = mix(ColorA.rgb, ColorB.rgb, uv.x) + vec3(1.0) * pow(1.0 - r, 6.0) * 0.5;
    return vec4(col * a * Params.x, 1.0);
}

vec3 planet() {
    // Params = (brightness, band count, storm strength, seed); Params2.xyz = light direction (object space).
    vec3 n = normalize(objPos);
    float lat = n.y;
    float warp = fbm(n * 4.0 + vec3(Params.w, Time * 0.01, 0.0));
    float bands = 0.5 + 0.5 * sin(lat * Params.y + warp * 3.0);
    float storm = smoothstep(0.75, 0.9, noise(n * 3.0 + vec3(Params.w * 3.1))) * Params.z;
    vec3 col = mix(ColorB.rgb, ColorA.rgb, bands) * (0.8 + 0.4 * warp);
    col = mix(col, vec3(0.9, 0.55, 0.4), storm);
    float light = max(dot(n, normalize(Params2.xyz)), 0.0);
    float rim = pow(1.0 - facing(), 3.0);
    return col * (0.08 + 0.95 * light) * Params.x + ColorA.rgb * rim * 0.25;
}

void main() {
    int mode = int(Mode + 0.5);
    if (mode == 0) fragColor = vec4(star(), 1.0);
    else if (mode == 2) fragColor = prominence();
    else if (mode == 3) fragColor = jet();
    else if (mode == 4) fragColor = field();
    else if (mode == 5) fragColor = disk();
    else if (mode == 6) fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    else if (mode == 7) fragColor = glow();
    else if (mode == 8) fragColor = particle();
    else if (mode == 9) fragColor = beam();
    else if (mode == 10) fragColor = vec4(planet(), 1.0);
    else fragColor = vec4(ColorA.rgb, 1.0);
}
