package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.hraemc.HraeMachine;
import com.raishxn.gtia.mega.hraemc.HraePhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link HraeGameTests}, apart so the GTNA classes load only when it is registered. */
final class HraeTestRig {

    private HraeTestRig() {}

    static void kerrMath(GameTestHelper helper) {
        helper.assertTrue(Math.abs(HraePhysics.isco(0) - 6) < 1e-9, "r_isco = 6 GM/c² without spin");
        helper.assertTrue(Math.abs(HraePhysics.isco(0.998) - 1.237) < 0.01, "r_isco ≈ 1.24 at a = 0.998: " +
                HraePhysics.isco(0.998));
        helper.assertTrue(Math.abs(HraePhysics.efficiency(0) - 0.0572) < 0.001 &&
                Math.abs(HraePhysics.efficiency(0.998) - 0.32) < 0.01, "η 5.7% to 32%");
        helper.assertTrue(Math.abs(HraePhysics.rotationalShare(1) - 0.2929) < 0.001, "29% of the mass at full spin");
        helper.assertTrue(HraePhysics.accretionEut(1e12, 0.5, 10) == HraePhysics.eddingtonEut(10),
                "accretion stops at Eddington");
        helper.assertTrue(HraePhysics.spinChange(0, HraePhysics.penroseEut(100, 0.9, 10), 0.9, 10) < 0 &&
                HraePhysics.spinChange(1000, 0, 0.5, 10) > 0, "Penrose slows the hole, feeding spins it up");
        helper.succeed();
    }

    static void powerFromAHole(GameTestHelper helper) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass, spin = star.spin;
        try {
            MegaMachine.anyDimensionForTests = true;
            HraeMachine m = MegaTestRig.build(helper, HraeMachine.SHAPE, GTIAMegastructures.HRAEMC_DEF, 5, 16);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole plant forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);

            // Around a shining star it does nothing.
            star.kind = Kind.YELLOW_DWARF;
            m.testSecond(owner);
            helper.assertTrue(m.view()[4] == 0 && m.view()[5] == 0, "no hole, no power");

            star.kind = Kind.BLACK_HOLE;
            star.mass = 10;
            star.spin = 0.5;
            for (int s = 0; s < 4; s++) bus.getInventory().storage.insertItem(s, new ItemStack(Items.COBBLESTONE, 64),
                    false);
            m.setPenrose(0);
            m.testSecond(owner);
            helper.assertTrue(m.view()[4] > 0 && m.view()[6] > 0 && star.spin > 0.5,
                    "fed: accretion and jets, and the hole spins up: " + star.spin);
            m.setPenrose(100);
            double before = star.spin;
            m.testSecond(owner);
            helper.assertTrue(m.view()[5] > 0 && star.spin < before, "Penrose takes power and spin");
            helper.assertTrue(m.research() > 0, "the antennas give research");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
            star.spin = spin;
        }
    }

    static void noHoopNoPlant(GameTestHelper helper) {
        HraeMachine m = MegaTestRig.build(helper, HraeMachine.SHAPE, GTIAMegastructures.HRAEMC_DEF, 5, 17);
        var shape = MegaShape.get(HraeMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "frame", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing hoop block keeps the plant from forming");
        helper.succeed();
    }
}
