#version 150

// Megastructure effects (GTIA-D-316). Object space per mode:
//   sphere modes (star, field, horizon, planet): unit sphere; torus fields: major radius 1 (Shape.x = 1);
//   tube modes (jet, beam): unit cylinder, y 0..1 along the axis, radius Shape.x..Shape.y with exponent Shape.z;
//   disk: square in the XZ plane, UV 0..1; billboard and particles: see below;
//   streams, dipole arcs and gravity wells (13, 15, 16): meshes built in place, UV = (around, along).
in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;
uniform float Mode;
uniform vec4 Params;
uniform vec4 Shape;

out vec3 objPos;
out vec3 objNormal;
out vec3 viewPos;
out vec3 viewNormal;
out vec3 viewVel;
out vec2 uv;
out float fade;

#moj_import <gtia:mega_noise.glsl>

float h1(float n) {
    return fract(sin(n) * 43758.5453);
}

void main() {
    int mode = int(Mode + 0.5);
    uv = UV0;
    fade = 1.0;
    viewVel = vec3(0.0);
    if (mode == 7 || mode == 12) {
        // Billboard of radius Shape.x blocks at the object origin, pulled Shape.y blocks towards the camera so a glow
        // drawn at a star's centre lies over its near surface.
        // Near the star the pull shrinks, so the glow stays behind its surface instead of covering the camera.
        vec4 c = ModelViewMat * vec4(0.0, 0.0, 0.0, 1.0);
        float dist = length(c.xyz);
        float pull = min(Shape.y, max(0.0, dist - Shape.y * 1.08 - 1.0));
        c.xyz += normalize(-c.xyz) * pull;
        // Shape.z = radius of the body behind (a star): its silhouette on this nearer plane, as a share of the
        // billboard, so the glow starts right at the limb.
        if (Shape.z > 0.0) {
            float ang = asin(min(0.999, Shape.z / max(dist, 1e-3)));
            fade = (dist - pull) * tan(ang) / max(Shape.x, 1e-3);
        }
        objPos = vec3(Position.xy, 0.0);
        objNormal = vec3(0.0, 0.0, 1.0);
        viewNormal = objNormal;
        viewPos = c.xyz + vec3(Position.xy * Shape.x, 0.0);
        gl_Position = ProjMat * vec4(viewPos, 1.0);
        return;
    }
    if (mode == 17) {
        // EN: Matter orbiting between Shape.x and Shape.y blocks in the XZ plane at Keplerian speed (Shape.z rad/s at
        // the inner edge, falling as r^-3/2), Params.y blocks thick; Shape.w = sprite size.
        // PT: Matéria orbitando entre Shape.x e Shape.y blocos no plano XZ em velocidade kepleriana (Shape.z rad/s na
        // borda interna, caindo com r^-3/2), Params.y blocos de espessura; Shape.w = tamanho do sprite.
        // ZH: 物质在 XZ 平面内 Shape.x 到 Shape.y 格之间以开普勒速度绕行（内缘 Shape.z 弧度/秒，按 r^-3/2 递减），厚度 Params.y 格；Shape.w 为精灵大小。
        float seed = Position.x;
        float h = h1(seed * 5.13);
        float r = mix(Shape.x, Shape.y, h);
        float a = 6.2831853 * h1(seed * 9.37) + Time * Shape.z * pow(Shape.x / max(r, 1e-3), 1.5);
        vec3 p = vec3(cos(a) * r, (h1(seed * 3.3) - 0.5) * Params.y * 2.0, sin(a) * r);
        vec4 c = ModelViewMat * vec4(p, 1.0);
        float size = Shape.w * (0.5 + h1(seed * 2.31));
        objPos = vec3(Position.yz, 0.0);
        objNormal = vec3(0.0, 0.0, 1.0);
        viewNormal = objNormal;
        uv = vec2(h, h1(seed * 7.7));
        viewPos = c.xyz + vec3(Position.yz * size, 0.0);
        gl_Position = ProjMat * vec4(viewPos, 1.0);
        return;
    }
    if (mode == 8) {
        // Particles flowing along a tube from y = 0 to 1: Position = (seed, corner x, corner y). Shape: radius at the
        // start and at the end (blocks), flow speed (tube lengths per second), sprite size (blocks).
        float seed = Position.x;
        float t = fract(h1(seed * 17.13) + Time * Shape.z * (0.55 + 0.9 * h1(seed * 3.71)));
        float a = 6.2831853 * h1(seed * 9.37) + Time * 0.6 * (h1(seed * 1.9) - 0.5);
        float r = mix(Shape.x, Shape.y, t) * sqrt(h1(seed * 5.13));
        vec3 p = vec3(cos(a) * r, t, sin(a) * r);
        vec4 c = ModelViewMat * vec4(p, 1.0);
        float size = Shape.w * (1.0 - 0.55 * t) * (0.45 + h1(seed * 2.31));
        objPos = vec3(Position.yz, t);
        objNormal = vec3(0.0, 0.0, 1.0);
        viewNormal = objNormal;
        uv = vec2(t, h1(seed * 7.7));
        fade = smoothstep(0.0, 0.06, t) * (1.0 - t);
        viewPos = c.xyz + vec3(Position.yz * size, 0.0);
        gl_Position = ProjMat * vec4(viewPos, 1.0);
        return;
    }
    vec3 p = Position;
    vec3 n = Position;
    if ((mode == 0 || mode == 14) && Shape.w > 0.0) {
        // EN: Boiling surface: the sphere swells and sinks along its normal by Shape.w radii (coronal heaving on a
        // star, magnetic pulsation on a neutron star).
        // PT: Superfície fervendo: a esfera incha e afunda ao longo da normal em Shape.w raios (ondulação coronal numa
        // estrela, pulsação magnética numa estrela de nêutrons).
        // ZH: 沸腾表面：球体沿法线以 Shape.w 个半径起伏（恒星的日冕起伏，中子星的磁脉动）。
        p = Position * (1.0 + Shape.w * (fbm(Position * 2.5 + vec3(Time * 0.25)) - 0.5));
    }
    if (mode == 3 || mode == 9) {
        float r = mix(Shape.x, Shape.y, pow(clamp(Position.y, 0.0, 1.0), max(Shape.z, 0.05)));
        p = vec3(Position.x * r, Position.y, Position.z * r);
        n = vec3(Position.x, 0.0, Position.z);
    } else if (Shape.x > 0.5 && mode == 4) {
        // Torus of major radius 1: the normal points away from the centre circle.
        vec2 rim = normalize(Position.xz);
        n = Position - vec3(rim.x, 0.0, rim.y);
    }
    if (mode == 5) viewVel = mat3(ModelViewMat) * vec3(-Position.z, 0.0, Position.x);
    objPos = p;
    objNormal = normalize(n);
    vec4 v = ModelViewMat * vec4(p, 1.0);
    viewPos = v.xyz;
    viewNormal = normalize(mat3(ModelViewMat) * n);
    gl_Position = ProjMat * v;
}
