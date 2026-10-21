package com.raishxn.gtia.rules;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of every GTIA world rule. Values are read through {@link WorldRules#get(GTIARule)}. Normal keeps the values
 * of the pack's {@code config/gtceu.yaml}; Journey follows the Easy preset of GTO Rules where the two packs share the
 * mechanic (GTIA-D-256).
 */
public final class GTIARules {

    private static final Map<String, GTIARule> RULES = new LinkedHashMap<>();

    // Recipes: applied while GTCEu generates the recipes of the world being loaded.

    /** Multiplies the duration of every GTCEu-built recipe (GTCEu, GTNA, GTIACore), except fuels. */
    public static final GTIARule RECIPE_DURATION = register(
            GTIARule.multiplier("duration_multiplier", "recipes", 0.75, 1.0, 0.1, 10.0));
    /** Multiplies the EU/t of every GTCEu-built recipe; never raises a recipe to a higher voltage tier. */
    public static final GTIARule RECIPE_EU = register(
            GTIARule.multiplier("eu_multiplier", "recipes", 0.75, 1.0, 0.1, 10.0));
    public static final GTIARule CASINGS_PER_CRAFT = register(
            GTIARule.number("casings_per_craft", "recipes", 3, 2, 1, 3));
    public static final GTIARule MANUAL_COMPRESSION = register(
            GTIARule.toggle("manual_compression", "recipes", true, false));
    public static final GTIARule HARDER_RODS = register(GTIARule.toggle("harder_rods", "recipes", false, true));
    public static final GTIARule HARDER_BRICKS = register(GTIARule.toggle("harder_bricks", "recipes", false, false));
    public static final GTIARule NERF_WOOD = register(GTIARule.toggle("nerf_wood", "recipes", false, false));
    public static final GTIARule HARD_WOOD = register(GTIARule.toggle("hard_wood", "recipes", false, false));
    public static final GTIARule HARD_IRON = register(GTIARule.toggle("hard_iron", "recipes", false, true));
    public static final GTIARule HARD_REDSTONE = register(GTIARule.toggle("hard_redstone", "recipes", false, false));
    public static final GTIARule HARD_TOOLS_ARMOR = register(
            GTIARule.toggle("hard_tools_armor", "recipes", false, false));
    public static final GTIARule HARD_MISC = register(GTIARule.toggle("hard_misc", "recipes", false, false));
    public static final GTIARule HARD_GLASS = register(GTIARule.toggle("hard_glass", "recipes", false, true));
    public static final GTIARule NERF_PAPER = register(GTIARule.toggle("nerf_paper", "recipes", false, true));
    public static final GTIARule HARD_ADVANCED_IRON = register(
            GTIARule.toggle("hard_advanced_iron", "recipes", false, true));
    public static final GTIARule HARD_DYES = register(GTIARule.toggle("hard_dyes", "recipes", false, false));
    public static final GTIARule HARDER_CHARCOAL = register(GTIARule.toggle("harder_charcoal", "recipes", false, true));
    public static final GTIARule FLINT_AND_STEEL = register(
            GTIARule.toggle("flint_and_steel_steel", "recipes", false, true));
    public static final GTIARule REMOVE_VANILLA_BLOCKS = register(
            GTIARule.toggle("remove_vanilla_blocks", "recipes", false, false));
    public static final GTIARule REMOVE_VANILLA_TNT = register(
            GTIARule.toggle("remove_vanilla_tnt", "recipes", false, true));
    public static final GTIARule HARDER_CIRCUITS = register(
            GTIARule.toggle("harder_circuits", "recipes", false, false));
    public static final GTIARule HARD_CONTROLLERS = register(
            GTIARule.toggle("hard_controllers", "recipes", false, false));

    // Steam.

    /** Multiplies the parallel limit of every GTNA steam multiblock (through GTNASteamBalance). */
    public static final GTIARule STEAM_PARALLELS = register(
            GTIARule.multiplier("parallel_multiplier", "steam", 1.5, 1.0, 0.25, 8.0));
    /** Multiplies the recipe duration of every GTNA steam multiblock, on top of the recipe duration rule. */
    public static final GTIARule STEAM_DURATION = register(
            GTIARule.multiplier("duration_multiplier_steam", "steam", 0.75, 1.0, 0.1, 10.0));
    /** Steam made by the small GTCEu boilers (solid, liquid and solar, both pressures). */
    public static final GTIARule BOILER_OUTPUT = register(
            GTIARule.multiplier("boiler_output", "steam", 1.5, 1.0, 0.25, 4.0).needsRestart());
    /** Steam made by the large boilers per unit of water. */
    public static final GTIARule LARGE_BOILER_STEAM = register(
            GTIARule.multiplier("large_boiler_steam", "steam", 1.25, 1.0, 0.25, 4.0));
    /** GTNA wireless steam hatches can be crafted (GTIA-D-340: Journey only; Normal moves steam through pipes). */
    public static final GTIARule WIRELESS_STEAM = register(
            GTIARule.toggle("wireless_steam", "steam", true, false));

    // Power.

    /** How long one unit of fuel lasts in generators and boilers (duration of their fuel recipes). */
    public static final GTIARule FUEL_EFFICIENCY = register(
            GTIARule.multiplier("fuel_efficiency", "power", 1.15, 1.0, 0.5, 2.0));

    // Machines.

    public static final GTIARule MAINTENANCE = register(
            GTIARule.toggle("maintenance", "machines", false, true).needsRestart());
    public static final GTIARule CLEANROOM = register(GTIARule.toggle("cleanroom", "machines", true, true));
    public static final GTIARule CLEAN_MULTIBLOCKS = register(
            GTIARule.toggle("clean_multiblocks", "machines", true, false));
    /**
     * Final era (GTIA-D-312/D-332): the world's finite negentropy, in units, and whether the last shot ends the game.
     */
    public static final GTIARule NEGENTROPY_STOCK = register(
            GTIARule.number("negentropy_stock", "machines", 50_000, 100_000, 1_000, 10_000_000));
    public static final GTIARule FINAL_ENDING = register(GTIARule.toggle("final_ending", "machines", true, true));
    public static final GTIARule EXPLOSIONS_DAMAGE_TERRAIN = register(
            GTIARule.toggle("explosions_damage_terrain", "machines", false, true));
    public static final GTIARule WEATHER_EXPLOSIONS = register(
            GTIARule.toggle("weather_explosions", "machines", false, false));
    public static final GTIARule HARMLESS_ACTIVE_TRANSFORMERS = register(
            GTIARule.toggle("harmless_active_transformers", "machines", true, false));
    public static final GTIARule ORDERED_ASSEMBLY_ITEMS = register(
            GTIARule.toggle("ordered_assembly_items", "machines", false, true));
    public static final GTIARule ORDERED_ASSEMBLY_FLUIDS = register(
            GTIARule.toggle("ordered_assembly_fluids", "machines", false, true));
    /** Speed of the single-block GTCEu miners. */
    public static final GTIARule MINER_SPEED = register(
            GTIARule.multiplier("miner_speed", "machines", 1.5, 1.0, 0.25, 4.0).needsRestart());

    /** GregTech machines and pipes only drop with a GT tool (GTO: Require GT Tools). */
    public static final GTIARule REQUIRE_GT_TOOLS = register(
            GTIARule.toggle("require_gt_tools", "machines", false, true).needsRestart());

    // Tools.

    public static final GTIARule ELECTRIC_TOOL_ENERGY = register(
            GTIARule.multiplier("electric_tool_energy", "tools", 0.5, 1.0, 0.0, 10.0));
    public static final GTIARule ELECTRIC_TOOL_DAMAGE = register(
            GTIARule.number("electric_tool_damage_chance", "tools", 5, 10, 0, 100));
    public static final GTIARule PROSPECTOR_ENERGY = register(
            GTIARule.multiplier("prospector_energy", "tools", 0.5, 1.0, 0.0, 10.0));
    public static final GTIARule ENCHANTED_TOOLS = register(
            GTIARule.toggle("enchanted_tools", "tools", true, false).needsRestart());

    // Resources.

    /** Size in chunks of the grid with one ore vein per cell; smaller means more veins. Applies to new chunks. */
    public static final GTIARule ORE_VEIN_GRID = register(GTIARule.number("ore_vein_grid", "resources", 3, 3, 2, 8));
    /** Duration of the GTNA Electric Void Miner operations. */
    public static final GTIARule VOID_MINER_DURATION = register(
            GTIARule.multiplier("void_miner_duration", "resources", 0.75, 1.0, 0.1, 10.0));
    /** Multiplies the pack's rubber tree spawn chance in new chunks (GTO: Rubber Tree Spawn Chance). */
    public static final GTIARule RUBBER_TREES = register(
            GTIARule.multiplier("rubber_tree_chance", "resources", 1.5, 1.0, 0.0, 10.0));
    /** Bedrock fluid veins never run dry (GTO: Infinite Bedrock Resources). */
    public static final GTIARule INFINITE_BEDROCK_FLUIDS = register(
            GTIARule.toggle("infinite_bedrock_fluids", "resources", true, false));

    // Survival.

    public static final GTIARule ULTIMINE_LIMIT = register(
            GTIARule.number("ultimine_limit", "survival", 128, 64, 1, 1024));
    /** Set as the keepInventory game rule when the world is created; /gamerule can still change it later. */
    public static final GTIARule KEEP_INVENTORY = register(GTIARule.toggle("keep_inventory", "survival", true, false));

    /** Radioactive and toxic materials hurt players who carry them without protection (GTO: Material Hazards). */
    public static final GTIARule MATERIAL_HAZARDS = register(
            GTIARule.toggle("material_hazards", "survival", false, true));
    /** Food level from which players heal faster; 21 turns it off (GTO: Extra Regeneration Food Threshold). */
    public static final GTIARule EXTRA_REGEN_FOOD = register(
            GTIARule.number("extra_regen_food", "survival", 5, 15, 0, 21));
    /** Half hearts healed every 2.5 seconds above that food level (GTO: Extra Regeneration Strength). */
    public static final GTIARule EXTRA_REGEN_STRENGTH = register(
            GTIARule.number("extra_regen_strength", "survival", 3, 2, 0, 20));

    // Chemistry (GTIA-D-284): GTIA's own rules, not in GTO.

    /** Chance per run, in hundredths of a percent, that a chemistry catalyst wears out (0 = never). */
    public static final GTIARule CATALYST_WEAR = register(
            GTIARule.number("catalyst_wear", "chemistry", 0, 100, 0, 10000));
    /** Multiplies the steam the chemistry plants recover from their own heat. */
    public static final GTIARule HEAT_RECOVERY = register(
            GTIARule.multiplier("heat_recovery", "chemistry", 1.5, 1.0, 0.0, 4.0));

    // Organs (GTIA-D-279).

    /** Planets hurt players whose organ set tier is too low (GTO organ system); off on both presets (GTIA-D-338). */
    public static final GTIARule ORGAN_PLANET_REQUIREMENT = register(
            GTIARule.toggle("planet_requirement", "organs", false, false));
    /** Multiplies the parts of every organ recipe (GTO: ×1 on Easy, ×2 on Normal). */
    public static final GTIARule ORGAN_COST = register(GTIARule.number("organ_cost", "organs", 1, 2, 1, 16));

    private GTIARules() {}

    private static GTIARule register(GTIARule rule) {
        RULES.put(rule.id(), rule);
        return rule;
    }

    public static Map<String, GTIARule> all() {
        return Collections.unmodifiableMap(RULES);
    }
}
