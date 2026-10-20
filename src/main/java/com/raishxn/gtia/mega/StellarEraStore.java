package com.raishxn.gtia.mega;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import com.raishxn.gtia.mega.engine.EnginePhysics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Stellar era (GTIA-D-312, D-315): how far each owner's Stellar Engine has moved the Nexus System, as the Δv it
 * gathered (km/s). The farthest region reached never goes back; other megastructures read its bonuses.
 */
public final class StellarEraStore extends SavedData {

    private static final String NAME = "gtia_stellar_era";
    private final Map<UUID, Double> dv = new HashMap<>();

    public static StellarEraStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StellarEraStore::load, StellarEraStore::new, NAME);
    }

    private static StellarEraStore load(CompoundTag tag) {
        var store = new StellarEraStore();
        for (String key : tag.getAllKeys()) store.dv.put(UUID.fromString(key), tag.getDouble(key));
        return store;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        dv.forEach((owner, v) -> tag.putDouble(owner.toString(), v));
        return tag;
    }

    public double dv(UUID owner) {
        return dv.getOrDefault(owner, 0.0);
    }

    public EnginePhysics.Region farthest(UUID owner) {
        return EnginePhysics.Region.at(dv(owner));
    }

    public void add(UUID owner, double kms) {
        if (kms <= 0) return;
        dv.merge(owner, kms, Double::sum);
        setDirty();
    }

    /** Region bonuses of an owner, or none without one (or without a server). */
    public static EnginePhysics.Region farthest(MinecraftServer server, UUID owner) {
        return server == null || owner == null ? EnginePhysics.Region.LOCAL_CLOUD : get(server).farthest(owner);
    }
}
