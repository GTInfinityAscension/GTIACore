package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.rules.GTIARecipeRules;
import com.raishxn.gtia.rules.GTIARuleEffects;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.RulePreset;
import com.raishxn.gtia.rules.WorldRules;

import java.util.List;
import java.util.Map;

/** One check per group of GTIA Rules (GTIA-D-256): each preset reaches the setting it is meant to change. */
@GameTestHolder(GTIAMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RulesGameTests {

    private static final WorldRules.Snapshot JOURNEY = WorldRules.Snapshot.of(RulePreset.JOURNEY);
    private static final WorldRules.Snapshot NORMAL = WorldRules.Snapshot.of(RulePreset.NORMAL);

    private RulesGameTests() {}

    /** Recipes: duration and EU/t scale, fuels use the fuel rule, EU/t never crosses a tier, config flags follow. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void recipeRulesReachRecipes(GameTestHelper helper) {
        WorldRules.Snapshot previous = WorldRules.swapActiveForTest(JOURNEY);
        try {
            // The mixin only loads from a jar manifest, so the GameTest checks the rule logic it calls; the
            // production self-test checks the regenerated recipes.
            var processing = GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTIAMod.id("rules_test"))
                    .inputItems(Items.COBBLESTONE).outputItems(Items.GRAVEL).EUt(32).duration(100);
            int duration = GTIARecipeRules.duration(100, processing.recipeType, processing.tickOutput);
            helper.assertTrue(duration == 75, "Journey duration 100 -> 75, got " + duration);
            helper.assertTrue(eu(GTIARecipeRules.tickInputs(processing.tickInput)) == 24, "Journey EU/t 32 -> 24");
            helper.assertTrue(eu(processing.tickInput) == 32, "the builder itself is unchanged");
            var fuel = GTRecipeTypes.COMBUSTION_GENERATOR_FUELS.recipeBuilder(GTIAMod.id("rules_test_fuel"))
                    .EUt(-32).duration(100);
            int fuelDuration = GTIARecipeRules.duration(100, fuel.recipeType, fuel.tickOutput);
            helper.assertTrue(fuelDuration == 115, "Journey fuel lasts 115%, got " + fuelDuration);

            WorldRules.swapActiveForTest(new WorldRules.Snapshot(RulePreset.CUSTOM,
                    Map.of(GTIARules.RECIPE_EU.id(), 1.5)));
            var capped = GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTIAMod.id("rules_test_cap"))
                    .inputItems(Items.COBBLESTONE).outputItems(Items.GRAVEL).EUt(30).duration(100);
            long cappedEU = eu(GTIARecipeRules.tickInputs(capped.tickInput));
            helper.assertTrue(cappedEU == 32, "EU/t ×1.5 stays in LV (32), got " + cappedEU);

            GTIARuleEffects.applyRecipeRules(JOURNEY);
            helper.assertTrue(ConfigHolder.INSTANCE.recipes.casingsPerCraft == 3, "Journey makes 3 casings");
            helper.assertTrue(!ConfigHolder.INSTANCE.recipes.disableManualCompression, "Journey compresses");
            GTIARuleEffects.applyRecipeRules(NORMAL);
            helper.assertTrue(ConfigHolder.INSTANCE.recipes.casingsPerCraft == 2, "Normal makes 2 casings");
            helper.assertTrue(ConfigHolder.INSTANCE.recipes.hardIronRecipes, "Normal keeps hard iron recipes");
        } finally {
            WorldRules.swapActiveForTest(previous);
            GTIARuleEffects.applyRecipeRules(previous);
        }
        helper.succeed();
    }

    /** Steam and power: the large boilers make more steam per water in Journey. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void steamRulesReachBoilers(GameTestHelper helper) {
        WorldRules.Snapshot previous = WorldRules.swapActiveForTest(NORMAL);
        try {
            int normal = ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater;
            WorldRules.swapActiveForTest(JOURNEY);
            int journey = ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater;
            helper.assertTrue(journey == Math.round(normal * 1.25), "steam per water " + normal + " -> " + journey);
        } finally {
            WorldRules.swapActiveForTest(previous);
        }
        helper.succeed();
    }

    /** Machines and tools: runtime settings follow the active world's rules. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void machineAndToolRulesFollowTheWorld(GameTestHelper helper) {
        WorldRules.Snapshot previous = WorldRules.swapActiveForTest(JOURNEY);
        try {
            helper.assertTrue(ConfigHolder.INSTANCE.machines.harmlessActiveTransformers, "Journey: harmless AT");
            helper.assertTrue(ConfigHolder.INSTANCE.tools.rngDamageElectricTools == 5, "Journey: 5% tool wear");
            helper.assertTrue(ConfigHolder.INSTANCE.machines.energyUsageMultiplier == 50, "Journey: half energy");
            WorldRules.swapActiveForTest(NORMAL);
            helper.assertTrue(!ConfigHolder.INSTANCE.machines.harmlessActiveTransformers, "Normal: AT hurts");
            helper.assertTrue(ConfigHolder.INSTANCE.tools.rngDamageElectricTools == 10, "Normal: 10% tool wear");
        } finally {
            WorldRules.swapActiveForTest(previous);
        }
        helper.succeed();
    }

    /** Resources: the GTNA Electric Void Miner gets shorter operations in Journey. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void resourceRulesReachTheVoidMiner(GameTestHelper helper) {
        if (!ModList.get().isLoaded("gtna")) {
            helper.succeed();
            return;
        }
        WorldRules.Snapshot previous = WorldRules.swapActiveForTest(NORMAL);
        try {
            int normal = com.raishxn.gtna.config.GTNABalance.getElectricVoidMiner().baseDuration;
            WorldRules.swapActiveForTest(JOURNEY);
            int journey = com.raishxn.gtna.config.GTNABalance.getElectricVoidMiner().baseDuration;
            helper.assertTrue(journey == Math.round(normal * 0.75), "void miner " + normal + " -> " + journey);
        } finally {
            WorldRules.swapActiveForTest(previous);
        }
        helper.succeed();
    }

    /** Survival: a new Journey world keeps inventories; restart rules are reported as such. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void survivalAndRestartRules(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        boolean before = server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        try {
            GTIARuleEffects.applyOnServerStart(server, JOURNEY, true);
            helper.assertTrue(server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY), "Journey keeps items");
            GTIARuleEffects.applyOnServerStart(server, NORMAL, true);
            helper.assertTrue(!server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY), "Normal drops items");
        } finally {
            server.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(before, server);
            GTIARuleEffects.applyOnServerStart(server, WorldRules.active(), false);
        }
        var journey = GTIARuleEffects.restartValues(JOURNEY);
        helper.assertTrue(journey.get(GTIARules.MAINTENANCE.id()) == 0.0, "Journey turns maintenance off");
        helper.assertTrue(!journey.equals(GTIARuleEffects.restartValues(NORMAL)), "presets differ on restart rules");
        helper.assertTrue(!GTIARules.RECIPE_DURATION.restart(), "recipe rules never need a restart");
        helper.succeed();
    }

    private static long eu(Map<RecipeCapability<?>, List<Content>> tickInputs) {
        return ((EnergyStack) tickInputs.get(EURecipeCapability.CAP).get(0).content).voltage();
    }
}
