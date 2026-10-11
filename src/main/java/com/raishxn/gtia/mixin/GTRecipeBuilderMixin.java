package com.raishxn.gtia.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.raishxn.gtia.rules.GTIARecipeRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.Map;

/**
 * Applies the recipe rules ({@link GTIARecipeRules}) to every recipe GTCEu builds. The recipe keeps the maps it is
 * given, so the rules change the constructor arguments and the builder itself is never changed.
 */
@Mixin(value = GTRecipeBuilder.class, remap = false)
public abstract class GTRecipeBuilderMixin {

    @Unique
    private static final String GTIA$NEW_RECIPE = "Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;<init>(" +
            "Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;Lnet/minecraft/resources/ResourceLocation;" +
            "Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;" +
            "Ljava/util/Map;Ljava/util/Map;Ljava/util/List;Ljava/util/List;Lnet/minecraft/nbt/CompoundTag;I" +
            "Lcom/gregtechceu/gtceu/api/recipe/category/GTRecipeCategory;I)V";

    @Shadow
    public GTRecipeType recipeType;

    @Shadow
    @Final
    public Map<RecipeCapability<?>, List<Content>> tickOutput;

    @ModifyArg(method = "buildRawRecipe", at = @At(value = "INVOKE", target = GTIA$NEW_RECIPE), index = 13)
    private int gtia$duration(int base) {
        return GTIARecipeRules.duration(base, recipeType, tickOutput);
    }

    @ModifyArg(method = "buildRawRecipe", at = @At(value = "INVOKE", target = GTIA$NEW_RECIPE), index = 4)
    private Map<RecipeCapability<?>, List<Content>> gtia$tickInputs(Map<RecipeCapability<?>, List<Content>> base) {
        return GTIARecipeRules.tickInputs(base);
    }
}
