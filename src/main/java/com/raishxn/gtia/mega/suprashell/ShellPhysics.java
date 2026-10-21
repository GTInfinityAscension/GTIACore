package com.raishxn.gtia.mega.suprashell;

/**
 * Physics of the Black Hole Suprashell (GTIA-D-329): a shell world held static at radius r round a black hole, r in
 * Schwarzschild radii.
 *
 * <ul>
 * <li>Clocks on the shell run slow by the time dilation tau = sqrt(1 - rs/r): seen from outside, its factories work
 * at tau of their speed.</li>
 * <li>Matter lowered from far away to the shell gives up 1 - tau of its rest energy (Geroch's lowering): the closer
 * the shell, the less EU each recipe costs, tau of the base.</li>
 * <li>The shell's area grows as r^2, and with it the room for modules.</li>
 * </ul>
 */
public final class ShellPhysics {

    public static final double R_MIN = 1.5, R_MAX = 20, R_STEP = 0.5;
    public static final int PARALLEL_PER_MODULE = 64, MODULES_PER_PHASE = 64;

    private ShellPhysics() {}

    public static double tau(double r) {
        return Math.sqrt(Math.max(0, 1 - 1 / Math.max(1.0001, r)));
    }

    /** Modules the shell has room for: its area in units of rs^2, up to 64 a phase. */
    public static int capacity(double r, int phases) {
        return (int) Math.min(Math.floor(r * r), MODULES_PER_PHASE * Math.max(1, Math.min(5, phases)));
    }

    public static int parallel(int modules) {
        return PARALLEL_PER_MODULE * Math.max(0, modules);
    }

    /** Energy freed by lowering matter from infinity to the shell, share of its rest energy. */
    public static double loweringShare(double r) {
        return 1 - tau(r);
    }
}
