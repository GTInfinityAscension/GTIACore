package com.raishxn.gtia.mixin;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.WorldRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the recipe duration rule to every recipe GTCEu builds, without changing the builder itself. */
@Mixin(value = GTRecipeBuilder.class, remap = false)
public abstract class GTRecipeBuilderMixin {

    @Shadow
    public int duration;

    @Unique
    private int gtia$baseDuration = -1;

    @Inject(method = "buildRawRecipe", at = @At("HEAD"))
    private void gtia$scaleDuration(CallbackInfoReturnable<GTRecipe> cir) {
        double factor = WorldRules.get(GTIARules.RECIPE_DURATION);
        if (factor == 1.0 || duration <= 0) return;
        gtia$baseDuration = duration;
        duration = Math.max(1, (int) Math.round(duration * factor));
    }

    @Inject(method = "buildRawRecipe", at = @At("RETURN"))
    private void gtia$restoreDuration(CallbackInfoReturnable<GTRecipe> cir) {
        if (gtia$baseDuration >= 0) {
            duration = gtia$baseDuration;
            gtia$baseDuration = -1;
        }
    }
}
