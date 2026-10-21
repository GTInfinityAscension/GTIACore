package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.suppressor.FusionSuppressorMachine;
import com.raishxn.gtia.mega.suppressor.SuppressorPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link FusionSuppressorGameTests}, apart so the GTNA classes load only when it is registered. */
final class FusionSuppressorTestRig {

    private FusionSuppressorTestRig() {}

    static void suppressorMath(GameTestHelper helper) {
        helper.assertTrue(SuppressorPhysics.research(100, 1, 2) == 4 && SuppressorPhysics.research(100, 1, 1) == 0,
                "the detectors (phase 2) read 4 points a second at full suppression of a Sun");
        helper.assertTrue(SuppressorPhysics.deficit(100) == 25 && SuppressorPhysics.deficit(40) == -5,
                "the deficit grows above half suppression and heals below");
        helper.assertTrue(Math.abs(SuppressorPhysics.dampedTemperature(10000, 100) - 7000) < 1e-6,
                "full suppression cools the surface by 30%");
        helper.assertTrue(SuppressorPhysics.supernovaDust(15, 1, true) == 960 &&
                SuppressorPhysics.supernovaDust(15, 1, false) == 96, "64 × M × Z, a tenth without the corolla");
        helper.assertTrue(SuppressorPhysics.holeMass(20) == 12, "60% of the star falls into the hole");
        helper.succeed();
    }

    static void endsAStar(GameTestHelper helper) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass, fuel = star.fuel;
        try {
            MegaMachine.anyDimensionForTests = true;
            FusionSuppressorMachine m = MegaTestRig.build(helper, FusionSuppressorMachine.SHAPE,
                    GTIAMegastructures.FUSION_SUPPRESSOR_DEF, 5, 12);
            helper.assertTrue(m.checkPatternWithLock(), "the whole Suppressor forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setSuppression(100);

            // A Sun-like star only flares.
            star.kind = Kind.YELLOW_DWARF;
            star.mass = 1;
            m.testSecond(owner);
            helper.assertTrue(m.view()[6] == 1 && m.research() > 0, "it damps the star and reads its neutrinos");
            m.setDeficit(SuppressorPhysics.DEFICIT_FULL - 1);
            m.testSecond(owner);
            helper.assertTrue(m.event() == 3 && m.integrity() < SuppressorPhysics.INTEGRITY && !m.ready(),
                    "a light star flares and the pylons take it");

            // A blue giant held until its core gives way can go supernova.
            star.kind = Kind.BLUE_GIANT;
            star.mass = 15;
            m.setDeficit(SuppressorPhysics.DEFICIT_FULL - 1);
            m.testSecond(owner);
            helper.assertTrue(m.ready(), "a heavy star is ready to end");
            helper.assertTrue(m.end(helper.getLevel(), true), "the supernova goes off");
            helper.assertTrue(star.kind == Kind.NEUTRON_STAR && star.mass == 1.4 && m.owed() > 0,
                    "a neutron star is left and r-process dust is owed: " + m.owed());

            // Again with a black hole.
            star.kind = Kind.BLUE_GIANT;
            star.mass = 20;
            m.setDeficit(SuppressorPhysics.DEFICIT_FULL);
            m.testSecond(owner);
            helper.assertTrue(m.end(helper.getLevel(), false) && star.kind == Kind.BLACK_HOLE &&
                    Math.abs(star.mass - 12) < 1e-9, "a direct collapse leaves a 12 M☉ black hole");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
            star.fuel = fuel;
        }
    }

    static void noPylonNoSuppressor(GameTestHelper helper) {
        FusionSuppressorMachine m = MegaTestRig.build(helper, FusionSuppressorMachine.SHAPE,
                GTIAMegastructures.FUSION_SUPPRESSOR_DEF, 5, 13);
        var shape = MegaShape.get(FusionSuppressorMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "pylons", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing pylon block keeps the Suppressor from forming");
        helper.succeed();
    }
}
