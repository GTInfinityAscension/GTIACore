package com.raishxn.gtia.mega.engine;

/**
 * Physics of the Stellar Engine (GTIA-D-315): a Shkadov mirror and a Caplan engine moving the Nexus System.
 *
 * <ul>
 * <li>Shkadov: {@code F = f L / c}, f the share of the star's sky the mirror covers.</li>
 * <li>Caplan: {@code F = beta * mdot * v_e}; the engine lifts {@code mdot} of hydrogen and helium from the star,
 * sends the share {@code beta} out as the main jet and the rest back into the star as the counter-jet.</li>
 * <li>{@code a = F / M}; one second of play is {@link #YEARS_PER_SECOND} years of engine, so
 * {@code dv = a * t} per second.</li>
 * </ul>
 */
public final class EnginePhysics {

    public static final double C = 2.998e8, L_SUN = 3.828e26, M_SUN = 1.989e30, SECONDS_PER_YEAR = 3.156e7;
    public static final double YEARS_PER_SECOND = 1e6;
    /** A 60 degree cap covers (1 - cos 60) / 2 of the sky. */
    public static final double MIRROR_SHARE = (1 - Math.cos(Math.toRadians(60))) / 2;
    /** Matter lifted at full power, kg/s, and the main jet's exhaust speed (0.01 c). */
    public static final double MDOT_MAX = 1e12, V_EXHAUST = 0.01 * C;
    /** EU/t the lifting magnets draw from the Nexus network at full power. */
    public static final long CAPLAN_EUT = 1L << 36;
    /** Main jet share: from even (0.5) to 0.9; above {@link #BETA_SAFE} the counter-jet cannot hold the star. */
    public static final int BETA_MIN = 50, BETA_MAX = 90, BETA_STEP = 5, BETA_SAFE = 70;
    /** Cage integrity in per mille, lost per second at full tension, regained per rib block. */
    public static final int INTEGRITY = 1000, WEAR = 20, REPAIR = 50;
    /** Plasma at full power per second, mB. */
    public static final long HYDROGEN_PLASMA = 1000, HELIUM_PLASMA = 250;

    private EnginePhysics() {}

    /** Thrust of the mirror, newtons. */
    public static double shkadovThrust(boolean mirror) {
        return mirror ? MIRROR_SHARE * L_SUN / C : 0;
    }

    /** Thrust of the Caplan engine, newtons: {@code beta * mdot * v_e}. */
    // EN: The main jet carries the share beta of the lifted mass; the rest goes back into the star so it follows the
    // engine (Caplan 2019).
    // PT: O jato principal leva a parte beta da massa sugada; o resto volta para a estrela, que assim acompanha o
    // motor (Caplan 2019).
    // ZH: 主喷流携带被抽取质量的 beta 份额；其余返回恒星，使恒星跟随引擎（Caplan 2019）。
    public static double caplanThrust(double mdot, int betaPct) {
        return betaPct / 100.0 * mdot * V_EXHAUST;
    }

    /** Δv gained in one second of play, km/s. */
    public static double dvPerSecond(double thrust) {
        return thrust / M_SUN * YEARS_PER_SECOND * SECONDS_PER_YEAR / 1000;
    }

    /** Tension on the cage, 0..1: zero up to {@link #BETA_SAFE}, full at {@link #BETA_MAX}. */
    public static double tension(int betaPct) {
        return Math.max(0, Math.min(1, (betaPct - BETA_SAFE) / (double) (BETA_MAX - BETA_SAFE)));
    }

    /** Regions of the galaxy along the route, with the Δv (km/s) that reaches them. */
    public enum Region {

        LOCAL_CLOUD(0, 0),
        ORION_EDGE(20, 1),
        INTERARM_VOID(60, 2),
        EMISSION_NEBULA(120, 2),
        PULSAR_VICINITY(250, 2),
        GALACTIC_CORE(500, 3);

        public final double dv;
        /** Level of the Stellar era it gives (D-312: I to III). */
        public final int era;

        Region(double dv, int era) {
            this.dv = dv;
            this.era = era;
        }

        public static Region at(double dv) {
            Region r = LOCAL_CLOUD;
            for (Region x : values()) if (dv >= x.dv) r = x;
            return r;
        }

        public Region next() {
            return this == GALACTIC_CORE ? this : values()[ordinal() + 1];
        }

        /** Regions reached so far count: the farthest one gives every bonus of those before it. */
        public boolean reached(Region farthest) {
            return farthest.ordinal() >= ordinal();
        }
    }

    /** Lifted matter is 25 % richer from the Orion arm's edge on (denser stellar winds). */
    public static double mdotBonus(Region farthest) {
        return Region.ORION_EDGE.reached(farthest) ? 1.25 : 1;
    }

    /** Cold background of the void between the arms: the Matrioshka's shells run 10 % colder. */
    public static double shellCooling(Region farthest) {
        return Region.INTERARM_VOID.reached(farthest) ? 0.9 : 1;
    }

    /** Nebula gas doubles the hydrogen plasma. */
    public static double hydrogenBonus(Region farthest) {
        return Region.EMISSION_NEBULA.reached(farthest) ? 2 : 1;
    }
}
