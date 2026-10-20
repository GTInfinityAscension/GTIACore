package com.raishxn.gtia.mega.matrioshka;

/**
 * Computation of the Matrioshka Brain's shells (GTIA-D-311).
 *
 * <p>
 * Each built phase is one shell around the Nexus star. The shell sits in the star's light at its radius and runs at
 * the temperature that light gives it; by Landauer's limit (erasing a bit costs {@code k T ln 2}) a colder shell does
 * more computation with the same energy.
 */
public final class MatrioshkaShells {

    public static final int PHASES = 5;
    /** Blocks per astronomical unit: the outer relays (96 blocks) sit about 1 AU from the star. */
    public static final double BLOCKS_PER_AU = 100;
    /** Equilibrium temperature at 1 AU of a Sun-like star, {@code (L / 16 pi sigma r^2)^(1/4)}. */
    public static final double T_1AU = 278.6;
    /** Reference temperature of the Landauer factor. */
    public static final double T_REF = 300;
    /** CWU-ticks per Ψ (thought cycle). */
    public static final long CWU_PER_PSI = 1L << 20;

    private MatrioshkaShells() {}

    /** Shell temperature in kelvin at a radius in blocks. */
    public static double temperature(double radiusBlocks) {
        return T_1AU / Math.sqrt(radiusBlocks / BLOCKS_PER_AU);
    }

    /**
     * CWU/t of shell {@code phase} (1..5): 2^(21 + phase) at 300 K, times 300 K / T.
     */
    // EN: Each shell is a power of two above the one before (2^22 to 2^26 CWU/t, GTIA-D-311); the Landauer factor
    // then rewards the cold outer shells and costs the hot inner ones.
    // PT: Cada casca rende uma potência de 2 acima da anterior (2^22 a 2^26 CWU/t, GTIA-D-311); o fator de Landauer
    // premia as cascas frias de fora e cobra das quentes de dentro.
    // ZH: 每层外壳比上一层高一个 2 的幂（2^22 至 2^26 CWU/t，GTIA-D-311）；兰道尔因子奖励外层冷壳、惩罚内层热壳。
    public static long shellCwut(int phase, double radiusBlocks) {
        return Math.round((1L << (21 + phase)) * (T_REF / temperature(radiusBlocks)));
    }

    /** CWU/t of the first {@code phases} shells; at most {@link Integer#MAX_VALUE} (GTCEu's CWU is an int). */
    public static int totalCwut(int phases, float[] radius) {
        long sum = 0;
        for (int p = 1; p <= Math.min(phases, PHASES); p++) sum += shellCwut(p, radius[p - 1]);
        return (int) Math.min(Integer.MAX_VALUE, sum);
    }
}
