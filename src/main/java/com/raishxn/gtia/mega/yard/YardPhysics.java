package com.raishxn.gtia.mega.yard;

/**
 * Physics of the Hyperstructural Assembly Yard (GTIA-D-326): a shipyard ring whose fabrication lasers run on the
 * light of its star.
 *
 * <ul>
 * <li>Each phase opens 18 slipways (90 at phase 5); each active slipway adds 16 parallels.</li>
 * <li>The lasers take their power from starlight: the build rate follows the star's luminosity on a log scale,
 * 1 + log10(L/L☉), from x0.5 round a dim star to x4 round a blue giant.</li>
 * <li>Rush shifts double the rate but waste power: EU x2.5.</li>
 * <li>Megastructure controllers are what the yard is built for: their recipes cost half the EU here.</li>
 * </ul>
 */
public final class YardPhysics {

    public static final int SLIPWAYS_PER_PHASE = 18, PARALLEL_PER_SLIPWAY = 16, STEP = 6;
    public static final double RUSH_SPEED = 2, RUSH_EU = 2.5, SPEED_MIN = 0.5, SPEED_MAX = 4;
    /** EU factor for recipes that make a megastructure controller: the yard is built for them. */
    public static final double MEGA_DISCOUNT = 0.5;

    private YardPhysics() {}

    public static int slipways(int phases) {
        return SLIPWAYS_PER_PHASE * Math.max(0, Math.min(5, phases));
    }

    public static int parallel(int active) {
        return PARALLEL_PER_SLIPWAY * Math.max(0, active);
    }

    /** Build-rate multiplier from the star's luminosity (L☉); a dark centre leaves the minimum. */
    public static double speed(double luminosity, boolean rush) {
        double s = luminosity <= 0 ? SPEED_MIN : 1 + Math.log10(luminosity);
        s = Math.max(SPEED_MIN, Math.min(SPEED_MAX, s));
        return rush ? s * RUSH_SPEED : s;
    }

    public static double euFactor(boolean rush) {
        return rush ? RUSH_EU : 1;
    }
}
