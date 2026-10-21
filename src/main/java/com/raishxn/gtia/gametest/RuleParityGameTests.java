package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.rules.GTIARuleEffects;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.GTIASurvivalRules;
import com.raishxn.gtia.rules.RulePreset;
import com.raishxn.gtia.rules.WorldRules;

/** The rules added for parity with GTO Rules (GTIA-D-280). */
@GameTestHolder(GTIAMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RuleParityGameTests {

    private static final WorldRules.Snapshot JOURNEY = WorldRules.Snapshot.of(RulePreset.JOURNEY);
    private static final WorldRules.Snapshot NORMAL = WorldRules.Snapshot.of(RulePreset.NORMAL);

    private RuleParityGameTests() {}

    /** Rubber trees, bedrock fluids and hazards follow the preset at runtime. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void worldRulesFollowPreset(GameTestHelper helper) {
        var worldgen = ConfigHolder.INSTANCE.worldgen;
        try {
            GTIARuleEffects.applyRuntimeRules(NORMAL);
            float normalRubber = worldgen.rubberTreeSpawnChance;
            helper.assertFalse(worldgen.oreVeins.infiniteBedrockOresFluids, "Normal: bedrock fluids run dry");
            helper.assertTrue(ConfigHolder.INSTANCE.gameplay.hazardsEnabled, "Normal: material hazards on");
            GTIARuleEffects.applyRuntimeRules(JOURNEY);
            helper.assertTrue(Math.abs(worldgen.rubberTreeSpawnChance - normalRubber * 1.5F) < 1e-4,
                    "Journey: 1.5x rubber trees, got " + worldgen.rubberTreeSpawnChance + " from " + normalRubber);
            helper.assertTrue(worldgen.oreVeins.infiniteBedrockOresFluids, "Journey: infinite bedrock fluids");
            helper.assertFalse(ConfigHolder.INSTANCE.gameplay.hazardsEnabled, "Journey: no material hazards");
        } finally {
            GTIARuleEffects.applyRuntimeRules(WorldRules.active());
        }
        helper.succeed();
    }

    /** Extra regeneration starts at food 5 on Journey and 15 on Normal. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void extraRegenerationFollowsFood(GameTestHelper helper) {
        var player = TestPlayers.survival(helper);
        WorldRules.Snapshot previous = WorldRules.swapActiveForTest(NORMAL);
        try {
            player.setHealth(10);
            player.getFoodData().setFoodLevel(10);
            helper.assertTrue(GTIASurvivalRules.regenerate(player) == 0, "Normal: food 10 is below 15");
            player.getFoodData().setFoodLevel(16);
            helper.assertTrue(GTIASurvivalRules.regenerate(player) == 2, "Normal heals 2");
            WorldRules.swapActiveForTest(JOURNEY);
            player.setHealth(10);
            player.getFoodData().setFoodLevel(6);
            helper.assertTrue(GTIASurvivalRules.regenerate(player) == 3, "Journey heals 3 from food 5");
        } finally {
            WorldRules.swapActiveForTest(previous);
        }
        helper.succeed();
    }

    /** The new rules have the GTO-like values on each preset. */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void parityRuleValues(GameTestHelper helper) {
        helper.assertTrue(GTIARules.all().size() == 57, "57 rules, got " + GTIARules.all().size());
        helper.assertTrue(GTIARules.REQUIRE_GT_TOOLS.restart(), "GT tools rule is read at startup");
        helper.assertTrue(GTIARules.ORGAN_PLANET_REQUIREMENT.valueFor(RulePreset.JOURNEY) == 0 &&
                GTIARules.ORGAN_PLANET_REQUIREMENT.valueFor(RulePreset.NORMAL) == 0,
                "organs never gate planets (D-338)");
        helper.assertTrue(GTIARules.WIRELESS_STEAM.valueFor(RulePreset.JOURNEY) == 1 &&
                GTIARules.WIRELESS_STEAM.valueFor(RulePreset.NORMAL) == 0, "wireless steam only on Journey (D-340)");
        helper.assertTrue(GTIARules.ORGAN_COST.valueFor(RulePreset.JOURNEY) == 1 &&
                GTIARules.ORGAN_COST.valueFor(RulePreset.NORMAL) == 2, "organ cost 1/2");
        helper.succeed();
    }

    /** GTIA's own chemistry rules: Journey never wears catalysts and recovers more heat (GTIA-D-284). */
    @GameTest(template = "qa_empty_5x5", batch = "rules", timeoutTicks = 20)
    public static void chemistryRuleValues(GameTestHelper helper) {
        helper.assertTrue(GTIARules.CATALYST_WEAR.valueFor(RulePreset.JOURNEY) == 0 &&
                GTIARules.CATALYST_WEAR.valueFor(RulePreset.NORMAL) == 100, "catalyst wear 0 / 1%");
        helper.assertTrue(GTIARules.HEAT_RECOVERY.valueFor(RulePreset.JOURNEY) == 1.5, "Journey recovers 1.5x heat");
        helper.succeed();
    }
}
