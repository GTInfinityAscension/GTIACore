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
 * <li>coolant: three tiers ({@link Coolant}) pull the shells towards their own temperature;</li>
 * <li>the limit: a shell above the limit overheats, computes nothing and wears; at zero integrity it fractures until
 * repaired with its own blocks.</li>
 * </ul>
 * Temperatures move towards their target with a 10 s time constant, so a short clock peak can be held.
 */
public final class MatrioshkaShells {

    public static final int PHASES = 5;
    /** Blocks per astronomical unit: the outer ring (109 blocks) sits about 1 AU from the star. */
    public static final double BLOCKS_PER_AU = 100;
    /** Equilibrium temperature at 1 AU of a Sun-like star, {@code (L / 16 pi sigma r^2)^(1/4)}. */
    public static final double T_1AU = 278.6;
    /** Reference temperature of the Landauer factor. */
    public static final double T_REF = 300;
    /** A shell hotter than this overheats; the main rings of phase 4 radiate {@link #T_LIMIT_PHASE4} more. */
    public static final double T_LIMIT = 500, T_LIMIT_PHASE4 = 40;
    /** Fraction of the gap to the target temperature closed each second (10 s time constant). */
    public static final double INERTIA = 0.1;
    /** Integrity in per mille: lost per overheated second (halved by the outer field of phase 5), regained per block. */
    public static final int INTEGRITY = 1000, WEAR = 20, REPAIR = 50;

    /** Coolants, best last: temperature they pull towards, share of the heat they take, mB/s per phase at 100 %. */
    public enum Coolant {

        NONE(0, 0, 0),
        LIQUID_HELIUM(4, 0.40, 250),
        SUPERFLUID_HELIUM(2, 0.60, 100),
        SHIFTED_SUPERFLUID(0.1, 0.85, 10);

        public final double kelvin, eta;
        public final long perPhase;

        Coolant(double kelvin, double eta, long perPhase) {
            this.kelvin = kelvin;
            this.eta = eta;
            this.perPhase = perPhase;
        }
    }
    /** Clock range and step, in percent. */
    public static final int CLOCK_MIN = 50, CLOCK_MAX = 200, CLOCK_STEP = 25;
    /** CWU-ticks per Ψ (thought cycle). */
    public static final long CWU_PER_PSI = 1L << 20;

    private MatrioshkaShells() {}

    /** Uncooled temperature in kelvin of a shell at a radius in blocks and a clock in percent. */
    // EN: T = T_1AU / sqrt(r / 1 AU) * clock^(1/4): a shell radiating more power must be hotter (Stefan-Boltzmann).
    // PT: T = T_1UA / sqrt(r / 1 UA) * clock^(1/4): uma casca que irradia mais potência precisa estar mais quente
    // (Stefan-Boltzmann).
    // ZH: T = T_1AU / sqrt(r / 1 AU) * 频率^(1/4)：辐射功率越大的外壳温度越高（斯特藩-玻尔兹曼）。
    public static double temperature(double radiusBlocks, int clockPct) {
        return T_1AU / Math.sqrt(radiusBlocks / BLOCKS_PER_AU) * Math.pow(clockPct / 100.0, 0.25);
    }

    public static double temperature(double radiusBlocks) {
        return temperature(radiusBlocks, 100);
    }

    /** Temperature a shell settles at with a coolant supplied at {@code supply} (0..1). */
    // EN: The coolant pulls the shell towards its own temperature: T' = Tc + (T - Tc) * (1 - eta * supply).
    // PT: O refrigerante puxa a casca para a temperatura dele: T' = Tc + (T - Tc) * (1 - eta * fornecimento).
    // ZH: 冷却剂把外壳拉向自身温度：T' = Tc + (T - Tc) * (1 - eta * 供给比例)。
    public static double target(double radiusBlocks, int clockPct, Coolant coolant, double supply) {
        double t = temperature(radiusBlocks, clockPct);
        double f = Math.max(0, Math.min(1, supply));
        return coolant.kelvin + (t - coolant.kelvin) * (1 - coolant.eta * f);
    }

    /** One second of thermal inertia from {@code now} towards {@code target}. */
    public static double step(double now, double target) {
        return now <= 0 ? target : now + (target - now) * INERTIA;
    }

    public static double limit(int phases) {
        return T_LIMIT + (phases >= 4 ? T_LIMIT_PHASE4 : 0);
    }

    public static boolean overheated(double temperature, int phases) {
        return temperature > limit(phases);
    }

    /** CWU/t of shell {@code phase} (1..5) at temperature {@code t}: 2^(21+phase) x clock x 300 K / T. */
    // EN: Each shell is a power of two above the one before (2^22 to 2^26 CWU/t at 300 K, GTIA-D-311); the Landauer
    // factor rewards the cold outer shells and costs the hot inner ones.
    // PT: Cada casca rende uma potência de 2 acima da anterior (2^22 a 2^26 CWU/t a 300 K, GTIA-D-311); o fator de
    // Landauer premia as cascas frias de fora e cobra das quentes de dentro.
    // ZH: 每层外壳比上一层高一个 2 的幂（300 K 时 2^22 至 2^26 CWU/t，GTIA-D-311）；兰道尔因子奖励外层冷壳、惩罚内层热壳。
    public static long shellCwut(int phase, double t, int clockPct) {
        return Math.round((1L << (21 + phase)) * (clockPct / 100.0) * (T_REF / Math.max(1, t)));
    }

    /** CWU/t of an uncooled shell at 100 % clock, settled. */
    public static long shellCwut(int phase, double radiusBlocks) {
        return shellCwut(phase, temperature(radiusBlocks), 100);
    }

    /** CWU/t of the first {@code phases} settled shells, uncooled at 100 % clock, none overheated. */
    public static int totalCwut(int phases, float[] radius) {
        long sum = 0;
        for (int p = 1; p <= Math.min(phases, PHASES); p++) sum += shellCwut(p, radius[p - 1]);
        return (int) Math.min(Integer.MAX_VALUE, sum);
    }

    /** Coolant the shells ask for per second, in mB. */
    public static long coolantDemand(Coolant coolant, int phases, int clockPct) {
        return coolant.perPhase * phases * clockPct / 100;
    }
}
