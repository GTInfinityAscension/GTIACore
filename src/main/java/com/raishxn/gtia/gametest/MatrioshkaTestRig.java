package com.raishxn.gtia.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.PsiStore;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaBrainMachine;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaShells;

import java.util.UUID;

/** Bodies of {@link MatrioshkaGameTests}, apart so the GTNA classes load only when the brain is registered. */
final class MatrioshkaTestRig {

    private static final Direction FRONT = MegaTestRig.FRONT;

    private MatrioshkaTestRig() {}

    private static MatrioshkaBrainMachine build(GameTestHelper helper, int phases, int slot) {
        return MegaTestRig.build(helper, MatrioshkaBrainMachine.SHAPE, GTIAMegastructures.MATRIOSHKA_BRAIN, phases,
                slot);
    }

    private static String error(MatrioshkaBrainMachine m) {
        return MegaTestRig.error(m);
    }

    private static MegaShape.Part part(MegaShape shape, String ring, String kind) {
        return MegaTestRig.part(shape, ring, kind);
    }

    private static int[] firstCell(MegaShape shape, MegaShape.Part part) {
        return MegaTestRig.firstCell(shape, part);
    }

    static void phaseOneFormsAndNeedsTheStar(GameTestHelper helper) {
        try {
            var m = build(helper, 1, 0);
            helper.assertTrue(m.getFrontFacing() == FRONT, "controller faces north: " + m.getFrontFacing());
            helper.assertTrue(m.checkPatternWithLock(), "phase 1 alone forms: " + error(m));
            m.onStructureFormed();

            // The capture and the renderer place cells with MegaShape.world; it must agree with GTCEu.
            var shape = MegaShape.get(MatrioshkaBrainMachine.SHAPE);
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
            MegaMachine.anyDimensionForTests = false;
            m.testSecond(owner);
            helper.assertTrue(!m.isRunning() && m.getMaxCWUt() == 0, "no CWU outside the Nexus System");

            MegaMachine.anyDimensionForTests = true;
            m.testSecond(owner);
            helper.assertTrue(m.phases() == 1, "phase 1: " + m.phases());
            int expected = MatrioshkaShells.totalCwut(1, shape.radius);
            helper.assertTrue(m.isRunning() && m.getMaxCWUt() == expected,
                    "one shell: " + m.getMaxCWUt() + " CWU/t, expected " + expected);
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
        }
    }

    static void allPhasesComputeAndTurn(GameTestHelper helper) {
        try {
            var m = build(helper, MatrioshkaShells.PHASES, 1);
            helper.assertTrue(m.checkPatternWithLock(), "all five phases form: " + error(m));
            m.onStructureFormed();
            var shape = MegaShape.get(MatrioshkaBrainMachine.SHAPE);
            MegaMachine.anyDimensionForTests = true;
            UUID owner = UUID.randomUUID();
            m.testSecond(owner);
            helper.assertTrue(m.phases() == 5, "five phases: " + m.phases());
            int expected = MatrioshkaShells.totalCwut(5, shape.radius);
            helper.assertTrue(m.getMaxCWUt() == expected && expected > 1 << 26,
                    "five shells: " + m.getMaxCWUt() + " CWU/t, expected " + expected);
            helper.assertTrue(MatrioshkaShells.shellCwut(5, shape.radius[4]) > 1L << 26,
                    "the cold outer shell beats 2^26 (Landauer)");

            // Rings are taken in while it runs, and the phase count does not drop for it.
            long ring = MegaCapture.positions(MatrioshkaBrainMachine.SHAPE, m.getPos(), FRONT).get(part(shape, "inner_up", "body"))[0];
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

            // Overclocking without helium overheats the inner shells: more clock, but not twice the CWU.
            m.setClock(MatrioshkaShells.CLOCK_MAX);
            m.testSecond(owner);
            helper.assertTrue((((int) m.view()[5]) & 1) == 0, "a clock peak does not overheat at once (inertia)");
            for (int s = 0; s < 40; s++) m.testSecond(owner);
            int hot = (int) m.view()[5];
            helper.assertTrue((hot & 1) != 0, "the inner shell overheats after a while at 200%: mask " + hot);
            helper.assertTrue(m.integrity(1) < MatrioshkaShells.INTEGRITY, "and wears: " + m.integrity(1));
            helper.assertTrue(m.getMaxCWUt() > expected, "200% clock still gives more: " + m.getMaxCWUt());
            m.setClock(100);
            for (int s = 0; s < 40; s++) m.testSecond(owner);

            m.setWorkingEnabled(false);
            helper.assertTrue(!helper.getLevel().getBlockState(BlockPos.of(ring)).isAir(),
                    "switching off puts the rings back");
            helper.assertTrue(m.checkPatternWithLock(), "and the structure still forms: " + error(m));
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
        }
    }

    static void landauerClockAndCoolant(GameTestHelper helper) {
        var shape = MegaShape.get(MatrioshkaBrainMachine.SHAPE);
        float[] r = shape.radius;
        for (int p = 1; p < MatrioshkaShells.PHASES; p++)
            helper.assertTrue(r[p] > r[p - 1], "shells grow outwards: " + java.util.Arrays.toString(r));
        helper.assertTrue(MatrioshkaShells.temperature(r[4]) < MatrioshkaShells.temperature(r[0]),
                "the outer shell is colder");
        helper.assertTrue(MatrioshkaShells.shellCwut(5, r[4]) > 1L << 26, "the cold outer shell beats 2^26");
        var helium = MatrioshkaShells.Coolant.LIQUID_HELIUM;
        var shifted = MatrioshkaShells.Coolant.SHIFTED_SUPERFLUID;
        double t = MatrioshkaShells.temperature(r[0], 100);
        double cooled = MatrioshkaShells.target(r[0], 100, helium, 1);
        helper.assertTrue(Math.abs(cooled - (4 + (t - 4) * 0.6)) < 1e-6, "helium pulls a shell 40% towards 4 K");
        helper.assertTrue(MatrioshkaShells.target(r[0], 100, shifted, 1) < MatrioshkaShells.target(r[0], 100,
                MatrioshkaShells.Coolant.SUPERFLUID_HELIUM, 1) && MatrioshkaShells.target(r[0], 100,
                        MatrioshkaShells.Coolant.SUPERFLUID_HELIUM, 1) < cooled,
                "each coolant tier is colder than the one before");
        helper.assertTrue(MatrioshkaShells.temperature(r[0], 200) > MatrioshkaShells.limit(5),
                "200% clock overheats the uncooled inner shell even with the main rings");
        helper.assertTrue(MatrioshkaShells.target(r[0], 200, helium, 1) < MatrioshkaShells.T_LIMIT,
                "with helium it stays under the limit");
        helper.assertTrue(MatrioshkaShells.coolantDemand(helium, 5, 200) == 2500 &&
                MatrioshkaShells.coolantDemand(shifted, 5, 200) == 100, "coolant demand per tier");
        // Inertia: one second closes a tenth of the gap.
        helper.assertTrue(Math.abs(MatrioshkaShells.step(400, 500) - 410) < 1e-9, "10 s time constant");
        helper.assertTrue(MatrioshkaShells.limit(4) == 540 && MatrioshkaShells.limit(3) == 500,
                "the main rings of phase 4 raise the limit by 40 K");
        // Wear: an overheated shell loses integrity and, at zero, stops even when cool again.
        double[] temps = { 1000, 0, 0, 0, 0 };
        int[] integrity = { 20, 1000, 1000, 1000, 1000 };
        var res = com.raishxn.gtia.mega.matrioshka.MatrioshkaThermal.second(r, 1, 100,
                MatrioshkaShells.Coolant.NONE, 0, temps, integrity);
        helper.assertTrue(integrity[0] == 0 && (res.fractured() & 1) != 0 && res.cwut() == 0,
                "an overheated shell wears to zero and fractures: " + integrity[0]);
        temps[0] = 200;
        res = com.raishxn.gtia.mega.matrioshka.MatrioshkaThermal.second(r, 1, 100,
                MatrioshkaShells.Coolant.NONE, 0, temps, integrity);
        helper.assertTrue(res.cwut() == 0, "a fractured shell computes nothing until repaired");
        helper.succeed();
    }

    static void wrongBlockDoesNotForm(GameTestHelper helper) {
        var m = build(helper, MatrioshkaShells.PHASES, 2);
        var shape = MegaShape.get(MatrioshkaBrainMachine.SHAPE);
        int[] c = firstCell(shape, part(shape, "data_1", "body"));
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
        int[] p = firstCell(shape, part(shape, "polar", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), FRONT, p[0], p[1], p[2]), Blocks.AIR.defaultBlockState(),
                2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing polar ring block keeps it from forming");
        helper.succeed();
    }
}
