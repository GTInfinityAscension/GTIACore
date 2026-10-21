package com.raishxn.gtia.mega.hraemc;

/**
 * Physics of the HRAE-MC (GTIA-D-322): power from a natural black hole.
 *
 * <ul>
 * <li>Accretion: matter spiralling down to the innermost stable orbit gives off η of its rest energy, η = 1 -
 * √(1 - 2 / 3r_isco), with r_isco from Bardeen, Press and Teukolsky (1972): 6 GM/c² without spin (η 5.7 %), 1.24
 * at a = 0.998 (η 32 %). It spins the hole up.</li>
 * <li>Light pressure caps it at the Eddington luminosity, L_Edd = 1.26 × 10³¹ W × M / M☉.</li>
 * <li>Penrose process: particles split in the ergosphere take the hole's rotational energy, up to 29 % of its mass at
 * full spin (M_irr = M √((1 + √(1 - a²)) / 2)); it spins the hole down.</li>
 * <li>Blandford–Znajek jets carry a share a² of the accretion power out of the poles.</li>
 * </ul>
 */
public final class HraePhysics {

    /** Rest energy of one unit of matter fed (an item, or 1000 mB), EU. */
    public static final double EU_PER_UNIT = Math.pow(2, 32);
    /** Eddington cap, EU/t per M☉. */
    public static final double EDDINGTON_EUT = Math.pow(2, 40);
    /** Penrose extraction at full share and full spin, EU/t per M☉. */
    public static final double PENROSE_EUT = Math.pow(2, 38);
    /** The hole's mass-energy in play, EU per M☉ (only its rotational share can be taken). */
    public static final double EU_PER_SOLAR_MASS = Math.pow(2, 62);
    public static final double MAX_SPIN = 0.998;
    /** Spin added per unit of matter, for a hole of 1 M☉. */
    public static final double SPIN_PER_UNIT = 2e-5;
    public static final int SHARE_STEP = 10;

    private HraePhysics() {}

    /** Innermost stable circular orbit (prograde), in GM/c². */
    public static double isco(double a) {
        double z1 = 1 + Math.cbrt(1 - a * a) * (Math.cbrt(1 + a) + Math.cbrt(1 - a));
        double z2 = Math.sqrt(3 * a * a + z1 * z1);
        return 3 + z2 - Math.sqrt((3 - z1) * (3 + z1 + 2 * z2));
    }

    public static double efficiency(double a) {
        return 1 - Math.sqrt(1 - 2 / (3 * isco(a)));
    }

    /** Share of the mass held as rotation: 1 - M_irr / M. */
    public static double rotationalShare(double a) {
        return 1 - Math.sqrt((1 + Math.sqrt(1 - a * a)) / 2);
    }

    public static double eddingtonEut(double massSun) {
        return EDDINGTON_EUT * massSun;
    }

    /** Accretion power for {@code units} fed this second, EU/t, capped by Eddington. */
    public static double accretionEut(double units, double a, double massSun) {
        return Math.min(eddingtonEut(massSun), units * EU_PER_UNIT * efficiency(a) / 20);
    }

    public static double penroseEut(int sharePct, double a, double massSun) {
        return PENROSE_EUT * massSun * sharePct / 100.0 * a;
    }

    public static double jetEut(double accretionEut, double a) {
        return accretionEut * a * a;
    }

    /** Spin change in one second: up by accretion, down by what Penrose took. */
    public static double spinChange(double units, double penroseEut, double a, double massSun) {
        double up = units * SPIN_PER_UNIT / Math.max(0.1, massSun);
        // The Penrose energy comes off the rotational share: da follows d(share)/da near the current spin.
        double share = rotationalShare(a), next = rotationalShare(Math.max(0, a - 1e-3));
        double perSpin = Math.max(1e-9, (share - next) / 1e-3) * massSun * EU_PER_SOLAR_MASS;
        double down = penroseEut * 20 / perSpin;
        return up - down;
    }

    /** Research points a second (phase 5): grows with the logarithm of the power. */
    public static long research(double eut) {
        return eut <= 0 ? 0 : Math.round(Math.log(1 + eut) / Math.log(2) / 4);
    }
}
