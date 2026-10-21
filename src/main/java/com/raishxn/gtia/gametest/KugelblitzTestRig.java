package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.kugelblitz.KugelblitzMachine;
import com.raishxn.gtia.mega.kugelblitz.KugelblitzPhysics;
import com.raishxn.gtna.api.capability.WirelessEnergyManager;

import java.util.UUID;

/** Bodies of {@link KugelblitzGameTests}, apart so the GTNA classes load only when the silo is registered. */
final class KugelblitzTestRig {

    private KugelblitzTestRig() {}

    static void hawkingMath(GameTestHelper helper) {
        double p6 = KugelblitzPhysics.hawkingEut(1e6), p5 = KugelblitzPhysics.hawkingEut(1e5);
        helper.assertTrue(Math.abs(p6 - Math.pow(2, 30)) < 1, "10^6 kg gives 2^30 EU/t");
        helper.assertTrue(Math.abs(p5 / p6 - 100) < 1e-6, "P ∝ 1/M²: a tenth of the mass, a hundred times the power");
        double t6 = KugelblitzPhysics.lifetimeSeconds(1e6), t5 = KugelblitzPhysics.lifetimeSeconds(1e5);
        helper.assertTrue(Math.abs(t6 / t5 - 1000) < 1e-6, "t ∝ M³");
        helper.assertTrue(KugelblitzPhysics.lifetimeSeconds(1e4) < 20, "10^4 kg is gone in under 20 s");
        helper.assertTrue(Math.abs(KugelblitzPhysics.hawkingKelvin(1e6) - 1.227e17) < 1e12,
                "Hawking temperature 1.227e23 K / M");
        helper.assertTrue(KugelblitzPhysics.critical(3) < KugelblitzPhysics.critical(2), "stabilisers hold a lighter hole");
        helper.succeed();
    }

    static void lightIntoAHole(GameTestHelper helper) {
        try {
            MegaMachine.anyDimensionForTests = true;
            KugelblitzMachine m = MegaTestRig.build(helper, KugelblitzMachine.SHAPE, GTIAMegastructures.KUGELBLITZ_DEF,
                    5, 14);
            helper.assertTrue(m.checkPatternWithLock(), "the whole silo forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setRate(4);
            m.setTarget(3);
            for (int i = 0; i < 60 && !m.hole(); i++) m.testSecond(owner);
            helper.assertTrue(m.hole() && m.mass() > 0, "the lasers make a hole: " + m.mass());

            // A heavy hole radiates little; the petals give it back to the network.
            m.setMass(1e5, true);
            var before = WirelessEnergyManager.getEnergy(helper.getLevel(), owner);
            m.setRate(0);
            m.testSecond(owner);
            helper.assertTrue(m.view()[1] > 0 && m.mass() < 1e5, "Hawking radiation caught as EU, the hole loses mass");
            var after = WirelessEnergyManager.getEnergy(helper.getLevel(), owner);
            helper.assertTrue(after.compareTo(before) > 0, "the owner's network gains EU");
            MegaTestRig.capturesAndReleases(helper, m);

            // Under the critical mass it flashes out.
            m.setMass(KugelblitzPhysics.critical(5) * 1.01, true);
            m.testSecond(owner);
            helper.assertTrue(!m.hole() && m.mass() == 0 && m.integrity() == 0, "the hole flashes out and breaks the chamber");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
        }
    }

    static void noChamberNoSilo(GameTestHelper helper) {
        KugelblitzMachine m = MegaTestRig.build(helper, KugelblitzMachine.SHAPE, GTIAMegastructures.KUGELBLITZ_DEF, 5,
                15);
        var shape = MegaShape.get(KugelblitzMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "chamber", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing chamber block keeps the silo from forming");
        helper.succeed();
    }
}
