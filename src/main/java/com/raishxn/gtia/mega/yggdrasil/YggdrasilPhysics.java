package com.raishxn.gtia.mega.yggdrasil;

/**
 * Physics of the Yggdrasil Orchid (GTIA-D-327): gardens floating in a gas giant's atmosphere.
 *
 * <ul>
 * <li>Light falls off with depth (Beer-Lambert): I = I0 e^-z, z in scale heights, I0 the star's luminosity (L☉);
 * carbon dioxide rises with pressure (barometric): c = c0 e^z.</li>
 * <li>Both limit growth with Michaelis-Menten saturation: g = I / (I + K_I) x c / (c + K_C).</li>
 * <li>The best depth sets both terms equal: z* = 0.5 ln(I0 K_C / (c0 K_I)), deeper round a brighter star.</li>
 * </ul>
 */
public final class YggdrasilPhysics {

    public static final double K_I = 0.3, K_C = 1.0, C0 = 0.05, DEPTH_MAX = 8, DEPTH_STEP = 0.5, SPEED_PER_G = 10,
            SPEED_MAX = 6, SPEED_MIN = 0.05;
    public static final int PARALLEL_PER_PHASE = 256, BIOMASS_PER_CRAFT = 100;

    private YggdrasilPhysics() {}

    public static double light(double luminosity, double depth) {
        return Math.max(0, luminosity) * Math.exp(-depth);
    }

    public static double carbon(double depth) {
        return C0 * Math.exp(depth);
    }

    /** Growth rate, 0 to 1. */
    public static double growth(double luminosity, double depth) {
        double i = light(luminosity, depth), c = carbon(depth);
        return i / (i + K_I) * c / (c + K_C);
    }

    /** Depth of fastest growth, within the gardens' range. */
    public static double bestDepth(double luminosity) {
        if (luminosity <= 0) return 0;
        double z = 0.5 * Math.log(luminosity * K_C / (C0 * K_I));
        return Math.max(0, Math.min(DEPTH_MAX, z));
    }

    public static double speed(double growth) {
        return Math.max(SPEED_MIN, Math.min(SPEED_MAX, SPEED_PER_G * growth));
    }

    public static int parallel(int phases) {
        return PARALLEL_PER_PHASE * Math.max(1, Math.min(5, phases));
    }
}
