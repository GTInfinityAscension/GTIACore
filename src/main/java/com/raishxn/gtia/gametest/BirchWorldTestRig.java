package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.EraStore;
import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.birch.BirchPhysics;
import com.raishxn.gtia.mega.birch.BirchWorldMachine;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link BirchWorldGameTests}, apart so the GTNA classes load only when it is registered. */
final class BirchWorldTestRig {

    private BirchWorldTestRig() {}

    static void causalMath(GameTestHelper helper) {
        helper.assertTrue(BirchPhysics.layers(5) == 200 && BirchPhysics.capacity(5) == 400, "40 layers a phase, 2 "
                + "modules a layer");
        helper.assertTrue(BirchPhysics.credit(5, 10) == 10 * BirchPhysics.credit(5, 1) &&
                BirchPhysics.credit(2, 1) * 2.5 == BirchPhysics.credit(5, 1), "credit ~ layers x mass");
        helper.assertTrue(Math.abs(BirchPhysics.grow(1000) - 1000.5) < 1e-9, "0.05 % a second");
        helper.assertTrue(BirchPhysics.repayment(16) == 16 * Math.pow(2, 40) && BirchPhysics.repayment(99) ==
                BirchPhysics.repayment(16), "repayment steps of 2^40, up to 16");
        helper.assertTrue(BirchPhysics.eraLevel(Math.pow(2, 49)) == 0 && BirchPhysics.eraLevel(Math.pow(2, 56)) == 2,
                "era levels by debt paid");
        helper.succeed();
    }

    /** An Assembler recipe with a few plain item inputs and an item output. */
    private static GTRecipe simpleAssembly(GameTestHelper helper) {
        GTRecipe best = null;
        for (var r : helper.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.ASSEMBLER_RECIPES)) {
            if (!RecipeHelper.getInputFluids(r).isEmpty() || !RecipeHelper.getOutputFluids(r).isEmpty()) continue;
            var in = RecipeHelper.getInputItems(r);
            if (in.isEmpty() || in.size() > 3 || RecipeHelper.getOutputItems(r).isEmpty()) continue;
            if (best == null || in.size() < RecipeHelper.getInputItems(best).size()) best = r;
        }
        if (best == null) throw new IllegalStateException("no plain Assembler recipe");
        return best;
    }

    static void debtAndParadox(GameTestHelper helper) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass;
        try {
            MegaMachine.anyDimensionForTests = true;
            star.kind = Kind.BLACK_HOLE;
            star.mass = 10;
            BirchWorldMachine m = MegaTestRig.build(helper, BirchWorldMachine.SHAPE, GTIAMegastructures.BIRCH_WORLD_DEF,
                    5, 30);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var modules = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 1,
                    GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var out = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 2, GTMachines.ITEM_EXPORT_BUS[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole world forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            modules.getInventory().storage.insertItem(0, GCYMMachines.LARGE_ASSEMBLER.asStack(2), false);
            var recipe = simpleAssembly(helper);
            for (var stack : RecipeHelper.getInputItems(recipe)) {
                var big = stack.copyWithCount(64);
                for (int s = 0; s < bus.getInventory().getSlots() && !big.isEmpty(); s++)
                    big = bus.getInventory().storage.insertItem(s, big, false);
            }
            m.setRepay(0);
            for (int i = 0; i < 4 && m.crafted() == 0; i++) m.testSecond(owner);
            helper.assertTrue(m.crafted() > 1 && m.debt() > 0, "hosted work is done and owed: " + m.crafted() + ", " +
                    m.debt());
            double before = m.debt();
            m.setRepay(BirchPhysics.REPAY_MAX);
            m.testSecond(owner);
            helper.assertTrue(m.debt() < before && m.paid() > 0, "repayment brings the debt down");
            helper.assertTrue(EraStore.get(helper.getLevel().getServer()).get(EraStore.CAUSAL_PAID, owner) == m.paid(),
                    "the Causal era keeps the debt paid");
            // Past the credit: a paradox.
            m.setRepay(0);
            m.addDebt(BirchPhysics.credit(5, 10) * 2);
            m.testSecond(owner);
            boolean empty = true;
            for (int s = 0; s < out.getInventory().getSlots(); s++)
                empty &= out.getInventory().storage.getStackInSlot(s).isEmpty();
            helper.assertTrue(m.paradoxes() == 1 && m.debt() == 0 && empty,
                    "a paradox erases the debt and what it bought");
            long made = m.crafted();
            m.testSecond(owner);
            helper.assertTrue(m.crafted() == made && m.view()[9] > 0, "the loops are closed for a while");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
        }
    }

    static void noLayerNoWorld(GameTestHelper helper) {
        BirchWorldMachine m = MegaTestRig.build(helper, BirchWorldMachine.SHAPE, GTIAMegastructures.BIRCH_WORLD_DEF, 5,
                31);
        var shape = MegaShape.get(BirchWorldMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "layer_1", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing inner-layer block keeps the world from forming");
        helper.succeed();
    }
}
