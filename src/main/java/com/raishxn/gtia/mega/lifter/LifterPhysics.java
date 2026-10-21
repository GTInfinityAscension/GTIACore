package com.raishxn.gtia.mega.lifter;

import com.raishxn.gtia.mega.system.StarPhysics;

/**
 * Physics of the Star Lifter (GTIA-D-317): matter lifted off the star (Criswell's star lifting).
 *
 * <ul>
 * <li>Each kilogram has to climb out of the star's gravity well: e = G M / R (1.9e11 J/kg for the Sun), done at
 * {@link #LIFT_EFFICIENCY}.</li>
 * <li>Lift rate ṁ (kg/s): heater petals alone up to 1e11, the equatorial ring current funnels it out of the poles up
 * to 1e12. The polar chalices catch 85 % of it; without them 15 %.</li>
 * <li>One second of play is {@link #YEARS_PER_SECOND} years of lifting: the star loses ṁ × 3.16e13 kg each second.
 * L ∝ M⁴ (main sequence), so a lighter star is dimmer, cooler and lives longer (t ∝ M / L).</li>
 * <li>What comes out is the star's surface mix (solar mass fractions); deep stirring brings up the core's helium and
 * metals and mixes fresh hydrogen down, adding to the fuel.</li>
 * </ul>
 */
public final class LifterPhysics {

    public static final double G_M_OVER_R_SUN = 1.907e11, LIFT_EFFICIENCY = 0.5, YEARS_PER_SECOND = 1e6;
    public static final double MDOT_THERMAL = 1e11, MDOT_RING = 1e12;
    public static final double CAPTURE_CHALICES = 0.85, CAPTURE_OPEN = 0.15;
    /** Below this mass the ring current cannot hold the outflow. */
    public static final double MIN_MASS = 0.5;
    /** EU/t at 1e12 kg/s out of the Sun's well. */
    public static final double EUT_AT_FULL = Math.pow(2, 34);
    public static final int RATE_MIN = 10, RATE_MAX = 100, RATE_STEP = 10;
    /** Deep stirring above this rate grows instability (per mille per second at full rate). */
    public static final int DEEP_SAFE = 60, INSTABILITY_RATE = 25, INTEGRITY = 1000, REPAIR = 50;

    /** How the matter is lifted. */
    public enum Mode {

        /** Heater petals warm the poles: a thermal wind (phase 1). */
        THERMAL(1),
        /** The ring current pinches the field and the plasma flows out of the poles (phase 2). */
        RING(2),
        /** The stirring arcs mix the core up (phase 5): richer matter and fresh fuel, but the star grows restless. */
        DEEP(5);

        public final int phase;

        Mode(int phase) {
            this.phase = phase;
        }
    }

    /**
     * Elements lifted, mass fractions at the surface and with deep stirring. Surface: solar photosphere (Asplund
     * 2009, rounded); deep: a helium-rich, metal-richer mix from the core's edge.
     */
    public enum Element {

        HYDROGEN(0.735, 0.35), HELIUM(0.249, 0.62), OXYGEN(0.0077, 0.012), CARBON(0.0029, 0.008),
        IRON(0.0016, 0.004), NEON(0.0012, 0.004), NITROGEN(0.0009, 0.002), SILICON(0.0007, 0.0015),
        MAGNESIUM(0.0005, 0.0012), NICKEL(0.0001, 0.0004);

        public final double surface, deep;

        Element(double surface, double deep) {
            this.surface = surface;
            this.deep = deep;
        }

        public double fraction(Mode mode) {
            return mode == Mode.DEEP ? deep : surface;
        }

        /** Hydrogen and helium come out as plasma, the rest as dust from the separation spires. */
        public boolean plasma() {
            return this == HYDROGEN || this == HELIUM;
        }
    }

    /** Plasma (mB) and dust (items) per kilogram caught: 2000 mB/s of hydrogen plasma at full ring-current rate. */
    public static final double MB_PER_KG = 3.2e-9, DUST_PER_KG = 8e-10;

    private LifterPhysics() {}

    /** Escape energy per kilogram, J/kg: G M / R in solar units times the Sun's. */
    public static double escapeEnergy(double mass) {
        return G_M_OVER_R_SUN * mass / StarPhysics.radius(StarPhysics.Kind.YELLOW_DWARF, mass);
    }

    /** Lift rate, kg/s, for a mode at {@code ratePct}. */
    public static double mdot(Mode mode, int ratePct) {
        return (mode == Mode.THERMAL ? MDOT_THERMAL : MDOT_RING) * ratePct / 100.0;
    }

    /** EU per tick the lifting draws: proportional to ṁ and to the depth of the well. */
    public static double eut(double mdot, double mass) {
        return EUT_AT_FULL * mdot / MDOT_RING * escapeEnergy(mass) / G_M_OVER_R_SUN;
    }

    /** Star mass lost in one second of play, M☉. */
    public static double massLost(double mdot) {
        return mdot * YEARS_PER_SECOND * StarPhysics.YEAR / StarPhysics.M_SUN;
    }

    /**
     * Core fuel deep stirring adds: the arcs mix down 3.5 times the mass they lift, as a share of the star.
     */
    // EN: Mixing fresh hydrogen into the core is how stellar rejuvenation works; the factor 3.5 is a balance choice so an
    // hour of deep lifting at full rate refills about a fifth of the fuel.
    // PT: Misturar hidrogênio novo no núcleo é como uma estrela rejuvenesce; o fator 3,5 é balanço, para uma hora de
    // agitação profunda no máximo repor cerca de um quinto do combustível.
    // ZH: 把新鲜氢混入核心是恒星返老还童的方式；系数 3.5 为平衡取值，使满速深层搅拌一小时约补充五分之一燃料。
    public static double fuelAdded(Mode mode, double lost, double mass) {
        return mode == Mode.DEEP ? 3.5 * lost / Math.max(mass, 1e-6) : 0;
    }

    /** Instability gained per second: deep stirring above {@link #DEEP_SAFE} % only. */
    public static int instability(Mode mode, int ratePct) {
        if (mode != Mode.DEEP || ratePct <= DEEP_SAFE) return 0;
        return (int) Math.ceil(INSTABILITY_RATE * (ratePct - DEEP_SAFE) / (double) (RATE_MAX - DEEP_SAFE));
    }
}
