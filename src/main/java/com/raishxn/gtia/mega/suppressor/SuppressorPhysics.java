package com.raishxn.gtia.mega.suppressor;

/**
 * Physics of the Fusion Suppressor (GTIA-D-320).
 *
 * <ul>
 * <li>Suppressing fusion lowers the neutrino flux and the light the core makes; the core loses pressure support and
 * contracts (virial theorem: half the gravitational energy heats it). Watching the neutrinos gives research.</li>
 * <li>Above half suppression the core's pressure deficit grows; when it is full a star of at least
 * {@link #COLLAPSE_MASS} M☉ (the limit for core collapse) is ready to end: as a supernova, which spreads r-process
 * elements, or collapsing straight into a black hole. A lighter star only flares (a helium flash) and the pylons take
 * the blow.</li>
 * </ul>
 */
public final class SuppressorPhysics {

    public static final int STEP = 10, MAX = 100, DEFICIT_FULL = 1000, INTEGRITY = 1000, FLASH_WEAR = 250,
            REPAIR = 50, EVENT_SECONDS = 60;
    public static final double COLLAPSE_MASS = 8;
    /** EU/t of the suppression field at full suppression for the Sun's light. */
    public static final double EUT_FULL = Math.pow(2, 36);

    /** r-process products of a supernova, dust per M☉ and per unit of metallicity. */
    public static final int DUST_PER_SOLAR_MASS = 64;

    private SuppressorPhysics() {}

    /** Research points per second: what the detectors read rises with suppression and the square root of L. */
    public static long research(int suppressionPct, double light, int phases) {
        if (phases < 2) return 0;
        return Math.round(4 * suppressionPct / 100.0 * Math.sqrt(Math.max(0, light)));
    }

    /** Change of the core's pressure deficit per second (per mille). */
    public static int deficit(int suppressionPct) {
        return suppressionPct > 50 ? (suppressionPct - 50) / 2 : -5;
    }

    public static double eut(int suppressionPct, double light) {
        return EUT_FULL * suppressionPct / 100.0 * Math.sqrt(Math.max(0, light));
    }

    /** The surface cools as fusion is damped: T × (1 - 0.3 S). */
    public static double dampedTemperature(double kelvin, int suppressionPct) {
        return kelvin * (1 - 0.3 * suppressionPct / 100.0);
    }

    /** Black hole left by a direct collapse: most of the star falls in. */
    public static double holeMass(double starMass) {
        return 0.6 * starMass;
    }

    /** Dust of each r-process element a supernova gives: full with the corolla, a tenth without. */
    public static long supernovaDust(double starMass, double z, boolean corolla) {
        return Math.round(DUST_PER_SOLAR_MASS * starMass * z * (corolla ? 1 : 0.1));
    }
}
