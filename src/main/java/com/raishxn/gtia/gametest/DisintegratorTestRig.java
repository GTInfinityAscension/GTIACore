package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.disintegrator.DisintegratorMachine;
import com.raishxn.gtia.mega.disintegrator.DisintegratorPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link DisintegratorGameTests}, apart so the GTNA classes load only when it is registered. */
final class DisintegratorTestRig {

    private DisintegratorTestRig() {}

    static void bindingMath(GameTestHelper helper) {
        for (double f : new double[] { 0.1, 0.25, 0.6, 0.9 })
            helper.assertTrue(Math.abs(DisintegratorPhysics.massShare(DisintegratorPhysics.energyShare(f)) - f) < 1e-9,
                    "e(f) and f(e) are inverse");
        helper.assertTrue(Math.abs(DisintegratorPhysics.energyShare(1 - 0.125) - (1 - Math.pow(0.125, 5 / 3.0))) <
                1e-12, "e = 1 - (1 - f)^(5/3)");
        helper.assertTrue(DisintegratorPhysics.binding(2) == 4 * DisintegratorPhysics.binding(1), "U grows as M^2");
        helper.assertTrue(DisintegratorPhysics.layer(0.1) == 0 && DisintegratorPhysics.layer(0.3) == 1 &&
                DisintegratorPhysics.layer(0.7) == 2 && DisintegratorPhysics.layer(0.95) == 3, "crust, mantle, core, star");
        helper.succeed();
    }

    static void takesASystemApart(GameTestHelper helper) {
        var systems = StarSystems.get(helper.getLevel().getServer());
        int slot = systems.freeSlot();
        helper.assertTrue(slot >= 0, "a free slot to make a system in");
        UUID owner = UUID.randomUUID();
        try {
            MegaMachine.anyDimensionForTests = true;
            systems.create(slot, Kind.YELLOW_DWARF, owner, 42);
            DisintegratorMachine m = MegaTestRig.build(helper, DisintegratorMachine.SHAPE,
                    GTIAMegastructures.DISINTEGRATOR_DEF, 5, 28);
            var out = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_EXPORT_BUS[GTValues.UV]);
            var gas = (FluidHatchPartMachine) MegaTestRig.hatch(helper, m, 1,
                    GTMachines.FLUID_EXPORT_HATCH[GTValues.UV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole disintegrator forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            StellarEngineTestRig.power(helper, owner);
            m.testSecond(owner);
            m.pick(helper.getLevel(), slot);
            m.setStopAt(0);
            for (int i = 0; i < 2000 && m.taken() < DisintegratorPhysics.CRUST - 1e-9; i++) m.testSecond(owner);
            helper.assertTrue(Math.abs(m.taken() - DisintegratorPhysics.CRUST) < 1e-9, "the crust is taken: " +
                    m.taken());
            m.testSecond(owner);
            helper.assertTrue(systems.star(StarSystems.slot(slot)).kind == Kind.YELLOW_DWARF,
                    "stopping at the crust keeps the system");
            boolean dust = false;
            for (int s = 0; s < out.getInventory().getSlots(); s++)
                dust |= !out.getInventory().storage.getStackInSlot(s).isEmpty();
            helper.assertTrue(dust, "the crust's dust reaches the bus");
            m.setStopAt(3);
            for (int i = 0; i < 4000 && m.target() >= 0; i++) m.testSecond(owner);
            helper.assertTrue(systems.star(StarSystems.slot(slot)).kind == Kind.NONE && m.erased() == 1,
                    "taking the star erases the system");
            helper.assertTrue(gas.tank.getFluidInTank(0).getAmount() > 0 || m.owedGas() > 0,
                    "the star pours out as gas");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            if (slot >= 0) systems.clear(slot);
        }
    }

    static void noRingNoDisintegrator(GameTestHelper helper) {
        DisintegratorMachine m = MegaTestRig.build(helper, DisintegratorMachine.SHAPE,
                GTIAMegastructures.DISINTEGRATOR_DEF, 5, 29);
        var shape = MegaShape.get(DisintegratorMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "support", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing support block keeps the disintegrator from forming");
        helper.succeed();
    }
}
