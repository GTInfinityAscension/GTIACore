package com.raishxn.gtia.mega.qso;

/**
 * Physics of the Quasi-Stellar Obliterator (GTIA-D-332), the Final era (GTIA-D-312): a quasar's light gathered in a
 * capacitor and fired.
 *
 * <ul>
 * <li>The quasar shines at its Eddington limit, 2^40 EU/t per solar mass of the hole; the Nexus network can add more.
 * The capacitor leaks 1 % of its charge a second, so it saturates at (input / 0.01): bigger shots need a heavier
 * hole.</li>
 * <li>Each shot pays a fixed aiming cost; what is left is ordered energy fired against the 2.7 K sky, harvested as
 * negentropy. The world holds a finite stock of it (a world rule).</li>
 * </ul>
 */
public final class QsoPhysics {

    public static final double EUT_PER_SOLAR_MASS = Math.pow(2, 40), DRAW_STEP = Math.pow(2, 42), LEAK = 0.01,
            AIM = Math.pow(2, 50), EU_PER_UNIT = Math.pow(2, 48);
    public static final int DRAW_MAX = 16, SIZE_MAX = 7;

    private QsoPhysics() {}

    /** Charge gathered in one second from the quasar and the network. */
    public static double input(double holeMass, int draw) {
        return EUT_PER_SOLAR_MASS * Math.max(0, holeMass) * 20 + DRAW_STEP * Math.max(0, Math.min(DRAW_MAX, draw));
    }

    public static double charge(double charge, double input) {
        return charge * (1 - LEAK) + input;
    }

    /** The most the capacitor can hold at this input. */
    public static double ceiling(double input) {
        return input / LEAK;
    }

    /** Energy of a shot of size k: the aiming cost times 2^(k+1). */
    public static double shot(int k) {
        return AIM * Math.pow(2, Math.max(0, Math.min(SIZE_MAX, k)) + 1);
    }

    /** Largest size the phases allow: two plus the phases. */
    public static int maxSize(int phases) {
        return Math.min(SIZE_MAX, 2 + Math.max(1, phases));
    }

    /** Negentropy units a shot gives (the aiming cost is lost). */
    public static double yield(int k) {
        return (shot(k) - AIM) / EU_PER_UNIT;
    }
}
