package com.raishxn.gtia.mega.common.client;

import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

/** Screen-space gravitational lens of the black holes drawn this frame, applied after the level is drawn. */
final class MegaLens {

    /** View-space centre and radii (blocks) of each hole: x, y, z, shadow, Einstein ring, reach, strength. */
    private static final List<float[]> HOLES = new ArrayList<>();
    @Nullable
    private static TextureTarget copy;

    private MegaLens() {}

    static void add(Matrix4f model, float shadow, float einstein, float reach, float strength) {
        HOLES.add(new float[] { model.m30(), model.m31(), model.m32(), shadow, einstein, reach, strength });
    }

    static void apply(Matrix4f projection) {
        try {
            var shader = MegaFxShaders.lens;
            if (shader == null || HOLES.isEmpty()) return;
            var main = Minecraft.getInstance().getMainRenderTarget();
            if (copy == null || copy.width != main.width || copy.height != main.height) {
                if (copy != null) copy.destroyBuffers();
                copy = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            }
            float aspect = (float) main.width / main.height;
            for (float[] h : HOLES) {
                var centre = new Vector4f(h[0], h[1], h[2], 1).mul(projection);
                if (centre.w <= 0) continue;
                float cx = centre.x / centre.w * 0.5f + 0.5f, cy = centre.y / centre.w * 0.5f + 0.5f;
                float shadow = screen(projection, h, h[3], cy), einstein = screen(projection, h, h[4], cy),
                        reach = screen(projection, h, h[5], cy);
                if (einstein > 2f || reach < 1e-3f) continue;
                // Depth of the near side of the lens sphere: anything nearer stays unbent.
                float dist = (float) Math.sqrt(h[0] * h[0] + h[1] * h[1] + h[2] * h[2]);
                float k = Math.max(0, dist - h[5]) / Math.max(dist, 1e-3f);
                var front = new Vector4f(h[0] * k, h[1] * k, h[2] * k, 1).mul(projection);
                float frontDepth = front.w <= 0 ? 0 : front.z / front.w * 0.5f + 0.5f;
                GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
                GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, copy.frameBufferId);
                GlStateManager._glBlitFrameBuffer(0, 0, main.width, main.height, 0, 0, main.width, main.height,
                        GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
                main.bindWrite(false);
                shader.setSampler("Scene", copy.getColorTextureId());
                shader.setSampler("SceneDepth", copy.getDepthTextureId());
                shader.safeGetUniform("Center").set(cx, cy);
                shader.safeGetUniform("Einstein").set(einstein);
                shader.safeGetUniform("Horizon").set(shadow * 0.98f);
                shader.safeGetUniform("Strength").set(h[6]);
                shader.safeGetUniform("Aspect").set(aspect);
                shader.safeGetUniform("FrontDepth").set(frontDepth);
                shader.safeGetUniform("Reach").set(reach);
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.setShader(() -> shader);
                var buf = Tesselator.getInstance().getBuilder();
                buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                buf.vertex(-1, -1, 0).uv(0, 0).endVertex();
                buf.vertex(1, -1, 0).uv(1, 0).endVertex();
                buf.vertex(1, 1, 0).uv(1, 1).endVertex();
                buf.vertex(-1, 1, 0).uv(0, 1).endVertex();
                BufferUploader.drawWithShader(buf.end());
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
            }
        } finally {
            HOLES.clear();
        }
    }

    /** A view-space length at the hole, in screen-height units (as the shader measures, x scaled by aspect). */
    private static float screen(Matrix4f projection, float[] h, float size, float cy) {
        var edge = new Vector4f(h[0], h[1] + size, h[2], 1).mul(projection);
        return edge.w <= 0 ? 0 : Math.abs(edge.y / edge.w * 0.5f + 0.5f - cy);
    }
}
