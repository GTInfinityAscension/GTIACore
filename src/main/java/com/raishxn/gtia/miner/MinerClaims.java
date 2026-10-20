package com.raishxn.gtia.miner;

import net.minecraft.core.GlobalPos;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Which Entangled Miners work each vein. The first {@link #MAX_PER_VEIN} miners to claim a vein mine it; the others
 * wait until a slot frees. A miner renews its claim every operation; a claim not renewed for {@link #EXPIRY_TICKS}
 * (machine broken, chunk unloaded, card taken) frees its slot. Kept in memory: claims rebuild as miners run.
 */
public final class MinerClaims {

    // EN: 4 per vein (D-310): to grow, find more veins instead of stacking miners on one.
    // PT: 4 por veio (D-310): para crescer, ache mais veios em vez de empilhar miners num só.
    // ZH: 每条矿脉 4 台（D-310）：想扩大产量就去寻找更多矿脉，而不是在一条矿脉上堆机器。
    public static final int MAX_PER_VEIN = 4;
    /** Longer than the slowest operation (LV, 160 ticks) with margin for a full output. */
    public static final long EXPIRY_TICKS = 600;

    private static final Map<String, LinkedHashMap<GlobalPos, Long>> CLAIMS = new HashMap<>();

    private MinerClaims() {}

    /** Renews {@code miner}'s claim on {@code vein}; true if it is among the first {@link #MAX_PER_VEIN}. */
    public static synchronized boolean claim(String vein, GlobalPos miner, long now) {
        var miners = CLAIMS.computeIfAbsent(vein, k -> new LinkedHashMap<>());
        miners.entrySet().removeIf(e -> !e.getKey().equals(miner) && now - e.getValue() > EXPIRY_TICKS);
        // A miner that was claiming a different vein gives that slot up.
        for (var entry : CLAIMS.entrySet()) if (!entry.getKey().equals(vein)) entry.getValue().remove(miner);
        miners.put(miner, now);
        int index = 0;
        for (GlobalPos pos : miners.keySet()) {
            if (pos.equals(miner)) return index < MAX_PER_VEIN;
            index++;
        }
        return false;
    }

    public static synchronized void release(GlobalPos miner) {
        for (var miners : CLAIMS.values()) miners.remove(miner);
    }

    public static synchronized int count(String vein) {
        var miners = CLAIMS.get(vein);
        return miners == null ? 0 : miners.size();
    }

    /** GameTests start from a clean slate. */
    public static synchronized void clear() {
        CLAIMS.clear();
    }
}
