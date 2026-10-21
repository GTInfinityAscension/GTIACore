package com.raishxn.gtia.mega.common.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.raishxn.gtia.mega.system.SkyStarNetwork;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.raishxn.gtia.mega.common.client.MegaFxRenderer.draw;
import static com.raishxn.gtia.mega.common.client.MegaFxRenderer.mix;
import static com.raishxn.gtia.mega.common.client.MegaFxRenderer.set;

/**
 * The sun in the sky of a star system: the system's own star with the photosphere shader, coloured by its temperature
 * and sized by its radius; a white dwarf or neutron star as a hot point, a black hole as a shadow with its photon ring.
 * The Ad Astra sky of these dimensions keeps its stars but no sun texture.
 */
public final class MegaSkyStar {

    /** Where the star hangs: high in the south-east, fixed (the systems have no day cycle). */
    private static final Vector3f DIR = new Vector3f(0.45f, 0.78f, -0.43f).normalize();
    private static final float DISTANCE = 120;

    private MegaSkyStar() {}

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        var sky = SkyStarNetwork.current;
        var level = Minecraft.getInstance().level;
        if (sky.kind() == Kind.NONE || level == null ||
                !level.dimension().location().toString().equals(sky.dimension()))
            return;
        var opaque = MegaFxShaders.fx;
        var glow = MegaFxShaders.fxAdd;
        if (opaque == null || glow == null) return;
        float time = (float) ((net.minecraft.Util.getMillis() % 3_600_000L) / 1000.0);
        for (var sh : new net.minecraft.client.renderer.ShaderInstance[] { opaque, glow })
            sh.safeGetUniform("Time").set(time);
        // EN: Angular radius 0.05 rad (about ten Suns as seen from Earth) times the drawn scale, so a red dwarf
        // still reads and a blue giant fills a good part of the sky.
        // PT: Raio angular de 0,05 rad (umas dez vezes o Sol visto da Terra) vezes a escala desenhada, para a anã
        // vermelha ainda aparecer e a gigante azul tomar boa parte do céu.
        // ZH: 角半径 0.05 弧度（约为地球所见太阳的十倍）乘以绘制比例，使红矮星仍可见，蓝巨星占据大片天空。
        float r = DISTANCE * 0.05f * Math.max(0.3f, sky.scale());
        var model = new Matrix4f(event.getPoseStack().last().pose())
                .translate(DIR.x * DISTANCE, DIR.y * DISTANCE, DIR.z * DISTANCE);
        var proj = new Matrix4f(event.getProjectionMatrix());
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            var body = new Matrix4f(model).scale(r);
            boolean hole = sky.kind() == Kind.BLACK_HOLE;
            float[] hot = hole ? new float[] { 1, 0.78f, 0.45f, 1 } : MegaFx.blackbody(sky.temperature());
            if (hole) {
                set(opaque, 6, null, null, null, null, null);
                draw(MegaFxMeshes.sphere(), body, proj, opaque);
            } else if (sky.kind() == Kind.NEUTRON_STAR) {
                set(opaque, 14, new float[] { 0.75f, 0.85f, 1, 1 }, new float[] { 0.35f, 0.5f, 1, 1 },
                        new float[] { 1.2f, 7, 0, 0 }, null, null);
                draw(MegaFxMeshes.sphere(), body, proj, opaque);
            } else {
                set(opaque, 0, hot, MegaFx.blackbody(sky.temperature() * 0.62f), new float[] { 1.3f, 30, 0, 0 },
                        null, null);
                draw(MegaFxMeshes.sphere(), body, proj, opaque);
            }
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            if (hole) {
                set(glow, 11, mix(hot, 0.6f), new float[] { 1, 0.5f, 0.2f, 1 }, new float[] { 0.9f, 0, 0, 0 },
                        null, null);
                draw(MegaFxMeshes.sphere(), new Matrix4f(model).scale(r * 1.04f), proj, glow);
                // Halo seen nearly face-on from the planet: a full ring of lensed disk light.
                float size = r * 2.6f;
                set(glow, 12, hot, new float[] { 0.9f, 0.4f, 0.7f, 1 }, new float[] { 0.8f, r * 1.04f / size, 0, 0 },
                        new float[] { 0, 1, 0.2f, 0 }, new float[] { size, 0, 0, 0 });
                draw(MegaFxMeshes.quad(), model, proj, glow);
            } else {
                // Corona with streamers, then the wide bloom (as the in-world stars).
                set(glow, 7, hot, new float[] { hot[0], hot[1] * 0.7f, hot[2] * 0.5f, 1 },
                        new float[] { 0.6f, 1 / 3.2f, 4.2f, 0.85f }, new float[4], new float[] { r * 3.2f, r, r, 0 });
                draw(MegaFxMeshes.quad(), model, proj, glow);
                set(glow, 7, scale(hot, 0.32f), scale(hot, 0.12f), new float[] { 0.3f, 1 / 8.5f, 5.5f, 0 },
                        new float[4], new float[] { r * 8.5f, r, r, 0 });
                draw(MegaFxMeshes.quad(), model, proj, glow);
            }
        } finally {
            VertexBuffer.unbind();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
        }
    }

    private static float[] scale(float[] c, float f) {
        return new float[] { c[0] * f, c[1] * f, c[2] * f, 1 };
    }
}
