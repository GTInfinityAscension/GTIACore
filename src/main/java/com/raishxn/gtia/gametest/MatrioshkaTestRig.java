package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.PsiStore;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaBrainMachine;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaCapture;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaShape;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaShells;

import java.util.UUID;

/** Bodies of {@link MatrioshkaGameTests}, apart so the GTNA classes load only when the brain is registered. */
final class MatrioshkaTestRig {

    private static final Direction FRONT = Direction.NORTH;

    private MatrioshkaTestRig() {}

    /**
     * Builds the phases up to {@code phases} 250 blocks above the test (clear of the terrain) and returns the
     * controller. GameTests run
     * side by side 63 blocks apart and other tests leave blocks around, so each test builds 3000 blocks
     * away and {@code 400 * slot} blocks to the east of the others.
     */
    private static MatrioshkaBrainMachine build(GameTestHelper helper, int phases, int slot) {
        var shape = MatrioshkaShape.get();
        var level = helper.getLevel();
        BlockPos controller = helper.absolutePos(new BlockPos(2 + 400 * slot, 250, 3000));
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
                    var part = MatrioshkaShape.Part.of(shape.letter(k, j, i));
                    if (part == null || part.phase > phases) continue;
                    level.setBlock(shape.world(controller, FRONT, k, j, i),
                            MatrioshkaCapture.block(part).defaultBlockState(), 2 | 16);
                }
        var state = GTIAMegastructures.MATRIOSHKA_BRAIN.getBlock().defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, FRONT);
        level.setBlock(controller, state, 3);
        // GTCEu's walk refuses unloaded chunks: keep the whole footprint loaded for the test.
        int reach = Math.max(shape.nx, shape.nz) + 2;
        for (int cx = (controller.getX() - reach) >> 4; cx <= (controller.getX() + reach) >> 4; cx++)
            for (int cz = (controller.getZ() - reach) >> 4; cz <= (controller.getZ() + reach) >> 4; cz++)
                level.setChunkForced(cx, cz, true);
        return (MatrioshkaBrainMachine) ((IMachineBlockEntity) level.getBlockEntity(controller)).getMetaMachine();
    }

    private static String error(MatrioshkaBrainMachine m) {
        var state = m.getMultiblockState();
        var e = state.error;
        return e == null ? "none" : e.getErrorInfo().getString() + " found " + state.getBlockState();
    }

    /** First lettered cell of a part (k, j, i), scanning aisle by aisle. */
    private static int[] firstCell(MatrioshkaShape shape, MatrioshkaShape.Part part) {
        for (int i = 0; i < shape.nz; i++)
            for (int j = 0; j < shape.ny; j++)
                for (int k = 0; k < shape.nx; k++)
                    if (shape.letter(k, j, i) == part.letter) return new int[] { k, j, i };
        throw new IllegalStateException("no " + part);
    }

    static void phaseOneFormsAndNeedsTheStar(GameTestHelper helper) {
        try {
            var m = build(helper, 1, 0);
            helper.assertTrue(m.getFrontFacing() == FRONT, "controller faces north: " + m.getFrontFacing());
            helper.assertTrue(m.checkPatternWithLock(), "phase 1 alone forms: " + error(m));
            m.onStructureFormed();

            // The capture and the renderer place cells with MatrioshkaShape.world; it must agree with GTCEu.
            var shape = MatrioshkaShape.get();
            var layout = m.structureWatch().layout();
            int checked = 0;
            for (int i = 0; i < shape.nz; i += 7)
                for (int j = 0; j < shape.ny; j += 5)
                    for (int k = 0; k < shape.nx; k += 3) {
                        char letter = shape.letter(k, j, i);
                        if (letter == ' ' || letter == '~') continue;
                        int index = layout.indexOf(shape.world(m.getPos(), FRONT, k, j, i).asLong());
                        helper.assertTrue(index >= 0 && layout.matcher(index).letter() == letter,
                                "cell " + k + "," + j + "," + i + " (" + letter + ") is where GTCEu puts it");
                        checked++;
                    }
            helper.assertTrue(checked > 100, "checked " + checked + " cells");

            UUID owner = UUID.randomUUID();
            MatrioshkaBrainMachine.anyDimensionForTests = false;
            m.testSecond(owner);
            helper.assertTrue(!m.isRunning() && m.getMaxCWUt() == 0, "no CWU outside the Nexus System");

            MatrioshkaBrainMachine.anyDimensionForTests = true;
            m.testSecond(owner);
            helper.assertTrue(m.phases() == 1, "phase 1: " + m.phases());
            int expected = MatrioshkaShells.totalCwut(1, shape.radius);
            helper.assertTrue(m.isRunning() && m.getMaxCWUt() == expected,
                    "one shell: " + m.getMaxCWUt() + " CWU/t, expected " + expected);
            helper.succeed();
        } finally {
            MatrioshkaBrainMachine.anyDimensionForTests = false;
        }
    }

    static void allPhasesComputeAndTurn(GameTestHelper helper) {
        try {
            var m = build(helper, MatrioshkaShells.PHASES, 1);
            helper.assertTrue(m.checkPatternWithLock(), "all five phases form: " + error(m));
            m.onStructureFormed();
            var shape = MatrioshkaShape.get();
            MatrioshkaBrainMachine.anyDimensionForTests = true;
            UUID owner = UUID.randomUUID();
            m.testSecond(owner);
            helper.assertTrue(m.phases() == 5, "five phases: " + m.phases());
            int expected = MatrioshkaShells.totalCwut(5, shape.radius);
            helper.assertTrue(m.getMaxCWUt() == expected && expected > 1 << 26,
                    "five shells: " + m.getMaxCWUt() + " CWU/t, expected " + expected);
            helper.assertTrue(MatrioshkaShells.shellCwut(5, shape.radius[4]) > 1L << 26,
                    "the cold outer shell beats 2^26 (Landauer)");

            // Rings are taken in while it runs, and the phase count does not drop for it.
            long ring = MatrioshkaCapture.positions(m.getPos(), FRONT).get(MatrioshkaShape.Part.INNER_RING_UP)[0];
            helper.assertTrue(m.isCaptured() && helper.getLevel().getBlockState(BlockPos.of(ring)).isAir(),
                    "the inner ring is in the controller");
            m.countPhases(helper.getLevel());
            helper.assertTrue(m.phases() == 5, "captured rings still count: " + m.phases());

            // A consumer takes CWU; what nobody takes becomes Ψ.
            helper.assertTrue(m.requestCWUt(1000, false) == 1000, "gives the CWU asked for");
            helper.assertTrue(m.requestCWUt(Integer.MAX_VALUE, true) == expected - 1000,
                    "no more than its CWU/t in a tick");
            long psi0 = PsiStore.get(helper.getLevel().getServer()).balance(owner);
            m.testSecond(owner);
            long psi = PsiStore.get(helper.getLevel().getServer()).balance(owner) - psi0;
            long idle = ((long) expected * 20 - 1000) / MatrioshkaShells.CWU_PER_PSI;
            helper.assertTrue(Math.abs(psi - idle) <= 1, "idle CWU becomes Ψ: " + psi + ", expected " + idle);

            m.setWorkingEnabled(false);
            helper.assertTrue(!helper.getLevel().getBlockState(BlockPos.of(ring)).isAir(),
                    "switching off puts the rings back");
            helper.assertTrue(m.checkPatternWithLock(), "and the structure still forms: " + error(m));
            helper.succeed();
        } finally {
            MatrioshkaBrainMachine.anyDimensionForTests = false;
        }
    }

    static void wrongBlockDoesNotForm(GameTestHelper helper) {
        var m = build(helper, MatrioshkaShells.PHASES, 2);
        var shape = MatrioshkaShape.get();
        int[] c = firstCell(shape, MatrioshkaShape.Part.DATA_RING_1);
        BlockPos dataRing = shape.world(m.getPos(), FRONT, c[0], c[1], c[2]);

        // A missing phase-3 block leaves phases 1 and 2.
        helper.getLevel().setBlock(dataRing, Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(m.checkPatternWithLock(), "a missing phase-3 block still forms: " + error(m));
        m.onStructureFormed();
        m.countPhases(helper.getLevel());
        helper.assertTrue(m.phases() == 2, "phases 1 and 2 only: " + m.phases());

        // A wrong block does not form at all, nor does a missing platform block.
        m.onStructureInvalid();
        helper.getLevel().setBlock(dataRing, Blocks.STONE.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "stone in the data sphere keeps it from forming");
        helper.getLevel().setBlock(dataRing, Blocks.AIR.defaultBlockState(), 2 | 16);
        int[] p = firstCell(shape, MatrioshkaShape.Part.POLAR_RING);
        helper.getLevel().setBlock(shape.world(m.getPos(), FRONT, p[0], p[1], p[2]), Blocks.AIR.defaultBlockState(),
                2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing polar ring block keeps it from forming");
        helper.succeed();
    }
}
