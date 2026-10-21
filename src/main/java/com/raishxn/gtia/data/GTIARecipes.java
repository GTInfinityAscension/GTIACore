package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Consumer;

public final class GTIARecipes {

    private GTIARecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(GTIAMod.id("pulsating_alloy_from_ender_pearl"))
                .inputItems(TagPrefix.ingot, GTMaterials.Iron)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)
                .duration(300).EUt(7).save(provider);

        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(GTIAMod.id("pulsating_alloy_from_phase_dust"))
                .inputItems(TagPrefix.ingot, GTMaterials.Iron)
                .inputItems(TagPrefix.dustImpure, GTIAAlloys.PHASE_MATRIX)
                .outputItems(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)
                .duration(300).EUt(7).save(provider);

        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(GTIAMod.id("conductive_alloy_from_pulsating_alloy"))
                .inputItems(TagPrefix.ingot, GTMaterials.RedAlloy)
                .inputItems(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)
                .outputItems(TagPrefix.ingot, GTIAAlloys.CONDUCTIVE_ALLOY, 2)
                .duration(300).EUt(7).save(provider);

        // EN: Farm machines (D-259, D-271, D-273) in GT's tiered pattern: the Apiary watches the bees (sensor) and
        // pumps water, the Mutation Chamber drives mutations with emitters, the Seed Analyzer is a small scanner.
        // PT: Máquinas de fazenda (D-259, D-271, D-273) no padrão por tier do GT: o Apiário observa as abelhas
        // (sensor) e bombeia água, a Câmara de mutação força a mutação com emissores, o Analisador é um scanner
        // pequeno.
        // ZH: 农场机器（D-259、D-271、D-273）采用 GT 分级配方：工业蜂箱用传感器观察蜜蜂并泵水，突变室用发射器驱动突变，
        // 种子分析仪是小型扫描仪。
        for (int tier : GTValues.tiersBetween(GTIABeeMachines.FIRST_TIER, GTValues.UV)) {
            VanillaRecipeHelper.addShapedRecipe(provider, true,
                    GTIAMod.id(GTIABeeMachines.INDUSTRIAL_APIARY[tier].getName()),
                    GTIABeeMachines.INDUSTRIAL_APIARY[tier].asStack(), "GSG", "PMP", "WCW",
                    'G', GTCraftingComponents.GLASS.get(tier),
                    'S', GTCraftingComponents.SENSOR.get(tier),
                    'P', GTCraftingComponents.PUMP.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier));
            VanillaRecipeHelper.addShapedRecipe(provider, true,
                    GTIAMod.id(GTIABeeMachines.MUTATION_CHAMBER[tier].getName()),
                    GTIABeeMachines.MUTATION_CHAMBER[tier].asStack(), "GEG", "CMC", "WPW",
                    'G', GTCraftingComponents.GLASS.get(tier),
                    'E', GTCraftingComponents.EMITTER.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier),
                    'P', GTCraftingComponents.PUMP.get(tier));
        }
        for (int tier : GTValues.tiersBetween(GTValues.LV, GTValues.HV)) {
            VanillaRecipeHelper.addShapedRecipe(provider, true,
                    GTIAMod.id(GTIABeeMachines.SEED_ANALYZER[tier].getName()),
                    GTIABeeMachines.SEED_ANALYZER[tier].asStack(), "SCS", "EMG", "WCW",
                    'S', GTCraftingComponents.SENSOR.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier),
                    'E', GTCraftingComponents.EMITTER.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'G', GTCraftingComponents.GLASS.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier));
        }
    }
}
