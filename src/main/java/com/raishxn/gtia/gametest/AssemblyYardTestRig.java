package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import com.raishxn.gtia.mega.yard.AssemblyYardMachine;
import com.raishxn.gtia.mega.yard.YardPhysics;

import java.util.UUID;

/** Bodies of {@link AssemblyYardGameTests}, apart so the GTNA classes load only when it is registered. */
final class AssemblyYardTestRig {

    private AssemblyYardTestRig() {}

    static void slipwayMath(GameTestHelper helper) {
        helper.assertTrue(YardPhysics.slipways(1) == 18 && YardPhysics.slipways(5) == 90, "18 slipways a phase, 90");
        helper.assertTrue(YardPhysics.parallel(90) == 1440, "16 parallels a slipway");
        helper.assertTrue(Math.abs(YardPhysics.speed(1, false) - 1) < 1e-9, "the Sun builds at x1");
        helper.assertTrue(YardPhysics.speed(Math.pow(10, 4.6), false) == YardPhysics.SPEED_MAX &&
                YardPhysics.speed(0.008, false) == YardPhysics.SPEED_MIN && YardPhysics.speed(0, false) ==
                        YardPhysics.SPEED_MIN, "a blue giant hits the ceiling, a red dwarf or a dark centre the floor");
        helper.assertTrue(YardPhysics.speed(1, true) == 2 && YardPhysics.euFactor(true) == 2.5,
                "rush: twice the rate for 2.5 times the EU");
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

    static void assemblesOnSlipways(GameTestHelper helper) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass;
        try {
            MegaMachine.anyDimensionForTests = true;
            star.kind = Kind.YELLOW_DWARF;
            star.mass = 1;
            AssemblyYardMachine m = MegaTestRig.build(helper, AssemblyYardMachine.SHAPE,
                    GTIAMegastructures.ASSEMBLY_YARD_DEF, 5, 20);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var out = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 1, GTMachines.ITEM_EXPORT_BUS[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole yard forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            var recipe = simpleAssembly(helper);
            for (var stack : RecipeHelper.getInputItems(recipe)) {
                var big = stack.copyWithCount(64);
                for (int s = 0; s < bus.getInventory().getSlots() && !big.isEmpty(); s++)
                    big = bus.getInventory().storage.insertItem(s, big, false);
            }
            m.setActive(0);
            m.testSecond(owner);
            helper.assertTrue(m.crafted() == 0 && "slipways".equals(m.blocked()), "every slipway closed: it waits");
            m.setActive(18);
            for (int i = 0; i < Math.max(2, recipe.duration / 20 + 2) && m.crafted() == 0; i++) m.testSecond(owner);
            helper.assertTrue(m.crafted() > 1, "the yard runs " + recipe.id + " in parallel: " + m.crafted() + " (" +
                    m.blocked() + ")");
            boolean any = false;
            for (int s = 0; s < out.getInventory().getSlots(); s++)
                any |= !out.getInventory().storage.getStackInSlot(s).isEmpty();
            helper.assertTrue(any, "the assemblies reach the output bus");
            helper.assertTrue(m.view()[0] == 288 && Math.abs(m.view()[1] - 1) < 1e-9, "18 slipways x16, the Sun x1");
            helper.assertTrue(AssemblyYardMachine.discount(recipe) == 1, "an ordinary assembly pays full EU");
            GTRecipe mega = null;
            for (var r : helper.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.ASSEMBLY_LINE_RECIPES))
                for (var o : RecipeHelper.getOutputItems(r))
                    if (o.is(GTIAMegastructures.ASSEMBLY_YARD_DEF.asStack().getItem())) mega = r;
            helper.assertTrue(mega != null && AssemblyYardMachine.discount(mega) == YardPhysics.MEGA_DISCOUNT,
                    "a megastructure controller's recipe costs half the EU in the yard");
            m.setRush(true);
            m.testSecond(owner);
            helper.assertTrue(Math.abs(m.view()[1] - 2) < 1e-9, "rush doubles the rate");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
        }
    }

    static void noHubNoYard(GameTestHelper helper) {
        AssemblyYardMachine m = MegaTestRig.build(helper, AssemblyYardMachine.SHAPE,
                GTIAMegastructures.ASSEMBLY_YARD_DEF, 5, 21);
        var shape = MegaShape.get(AssemblyYardMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "hub", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing hub block keeps the yard from forming");
        helper.succeed();
    }
}
