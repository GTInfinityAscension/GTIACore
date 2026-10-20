package com.raishxn.gtia.mega.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A megastructure's moving parts: taken out of the world while it runs and drawn moving by the renderer, put back
 * when it stops (as GTNA does with the collider's rings).
 */
public final class MegaCapture {

    private record Key(String name, BlockPos controller, Direction front) {}

    private static final Map<Key, Map<MegaShape.Part, long[]>> POSITIONS = new ConcurrentHashMap<>();

    private MegaCapture() {}

    /** World positions of every moving part for a controller placement. */
    public static Map<MegaShape.Part, long[]> positions(String name, BlockPos controller, Direction front) {
        return POSITIONS.computeIfAbsent(new Key(name, controller.immutable(), front), k -> {
            if (POSITIONS.size() > 16) POSITIONS.clear();
            var shape = MegaShape.get(name);
            Map<MegaShape.Part, LongArrayList> found = new java.util.LinkedHashMap<>();
            if (shape != null) for (int i = 0; i < shape.nz; i++)
                for (int j = 0; j < shape.ny; j++)
                    for (int x = 0; x < shape.nx; x++) {
                        var part = shape.part(shape.letter(x, j, i));
                        if (part == null || !part.moving()) continue;
                        found.computeIfAbsent(part, p -> new LongArrayList())
                                .add(shape.world(k.controller(), front, x, j, i).asLong());
                    }
            Map<MegaShape.Part, long[]> out = new java.util.LinkedHashMap<>();
            found.forEach((p, list) -> out.put(p, list.toLongArray()));
            return out;
        });
    }

    public static Block block(MegaShape.Part part) {
        var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(part.block()));
        return block == null ? Blocks.AIR : block;
    }

    static void capture(MegaMachine m, int phases) {
        var level = m.getLevel();
        var air = Blocks.AIR.defaultBlockState();
        // Each removed block would otherwise trigger a structure check.
        NexusBuildCheckGuard.quiet(m.getMultiblockState(), () -> positions(m.shapeName(), m.getPos(), m.getFrontFacing())
                .forEach((part, list) -> {
                    if (part.phase() > phases) return;
                    for (long p : list) set(level, BlockPos.of(p), air);
                }));
    }

    /** Puts back, where there is air, the rings of the phases that were captured. */
    static void release(MegaMachine m, int phases) {
        var level = m.getLevel();
        NexusBuildCheckGuard.quiet(m.getMultiblockState(), () -> positions(m.shapeName(), m.getPos(), m.getFrontFacing())
                .forEach((part, list) -> {
                    if (part.phase() > phases) return;
                    var state = block(part).defaultBlockState();
                    for (long p : list) {
                        var pos = BlockPos.of(p);
                        if (level.getBlockState(pos).isAir()) set(level, pos, state);
                    }
                }));
    }

    /** Sets a plain block straight in its chunk, with the light check and the client update (no neighbour work). */
    private static void set(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server)) {
            level.setBlock(pos, state, 2 | 16);
            return;
        }
        var chunk = server.getChunkAt(pos);
        var old = chunk.setBlockState(pos, state, false);
        if (old == null || old == state) return;
        server.getChunkSource().getLightEngine().checkBlock(pos);
        server.getChunkSource().blockChanged(pos);
        chunk.setUnsaved(true);
    }
}
