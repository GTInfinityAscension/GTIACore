package com.raishxn.gtia.mega.matrioshka;

/**
 * Computation of the Matrioshka Brain's shells (GTIA-D-311, D-313).
 *
 * <p>
 * Each built phase is one shell around the Nexus star. A shell sits in the star's light at its radius and runs at
 * the temperature that light gives it. By Landauer's limit (erasing a bit costs {@code k T ln 2}) a colder shell
 * does more computation with the same energy. The player trades three things:
 * <ul>
 * <li>clock: more CWU, but every shell has to radiate more and runs hotter ({@code T ~ clock^(1/4)});</li>
 * <li>coolant: liquid helium carries heat away, up to 40 % colder at full supply;</li>
 * <li>the limit: a shell above {@link #T_LIMIT} overheats and computes nothing until it cools.</li>
 * </ul>
 */
public final class MatrioshkaShells {

    public static final int PHASES = 5;
    /** Blocks per astronomical unit: the outer ring (109 blocks) sits about 1 AU from the star. */
    public static final double BLOCKS_PER_AU = 100;
    /** Equilibrium temperature at 1 AU of a Sun-like star, {@code (L / 16 pi sigma r^2)^(1/4)}. */
    public static final double T_1AU = 278.6;
    /** Reference temperature of the Landauer factor. */
    public static final double T_REF = 300;
    /** A shell hotter than this overheats. */
    public static final double T_LIMIT = 500;
    /** Coldest the coolant can make a shell, as a fraction of its uncooled temperature. */
    public static final double COOLING = 0.4;
    /** Clock range and step, in percent. */
    public static final int CLOCK_MIN = 50, CLOCK_MAX = 200, CLOCK_STEP = 25;
    /** Liquid helium per second per built phase at 100 % clock, in mB. */
    public static final long COOLANT_PER_PHASE = 250;
    /** CWU-ticks per Ψ (thought cycle). */
    public static final long CWU_PER_PSI = 1L << 20;

    private MatrioshkaShells() {}

    /** Shell temperature in kelvin: radius in blocks, clock in percent, coolant supply 0..1. */
    // EN: T = T_1AU / sqrt(r / 1 AU) * clock^(1/4) * (1 - 0.4 * coolant); a shell radiating more power must be hotter
    // (Stefan-Boltzmann), helium takes part of that heat away.
    // PT: T = T_1UA / sqrt(r / 1 UA) * clock^(1/4) * (1 - 0,4 * refrigeração); uma casca que irradia mais potência
    // precisa estar mais quente (Stefan-Boltzmann), o hélio leva parte desse calor.
    // ZH: T = T_1AU / sqrt(r / 1 AU) * clock^(1/4) * (1 - 0.4 * 冷却)；辐射功率越大的外壳温度越高（斯特藩-玻尔兹曼），液氦带走部分热量。
    public static double temperature(double radiusBlocks, int clockPct, double coolant) {
        return T_1AU / Math.sqrt(radiusBlocks / BLOCKS_PER_AU) * Math.pow(clockPct / 100.0, 0.25) *
                (1 - COOLING * Math.max(0, Math.min(1, coolant)));
    }

    public static double temperature(double radiusBlocks) {
        return temperature(radiusBlocks, 100, 0);
    }

    public static boolean overheated(double temperature) {
        return temperature > T_LIMIT;
    }

    /** CWU/t of shell {@code phase} (1..5): 2^(21+phase) x clock x 300 K / T; zero when it overheats. */
    // EN: Each shell is a power of two above the one before (2^22 to 2^26 CWU/t at 300 K, GTIA-D-311); the Landauer
    // factor rewards the cold outer shells and costs the hot inner ones.
    // PT: Cada casca rende uma potência de 2 acima da anterior (2^22 a 2^26 CWU/t a 300 K, GTIA-D-311); o fator de
    // Landauer premia as cascas frias de fora e cobra das quentes de dentro.
    // ZH: 每层外壳比上一层高一个 2 的幂（300 K 时 2^22 至 2^26 CWU/t，GTIA-D-311）；兰道尔因子奖励外层冷壳、惩罚内层热壳。
    public static long shellCwut(int phase, double radiusBlocks, int clockPct, double coolant) {
        double t = temperature(radiusBlocks, clockPct, coolant);
        if (overheated(t)) return 0;
        return Math.round((1L << (21 + phase)) * (clockPct / 100.0) * (T_REF / t));
    }

    public static long shellCwut(int phase, double radiusBlocks) {
        return shellCwut(phase, radiusBlocks, 100, 0);
    }

    /** CWU/t of the first {@code phases} shells; at most {@link Integer#MAX_VALUE} (GTCEu's CWU is an int). */
    public static int totalCwut(int phases, float[] radius, int clockPct, double coolant) {
        long sum = 0;
        for (int p = 1; p <= Math.min(phases, PHASES); p++) sum += shellCwut(p, radius[p - 1], clockPct, coolant);
        return (int) Math.min(Integer.MAX_VALUE, sum);
    }

    public static int totalCwut(int phases, float[] radius) {
        return totalCwut(phases, radius, 100, 0);
    }

    /** Liquid helium the shells ask for per second, in mB. */
    public static long coolantDemand(int phases, int clockPct) {
        return COOLANT_PER_PHASE * phases * clockPct / 100;
    }
}
