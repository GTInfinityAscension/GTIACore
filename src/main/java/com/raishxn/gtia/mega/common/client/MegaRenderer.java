package com.raishxn.gtia.mega.common.client;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Draws every megastructure's effects ({@link MegaFx}: stars, jets, fields, black holes; never blocks) and, while
 * one runs, the moving pieces it took out of the world, each moved by its track.
 */
public final class MegaRenderer {

    private static final Map<BlockPos, MegaMachine> QUEUE = new LinkedHashMap<>();
    private static final Map<Placement, Baked> BAKED = new HashMap<>();

    private record Placement(String name, BlockPos controller, Direction front) {}

    /** GPU meshes of one placement, built once. */
    private static final class Baked {

        final Vec3 pivot;
        final Matrix3f axes;
        final Map<MegaShape.Part, VertexBuffer> rings = new java.util.LinkedHashMap<>();

        Baked(Vec3 pivot, Matrix3f axes) {
            this.pivot = pivot;
            this.axes = axes;
        }

        void close() {
            rings.values().forEach(VertexBuffer::close);
        }
    }

    private MegaRenderer() {}

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            MegaLens.apply(new Matrix4f(event.getProjectionMatrix()));
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || QUEUE.isEmpty()) return;
        try {
            Vec3 cam = event.getCamera().getPosition();
            Matrix4f projection = new Matrix4f(event.getProjectionMatrix());
            double seconds = (net.minecraft.Util.getMillis() % 86_400_000L) / 1000.0;
            for (var m : QUEUE.values()) {
                if (m.isInValid() || !m.isFormed() || m.getLevel() == null) continue;
                var shape = m.shape();
                var motion = MegaMotion.get(m.shapeName());
                if (shape == null || motion == null) continue;
                var baked = BAKED.computeIfAbsent(
                        new Placement(m.shapeName(), m.getPos().immutable(), m.getFrontFacing()),
                        p -> bake(shape, p));
                drawRings(m, baked, motion, event.getPoseStack(), cam, projection, seconds);
                var fx = MegaFx.get(m.shapeName());
                if (fx != null) MegaFxRenderer.draw(m, fx, placement(shape, baked, motion, event.getPoseStack(), cam,
                        seconds), projection, seconds);
            }
        } finally {
            QUEUE.clear();
        }
    }

    /** World matrix of a track: grid rotation turned into world axes, about the pivot, plus its shift. */
    private static Matrix4f model(PoseStack pose, Baked b, MegaMotion.Track track, Vec3 cam, double seconds) {
        var q = new Quaternionf();
        var shift = new Vector3f();
        track.at(seconds, q, shift);
        Matrix3f r = new Matrix3f(b.axes).mul(new Matrix3f().rotation(q)).mul(new Matrix3f(b.axes).transpose());
        shift.mul(b.axes);
        return new Matrix4f(pose.last().pose())
                .translate((float) (b.pivot.x - cam.x) + shift.x, (float) (b.pivot.y - cam.y) + shift.y,
                        (float) (b.pivot.z - cam.z) + shift.z)
                .mul(new Matrix4f().set(r));
    }

    private static void drawRings(MegaMachine m, Baked b, MegaMotion motion, PoseStack pose,
                                  Vec3 cam, Matrix4f projection, double seconds) {
        int phases = m.capturedPhases();
        if (phases == 0) return;
        var shader = GameRenderer.getRendertypeCutoutShader();
        if (shader == null) return;
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        for (var part : m.shape().parts.values()) {
            if (!part.moving() || part.phase() > phases) continue;
            var mesh = b.rings.computeIfAbsent(part, p -> ringMesh(m, b, p));
            var track = motion.tracks.get(part.track());
            if (mesh == null || track == null) continue;
            mesh.bind();
            mesh.drawWithShader(model(pose, b, track, cam, seconds), projection, shader);
        }
        VertexBuffer.unbind();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
    }

    /** Effect placement of one machine this frame: tracks as world matrices, grid axes, pivot. */
    private static MegaFxRenderer.Placement placement(MegaShape shape, Baked b, MegaMotion motion, PoseStack pose,
                                                      Vec3 cam, double seconds) {
        var still = new Matrix4f(pose.last().pose()).translate((float) (b.pivot.x - cam.x),
                (float) (b.pivot.y - cam.y), (float) (b.pivot.z - cam.z));
        var axes = new Matrix4f().set(b.axes);
        var pivot = new Vector3f(shape.pivotX, shape.pivotY, shape.pivotZ);
        var cache = new HashMap<String, Matrix4f>();
        return new MegaFxRenderer.Placement() {

            @Override
            public Matrix4f track(String track) {
                var t = track == null ? null : motion.tracks.get(track);
                return t == null ? still : cache.computeIfAbsent(track, k -> model(pose, b, t, cam, seconds));
            }

            @Override
            public Vector3f pivot() {
                return pivot;
            }

            @Override
            public Matrix4f axes() {
                return axes;
            }
        };
    }

    // ------------------------------------------------------------------ baking

    private static Baked bake(MegaShape shape, Placement p) {
        if (BAKED.size() > 4) {
            BAKED.values().forEach(Baked::close);
            BAKED.clear();
        }
        float[] a = MegaShape.axes(p.front());
        var axes = new Matrix3f(a[0], a[3], a[6], a[1], a[4], a[7], a[2], a[5], a[8]);
        var rel = new Vector3f(shape.pivotX - shape.ck, shape.pivotY - shape.cj, shape.pivotZ - shape.ci).mul(axes);
        var pivot = new Vec3(p.controller().getX() + 0.5 + rel.x, p.controller().getY() + 0.5 + rel.y,
                p.controller().getZ() + 0.5 + rel.z);
        return new Baked(pivot, axes);
    }

    /** A captured ring from its block models, positioned around the pivot. */
    private static VertexBuffer ringMesh(MegaMachine m, Baked b, MegaShape.Part part) {
        long[] positions = MegaCapture.positions(m.shapeName(), m.getPos(), m.getFrontFacing()).get(part);
        if (positions == null || positions.length == 0) return null;
        long t0 = System.nanoTime();
        var occupied = new LongOpenHashSet(positions);
        var state = MegaCapture.block(part).defaultBlockState();
        var level = m.getLevel();
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        var blockModel = dispatcher.getBlockModel(state);
        var random = RandomSource.create(42);
        var builder = new BufferBuilder(Math.min(positions.length * 4 * 4, 1 << 18) *
                DefaultVertexFormat.BLOCK.getVertexSize());
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        var ps = new PoseStack();
        float[] shades = new float[6];
        for (Direction side : Direction.values()) shades[side.ordinal()] = level.getShade(side, true);
        for (long packed : positions) {
            var pos = BlockPos.of(packed);
            ps.pushPose();
            ps.translate(pos.getX() - b.pivot.x, pos.getY() - b.pivot.y, pos.getZ() - b.pivot.z);
            for (Direction side : Direction.values()) {
                if (occupied.contains(BlockPos.offset(packed, side))) continue;
                random.setSeed(42);
                float shade = shades[side.ordinal()];
                int light = LevelRenderer.getLightColor(level, pos.relative(side));
                for (var quad : blockModel.getQuads(state, side, random, ModelData.EMPTY, null))
                    builder.putBulkData(ps.last(), quad, quad.isShade() ? shade : 1f, quad.isShade() ? shade : 1f,
                            quad.isShade() ? shade : 1f, light, OverlayTexture.NO_OVERLAY);
            }
            ps.popPose();
        }
        var mesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        mesh.bind();
        mesh.upload(builder.end());
        VertexBuffer.unbind();
        GTIAMod.LOGGER.info("[GTIA perf] {} {} mesh: {} blocks in {} ms", m.shapeName(), part.track(), positions.length,
                (System.nanoTime() - t0) / 1_000_000);
        return mesh;
    }

    // ------------------------------------------------------------------ dynamic render hook

    public static final class Render extends DynamicRender<MegaMachine, Render> {

        public static final Codec<Render> CODEC = Codec.unit(Render::new);
        public static final DynamicRenderType<MegaMachine, Render> TYPE = DynamicRenderManager
                .register(GTIAMod.id("mega/render"), new DynamicRenderType<>(CODEC));

        @Override
        public DynamicRenderType<MegaMachine, Render> getType() {
            return TYPE;
        }

        @Override
        public boolean shouldRenderOffScreen(MegaMachine machine) {
            return true;
        }

        @Override
        public int getViewDistance() {
            return 512;
        }

        @Override
        public boolean shouldRender(MegaMachine machine, Vec3 cameraPos) {
            return machine.isFormed();
        }

        @Override
        public void render(MegaMachine machine, float partialTicks, PoseStack poseStack,
                           MultiBufferSource buffer, int light, int overlay) {
            if (machine.getLevel() != null && machine.isFormed()) QUEUE.put(machine.getPos().immutable(), machine);
        }

        @Override
        public AABB getRenderBoundingBox(MegaMachine machine) {
            var shape = machine.shape();
            int reach = shape == null ? 64 : Math.max(shape.nx, Math.max(shape.ny, shape.nz)) + 64;
            return new AABB(machine.getPos()).inflate(reach);
        }
    }
}
