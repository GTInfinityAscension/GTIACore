package com.raishxn.gtia.mega;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ψ (thought cycles), the resource of the Ψ era (GTIA-D-312): one balance per owner, kept with the world like the
 * Nexus network's EU.
 */
public final class PsiStore extends SavedData {

    private static final String NAME = "gtia_psi";
    private final Map<UUID, Long> balance = new HashMap<>();

    public static PsiStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PsiStore::load, PsiStore::new, NAME);
    }

    private static PsiStore load(CompoundTag tag) {
        var store = new PsiStore();
        for (String key : tag.getAllKeys()) store.balance.put(UUID.fromString(key), tag.getLong(key));
        return store;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        balance.forEach((owner, psi) -> tag.putLong(owner.toString(), psi));
        return tag;
    }

    public long balance(UUID owner) {
        return balance.getOrDefault(owner, 0L);
    }

    public void add(UUID owner, long psi) {
        if (psi <= 0) return;
        long now = balance(owner);
        balance.put(owner, now > Long.MAX_VALUE - psi ? Long.MAX_VALUE : now + psi);
        setDirty();
    }

    /** Takes {@code psi} if the owner has it. */
    public boolean spend(UUID owner, long psi) {
        long now = balance(owner);
        if (psi < 0 || now < psi) return false;
        balance.put(owner, now - psi);
        setDirty();
        return true;
    }
}
