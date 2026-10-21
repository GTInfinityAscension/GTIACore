package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;

/** Building and probing megastructures in GameTests; loaded only when one is registered (it touches GTNA). */
final class MegaTestRig {

    static final Direction FRONT = Direction.NORTH;

    private MegaTestRig() {}

    /**
     * Builds the phases up to {@code phases} 250 blocks above the test (clear of the terrain) and returns the
     * controller. GameTests run side by side 63 blocks apart and other tests leave blocks around, so each test builds
     * 3000 blocks away and {@code 400 * slot} blocks to the east of the others.
     */
    @SuppressWarnings("unchecked")
    static <M extends MegaMachine> M build(GameTestHelper helper, String name, MultiblockMachineDefinition definition,
                                           int phases, int slot) {
        var shape = MegaShape.get(name);
        var level = helper.getLevel();
        // 250 blocks up, lower for tall shapes so the top stays under the build limit.
        int up = Math.min(250, 314 - (shape.ny - shape.cj) - helper.absolutePos(BlockPos.ZERO).getY());
        BlockPos controller = helper.absolutePos(new BlockPos(2 + 400 * slot, up, 3000));
        // GTCEu's walk refuses unloaded chunks: keep the whole footprint loaded for the test.
        int reach = Math.max(shape.nx, shape.nz) + 2;
        for (int cx = (controller.getX() - reach) >> 4; cx <= (controller.getX() + reach) >> 4; cx++)
            for (int cz = (controller.getZ() - reach) >> 4; cz <= (controller.getZ() + reach) >> 4; cz++)
                level.setChunkForced(cx, cz, true);
        // The test world is kept between runs: clear what an earlier run (or an older shape) left in the box.
        var air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < shape.nz; i++)
            for (int j = 0; j < shape.ny; j++)
                for (int k = 0; k < shape.nx; k++) {
                    var pos = shape.world(controller, FRONT, k, j, i);
                    if (!level.getBlockState(pos).isAir()) level.setBlock(pos, air, 2 | 16);
                }
        for (int i = 0; i < shape.nz; i++)
            for (int j = 0; j < shape.ny; j++)
                for (int k = 0; k < shape.nx; k++) {
                    var part = shape.part(shape.letter(k, j, i));
                    if (part == null || part.phase() > phases) continue;
                    level.setBlock(shape.world(controller, FRONT, k, j, i),
                            MegaCapture.block(part).defaultBlockState(), 2 | 16);
                }
        var state = definition.getBlock().defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, FRONT);
        level.setBlock(controller, state, 3);
        return (M) ((IMachineBlockEntity) level.getBlockEntity(controller)).getMetaMachine();
    }

    /**
     * Replaces the {@code index}-th hatch casing of the station (scan order) with a part machine and returns it; call
     * before checking the pattern.
     */
    static com.gregtechceu.gtceu.api.machine.MetaMachine hatch(GameTestHelper helper, MegaMachine m, int index,
                                                               com.gregtechceu.gtceu.api.machine.MachineDefinition part) {
        var shape = m.shape();
        int seen = 0;
        for (int i = 0; i < shape.nz; i++)
            for (int j = 0; j < shape.ny; j++)
                for (int k = 0; k < shape.nx; k++) {
                    if (shape.letter(k, j, i) != MegaShape.Part.HATCH || seen++ != index) continue;
                    var pos = shape.world(m.getPos(), FRONT, k, j, i);
                    helper.getLevel().setBlock(pos, part.getBlock().defaultBlockState(), 3);
                    return ((IMachineBlockEntity) helper.getLevel().getBlockEntity(pos)).getMetaMachine();
                }
        throw new IllegalStateException("no hatch casing " + index);
    }

    static String error(MegaMachine m) {
        var state = m.getMultiblockState();
        var e = state.error;
        return e == null ? "none" : e.getErrorInfo().getString() + " found " + state.getBlockState();
    }

    /**
     * A running machine holds its moving pieces (their blocks are air and the renderer turns them); switching it off
     * puts them back as blocks, and switching it on again leaves it ready to take them on its next second.
     */
    static void capturesAndReleases(GameTestHelper helper, MegaMachine m) {
        MegaShape.Part moving = null;
        for (var part : m.shape().parts.values())
            if (part.moving() && part.phase() <= m.phases()) {
                moving = part;
                break;
            }
        helper.assertTrue(moving != null, "the shape has a moving piece in its built phases");
        long piece = MegaCapture.positions(m.shapeName(), m.getPos(), FRONT).get(moving)[0];
        helper.assertTrue(m.isCaptured() && helper.getLevel().getBlockState(BlockPos.of(piece)).isAir(),
                "the moving pieces are in the controller while it runs (" + moving.ring() + ")");
        m.setWorkingEnabled(false);
        helper.assertTrue(!helper.getLevel().getBlockState(BlockPos.of(piece)).isAir(),
                "switching off puts the moving pieces back as blocks");
        m.setWorkingEnabled(true);
    }

    static MegaShape.Part part(MegaShape shape, String ring, String kind) {
        for (var p : shape.parts.values()) if (p.ring().equals(ring) && p.kind().equals(kind)) return p;
        throw new IllegalStateException("no " + kind + " of " + ring);
    }

    /** First lettered cell of a part (k, j, i), scanning aisle by aisle. */
    static int[] firstCell(MegaShape shape, MegaShape.Part part) {
        for (int i = 0; i < shape.nz; i++)
            for (int j = 0; j < shape.ny; j++)
                for (int k = 0; k < shape.nx; k++)
                    if (shape.letter(k, j, i) == part.letter()) return new int[] { k, j, i };
        throw new IllegalStateException("no " + part);
    }
}
