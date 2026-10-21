package com.raishxn.gtia.mega.birch;

/**
 * Physics of the Birch World (GTIA-D-331), the Causal era (GTIA-D-312): shells round a black hole whose rotating
 * interior allows closed timelike curves.
 *
 * <ul>
 * <li>Work sent round a time loop is done before it is paid for: recipes finish 64 times as fast and their EU becomes
 * causal debt instead of being drawn.</li>
 * <li>The debt grows by 0.05 % a second (the loop's tension) until it is paid back from the Nexus network.</li>
 * <li>Novikov's self-consistency: a debt past the credit the shells can hold is a paradox. History corrects itself:
 * the debt is gone, and so is everything in the output buses, and the loops close for a minute.</li>
 * </ul>
 */
public final class BirchPhysics {

    public static final int LAYERS_PER_PHASE = 40, MODULES_PER_LAYER = 2, PARALLEL_PER_MODULE = 256, REPAY_MAX = 16,
            PARADOX_SECONDS = 60;
    public static final double SPEED = 64, INTEREST = 0.0005, CREDIT_PER_LAYER_MASS = Math.pow(2, 44),
            REPAY_STEP = Math.pow(2, 40);

    private BirchPhysics() {}

    public static int layers(int phases) {
        return LAYERS_PER_PHASE * Math.max(1, Math.min(5, phases));
    }

    public static int capacity(int phases) {
        return MODULES_PER_LAYER * layers(phases);
    }

    public static int parallel(int modules) {
        return PARALLEL_PER_MODULE * Math.max(0, modules);
    }

    /** The debt the shells can hold: more layers and a heavier hole hold more. */
    public static double credit(int phases, double holeMass) {
        return CREDIT_PER_LAYER_MASS * layers(phases) * Math.max(1, holeMass);
    }

    public static double grow(double debt) {
        return debt * (1 + INTEREST);
    }

    /** EU paid back a second at repayment level {@code level}. */
    public static double repayment(int level) {
        return REPAY_STEP * Math.max(0, Math.min(REPAY_MAX, level));
    }

    /** Level of the Causal era by debt paid back: I, II, III at 2^50, 2^55, 2^60 EU. */
    public static int eraLevel(double paid) {
        return paid >= Math.pow(2, 60) ? 3 : paid >= Math.pow(2, 55) ? 2 : paid >= Math.pow(2, 50) ? 1 : 0;
    }
}
