package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtia.data.GTIARecipeTypes;
import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import com.raishxn.gtia.mega.yggdrasil.YggdrasilMachine;
import com.raishxn.gtia.mega.yggdrasil.YggdrasilPhysics;

import java.util.UUID;

/** Bodies of {@link YggdrasilGameTests}, apart so the GTNA classes load only when it is registered. */
final class YggdrasilTestRig {

    private YggdrasilTestRig() {}

    static void gardenMath(GameTestHelper helper) {
        double best = YggdrasilPhysics.bestDepth(1);
        helper.assertTrue(Math.abs(best - 0.5 * Math.log(1 / 0.015)) < 1e-9, "the Sun's best depth is ~2.1: " + best);
        double g = YggdrasilPhysics.growth(1, best);
        helper.assertTrue(g > YggdrasilPhysics.growth(1, best - 1) && g > YggdrasilPhysics.growth(1, best + 1),
                "growth peaks at the best depth");
        helper.assertTrue(YggdrasilPhysics.bestDepth(1e6) == YggdrasilPhysics.DEPTH_MAX &&
                YggdrasilPhysics.bestDepth(0.01) == 0, "a supergiant wants the bottom, a red dwarf the top");
        helper.assertTrue(YggdrasilPhysics.growth(0, 2) == 0 && YggdrasilPhysics.speed(0) ==
                YggdrasilPhysics.SPEED_MIN, "no light, the floor rate");
        helper.succeed();
    }

    /** A Greenhouse recipe with item outputs; null when none is loaded (the crops come from Agronomica). */
    private static GTRecipe someGreenhouse(GameTestHelper helper) {
        for (var r : helper.getLevel().getRecipeManager().getAllRecipesFor(GTIARecipeTypes.GREENHOUSE))
            if (!RecipeHelper.getOutputItems(r).isEmpty() && RecipeHelper.getInputItems(r).size() <= 2) return r;
        return null;
    }

    static void harvestsWithBiomass(GameTestHelper helper) {
        if (someGreenhouse(helper) == null) {
            com.raishxn.gtia.GTIAMod.LOGGER.warn("No Greenhouse recipe (run with -PagronomicaDevJar): Yggdrasil harvest " +
                    "test skipped");
            helper.succeed();
            return;
        }
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass;
        try {
            MegaMachine.anyDimensionForTests = true;
            star.kind = Kind.YELLOW_DWARF;
            star.mass = 1;
            YggdrasilMachine m = MegaTestRig.build(helper, YggdrasilMachine.SHAPE, GTIAMegastructures.YGGDRASIL_DEF,
                    5, 22);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var water = (FluidHatchPartMachine) MegaTestRig.hatch(helper, m, 1,
                    GTMachines.FLUID_IMPORT_HATCH[GTValues.LuV]);
            var out = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 2, GTMachines.ITEM_EXPORT_BUS[GTValues.LuV]);
            var bio = (FluidHatchPartMachine) MegaTestRig.hatch(helper, m, 3,
                    GTMachines.FLUID_EXPORT_HATCH[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole orchid forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            var recipe = someGreenhouse(helper);
            for (var stack : RecipeHelper.getInputItems(recipe)) {
                var big = stack.copyWithCount(64);
                for (int s = 0; s < bus.getInventory().getSlots() && !big.isEmpty(); s++)
                    big = bus.getInventory().storage.insertItem(s, big, false);
            }
            for (var f : RecipeHelper.getInputFluids(recipe)) {
                var lots = f.copy();
                lots.setAmount(Math.max(64_000, f.getAmount() * 64));
                water.tank.fillInternal(lots, IFluidHandler.FluidAction.EXECUTE);
            }
            m.setDepth(YggdrasilPhysics.bestDepth(1));
            for (int i = 0; i < Math.max(2, recipe.duration * 4 / 20 + 4) && m.crafted() == 0; i++) m.testSecond(owner);
            helper.assertTrue(m.crafted() > 1, "the orchid harvests " + recipe.id + " in parallel: " + m.crafted() +
                    " (" + m.blocked() + ")");
            boolean any = false;
            for (int s = 0; s < out.getInventory().getSlots(); s++)
                any |= !out.getInventory().storage.getStackInSlot(s).isEmpty();
            helper.assertTrue(any, "the harvest reaches the output bus");
            helper.assertTrue(m.biomass() > 0 && bio.tank.getFluidInTank(0).getAmount() > 0, "and the biomass: " +
                    m.biomass() + " counted, " + bio.tank.getFluidInTank(0).getAmount() + " mB in the hatch");
            double atBest = m.view()[2];
            m.setDepth(YggdrasilPhysics.DEPTH_MAX);
            m.testSecond(owner);
            helper.assertTrue(m.view()[2] < atBest, "too deep: slower growth");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
        }
    }

    static void noShellNoOrchid(GameTestHelper helper) {
        YggdrasilMachine m = MegaTestRig.build(helper, YggdrasilMachine.SHAPE, GTIAMegastructures.YGGDRASIL_DEF, 5,
                23);
        var shape = MegaShape.get(YggdrasilMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "shell_1", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing shell block keeps the orchid from forming");
        helper.succeed();
    }
}
