package com.raishxn.gtia.mega.system;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Star systems as dimensions (GTIA-D-316): the Nexus System and {@link #SLOTS} systems the Interstellar System Creator
 * can make, each with its own star. Megastructures that lift, burn, suppress or take apart a star change the star of
 * the system they stand in; this keeps those stars with the world.
 */
public final class StarSystems extends SavedData {

    /** Systems the Interstellar System Creator can fill. */
    public static final int SLOTS = 6;
    public static final ResourceKey<Level> NEXUS = key("nexus_system");

    private static final String NAME = "gtia_star_systems";

    /** The star of one system. */
    public static final class Star {

        public Kind kind;
        /** Mass, M☉ (for a black hole, its mass). */
        public double mass;
        /** Core hydrogen left, 0..1 (main sequence only). */
        public double fuel;
        @Nullable
        public UUID owner;
        /** Picks the system's ore mix and the look of its star. */
        public long seed;
        /** Times the system was made (a disintegrated slot can be made again). */
        public int generation;
        /** Metal content, × the Sun's: richer systems give more when mined or taken apart. */
        public double metallicity = 1;
        /** Spin of a black hole, a = Jc / GM² (0 to 0.998). */
        public double spin = 0.6;

        Star(Kind kind, double mass, double fuel, @Nullable UUID owner, long seed) {
            this.kind = kind;
            this.mass = mass;
            this.fuel = fuel;
            this.owner = owner;
            this.seed = seed;
        }

        public double luminosity() {
            return StarPhysics.luminosity(kind, mass);
        }

        public double temperature() {
            return StarPhysics.temperature(kind, mass);
        }

        public float drawnScale() {
            return StarPhysics.drawnScale(kind, mass);
        }

        CompoundTag save() {
            var t = new CompoundTag();
            t.putString("kind", kind.name());
            t.putDouble("mass", mass);
            t.putDouble("fuel", fuel);
            if (owner != null) t.putUUID("owner", owner);
            t.putLong("seed", seed);
            t.putInt("generation", generation);
            t.putDouble("z", metallicity);
            t.putDouble("spin", spin);
            return t;
        }

        static Star load(CompoundTag t) {
            var s = new Star(Kind.valueOf(t.getString("kind")), t.getDouble("mass"), t.getDouble("fuel"),
                    t.hasUUID("owner") ? t.getUUID("owner") : null, t.getLong("seed"));
            s.generation = t.getInt("generation");
            s.metallicity = t.contains("z") ? t.getDouble("z") : 1;
            s.spin = t.contains("spin") ? t.getDouble("spin") : 0.6;
            return s;
        }
    }

    private final Map<ResourceLocation, Star> stars = new LinkedHashMap<>();

    public static ResourceKey<Level> key(String path) {
        return ResourceKey.create(Registries.DIMENSION, GTIAMod.id(path));
    }

    public static ResourceKey<Level> slot(int index) {
        return key("stellar_system_" + (index + 1));
    }

    /** Index of a created-system dimension, or -1. */
    public static int slotOf(ResourceKey<Level> dimension) {
        for (int i = 0; i < SLOTS; i++) if (slot(i) == dimension) return i;
        return -1;
    }

    public static boolean isSystem(ResourceKey<Level> dimension) {
        return dimension == NEXUS || slotOf(dimension) >= 0;
    }

    public static StarSystems get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StarSystems::load, StarSystems::new, NAME);
    }

    private static StarSystems load(CompoundTag tag) {
        var out = new StarSystems();
        for (String k : tag.getAllKeys()) out.stars.put(new ResourceLocation(k), Star.load(tag.getCompound(k)));
        return out;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        stars.forEach((k, s) -> tag.put(k.toString(), s.save()));
        return tag;
    }

    /**
     * The star of a system: the Nexus System starts with a Sun-like star halfway through its life; an empty slot has
     * none until the Interstellar System Creator makes one.
     */
    @Nullable
    public Star star(ResourceKey<Level> dimension) {
        if (!isSystem(dimension)) return null;
        var s = stars.get(dimension.location());
        if (s == null && dimension == NEXUS) {
            s = new Star(Kind.YELLOW_DWARF, 1.0, 0.5, null, 0);
            stars.put(dimension.location(), s);
            setDirty();
        }
        return s;
    }

    /** Puts a new star in an empty (or emptied) slot. */
    public boolean create(int slot, Kind kind, UUID owner, long seed) {
        return create(slot, kind, owner, seed, 1);
    }

    /** Puts a new star of metallicity {@code z} in an empty (or emptied) slot. */
    public boolean create(int slot, Kind kind, UUID owner, long seed, double z) {
        var key = slot(slot).location();
        var old = stars.get(key);
        if (old != null && old.kind != Kind.NONE) return false;
        var s = new Star(kind, kind.mass, kind.mainSequence() ? 1.0 : 0, owner, seed);
        s.generation = old == null ? 1 : old.generation + 1;
        s.metallicity = z;
        stars.put(key, s);
        setDirty();
        return true;
    }

    /** First slot without a star, or -1. */
    public int freeSlot() {
        for (int i = 0; i < SLOTS; i++) {
            var s = stars.get(slot(i).location());
            if (s == null || s.kind == Kind.NONE) return i;
        }
        return -1;
    }

    /** Systems an owner has made: the level of the Dimensional era (D-312). */
    public int madeBy(UUID owner) {
        int n = 0;
        for (int i = 0; i < SLOTS; i++) {
            var s = stars.get(slot(i).location());
            if (s != null && owner.equals(s.owner) && s.kind != Kind.NONE) n++;
        }
        return n;
    }

    /** Empties a slot (the Disintegrator; GameTests clean up with it). */
    public void clear(int slot) {
        var s = stars.get(slot(slot).location());
        if (s != null) {
            s.kind = Kind.NONE;
            s.owner = null;
            setDirty();
        }
    }

    public void changed() {
        setDirty();
    }

    /** The star of the system a level is in, or null outside the star systems (or without a server). */
    @Nullable
    public static Star of(Level level) {
        return level.getServer() == null ? null : get(level.getServer()).star(level.dimension());
    }
}
