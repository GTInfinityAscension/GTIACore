package com.raishxn.gtia.mega.system;

/**
 * Stars of the star systems (GTIA-D-316), in solar units: mass M (M☉), luminosity L (L☉), radius R (R☉), effective
 * temperature T (K).
 *
 * <ul>
 * <li>Main sequence: L = 0.23 M^2.3 below 0.43 M☉, M^4 up to 2 M☉, 1.4 M^3.5 above; R = M^0.8 up to 1 M☉, M^0.57
 * above; T = 5772 (L / R²)^¼ (Stefan–Boltzmann).</li>
 * <li>Main-sequence lifetime t = 10^10 yr × M / L.</li>
 * <li>Remnants: white dwarf (R 0.012, T 25 000 K), neutron star (R 1.4e-5, T 600 000 K), black hole (no surface;
 * Schwarzschild radius r_s = 2GM/c², 2.95 km per M☉).</li>
 * </ul>
 */
public final class StarPhysics {

    public static final double T_SUN = 5772, M_SUN = 1.989e30, L_SUN = 3.828e26, C = 2.998e8, YEAR = 3.156e7;

    /** What sits in the middle of a system. */
    public enum Kind {

        RED_DWARF(0.3), YELLOW_DWARF(1.0), WHITE_STAR(2.0), BLUE_GIANT(15.0), WHITE_DWARF(0.6), NEUTRON_STAR(1.4),
        BLACK_HOLE(10.0), NONE(0);

        /** Mass a new star of this kind is made with, M☉. */
        public final double mass;

        Kind(double mass) {
            this.mass = mass;
        }

        public boolean mainSequence() {
            return ordinal() <= BLUE_GIANT.ordinal();
        }

        public boolean shines() {
            return this != BLACK_HOLE && this != NONE;
        }
    }

    private StarPhysics() {}

    public static double luminosity(Kind kind, double mass) {
        return switch (kind) {
            case WHITE_DWARF -> 0.01;
            case NEUTRON_STAR -> 0.2;
            case BLACK_HOLE, NONE -> 0;
            default -> mass < 0.43 ? 0.23 * Math.pow(mass, 2.3) : mass < 2 ? Math.pow(mass, 4) :
                    1.4 * Math.pow(mass, 3.5);
        };
    }

    public static double radius(Kind kind, double mass) {
        return switch (kind) {
            case WHITE_DWARF -> 0.012;
            case NEUTRON_STAR -> 1.4e-5;
            case BLACK_HOLE -> schwarzschildKm(mass) / 6.957e5;
            case NONE -> 0;
            default -> mass <= 1 ? Math.pow(mass, 0.8) : Math.pow(mass, 0.57);
        };
    }

    public static double temperature(Kind kind, double mass) {
        return switch (kind) {
            case WHITE_DWARF -> 25_000;
            case NEUTRON_STAR -> 600_000;
            case BLACK_HOLE, NONE -> 0;
            default -> T_SUN * Math.pow(luminosity(kind, mass) / Math.pow(radius(kind, mass), 2), 0.25);
        };
    }

    /** Main-sequence lifetime in years. */
    public static double lifetimeYears(double mass) {
        return 1e10 * mass / Math.max(1e-9, luminosity(Kind.YELLOW_DWARF, mass));
    }

    public static double schwarzschildKm(double mass) {
        return 2.953 * mass;
    }

    /**
     * Radius the star is drawn with, as a factor of the megastructure's drawn star: compressed (√R) and capped so a
     * giant still fits inside the structure built around a Sun-like star and a remnant stays visible.
     */
    public static float drawnScale(Kind kind, double mass) {
        return switch (kind) {
            case WHITE_DWARF -> 0.3f;
            case NEUTRON_STAR -> 0.18f;
            case BLACK_HOLE -> 0.5f;
            case NONE -> 0;
            default -> (float) Math.max(0.45, Math.min(1.6, Math.sqrt(radius(kind, mass))));
        };
    }

    /**
     * Energy left in a main-sequence star's core hydrogen, joules: 0.7 % of the mass of the tenth of the star that
     * burns, times the fuel fraction still there (E = 0.007 · 0.1 · M c² · fuel).
     */
    public static double fuelEnergy(double mass, double fuel) {
        return 0.007 * 0.1 * mass * M_SUN * C * C * fuel;
    }
}
