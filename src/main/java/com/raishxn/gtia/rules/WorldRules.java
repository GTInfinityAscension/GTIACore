package com.raishxn.gtia.rules;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
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
 * The rules of the world being loaded. A world keeps its rules in {@code <world>/gtia_rules.json}, written when the
 * world is first started and never changed afterwards (GTIA-D-205).
 * <p>
 * Timing: the server data pack repository is created every time a world loads, and GTCEu generates its recipes when
 * that happens ({@code AddPackFindersEvent}). {@link #prepare(Path)} runs just before, from
 * {@code ServerPacksSourceMixin}, so recipes are always generated with the rules of the world that is loading.
 */
public final class WorldRules {

    public static final String FILE = "gtia_rules.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Rules the next new world will get (set by the create-world screen; the config default otherwise). */
    private static volatile Snapshot pending;
    private static volatile Snapshot active = Snapshot.of(RulePreset.NORMAL);
    /** True when the active rules were read from an existing world file. */
    private static volatile boolean locked;

    private WorldRules() {}

    public record Snapshot(RulePreset preset, Map<String, Double> overrides) {

        public static Snapshot of(RulePreset preset) {
            return new Snapshot(preset, Map.of());
        }

        public double get(GTIARule rule) {
            Double custom = preset == RulePreset.CUSTOM ? overrides.get(rule.id()) : null;
            return custom != null ? rule.clamp(custom) : rule.valueFor(preset);
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("version", 1);
            json.addProperty("preset", preset.id());
            JsonObject values = new JsonObject();
            GTIARules.all().values().forEach(rule -> values.addProperty(rule.id(), get(rule)));
            json.add("rules", values);
            return json;
        }

        static Snapshot fromJson(JsonObject json) {
            RulePreset preset = RulePreset.byId(json.has("preset") ? json.get("preset").getAsString() : "normal");
            Map<String, Double> overrides = new LinkedHashMap<>();
            if (preset == RulePreset.CUSTOM && json.has("rules")) {
                JsonObject values = json.getAsJsonObject("rules");
                for (String id : GTIARules.all().keySet()) {
                    if (values.has(id)) overrides.put(id, values.get(id).getAsDouble());
                }
            }
            return new Snapshot(preset, Map.copyOf(overrides));
        }
    }

    public static double get(GTIARule rule) {
        return active.get(rule);
    }

    public static Snapshot active() {
        return active;
    }

    public static boolean isLocked() {
        return locked;
    }

    /** Called by the create-world screen when the player picks a preset or edits a Custom rule. */
    public static void setPending(Snapshot snapshot) {
        pending = snapshot;
    }

    /**
     * Called with the data pack folder of the world about to load ({@code <world>/datapacks}). For a world being
     * created the folder is temporary and has no rules file, so the pending choice is used.
     */
    public static void prepare(Path datapackDir) {
        Path file = datapackDir == null || datapackDir.getParent() == null ? null :
                datapackDir.getParent().resolve(FILE);
        Snapshot fromWorld = file == null ? null : read(file);
        if (fromWorld != null) {
            active = fromWorld;
            locked = true;
        } else {
            active = pending != null ? pending : defaultForNewWorlds();
            locked = false;
        }
        GTIAMod.LOGGER.info("GTIA rules for {}: preset {}{}", datapackDir, active.preset().id(),
                locked ? " (from world)" : " (new world)");
    }

    /** Writes the rules into a world that has none yet, and locks them. */
    public static void onServerStarted(MinecraftServer server) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve(FILE);
        if (Files.exists(file)) return;
        try {
            Files.writeString(file, GSON.toJson(active.toJson()), StandardCharsets.UTF_8);
            locked = true;
            pending = null;
        } catch (IOException e) {
            GTIAMod.LOGGER.error("Could not write GTIA rules to {}", file, e);
        }
    }

    /** {@code config/gtia/new_world_rules.json}: the preset a dedicated server (or any new world) starts with. */
    private static Snapshot defaultForNewWorlds() {
        Path file = FMLPaths.CONFIGDIR.get().resolve("gtia").resolve("new_world_rules.json");
        Snapshot configured = read(file);
        if (configured != null) return configured;
        Snapshot normal = Snapshot.of(RulePreset.NORMAL);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(normal.toJson()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            GTIAMod.LOGGER.warn("Could not write {}", file, e);
        }
        return normal;
    }

    private static Snapshot read(Path file) {
        if (!Files.isRegularFile(file)) return null;
        try {
            return Snapshot.fromJson(GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class));
        } catch (IOException | RuntimeException e) {
            GTIAMod.LOGGER.error("Could not read GTIA rules from {}; using Normal", file, e);
            return Snapshot.of(RulePreset.NORMAL);
        }
    }
}
