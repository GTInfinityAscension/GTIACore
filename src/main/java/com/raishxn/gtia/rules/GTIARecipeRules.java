package com.raishxn.gtia.rules;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.utils.GTUtil;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The recipe rules, applied by {@code GTRecipeBuilderMixin} to every recipe GTCEu builds: duration and EU/t for
 * processing recipes, and the fuel efficiency rule (instead of the duration rule) for fuels, so a faster preset never
 * makes fuel worth less energy.
 */
public final class GTIARecipeRules {

    /** Recipe types whose duration is how long a fuel lasts, although they make steam instead of EU. */
    private static final Set<String> STEAM_FUELS = Set.of("large_boiler", "steam_boiler");

    private GTIARecipeRules() {}

    public static int duration(int base, GTRecipeType type, Map<RecipeCapability<?>, List<Content>> tickOutputs) {
        boolean fuel = tickOutputs.containsKey(EURecipeCapability.CAP) ||
                (type != null && STEAM_FUELS.contains(type.registryName.getPath()));
        double factor = WorldRules.get(fuel ? GTIARules.FUEL_EFFICIENCY : GTIARules.RECIPE_DURATION);
        if (factor == 1.0 || base <= 0) return base;
        return Math.max(1, (int) Math.round(base * factor));
    }

    /** Returns the tick inputs with the EU/t rule applied, as a copy (the recipe keeps the map it is given). */
    public static Map<RecipeCapability<?>, List<Content>> tickInputs(Map<RecipeCapability<?>, List<Content>> base) {
        double eu = WorldRules.get(GTIARules.RECIPE_EU);
        List<Content> input = base.get(EURecipeCapability.CAP);
        if (eu == 1.0 || input == null || input.isEmpty()) return base;
        Map<RecipeCapability<?>, List<Content>> copy = new IdentityHashMap<>(base);
        copy.put(EURecipeCapability.CAP, input.stream().map(c -> scaleEU(c, eu)).toList());
        return copy;
    }

    private static Content scaleEU(Content content, double factor) {
        if (!(content.content instanceof EnergyStack stack) || stack.voltage() <= 0) return content;
        // A cheaper or pricier recipe never changes the voltage tier it needs.
        long cap = GTValues.V[GTUtil.getTierByVoltage(stack.voltage())];
        long voltage = Math.max(1, Math.min(cap, Math.round(stack.voltage() * factor)));
        return new Content(new EnergyStack(voltage, stack.amperage()), content.chance, content.maxChance,
                content.tierChanceBoost);
    }
}
