package com.raishxn.gtia.miner;

/** Purity of a bedrock vein (GTIA-D-310): the raw ores one Entangled Miner operation takes from it. */
public enum VeinPurity {

    IMPURE(1),
    NORMAL(2),
    PURE(4);

    public final int ores;

    VeinPurity(int ores) {
        this.ores = ores;
    }

    /**
     * Rolled from the world seed, the dimension and the vein origin, so every player sees the same purity and it never
     * changes. EN: 40% impure, 40% normal, 20% pure, Satisfactory's ratios. PT: 40% impuro, 40% normal, 20% puro, as
     * proporções do Satisfactory. ZH: 40% 不纯、40% 普通、20% 纯净，与 Satisfactory 的比例相同。
     */
    public static VeinPurity of(long seed, String dimension, int originX, int originZ) {
        long h = seed ^ 0x9E3779B97F4A7C15L ^ (long) dimension.hashCode() << 32 ^ (originX * 341873128712L) ^
                (originZ * 132897987541L);
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        int roll = (int) Math.floorMod(h, 100L);
        return roll < 40 ? IMPURE : roll < 80 ? NORMAL : PURE;
    }

    public String langKey() {
        return "gtia.vein.purity." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
