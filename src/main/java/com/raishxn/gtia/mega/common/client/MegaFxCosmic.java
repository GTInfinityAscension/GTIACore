package com.raishxn.gtia.mega.common.client;

import net.minecraft.client.renderer.ShaderInstance;

import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.client.MegaFx.Effect;
import com.raishxn.gtia.mega.common.client.MegaFxRenderer.Placement;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.raishxn.gtia.mega.common.client.MegaFxRenderer.*;

/**
 * The cosmic additions of the effect pass (GTIA-D-324): a black hole's photon sphere and lensed halo, matter streams
 * along curves, dipole field arcs, orbiting matter and the gravity well. Drawn in the additive pass of
 * {@link MegaFxRenderer}.
 */
final class MegaFxCosmic {

    private MegaFxCosmic() {}

    /**
     * Light round an event horizon of radius {@code r}: the photon sphere (a thin rim just outside, 1.04 r) and the
     * far side of the disk bent over and under the shadow, brightest across the disk's plane as seen on screen.
     */
    static void horizonLight(Effect e, Placement p, Matrix4f proj, ShaderInstance sh, float level, float r) {
        set(sh, 11, mix(e.colourA(), 0.6f), e.colourB(), new float[] { 0.9f * level, 0, 0, 0 },
                null, null);
        draw(MegaFxMeshes.sphere(), at(p, e, e.a(), e.axis(), r * 1.04f, r * 1.04f, r * 1.04f), proj, sh);
        // The disk's normal in view space: the halo arcs cross it, and close into a full ring seen face-on.
        var model = at(p, e, e.a(), e.axis(), 1, 1, 1);
        var n = model.transformDirection(new Vector3f(0, 1, 0)).normalize();
        float size = r * 2.6f;
        set(sh, 12, e.colourA(), e.colourB(), new float[] { 0.8f * level, r * 1.04f / size, 0, 0 },
                new float[] { n.x, n.y, Math.abs(n.z), 0 }, new float[] { size, 0, 0, 0 });
        draw(MegaFxMeshes.quad(), at(p, e, e.a(), new float[3], 1, 1, 1), proj, sh);
    }

    static void effect(MegaMachine m, Effect e, Placement p, Matrix4f proj, ShaderInstance sh, float level, float scale) {
        float[] pr = e.params();
        switch (e.type()) {
            case STREAM -> {
                // Params = (brightness, grid cells around, flow speed, reverse); r0 at the start, r1 at the end.
                if (e.points().length < 2) return;
                set(sh, 13, e.colourA(), e.colourB(), new float[] { pr[0] * level, pr[1] > 0 ? pr[1] : 6,
                        pr[2] > 0 ? pr[2] : 0.25f, pr[3] }, null, null);
                var mesh = MegaFxCurves.stream(m.shapeName() + "/" + e.name(), e.points(), e.r0(), e.r1());
                draw(mesh, at(p, e, e.points()[0], new float[3], 1, 1, 1), proj, sh);
            }
            case ARCS -> {
                // Field lines out to r1 (L); r0 = the star's radius. Params = (brightness, pulses, speed, thickness).
                float l = e.r1() * scale;
                set(sh, 15, e.colourA(), e.colourB(), new float[] { pr[0] * level, pr[1] > 0 ? pr[1] : 3,
                        pr[2] > 0 ? pr[2] : 0.6f, 0 }, null, null);
                var mesh = MegaFxCurves.arcs(Math.max(1, e.count()), e.r0() / Math.max(1e-3f, e.r1()),
                        (pr[3] > 0 ? pr[3] : 0.6f) / Math.max(1e-3f, e.r1()));
                draw(mesh, at(p, e, e.a(), e.axis(), l, l, l), proj, sh);
            }
            case ORBIT -> {
                // Params = (brightness, half thickness, inner angular speed rad/s, sprite size).
                set(sh, 17, e.colourA(), e.colourB(), new float[] { pr[0] * level, pr[1], 0, 0 }, null,
                        new float[] { e.r0() * scale, e.r1() * scale, pr[2] > 0 ? pr[2] : 0.4f,
                                pr[3] > 0 ? pr[3] : 0.5f });
                draw(MegaFxMeshes.particles(Math.max(1, e.count())), at(p, e, e.a(), e.axis(), 1, 1, 1), proj, sh);
            }
            case WELL -> {
                // Grid of half-size r1 sinking r0 at the centre. Params = (brightness, lines across).
                float half = e.r1() * scale;
                set(sh, 16, e.colourA(), e.colourB(), new float[] { pr[0] * level, pr[1] > 0 ? pr[1] : 24,
                        1 / Math.max(1e-3f, e.r0() / Math.max(1e-3f, e.r1())), 0 }, null, null);
                draw(MegaFxCurves.well(e.r0() / Math.max(1e-3f, e.r1())), at(p, e, e.a(), e.axis(), half, half, half),
                        proj, sh);
            }
            default -> {}
        }
    }
}
