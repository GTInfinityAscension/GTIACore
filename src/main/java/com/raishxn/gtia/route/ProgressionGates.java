package com.raishxn.gtia.route;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;

import java.util.List;
import java.util.function.Consumer;

/**
 * Recipes moved for the progression of the restructuring: the Eye of Ender only in the HV Assembler (GTIA-D-352),
 * the personal dimension portal only as a quest reward (GTIA-D-347), the Nexus Flux Matrix in HV (GTIA-D-350; its
 * losses are in the pack's config/gtna/balance/nexus_flux_matrix.json).
 */
public final class ProgressionGates {

    static final List<String> REMOVED = List.of(
            "minecraft:ender_eye",
            "gtceu:chemical_bath/eye_of_ender",
            "modernspace:personal_space_portal",
            "gtceu:assembler/nexus_flux_matrix");

    private ProgressionGates() {}

    public static void removeRecipes(Consumer<ResourceLocation> consumer) {
        REMOVED.forEach(id -> consumer.accept(new ResourceLocation(id)));
    }

    public static List<ResourceLocation> ids() {
        return REMOVED.stream().map(ResourceLocation::new).toList();
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        // EN: HV emitters and sensors are the first parts that need the eye, so the End opens with them (GTIA-D-352).
        // PT: Emissores e sensores HV são as primeiras peças que pedem o olho, então o End abre junto (GTIA-D-352).
        // ZH: HV 发射器和传感器是最早需要末影之眼的部件，所以末地随之开放（GTIA-D-352）。
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("ender_eye"))
                .inputItems(new ItemStack(Items.ENDER_PEARL))
                .inputItems(new ItemStack(Items.BLAZE_POWDER, 2))
                .outputItems(new ItemStack(Items.ENDER_EYE))
                .duration(200).EUt(GTValues.VA[GTValues.HV]).save(provider);
        if (!net.minecraftforge.fml.ModList.get().isLoaded("gtna")) return;
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("nexus_flux_matrix"))
                .inputItems(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 4)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputItems(TagPrefix.plate, GTMaterials.EnderEye, 2)
                .inputItems(TagPrefix.frameGt, GTMaterials.StainlessSteel)
                .outputItems(new ItemStack(
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtna", "nexus_flux_matrix"))))
                .duration(400).EUt(GTValues.VA[GTValues.HV]).save(provider);
    }
}
