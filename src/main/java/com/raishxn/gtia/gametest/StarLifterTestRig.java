package com.raishxn.gtia.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.lifter.LifterPhysics;
import com.raishxn.gtia.mega.lifter.LifterPhysics.Mode;
import com.raishxn.gtia.mega.lifter.StarLifterMachine;
import com.raishxn.gtia.mega.system.StarPhysics;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link StarLifterGameTests}, apart so the GTNA classes load only when the lifter is registered. */
final class StarLifterTestRig {

    private StarLifterTestRig() {}

    private static StarLifterMachine build(GameTestHelper helper, int phases, int slot) {
        return MegaTestRig.build(helper, StarLifterMachine.SHAPE, GTIAMegastructures.STAR_LIFTER_DEF, phases, slot);
    }

    static void liftingMath(GameTestHelper helper) {
        helper.assertTrue(Math.abs(LifterPhysics.escapeEnergy(1) - 1.907e11) < 1e8, "G M / R of the Sun");
        helper.assertTrue(LifterPhysics.escapeEnergy(0.5) < LifterPhysics.escapeEnergy(1),
                "a lighter star has a shallower well");
        helper.assertTrue(LifterPhysics.mdot(Mode.RING, 100) == 10 * LifterPhysics.mdot(Mode.THERMAL, 100),
                "the ring current lifts ten times the thermal wind");
        double lost = LifterPhysics.massLost(1e12);
        helper.assertTrue(lost > 1.5e-5 && lost < 1.7e-5, "1e12 kg/s for a million years: 1.6e-5 M☉: " + lost);
        helper.assertTrue(LifterPhysics.instability(Mode.DEEP, 100) == LifterPhysics.INSTABILITY_RATE &&
                LifterPhysics.instability(Mode.DEEP, 60) == 0 && LifterPhysics.instability(Mode.RING, 100) == 0,
                "only deep stirring above 60% grows instability");
        helper.assertTrue(StarPhysics.lifetimeYears(0.5) > StarPhysics.lifetimeYears(1),
                "a lighter star lives longer");
        double t = StarPhysics.temperature(StarPhysics.Kind.YELLOW_DWARF, 1);
        helper.assertTrue(Math.abs(t - 5772) < 1, "the Sun's surface: " + t);
        double share = 0;
        for (var e : LifterPhysics.Element.values()) share += e.surface;
        helper.assertTrue(share > 0.99 && share <= 1.0, "surface shares add up to about 1: " + share);
        helper.succeed();
    }

    /** Runs {@code body} with a fresh Nexus star, then puts the star back as it was. */
    private static void withStar(GameTestHelper helper, Runnable body) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        double mass = star.mass, fuel = star.fuel;
        star.mass = 1;
        star.fuel = 0.5;
        try {
            MegaMachine.anyDimensionForTests = true;
            body.run();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.mass = mass;
            star.fuel = fuel;
        }
    }

    static void liftsAndShrinksTheStar(GameTestHelper helper) {
        withStar(helper, () -> {
            StarLifterMachine m = build(helper, 5, 5);
            helper.assertTrue(m.checkPatternWithLock(), "the whole lifter forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setMode(Mode.RING);
            m.setRate(100);
            m.testSecond(owner);
            var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
            helper.assertTrue(m.view()[8] == 1 && m.view()[0] >= 1e12, "the ring current lifts 1e12 kg/s");
            helper.assertTrue(star.mass < 1 && star.mass > 0.9999, "the star gets lighter: " + star.mass);
            helper.assertTrue(m.view()[9] > 1000 && m.view()[10] > 0, "hydrogen and helium plasma");
            helper.assertTrue(m.view()[13] > 0 || m.view()[11] > 0, "the spires sort out oxygen or iron");
            long ring = MegaCapture.positions(StarLifterMachine.SHAPE, m.getPos(), MegaTestRig.FRONT)
                    .get(MegaTestRig.part(m.shape(), "current", "body"))[0];
            helper.assertTrue(m.isCaptured() && helper.getLevel().getBlockState(BlockPos.of(ring)).isAir(),
                    "the ring current is in the controller");

            // Deep stirring above 60% grows instability; a full one makes a flare.
            m.setMode(Mode.DEEP);
            double fuel = star.fuel;
            m.testSecond(owner);
            helper.assertTrue(m.instability() > 0 && star.fuel > fuel, "deep stirring: restless star, more fuel");
            m.setInstability(LifterPhysics.INTEGRITY - 1);
            m.testSecond(owner);
            helper.assertTrue(m.integrity() <= LifterPhysics.INTEGRITY - 250, "a flare hits the girdle");

            m.setWorkingEnabled(false);
            helper.assertTrue(!helper.getLevel().getBlockState(BlockPos.of(ring)).isAir(),
                    "switching off puts the ring back");
            helper.succeed();
        });
    }

    static void plasmaOnlyBeforeTheSpires(GameTestHelper helper) {
        withStar(helper, () -> {
            StarLifterMachine m = build(helper, 3, 6);
            helper.assertTrue(m.checkPatternWithLock(), "three phases form: " + MegaTestRig.error(m));
            m.onStructureFormed();
            m.countPhases(helper.getLevel());
            helper.assertTrue(m.phases() == 3, "three phases counted: " + m.phases());
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setMode(Mode.DEEP);
            m.setRate(100);
            m.testSecond(owner);
            helper.assertTrue(m.view()[0] <= 1e12 && m.instability() == 0,
                    "without phase 5 deep stirring falls back and stays calm");
            helper.assertTrue(m.view()[9] > 0 && m.view()[11] == 0 && m.view()[13] == 0,
                    "before the spires only plasma comes out");
            helper.succeed();
        });
    }

    static void noGirdleNoLifter(GameTestHelper helper) {
        StarLifterMachine m = build(helper, 5, 7);
        var shape = MegaShape.get(StarLifterMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "girdle", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing girdle block keeps the lifter from forming");
        helper.succeed();
    }
}
