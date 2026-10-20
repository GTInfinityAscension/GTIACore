package com.raishxn.gtia.mega;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Consumer;

/** Recipes of the megastructures' controllers (provisional, until the Ψ era's progression is drawn). */
public final class MegaRecipes {

    private MegaRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        // Helium II: liquid helium cooled below the lambda point (2.17 K).
        GTRecipeTypes.VACUUM_RECIPES.recipeBuilder(GTIAMod.id("superfluid_helium"))
                .inputFluids(GTMaterials.Helium.getFluid(
                        com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.LIQUID, 1000))
                .outputFluids(MegaMaterials.SUPERFLUID_HELIUM.getFluid(1000))
                .EUt(GTValues.VA[GTValues.UHV]).duration(200)
                .save(provider);
        if (GTIAMegastructures.MATRIOSHKA_BRAIN == null) return;
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("matrioshka_brain"))
                .inputItems(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY.asStack(16))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UEV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(9216))
                .outputItems(GTIAMegastructures.MATRIOSHKA_BRAIN.asStack())
                .EUt(GTValues.VA[GTValues.UEV]).duration(6000)
                .save(provider);
    }
}
