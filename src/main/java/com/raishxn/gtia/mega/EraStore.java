package com.raishxn.gtia.mega;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resources of the later eras and of the megastructures that keep a balance per owner (GTIA-D-312): causal debt,
 * negentropy, contained mass, research... One number per (resource, owner), kept with the world. Ψ and the Stellar
 * era's Δv keep their own stores.
 */
public final class EraStore extends SavedData {

    public static final String CAUSAL_DEBT = "causal_debt", NEGENTROPY = "negentropy", FINAL_DONE = "final_done";

    private static final String NAME = "gtia_era_store";
    private final Map<String, Map<UUID, Double>> values = new HashMap<>();

    public static EraStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(EraStore::load, EraStore::new, NAME);
    }

    private static EraStore load(CompoundTag tag) {
        var store = new EraStore();
        for (String res : tag.getAllKeys()) {
            var inner = tag.getCompound(res);
            var map = new HashMap<UUID, Double>();
            for (String k : inner.getAllKeys()) map.put(UUID.fromString(k), inner.getDouble(k));
            store.values.put(res, map);
        }
        return store;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        values.forEach((res, map) -> {
            var inner = new CompoundTag();
            map.forEach((k, v) -> inner.putDouble(k.toString(), v));
            tag.put(res, inner);
        });
        return tag;
    }

    public double get(String resource, UUID owner) {
        var map = values.get(resource);
        return map == null ? 0 : map.getOrDefault(owner, 0.0);
    }

    public void set(String resource, UUID owner, double value) {
        values.computeIfAbsent(resource, k -> new HashMap<>()).put(owner, value);
        setDirty();
    }

    public double add(String resource, UUID owner, double amount) {
        double v = get(resource, owner) + amount;
        set(resource, owner, v);
        return v;
    }
}
