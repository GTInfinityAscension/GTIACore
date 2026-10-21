package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.nidavellir.NidavellirMachine;
import com.raishxn.gtia.mega.nidavellir.NidavellirPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link NidavellirGameTests}, apart so the GTNA classes load only when it is registered. */
final class NidavellirTestRig {

    private NidavellirTestRig() {}

    static void pressMath(GameTestHelper helper) {
        helper.assertTrue(NidavellirPhysics.parallel(0) == 256 && NidavellirPhysics.parallel(4) == 4096,
                "256 × 2^c parallels");
        helper.assertTrue(NidavellirPhysics.neutronium(3, false) == 8 && NidavellirPhysics.neutronium(3, true) == 4 &&
                NidavellirPhysics.neutronium(0, true) == 1, "2^c dust, half with the pulsar wind, never zero");
        helper.assertTrue(NidavellirPhysics.maxCompression(1) == 0 && NidavellirPhysics.maxCompression(5) == 4,
                "each phase opens a step of compression");
        helper.succeed();
    }

    /** An Alloy Blast Smelter recipe with item inputs only, the lowest heat there is. */
    private static GTRecipe simpleAlloy(GameTestHelper helper) {
        GTRecipe best = null;
        for (var r : helper.getLevel().getRecipeManager().getAllRecipesFor(GCYMRecipeTypes.ALLOY_BLAST_RECIPES)) {
            if (!RecipeHelper.getInputFluids(r).isEmpty() || RecipeHelper.getInputItems(r).size() > 3) continue;
            if (best == null || r.data.getInt("ebf_temp") < best.data.getInt("ebf_temp")) best = r;
        }
        if (best == null) throw new IllegalStateException("no plain Alloy Blast Smelter recipe");
        return best;
    }

    static void forgesUnderNeutronium(GameTestHelper helper) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        try {
            MegaMachine.anyDimensionForTests = true;
            NidavellirMachine m = MegaTestRig.build(helper, NidavellirMachine.SHAPE,
                    GTIAMegastructures.NIDAVELLIR_DEF, 5, 18);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var dustBus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 1,
                    GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            MegaTestRig.hatch(helper, m, 2, GTMachines.ITEM_EXPORT_BUS[GTValues.LuV]);
            MegaTestRig.hatch(helper, m, 3, GTMachines.FLUID_EXPORT_HATCH[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole forge forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            var recipe = simpleAlloy(helper);
            for (var stack : RecipeHelper.getInputItems(recipe)) {
                var big = stack.copyWithCount(64);
                for (int s = 0; s < bus.getInventory().getSlots() && !big.isEmpty(); s++)
                    big = bus.getInventory().storage.insertItem(s, big, false);
            }
            star.kind = Kind.YELLOW_DWARF;
            m.setCompression(0);

            // No neutronium: the forge waits.
            m.testSecond(owner);
            helper.assertTrue(m.crafted() == 0 && "neutronium".equals(m.blocked()), "no neutronium, no batch");

            dustBus.getInventory().storage.insertItem(0, ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neutronium,
                    64), false);
            for (int i = 0; i < Math.max(2, recipe.duration / 20 + 2) && m.crafted() == 0; i++) m.testSecond(owner);
            helper.assertTrue(m.crafted() > 1, "the forge runs " + recipe.id + " in parallel: " + m.crafted() + " (" +
                    m.blocked() + ")");
            helper.assertTrue(m.neutroniumUsed() > 0 && m.view()[1] == 1, "each batch takes neutronium, speed ×1");

            star.kind = Kind.NEUTRON_STAR;
            m.testSecond(owner);
            helper.assertTrue(m.view()[1] == NidavellirPhysics.SPEED_NEUTRON_STAR, "a neutron star presses ×4");
            MegaTestRig.capturesAndReleases(helper, m);
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
        }
    }

    static void noOuterRingNoForge(GameTestHelper helper) {
        NidavellirMachine m = MegaTestRig.build(helper, NidavellirMachine.SHAPE, GTIAMegastructures.NIDAVELLIR_DEF, 5,
                19);
        var shape = MegaShape.get(NidavellirMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "outer", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing outer-ring block keeps the forge from forming");
        helper.succeed();
    }
}
