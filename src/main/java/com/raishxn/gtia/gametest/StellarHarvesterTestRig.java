package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.harvester.HarvesterPhysics;
import com.raishxn.gtia.mega.harvester.StellarHarvesterMachine;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link StellarHarvesterGameTests}, apart so the GTNA classes load only when it is registered. */
final class StellarHarvesterTestRig {

    private StellarHarvesterTestRig() {}

    static void burnMath(GameTestHelper helper) {
        helper.assertTrue(HarvesterPhysics.exponent(1) == 4 && HarvesterPhysics.exponent(15) == 16,
                "p-p chain ~T^4, CNO cycle ~T^16");
        helper.assertTrue(Math.abs(HarvesterPhysics.boost(0.5, 1) - Math.pow(1.5, 4)) < 1e-9 &&
                HarvesterPhysics.boost(1.5, 15) == HarvesterPhysics.BOOST_MAX, "(1 + h)^n, capped at a million");
        helper.assertTrue(HarvesterPhysics.maxHeat(1) == 0.3 && Math.abs(HarvesterPhysics.maxHeat(5) - 1.5) < 1e-9,
                "0.3 of heating a phase");
        double eut = HarvesterPhysics.power(1, 1);
        helper.assertTrue(Math.abs(HarvesterPhysics.secondsLeft(eut, 1, 1) * eut * 20 -
                HarvesterPhysics.budget(1, 1)) < 1, "the countdown spends exactly the budget");
        helper.assertTrue(HarvesterPhysics.remnant(1) == Kind.WHITE_DWARF &&
                HarvesterPhysics.remnant(15) == Kind.NEUTRON_STAR && HarvesterPhysics.remnant(30) == Kind.BLACK_HOLE,
                "remnants by mass");
        helper.succeed();
    }

    static void burnsToARemnant(GameTestHelper helper) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass, fuel = star.fuel;
        try {
            MegaMachine.anyDimensionForTests = true;
            star.kind = Kind.YELLOW_DWARF;
            star.mass = 1;
            star.fuel = 1;
            StellarHarvesterMachine m = MegaTestRig.build(helper, StellarHarvesterMachine.SHAPE,
                    GTIAMegastructures.STELLAR_HARVESTER_DEF, 5, 24);
            helper.assertTrue(m.checkPatternWithLock(), "the whole harvester forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setHeat(0.5);
            m.testSecond(owner);
            helper.assertTrue(m.harvested() > 0 && star.fuel < 1 && m.view()[4] == Math.pow(1.5, 4),
                    "a heated Sun pays EU and burns fuel");
            // The last drop of a Sun's core: it ends as a white dwarf.
            star.fuel = 1e-12;
            m.testSecond(owner);
            helper.assertTrue(star.kind == Kind.WHITE_DWARF && m.event() == 1, "a spent Sun is a white dwarf");
            m.testSecond(owner);
            helper.assertTrue(!m.inPlace(), "a white dwarf cannot be harvested");
            // A 15 M☉ star leaves a neutron star.
            star.kind = Kind.BLUE_GIANT;
            star.mass = 15;
            star.fuel = 1e-12;
            m.setHeat(1.5);
            m.testSecond(owner);
            helper.assertTrue(star.kind == Kind.NEUTRON_STAR && star.mass == 1.4, "a spent 15 M☉ star: neutron star");
            MegaTestRig.capturesAndReleases(helper, m);
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
            star.fuel = fuel;
        }
    }

    static void noAnnulusNoHarvester(GameTestHelper helper) {
        StellarHarvesterMachine m = MegaTestRig.build(helper, StellarHarvesterMachine.SHAPE,
                GTIAMegastructures.STELLAR_HARVESTER_DEF, 5, 25);
        var shape = MegaShape.get(StellarHarvesterMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "annulus", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing annulus block keeps the harvester from forming");
        helper.succeed();
    }
}
