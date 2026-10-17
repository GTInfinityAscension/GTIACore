package com.raishxn.gtia.rules;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.raishxn.gtia.GTIAMod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns a world's rules into GTCEu, GTNA and FTB Ultimine settings (GTIA-D-256). There are three moments:
 * <ul>
 * <li>{@link #applyRecipeRules}: before GTCEu generates the recipes of the world being loaded;</li>
 * <li>{@link #applyRuntimeRules}: settings that machines and tools read while running (also on the client, from the
 * synced rules);</li>
 * <li>{@link #applyStartupRules}: settings GTCEu reads while registering machines and items. They come from
 * {@code config/gtia/startup_rules.json}, which a world with different values rewrites, asking for a restart.</li>
 * </ul>
 */
public final class GTIARuleEffects {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path STARTUP_FILE = FMLPaths.CONFIGDIR.get().resolve("gtia").resolve("startup_rules.json");
    /** Values of the restart rules this game session started with. */
    private static final Map<String, Double> startupValues = new LinkedHashMap<>();
    private static int baseLargeBoilerSteam = -1;
    private static int baseVoidMinerDuration = -1;
    private static int baseVoidMinerRandomDuration = -1;
    private static float baseRubberTreeChance = -1;

    private GTIARuleEffects() {}

    // Recipes.

    public static void applyRecipeRules(WorldRules.Snapshot rules) {
        var recipes = ConfigHolder.INSTANCE.recipes;
        recipes.casingsPerCraft = (int) rules.get(GTIARules.CASINGS_PER_CRAFT);
        recipes.disableManualCompression = !on(rules, GTIARules.MANUAL_COMPRESSION);
        recipes.harderRods = on(rules, GTIARules.HARDER_RODS);
        recipes.harderBrickRecipes = on(rules, GTIARules.HARDER_BRICKS);
        recipes.nerfWoodCrafting = on(rules, GTIARules.NERF_WOOD);
        recipes.hardWoodRecipes = on(rules, GTIARules.HARD_WOOD);
        recipes.hardIronRecipes = on(rules, GTIARules.HARD_IRON);
        recipes.hardRedstoneRecipes = on(rules, GTIARules.HARD_REDSTONE);
        recipes.hardToolArmorRecipes = on(rules, GTIARules.HARD_TOOLS_ARMOR);
        recipes.hardMiscRecipes = on(rules, GTIARules.HARD_MISC);
        recipes.hardGlassRecipes = on(rules, GTIARules.HARD_GLASS);
        recipes.nerfPaperCrafting = on(rules, GTIARules.NERF_PAPER);
        recipes.hardAdvancedIronRecipes = on(rules, GTIARules.HARD_ADVANCED_IRON);
        recipes.hardDyeRecipes = on(rules, GTIARules.HARD_DYES);
        recipes.harderCharcoalRecipe = on(rules, GTIARules.HARDER_CHARCOAL);
        recipes.flintAndSteelRequireSteel = on(rules, GTIARules.FLINT_AND_STEEL);
        recipes.removeVanillaBlockRecipes = on(rules, GTIARules.REMOVE_VANILLA_BLOCKS);
        recipes.removeVanillaTNTRecipe = on(rules, GTIARules.REMOVE_VANILLA_TNT);
        recipes.harderCircuitRecipes = on(rules, GTIARules.HARDER_CIRCUITS);
        recipes.hardMultiRecipes = on(rules, GTIARules.HARD_CONTROLLERS);
    }

    // Machines and tools, read while they run.

    public static void applyRuntimeRules(WorldRules.Snapshot rules) {
        var machines = ConfigHolder.INSTANCE.machines;
        if (baseLargeBoilerSteam < 0) baseLargeBoilerSteam = machines.largeBoilers.steamPerWater;
        machines.largeBoilers.steamPerWater = scaled(baseLargeBoilerSteam, rules.get(GTIARules.LARGE_BOILER_STEAM));
        machines.enableCleanroom = on(rules, GTIARules.CLEANROOM);
        machines.cleanMultiblocks = on(rules, GTIARules.CLEAN_MULTIBLOCKS);
        machines.doesExplosionDamagesTerrain = on(rules, GTIARules.EXPLOSIONS_DAMAGE_TERRAIN);
        machines.shouldWeatherOrTerrainExplosion = on(rules, GTIARules.WEATHER_EXPLOSIONS);
        machines.harmlessActiveTransformers = on(rules, GTIARules.HARMLESS_ACTIVE_TRANSFORMERS);
        machines.orderedAssemblyLineItems = on(rules, GTIARules.ORDERED_ASSEMBLY_ITEMS);
        machines.orderedAssemblyLineFluids = on(rules, GTIARules.ORDERED_ASSEMBLY_FLUIDS);
        machines.energyUsageMultiplier = scaled(100, rules.get(GTIARules.ELECTRIC_TOOL_ENERGY));
        machines.prospectorEnergyUseMultiplier = scaled(100, rules.get(GTIARules.PROSPECTOR_ENERGY));
        ConfigHolder.INSTANCE.tools.rngDamageElectricTools = (int) rules.get(GTIARules.ELECTRIC_TOOL_DAMAGE);
        ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize = (int) rules.get(GTIARules.ORE_VEIN_GRID);
        var worldgen = ConfigHolder.INSTANCE.worldgen;
        if (baseRubberTreeChance < 0) baseRubberTreeChance = worldgen.rubberTreeSpawnChance;
        worldgen.rubberTreeSpawnChance = (float) (baseRubberTreeChance * rules.get(GTIARules.RUBBER_TREES));
        worldgen.oreVeins.infiniteBedrockOresFluids = on(rules, GTIARules.INFINITE_BEDROCK_FLUIDS);
        ConfigHolder.INSTANCE.gameplay.hazardsEnabled = on(rules, GTIARules.MATERIAL_HAZARDS);
        if (ModList.get().isLoaded("gtna")) applyGtnaRules(rules);
    }

    private static void applyGtnaRules(WorldRules.Snapshot rules) {
        var miner = com.raishxn.gtna.config.GTNABalance.getElectricVoidMiner();
        if (baseVoidMinerDuration < 0) {
            baseVoidMinerDuration = miner.baseDuration;
            baseVoidMinerRandomDuration = miner.randomDuration;
        }
        double factor = rules.get(GTIARules.VOID_MINER_DURATION);
        miner.baseDuration = Math.max(1, scaled(baseVoidMinerDuration, factor));
        miner.randomDuration = Math.max(1, scaled(baseVoidMinerRandomDuration, factor));
    }

    // World start.

    /** Settings stored in the world itself: game rules on a new world, FTB Ultimine on every start. */
    public static void applyOnServerStart(MinecraftServer server, WorldRules.Snapshot rules, boolean newWorld) {
        if (newWorld) {
            server.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(on(rules, GTIARules.KEEP_INVENTORY),
                    server);
        }
        if (ModList.get().isLoaded("ftbultimine")) {
            try {
                Object value = Class.forName("dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig")
                        .getField("MAX_BLOCKS").get(null);
                value.getClass().getMethod("set", Object.class).invoke(value,
                        (int) rules.get(GTIARules.ULTIMINE_LIMIT));
            } catch (ReflectiveOperationException e) {
                GTIAMod.LOGGER.warn("Could not apply the Ultimine limit rule", e);
            }
        }
    }

    // Startup rules.

    /** Called from the mod constructor, before GTCEu registers its machines and items. */
    public static void applyStartupRules() {
        // Mods are constructed in parallel; GTCEu's init is idempotent, so make sure its config exists.
        ConfigHolder.init();
        startupValues.clear();
        JsonObject json = null;
        if (Files.isRegularFile(STARTUP_FILE)) {
            try {
                json = GSON.fromJson(Files.readString(STARTUP_FILE, StandardCharsets.UTF_8), JsonObject.class);
            } catch (IOException | RuntimeException e) {
                GTIAMod.LOGGER.error("Could not read {}; using Normal", STARTUP_FILE, e);
            }
        }
        for (GTIARule rule : GTIARules.all().values()) {
            if (!rule.restart()) continue;
            double value = json != null && json.has(rule.id()) ? rule.clamp(json.get(rule.id()).getAsDouble()) :
                    rule.normal();
            startupValues.put(rule.id(), value);
        }
        var machines = ConfigHolder.INSTANCE.machines;
        double boilers = startupValues.get(GTIARules.BOILER_OUTPUT.id());
        var small = machines.smallBoilers;
        small.solidBoilerBaseOutput = scaled(small.solidBoilerBaseOutput, boilers);
        small.hpSolidBoilerBaseOutput = scaled(small.hpSolidBoilerBaseOutput, boilers);
        small.liquidBoilerBaseOutput = scaled(small.liquidBoilerBaseOutput, boilers);
        small.hpLiquidBoilerBaseOutput = scaled(small.hpLiquidBoilerBaseOutput, boilers);
        small.solarBoilerBaseOutput = scaled(small.solarBoilerBaseOutput, boilers);
        small.hpSolarBoilerBaseOutput = scaled(small.hpSolarBoilerBaseOutput, boilers);
        machines.minerSpeed = Math.max(1, (int) Math.round(machines.minerSpeed /
                startupValues.get(GTIARules.MINER_SPEED.id())));
        machines.enableMaintenance = startupValues.get(GTIARules.MAINTENANCE.id()) != 0;
        ConfigHolder.INSTANCE.recipes.enchantedTools = startupValues.get(GTIARules.ENCHANTED_TOOLS.id()) != 0;
        machines.requireGTToolsForBlocks = startupValues.get(GTIARules.REQUIRE_GT_TOOLS.id()) != 0;
        GTIAMod.LOGGER.info("GTIA startup rules: {}", startupValues);
    }

    /**
     * Compares a world's restart rules with the ones the game started with. When they differ, the world's values are
     * written for the next start and the caller tells the player to restart. Returns true when a restart is needed.
     */
    public static boolean checkStartupRules(WorldRules.Snapshot rules) {
        Map<String, Double> wanted = restartValues(rules);
        if (wanted.equals(startupValues)) return false;
        JsonObject json = new JsonObject();
        wanted.forEach(json::addProperty);
        try {
            Files.createDirectories(STARTUP_FILE.getParent());
            Files.writeString(STARTUP_FILE, GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (IOException e) {
            GTIAMod.LOGGER.error("Could not write {}", STARTUP_FILE, e);
        }
        GTIAMod.LOGGER.warn("This world's restart rules {} differ from this session's {}; restart the game",
                wanted, startupValues);
        return true;
    }

    public static Map<String, Double> restartValues(WorldRules.Snapshot rules) {
        Map<String, Double> values = new LinkedHashMap<>();
        for (GTIARule rule : GTIARules.all().values()) {
            if (rule.restart()) values.put(rule.id(), rules.get(rule));
        }
        return values;
    }

    private static boolean on(WorldRules.Snapshot rules, GTIARule rule) {
        return rules.get(rule) != 0;
    }

    private static int scaled(int base, double factor) {
        return (int) Math.round(base * factor);
    }
}
