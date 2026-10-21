package com.raishxn.gtia.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.gtinfinityascension.gtialib.data.Int128;
import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.StellarEraStore;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.engine.EnginePhysics;
import com.raishxn.gtia.mega.engine.EnginePhysics.Region;
import com.raishxn.gtia.mega.engine.StellarEngineMachine;
import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;

import java.util.UUID;

/** Bodies of {@link StellarEngineGameTests}, apart so the GTNA classes load only when the engine is registered. */
final class StellarEngineTestRig {

    private StellarEngineTestRig() {}

    private static StellarEngineMachine build(GameTestHelper helper, int phases, int slot) {
        return MegaTestRig.build(helper, StellarEngineMachine.SHAPE, GTIAMegastructures.STELLAR_ENGINE_DEF, phases,
                slot);
    }

    /** A Nexus network for {@code owner} with plenty of EU, as GTNA's own GameTests set one up. */
    static void power(GameTestHelper helper, UUID owner) {
        var level = helper.getLevel();
        var network = NexusEnergyNetwork.get(level);
        network.setMatrixStats(owner, 750, 14, 1.0, Int128.fromString("100000000000000000000000000000"), true);
        network.setMatrixDimension(owner, level.dimension());
        WirelessEnergyManager.addMaxCapacity(level, owner, Int128.fromString("1000000000000000000000000000"));
        WirelessEnergyManager.setEnergy(level, owner, Int128.fromString("1000000000000000000000000"));
    }

    static void thrustRegionsAndTension(GameTestHelper helper) {
        double shk = EnginePhysics.shkadovThrust(true);
        helper.assertTrue(Math.abs(shk - 0.25 * EnginePhysics.L_SUN / EnginePhysics.C) < shk * 1e-9,
                "Shkadov: a quarter of L/c");
        double cap = EnginePhysics.caplanThrust(EnginePhysics.MDOT_MAX, 50);
        helper.assertTrue(cap > 4 * shk, "the Caplan engine pushes several times harder than the mirror");
        double dv = EnginePhysics.dvPerSecond(shk);
        helper.assertTrue(dv > 0.004 && dv < 0.006, "the mirror alone: about 5 m/s each second: " + dv);
        helper.assertTrue(Region.at(0) == Region.LOCAL_CLOUD && Region.at(20) == Region.ORION_EDGE &&
                Region.at(499) == Region.PULSAR_VICINITY && Region.at(500) == Region.GALACTIC_CORE,
                "regions by Δv");
        helper.assertTrue(EnginePhysics.shellCooling(Region.INTERARM_VOID) == 0.9 &&
                EnginePhysics.shellCooling(Region.ORION_EDGE) == 1, "the void cools the Matrioshka's shells");
        helper.assertTrue(EnginePhysics.tension(70) == 0 && EnginePhysics.tension(90) == 1,
                "tension from 70% to 90% of the jet");
        helper.succeed();
    }

    static void mirrorAndCaplanMoveTheSystem(GameTestHelper helper) {
        try {
            StellarEngineMachine m = build(helper, 5, 3);
            helper.assertTrue(m.checkPatternWithLock(), "the whole engine forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            MegaMachine.anyDimensionForTests = true;
            UUID owner = UUID.randomUUID();
            var store = StellarEraStore.get(helper.getLevel().getServer());

            // No Nexus network: only the mirror pushes.
            m.testSecond(owner);
            double shk = EnginePhysics.shkadovThrust(true);
            helper.assertTrue(m.view()[6] == 0 && Math.abs(m.view()[0] - shk) < shk * 1e-6,
                    "without power only the mirror: " + m.view()[0]);
            helper.assertTrue(store.dv(owner) > 0, "the owner gathers Δv");

            // With power the Caplan engine burns, the collector ring turns and plasma comes out.
            power(helper, owner);
            m.testSecond(owner);
            helper.assertTrue(m.view()[6] == 1 && m.view()[2] > 0, "the Caplan engine burns with Nexus power");
            long ring = MegaCapture.positions(StellarEngineMachine.SHAPE, m.getPos(), MegaTestRig.FRONT)
                    .get(MegaTestRig.part(m.shape(), "collector", "body"))[0];
            helper.assertTrue(m.isCaptured() && helper.getLevel().getBlockState(BlockPos.of(ring)).isAir(),
                    "the collector ring is in the controller");
            helper.assertTrue(m.view()[9] > 0 && m.view()[10] > 0, "hydrogen and helium plasma");

            // An over-long jet strains the cage; with a safe jet it is repaired... only with blocks.
            m.setBeta(EnginePhysics.BETA_MAX);
            m.testSecond(owner);
            helper.assertTrue(m.view()[7] == 1 && m.integrity() < EnginePhysics.INTEGRITY,
                    "at 90% the cage strains: " + m.integrity());
            m.setIntegrity(0);
            m.testSecond(owner);
            helper.assertTrue(m.view()[6] == 0, "a broken cage stops the Caplan engine");
            m.setIntegrity(EnginePhysics.INTEGRITY);
            m.setBeta(60);

            // Enough Δv reaches the next regions.
            store.add(owner, 60 - store.dv(owner));
            helper.assertTrue(store.farthest(owner) == Region.INTERARM_VOID, "60 km/s: the void between the arms");

            m.setWorkingEnabled(false);
            helper.assertTrue(!helper.getLevel().getBlockState(BlockPos.of(ring)).isAir(),
                    "switching off puts the ring back");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
        }
    }

    static void noCageNoEngine(GameTestHelper helper) {
        StellarEngineMachine m = build(helper, 5, 4);
        var shape = MegaShape.get(StellarEngineMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "ribs", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing rib keeps the engine from forming");
        helper.succeed();
    }
}
