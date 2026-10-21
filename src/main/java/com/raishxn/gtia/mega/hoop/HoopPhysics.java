package com.raishxn.gtia.mega.hoop;

/**
 * Physics of the Stellar Hoop (GTIA-D-318): a hoop that turns through the corona and works its plasma.
 *
 * <ul>
 * <li>The corona is a million-kelvin plasma whose density falls with height h above the photosphere with a scale
 * height of about 0.07 R☉ (hydrostatic: n ∝ e^(-h/H)). The hoop pulls plasma from the height the player sets:
 * lower is denser (more parallel) and hotter on the shields.</li>
 * <li>Coronal temperature: T(h) = 10⁴ K + 2 × 10⁶ K (1 - e^(-h/4 %)): the thin transition region climbs to ~2 MK.
 * Recipes up to that heat run (fusion, Electric Blast Furnace, Plasma Forge).</li>
 * <li>Parallel P = P₀(phases) × e^(-(h - 2 %)/10 %); shield load Q = 100 × e^(-(h - 2 %)/5 %) × P / 256, cut by 40 %
 * with the radiators. Coolant removes the load: liquid helium 1 unit/mB, Helium II 3, shifted superfluid 10.</li>
 * </ul>
 */
public final class HoopPhysics {

    public static final int HEIGHT_MIN = 2, HEIGHT_MAX = 30, HEIGHT_STEP = 2;
    /** Parallel at the lowest dive, by phases built (index 0 = phase 1). */
    static final int[] PARALLEL = { 16, 256, 512, 512, 1024 };
    /** Ticks of work per second: the corona does the heating, so recipes run twice as fast. */
    public static final double SPEED = 2;
    /** EU share the hoop pays: the star brings the heat, the network only the magnets. */
    public static final double EU_FACTOR = 0.5;
    public static final int INTEGRITY = 1000, WEAR = 20, REPAIR = 50;
    /** Heat units each mB of coolant takes away: liquid helium, Helium II, shifted superfluid. */
    public static final double[] COOLANT_UNITS = { 0, 1, 3, 10 };

    private HoopPhysics() {}

    public static double coronaKelvin(int heightPct) {
        return 1e4 + 2e6 * (1 - Math.exp(-heightPct / 4.0));
    }

    public static int parallel(int phases, int heightPct) {
        int base = PARALLEL[Math.max(0, Math.min(PARALLEL.length - 1, phases - 1))];
        return (int) Math.max(1, Math.round(base * Math.exp(-(heightPct - HEIGHT_MIN) / 10.0)));
    }

    /** Heat load on the shields, units per second. */
    public static double load(int phases, int heightPct) {
        double q = 100 * Math.exp(-(heightPct - HEIGHT_MIN) / 5.0) * parallel(phases, heightPct) / 256.0;
        return phases >= 4 ? q * 0.6 : q;
    }

    /** Coolant needed this second, mB, for a coolant of the given tier (1..3). */
    public static long coolantDemand(double load, int tier) {
        return tier <= 0 ? 0 : (long) Math.ceil(load / COOLANT_UNITS[tier]);
    }

    /** Shield wear per second for the share of the load left uncooled. */
    public static int wear(double uncooled) {
        return (int) Math.ceil(WEAR * Math.max(0, Math.min(1, uncooled)));
    }
}
