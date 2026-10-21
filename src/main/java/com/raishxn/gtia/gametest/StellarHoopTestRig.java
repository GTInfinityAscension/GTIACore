package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.hoop.HoopPhysics;
import com.raishxn.gtia.mega.hoop.StellarHoopMachine;

import java.util.UUID;

/** Bodies of {@link StellarHoopGameTests}, apart so the GTNA classes load only when the hoop is registered. */
final class StellarHoopTestRig {

    private StellarHoopTestRig() {}

    static void coronaModel(GameTestHelper helper) {
        helper.assertTrue(HoopPhysics.coronaKelvin(HoopPhysics.HEIGHT_MIN) > 1e5, "the corona is over 100 000 K");
        helper.assertTrue(HoopPhysics.coronaKelvin(30) > HoopPhysics.coronaKelvin(2) &&
                HoopPhysics.coronaKelvin(30) < 2.1e6, "it climbs towards 2 MK");
        helper.assertTrue(HoopPhysics.parallel(5, 2) == 1024 && HoopPhysics.parallel(5, 30) < 70,
                "parallel falls with height: " + HoopPhysics.parallel(5, 30));
        double low = HoopPhysics.load(3, 2) / HoopPhysics.parallel(3, 2),
                high = HoopPhysics.load(3, 30) / HoopPhysics.parallel(3, 30);
        helper.assertTrue(high < low, "a high dive is cheaper per craft in shield load");
        helper.assertTrue(HoopPhysics.load(4, 10) < HoopPhysics.load(3, 10) * 1.21,
                "the radiators cut the load");
        helper.assertTrue(HoopPhysics.coolantDemand(30, 3) == 3 && HoopPhysics.coolantDemand(30, 1) == 30,
                "the shifted superfluid takes ten times liquid helium");
        helper.succeed();
    }

    /** An Electric Blast Furnace recipe with only plain item inputs and outputs, within the corona's heat. */
    private static GTRecipe simpleBlast(GameTestHelper helper) {
        for (var r : helper.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.BLAST_RECIPES)) {
            if (!RecipeHelper.getInputFluids(r).isEmpty() || !RecipeHelper.getOutputFluids(r).isEmpty()) continue;
            var in = RecipeHelper.getInputItems(r);
            if (in.isEmpty() || in.size() > 2 || RecipeHelper.getOutputItems(r).isEmpty()) continue;
            if (r.data.getInt("ebf_temp") > 3000) continue;
            return r;
        }
        throw new IllegalStateException("no plain Electric Blast Furnace recipe");
    }

    static void forgesInTheCorona(GameTestHelper helper) {
        try {
            MegaMachine.anyDimensionForTests = true;
            StellarHoopMachine m = MegaTestRig.build(helper, StellarHoopMachine.SHAPE,
                    GTIAMegastructures.STELLAR_HOOP_DEF, 5, 8);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var out = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 1, GTMachines.ITEM_EXPORT_BUS[GTValues.LuV]);
            var coolant = (FluidHatchPartMachine) MegaTestRig.hatch(helper, m, 2,
                    GTMachines.FLUID_IMPORT_HATCH[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole hoop forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            var recipe = simpleBlast(helper);
            for (var stack : RecipeHelper.getInputItems(recipe)) {
                var big = stack.copyWithCount(Math.min(64, stack.getCount() * 32));
                for (int s = 0; s < bus.getInventory().getSlots() && !big.isEmpty(); s++)
                    big = bus.getInventory().storage.insertItem(s, big, false);
            }
            coolant.tank.fillInternal(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 64000),
                    IFluidHandler.FluidAction.EXECUTE);
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setHeight(HoopPhysics.HEIGHT_MIN);
            for (int i = 0; i < Math.max(2, recipe.duration / 40 + 2); i++) m.testSecond(owner);
            var match = RecipeHelper.matchContents(m, recipe);
            boolean found = GTRecipeTypes.BLAST_RECIPES.searchRecipe(m, r -> r == recipe).hasNext();
            helper.assertTrue(m.crafted() > 1, "the hoop runs " + recipe.id + " in parallel: " + m.crafted() +
                    " (" + m.blocked() + "; match " + match.isSuccess() + " " + match.reason() + "; found " + found +
                    "; proxies " + m.hasCapabilityProxies() + "; inputs " + RecipeHelper.getInputItems(recipe) + ")");
            boolean any = false;
            for (int s = 0; s < out.getInventory().getSlots(); s++)
                any |= !out.getInventory().storage.getStackInSlot(s).isEmpty();
            helper.assertTrue(any, "the outputs reach the output bus");
            helper.assertTrue(m.view()[3] >= 1 && m.integrity() == HoopPhysics.INTEGRITY,
                    "liquid helium carries the heat away");
            long piece = MegaCapture.positions(StellarHoopMachine.SHAPE, m.getPos(), MegaTestRig.FRONT)
                    .get(MegaTestRig.part(m.shape(), "hoop", "body"))[0];
            helper.assertTrue(m.isCaptured() && helper.getLevel().getBlockState(BlockPos.of(piece)).isAir(),
                    "the hoop is in the controller while it turns");

            // Without coolant the shields wear.
            coolant.tank.drainInternal(64000, IFluidHandler.FluidAction.EXECUTE);
            m.testSecond(owner);
            helper.assertTrue(m.integrity() < HoopPhysics.INTEGRITY, "no coolant: the shields wear");
            m.setWorkingEnabled(false);
            helper.assertTrue(!helper.getLevel().getBlockState(BlockPos.of(piece)).isAir(),
                    "switching off puts the hoop back");
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
        }
    }

    static void noClampNoHoop(GameTestHelper helper) {
        StellarHoopMachine m = MegaTestRig.build(helper, StellarHoopMachine.SHAPE,
                GTIAMegastructures.STELLAR_HOOP_DEF, 5, 9);
        var shape = MegaShape.get(StellarHoopMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "clamps", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing clamp block keeps the hoop from forming");
        helper.succeed();
    }
}
