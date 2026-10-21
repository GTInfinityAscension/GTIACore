package com.raishxn.gtia.mega.common.client;

import net.minecraft.client.renderer.ShaderInstance;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.client.MegaFx.Effect;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws a megastructure's effects with the {@code gtia:mega_fx} shader: opaque bodies first (photospheres, event
 * horizons, planets), then the additive light (coronas, bloom, prominences, jets, particles, fields, disks). Each
 * effect's strength, temperature and size can be changed by the machine ({@link MegaMachine#fxLevel} and friends).
 */
final class MegaFxRenderer {

    /** Places effects: the world matrix of a track (null: fixed) for a machine. */
    interface Placement {

        Matrix4f track(String track);

        /** Grid cells to the model origin (pivot), as a translation in grid space. */
        Vector3f pivot();

        /** Grid axes as a world matrix. */
        Matrix4f axes();
    }

    /** Dev aid ({@code /gtia_fx_debug <mask>}): bits that hide effect passes, see {@link #hidden}. */
    static int debugMask;

    private MegaFxRenderer() {}

    /**
     * Debug mask bits: 1 photosphere, 2 corona, 4 wide bloom, 8 prominences, 16 jets, 32 beams, 64 fields, 128 disks,
     * 256 holes, 512 planets, 1024 glows.
     */
    private static boolean hidden(int bit) {
        return (debugMask & bit) != 0;
    }

    static void draw(MegaMachine m, MegaFx fx, Placement place, Matrix4f projection, double seconds) {
        var opaque = MegaFxShaders.fx;
        var glow = MegaFxShaders.fxAdd;
        if (opaque == null || glow == null) return;
        float time = (float) (seconds % 3600.0);
        for (var sh : new ShaderInstance[] { opaque, glow }) sh.safeGetUniform("Time").set(time);
        try {
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            for (var e : fx.effects) if (shown(m, e)) bodies(m, e, place, projection, opaque);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.depthMask(false);
            for (var e : fx.effects) if (shown(m, e)) light(m, e, place, projection, glow);
        } finally {
            VertexBuffer.unbind();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
        }
    }

    private static boolean shown(MegaMachine m, Effect e) {
        if (e.phase() > Math.max(1, m.phases()) || e.active() && !m.isRunning()) return false;
        return m.fxLevel(e.name()) > 0;
    }

    // ------------------------------------------------------------------ placement

    /** Model matrix of an effect point in grid cells, turned so +Y follows {@code axis}, scaled by (sx, sy, sz). */
    private static Matrix4f at(Placement p, Effect e, float[] point, float[] axis, float sx, float sy, float sz) {
        var pv = p.pivot();
        var mat = new Matrix4f(p.track(e.track())).mul(p.axes())
                .translate(point[0] - pv.x, point[1] - pv.y, point[2] - pv.z);
        var dir = new Vector3f(axis[0], axis[1], axis[2]);
        if (dir.lengthSquared() > 1e-8) mat.rotate(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), dir.normalize()));
        return mat.scale(sx, sy, sz);
    }

    private static float[] sub(float[] b, float[] a) {
        return new float[] { b[0] - a[0], b[1] - a[1], b[2] - a[2] };
    }

    private static float len(float[] v) {
        return (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    // ------------------------------------------------------------------ passes

    private static void bodies(MegaMachine m, Effect e, Placement p, Matrix4f proj, ShaderInstance sh) {
        float level = m.fxLevel(e.name()), scale = m.fxScale(e.name());
        switch (e.type()) {
            case STAR -> {
                if (hidden(1)) return;
                float t = temperature(m, e);
                float[] hot = vivid(MegaFx.blackbody(t)), limb = vivid(MegaFx.blackbody(t * 0.62));
                set(sh, 0, hot, limb, new float[] { 1.3f * Math.min(level, 1.4f), 30, e.params()[2], e.params()[3] },
                        null, null);
                draw(MegaFxMeshes.sphere(), at(p, e, e.a(), e.axis(), e.r0() * scale, e.r0() * scale,
                        e.r0() * scale), proj, sh);
            }
            case HOLE -> {
                set(sh, 6, null, null, null, null, null);
                float r = e.r0() * scale;
                var model = at(p, e, e.a(), e.axis(), r, r, r);
                draw(MegaFxMeshes.sphere(), model, proj, sh);
                // EN: Lens: Einstein radius ~2.2 shadows here (it grows with the square root of the distance to the
                // light behind; the sky is far, so this is a look, not a measure).
                // PT: Lente: raio de Einstein ~2,2 sombras aqui (cresce com a raiz da distância até a luz de trás;
                // o céu está longe, então é aparência, não medida).
                // ZH: 透镜：此处爱因斯坦半径约为 2.2 个阴影半径（随到后方光源距离的平方根增长；天空很远，因此这是外观而非测量）。
                MegaLens.add(model, r, r * 2.2f, r * 8f, Math.min(1f, level));
            }
            case PLANET -> {
                float r = e.r0() * scale;
                float[] light = sub(e.b(), e.a());
                float l = Math.max(1e-4f, len(light));
                set(sh, 10, e.colourA(), e.colourB(), new float[] { e.params()[0] * level, e.params()[1],
                        e.params()[2], e.params()[3] }, new float[] { light[0] / l, light[1] / l, light[2] / l, 0 },
                        null);
                draw(MegaFxMeshes.sphere(), at(p, e, e.a(), new float[3], r, r, r), proj, sh);
            }
            default -> {}
        }
    }

    private static void light(MegaMachine m, Effect e, Placement p, Matrix4f proj, ShaderInstance sh) {
        float level = m.fxLevel(e.name()), scale = m.fxScale(e.name());
        float[] pr = e.params();
        switch (e.type()) {
            case STAR -> {
                float t = temperature(m, e), r = e.r0() * scale;
                float[] c = vivid(MegaFx.blackbody(t));
                float corona = pr[0] > 0 ? pr[0] : 3.2f;
                if (e.count() > 0 && !hidden(8)) {
                    // H-alpha red: prominences glow in the hydrogen line, not in the photosphere's colour.
                    set(sh, 2, new float[] { 1f, 0.28f, 0.22f, 1 }, new float[] { 0.9f, 0.35f, 0.3f, 1 },
                            new float[] { 0.75f * level, 5, 0.22f, pr[3] }, null, null);
                    draw(MegaFxMeshes.prominences(e.count(), (int) pr[3]), at(p, e, e.a(), e.axis(), r, r, r), proj,
                            sh);
                }
                // Corona with streamers, then a wide soft bloom.
                if (!hidden(2)) {
                    set(sh, 7, boost(c, 0.95f), new float[] { c[0], c[1] * 0.7f, c[2] * 0.5f, 1 },
                        new float[] { 0.6f * level, 1 / corona, 4.2f, 0.85f },
                        new float[] { 0, 0, 0, pr[3] }, new float[] { r * corona, r, r, 0 });
                    draw(MegaFxMeshes.quad(), at(p, e, e.a(), new float[3], 1, 1, 1), proj, sh);
                }
                if (!hidden(4)) {
                    set(sh, 7, boost(c, 0.32f), boost(c, 0.12f), new float[] { 0.3f * level, 1 / 8.5f, 5.5f, 0 },
                            new float[] { 0, 0, 0, 0 }, new float[] { r * 8.5f, r, r, 0 });
                    draw(MegaFxMeshes.quad(), at(p, e, e.a(), new float[3], 1, 1, 1), proj, sh);
                }
            }
            case JET -> {
                if (!hidden(16)) jet(e, p, proj, sh, level, scale);
            }
            case BEAM -> {
                if (hidden(32)) return;
                float[] d = sub(e.b(), e.a());
                float l = len(d), r = e.r0() * scale;
                set(sh, 9, e.colourA(), e.colourB(), new float[] { pr[0] * level, pr[1], pr[2], pr[3] }, null,
                        new float[] { r, e.r1() > 0 ? e.r1() * scale : r, 1, 0 });
                draw(MegaFxMeshes.tube(), at(p, e, e.a(), d, 1, l, 1), proj, sh);
                if (e.count() > 0) {
                    set(sh, 8, e.colourA(), e.colourB(), new float[] { 0.8f * level, 0, 0, 0 }, null,
                            new float[] { r * 0.8f, r * 0.8f, 0.35f, Math.max(0.4f, r * 0.35f) });
                    draw(MegaFxMeshes.particles(e.count()), at(p, e, e.a(), d, 1, l, 1), proj, sh);
                }
                for (float[] end : new float[][] { e.a(), e.b() }) {
                    set(sh, 7, e.colourA(), e.colourB(), new float[] { 0.7f * level, 0, 5, 0.3f }, new float[4],
                            new float[] { r * 4, 0, 0, 0 });
                    draw(MegaFxMeshes.quad(), at(p, e, end, new float[3], 1, 1, 1), proj, sh);
                }
            }
            case FIELD -> {
                if (hidden(64)) return;
                float r = e.r0() * scale;
                boolean torus = "torus".equals(e.shape());
                set(sh, 4, e.colourA(), e.colourB(), new float[] { pr[0] * level, pr[1], pr[2], pr[3] }, null,
                        new float[] { torus ? 1 : 0, 0, 0, 0 });
                var mesh = torus ? MegaFxMeshes.torus(Math.round(1000 * e.r1() / Math.max(1e-3f, e.r0()))) :
                        MegaFxMeshes.sphere();
                draw(mesh, at(p, e, e.a(), e.axis(), r, r, r), proj, sh);
            }
            case DISK -> {
                float r = e.r1() * scale;
                set(sh, 5, e.colourA(), e.colourB(), new float[] { e.r0() / Math.max(1e-3f, e.r1()),
                        pr[0] * level, pr[1], pr[2] }, null, null);
                draw(MegaFxMeshes.square(), at(p, e, e.a(), e.axis(), r, r, r), proj, sh);
            }
            case HOLE -> {
                // Photon ring just outside the shadow, and a faint glow.
                float r = e.r0() * scale;
                set(sh, 7, e.colourA(), e.colourB(), new float[] { 0.8f * level, 1 / 3f, 9, 0 },
                        new float[] { 0, 0.36f, 0.025f, 0 }, new float[] { r * 3, r * 1.05f, 0, 0 });
                draw(MegaFxMeshes.quad(), at(p, e, e.a(), new float[3], 1, 1, 1), proj, sh);
            }
            case PLANET -> {
                // Atmosphere rim.
                float r = e.r0() * scale;
                set(sh, 7, e.colourA(), e.colourB(), new float[] { 0.5f * level, 0.8f, 14, 0 }, new float[4],
                        new float[] { r * 1.25f, r * 0.2f, 0, 0 });
                draw(MegaFxMeshes.quad(), at(p, e, e.a(), new float[3], 1, 1, 1), proj, sh);
            }
            case GLOW -> {
                set(sh, 7, e.colourA(), e.colourB(), new float[] { pr[0] * level, pr[1], pr[2] > 0 ? pr[2] : 5,
                        pr[3] }, new float[4], new float[] { e.r0() * scale, e.r1(), 0, 0 });
                draw(MegaFxMeshes.quad(), at(p, e, e.a(), new float[3], 1, 1, 1), proj, sh);
            }
        }
    }

    /**
     * A rocket or plasma jet: a wide sheath and a white-hot core (both with shock diamonds and turbulence), particles
     * streaming out and a glow at the nozzle. Params: brightness, diamonds, flow speed, turbulence.
     */
    private static void jet(Effect e, Placement p, Matrix4f proj, ShaderInstance sh, float level, float scale) {
        float[] d = sub(e.b(), e.a());
        float l = len(d), r0 = e.r0() * scale, r1 = e.r1() * scale;
        float[] pr = e.params();
        var model = at(p, e, e.a(), d, 1, l, 1);
        set(sh, 3, e.colourA(), e.colourB(), new float[] { 0.32f * pr[0] * level, pr[1], pr[2], pr[3] }, null,
                new float[] { r0, r1, 0.7f, 0 });
        draw(MegaFxMeshes.tube(), model, proj, sh);
        set(sh, 3, mix(e.colourA(), 0.5f), e.colourA(),
                new float[] { 0.6f * pr[0] * level, pr[1], pr[2] * 1.4f, 0.5f },
                null, new float[] { r0 * 0.38f, r1 * 0.16f, 0.6f, 0 });
        draw(MegaFxMeshes.tube(), model, proj, sh);
        if (e.count() > 0) {
            set(sh, 8, e.colourA(), e.colourB(), new float[] { 0.55f * level, 0, 0, 0 }, null,
                    new float[] { r0 * 0.7f, r1 * 0.85f, 0.22f, Math.max(0.6f, r0 * 0.14f) });
            draw(MegaFxMeshes.particles(e.count()), model, proj, sh);
        }
        set(sh, 7, mix(e.colourA(), 0.5f), e.colourA(), new float[] { 0.9f * level, 0.0f, 4.5f, 0.35f },
                new float[4], new float[] { r0 * 3.2f, 0, 0, 0 });
        draw(MegaFxMeshes.quad(), at(p, e, e.a(), new float[3], 1, 1, 1), proj, sh);
    }

    // ------------------------------------------------------------------ helpers

    private static float temperature(MegaMachine m, Effect e) {
        float t = m.fxTemperature(e.name());
        return t > 0 ? t : e.temp();
    }

    /**
     * A black-body colour with its saturation raised: a real G star is nearly white, which hides the granulation and
     * the limb; this keeps the hue the temperature gives and makes it read.
     */
    private static float[] vivid(float[] c) {
        float grey = (c[0] + c[1] + c[2]) / 3;
        float[] v = new float[4];
        float max = 0;
        for (int i = 0; i < 3; i++) max = Math.max(max, v[i] = Math.max(0, grey + (c[i] - grey) * 2.2f));
        for (int i = 0; i < 3; i++) v[i] /= Math.max(max, 1e-3f);
        v[3] = 1;
        return v;
    }

    private static float[] boost(float[] c, float f) {
        return new float[] { c[0] * f, c[1] * f, c[2] * f, 1 };
    }

    /** Colour mixed towards white by {@code f}. */
    private static float[] mix(float[] c, float f) {
        return new float[] { c[0] + (1 - c[0]) * f, c[1] + (1 - c[1]) * f, c[2] + (1 - c[2]) * f, 1 };
    }

    private static void set(ShaderInstance sh, int mode, float[] a, float[] b, float[] params, float[] params2,
                            float[] shape) {
        sh.safeGetUniform("Mode").set((float) mode);
        if (a != null) sh.safeGetUniform("ColorA").set(a[0], a[1], a[2], a.length > 3 ? a[3] : 1);
        if (b != null) sh.safeGetUniform("ColorB").set(b[0], b[1], b[2], b.length > 3 ? b[3] : 1);
        float[] z = { 0, 0, 0, 0 };
        float[] p = params == null ? z : params, q = params2 == null ? z : params2, s = shape == null ? z : shape;
        sh.safeGetUniform("Params").set(p[0], p[1], p[2], p[3]);
        sh.safeGetUniform("Params2").set(q[0], q[1], q[2], q[3]);
        sh.safeGetUniform("Shape").set(s[0], s[1], s[2], s[3]);
    }

    private static void draw(VertexBuffer mesh, Matrix4f model, Matrix4f proj, ShaderInstance sh) {
        mesh.bind();
        mesh.drawWithShader(model, proj, sh);
    }
}
