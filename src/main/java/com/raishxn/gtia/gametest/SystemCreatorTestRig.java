package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.PsiStore;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.creator.CreatorPhysics;
import com.raishxn.gtia.mega.creator.CreatorPhysics.Stage;
import com.raishxn.gtia.mega.creator.SystemCreatorMachine;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link SystemCreatorGameTests}, apart so the GTNA classes load only when the Creator is registered. */
final class SystemCreatorTestRig {

    private SystemCreatorTestRig() {}

    static void creatorMath(GameTestHelper helper) {
        helper.assertTrue(CreatorPhysics.hydrogen(Kind.YELLOW_DWARF) == 1_000_000, "a Sun is 10^6 mB of hydrogen");
        helper.assertTrue(CreatorPhysics.collapseSeconds(Kind.YELLOW_DWARF) == 60, "a Sun falls in in 60 s");
        helper.assertTrue(CreatorPhysics.collapseSeconds(Kind.RED_DWARF) < 60 &&
                CreatorPhysics.collapseSeconds(Kind.BLUE_GIANT) > 60, "free fall scales with √(R³/M)");
        helper.assertTrue(CreatorPhysics.psi(Kind.NEUTRON_STAR, 1) == 4L * (1L << 20) * 15 &&
                CreatorPhysics.psi(Kind.BLUE_GIANT, 2) == 2L * (1L << 20) * 15,
                "remnants cost four times the Ψ of their cloud; metals double it at Z 2");
        helper.assertTrue(CreatorPhysics.igniteEu(Kind.BLUE_GIANT) > CreatorPhysics.igniteEu(Kind.YELLOW_DWARF),
                "a bigger cloud has more binding energy");
        helper.assertTrue(CreatorPhysics.era(0) == 0 && CreatorPhysics.era(1) == 1 && CreatorPhysics.era(3) == 2 &&
                CreatorPhysics.era(6) == 3, "Dimensional era I, II, III at 1, 3, 6 systems");
        helper.assertTrue(CreatorPhysics.phase(Kind.RED_DWARF) == 1 && CreatorPhysics.phase(Kind.BLACK_HOLE) == 5,
                "kinds unlock by phase");
        helper.succeed();
    }

    static void forgesARedDwarf(GameTestHelper helper) {
        var store = StarSystems.get(helper.getLevel().getServer());
        int made = -1;
        try {
            MegaMachine.anyDimensionForTests = true;
            SystemCreatorMachine m = MegaTestRig.build(helper, SystemCreatorMachine.SHAPE,
                    GTIAMegastructures.SYSTEM_CREATOR_DEF, 5, 10);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var tank = (FluidHatchPartMachine) MegaTestRig.hatch(helper, m, 1,
                    GTMachines.FLUID_IMPORT_HATCH[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole Creator forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setOwnerForTests(owner);
            m.setKind(Kind.RED_DWARF);
            int free = store.freeSlot();
            helper.assertTrue(free >= 0, "a free system slot");

            // No Ψ: it does not start.
            m.testSecond(owner);
            m.start(helper.getLevel());
            helper.assertTrue(m.stage() == Stage.IDLE && "dust".equals(m.message()) || "psi".equals(m.message()),
                    "without iron or Ψ it does not start: " + m.message());

            var iron = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron, 64);
            bus.getInventory().storage.insertItem(0, iron, false);
            PsiStore.get(helper.getLevel().getServer()).add(owner, CreatorPhysics.psi(Kind.RED_DWARF, 1) * 2);
            tank.tank.fillInternal(GTMaterials.Hydrogen.getFluid(FluidStorageKeys.PLASMA, 64000),
                    IFluidHandler.FluidAction.EXECUTE);
            m.start(helper.getLevel());
            helper.assertTrue(m.stage() == Stage.GATHER, "it starts gathering: " + m.message());
            made = m.targetSlot();
            for (int i = 0; i < 200 && m.stage() != Stage.IDLE; i++) m.testSecond(owner);
            var star = store.star(StarSystems.slot(made));
            helper.assertTrue(star != null && star.kind == Kind.RED_DWARF && owner.equals(star.owner),
                    "a red dwarf in a new system: " + (star == null ? "none" : star.kind) + " " + m.message());
            helper.assertTrue(store.madeBy(owner) == 1 && CreatorPhysics.era(store.madeBy(owner)) == 1,
                    "Dimensional era I");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            if (made >= 0) store.clear(made);
        }
    }

    static void noTalonNoCreator(GameTestHelper helper) {
        SystemCreatorMachine m = MegaTestRig.build(helper, SystemCreatorMachine.SHAPE,
                GTIAMegastructures.SYSTEM_CREATOR_DEF, 5, 11);
        var shape = MegaShape.get(SystemCreatorMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "talons", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing talon block keeps the Creator from forming");
        helper.succeed();
    }
}
