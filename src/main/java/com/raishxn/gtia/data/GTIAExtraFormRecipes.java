package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Consumer;

/** Material-conserving routes for GTIA's GTNA-style compound forms. */
public final class GTIAExtraFormRecipes {

    private GTIAExtraFormRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        for (Material material : new Material[] { GTIAAlloys.PULSATING_ALLOY, GTIAAlloys.CONDUCTIVE_ALLOY }) {
            ingots(provider, material);
            plate(provider, material, GTIAExtraForms.TRIPLE_PLATE, 3);
            plate(provider, material, GTIAExtraForms.QUADRUPLE_PLATE, 4);
            plate(provider, material, GTIAExtraForms.QUINTUPLE_PLATE, 5);
            plate(provider, material, GTIAExtraForms.SUPERDENSE_PLATE, 64);
        }
    }

    private static void ingots(Consumer<FinishedRecipe> provider, Material material) {
        var ingot = new MaterialEntry(TagPrefix.ingot, material);
        var doubleIngot = new MaterialEntry(GTIAExtraForms.DOUBLE_INGOT, material);
        var quadrupleIngot = new MaterialEntry(GTIAExtraForms.QUADRUPLE_INGOT, material);
        String name = material.getName();
        VanillaRecipeHelper.addShapedRecipe(provider, GTIAMod.id("double_" + name + "_ingot"),
                ChemicalHelper.get(GTIAExtraForms.DOUBLE_INGOT, material), "P", "P", 'P', ingot);
        VanillaRecipeHelper.addShapelessRecipe(provider, GTIAMod.id("triple_" + name + "_ingot"),
                ChemicalHelper.get(GTIAExtraForms.TRIPLE_INGOT, material), doubleIngot, ingot);
        VanillaRecipeHelper.addShapedRecipe(provider, GTIAMod.id("quadruple_" + name + "_ingot"),
                ChemicalHelper.get(GTIAExtraForms.QUADRUPLE_INGOT, material), "DD", 'D', doubleIngot);
        VanillaRecipeHelper.addShapelessRecipe(provider, GTIAMod.id("quintuple_" + name + "_ingot"),
                ChemicalHelper.get(GTIAExtraForms.QUINTUPLE_INGOT, material), quadrupleIngot, ingot);
    }

    private static void plate(Consumer<FinishedRecipe> provider, Material material, TagPrefix output, int count) {
        GTRecipeTypes.BENDER_RECIPES.recipeBuilder(GTIAMod.id(material.getName() + "_" + count + "x_plate"))
                .inputItems(TagPrefix.plate, material, count)
                .circuitMeta(count == 64 ? 1 : count)
                .outputItems(output, material)
                .duration(40 * count)
                .EUt(GTValues.VA[count == 64 ? GTValues.HV : GTValues.MV])
                .save(provider);
    }
}
