package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.EraStore;
import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.qso.QsoMachine;
import com.raishxn.gtia.mega.qso.QsoPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.WorldRules;

import java.util.UUID;

/** Bodies of {@link QsoGameTests}, apart so the GTNA classes load only when it is registered. */
final class QsoTestRig {

    private QsoTestRig() {}

    static void shotMath(GameTestHelper helper) {
        double in = QsoPhysics.input(10, 0);
        helper.assertTrue(in == Math.pow(2, 40) * 10 * 20, "the quasar shines at Eddington, 2^40 EU/t per M☉");
        helper.assertTrue(QsoPhysics.ceiling(in) == in * 100, "the leaky capacitor saturates at input / 0.01");
        double c = 0;
        for (int i = 0; i < 2000; i++) c = QsoPhysics.charge(c, in);
        helper.assertTrue(Math.abs(c - QsoPhysics.ceiling(in)) / QsoPhysics.ceiling(in) < 1e-6, "and gets there");
        helper.assertTrue(QsoPhysics.yield(0) == 4 && QsoPhysics.maxSize(5) == 7 && QsoPhysics.maxSize(1) == 3,
                "a size-0 shot gives 4 units; sizes open with phases");
        helper.succeed();
    }

    static void firesAndEnds(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var star = StarSystems.get(server).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass;
        var era = EraStore.get(server);
        double taken = era.get(EraStore.NEGENTROPY_TAKEN, EraStore.WORLD);
        double done = era.get(EraStore.FINAL_DONE, EraStore.WORLD);
        try {
            MegaMachine.anyDimensionForTests = true;
            star.kind = Kind.BLACK_HOLE;
            star.mass = 1000;
            QsoMachine m = MegaTestRig.build(helper, QsoMachine.SHAPE, GTIAMegastructures.QSO_DEF, 5, 32);
            helper.assertTrue(m.checkPatternWithLock(), "the whole gun forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            era.set(EraStore.NEGENTROPY_TAKEN, EraStore.WORLD, 0);
            m.setSize(0);
            for (int i = 0; i < 200 && m.shots() == 0; i++) m.testSecond(owner);
            helper.assertTrue(m.shots() > 0 && era.get(EraStore.NEGENTROPY, owner) > 0, "it fires and harvests "
                    + "negentropy: " + m.shots());
            MegaTestRig.lit(helper, m, "shot", "quasar_jet_n", "quasar_jet_s");
            // The world's stock spent: with the ending rule on, the next shot is the last.
            helper.assertTrue(WorldRules.get(GTIARules.FINAL_ENDING) != 0, "the ending is on by default");
            era.set(EraStore.NEGENTROPY_TAKEN, EraStore.WORLD, WorldRules.get(GTIARules.NEGENTROPY_STOCK));
            long before = m.shots();
            for (int i = 0; i < 200 && !m.ended(); i++) m.testSecond(owner);
            helper.assertTrue(m.ended() && m.shots() == before + 1 && era.get(EraStore.FINAL_DONE, EraStore.WORLD) == 1,
                    "the last shot ends the game");
            helper.assertTrue(!m.isWorkingEnabled(), "and the gun falls silent");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
            era.set(EraStore.NEGENTROPY_TAKEN, EraStore.WORLD, taken);
            era.set(EraStore.FINAL_DONE, EraStore.WORLD, done);
        }
    }

    static void noCageNoGun(GameTestHelper helper) {
        QsoMachine m = MegaTestRig.build(helper, QsoMachine.SHAPE, GTIAMegastructures.QSO_DEF, 5, 33);
        var shape = MegaShape.get(QsoMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "cage", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing cage block keeps the gun from forming");
        helper.succeed();
    }
}
