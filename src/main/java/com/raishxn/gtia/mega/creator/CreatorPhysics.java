package com.raishxn.gtia.mega.creator;

import com.raishxn.gtia.mega.system.StarPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;

/**
 * Physics of the Interstellar System Creator (GTIA-D-319): a star made from a gas cloud.
 *
 * <ul>
 * <li>Gather: the cloud is the star's mass in hydrogen, 10⁶ mB per M☉ (plasma counts ten times).</li>
 * <li>Collapse: free fall, t_ff = √(3π / 32 G ρ) ∝ √(R³ / M); 60 s for a Sun, clamped to 20–300 s.</li>
 * <li>Ignite: the Fusion Candle pays the cloud's binding energy, E ∝ (3/5) G M² / R: 2⁴¹ EU × M² / R in solar
 * units, spread over {@link #IGNITE_SECONDS}.</li>
 * <li>Remnants are made from heavier clouds collapsed through the lens: a neutron star from 15 M☉, a black hole from
 * 25 M☉ of gas, with four times the Ψ to compute the collapse.</li>
 * <li>Ψ (Matrioshka) designs the system: 2²⁰ × M × metallicity.</li>
 * </ul>
 */
public final class CreatorPhysics {

    public static final long MB_PER_SOLAR_MASS = 1_000_000;
    public static final int GATHER_MB_PER_SECOND = 50_000, IGNITE_SECONDS = 20, STABILIZE_SECONDS = 30;
    public static final double[] METALLICITY = { 0.5, 1, 2, 3 };
    /** Iron dust per M☉ and per unit of metallicity: the metals the cloud is seeded with. */
    public static final int DUST_PER_SOLAR_MASS = 16;

    public enum Stage {
        IDLE, GATHER, COLLAPSE, IGNITE, STABILIZE
    }

    private CreatorPhysics() {}

    /** Phase that unlocks each kind: the Candle for white stars, the Creator for giants, the lens for remnants. */
    public static int phase(Kind kind) {
        return switch (kind) {
            case RED_DWARF -> 1;
            case YELLOW_DWARF -> 2;
            case WHITE_STAR -> 3;
            case BLUE_GIANT -> 4;
            case NEUTRON_STAR, BLACK_HOLE -> 5;
            default -> 99;
        };
    }

    /** The kinds the Creator can make, in screen order. */
    public static final Kind[] KINDS = { Kind.RED_DWARF, Kind.YELLOW_DWARF, Kind.WHITE_STAR, Kind.BLUE_GIANT,
            Kind.NEUTRON_STAR, Kind.BLACK_HOLE };

    /** Mass of gas the cloud needs, M☉. */
    public static double cloudMass(Kind kind) {
        return switch (kind) {
            case NEUTRON_STAR -> 15;
            case BLACK_HOLE -> 25;
            default -> kind.mass;
        };
    }

    public static long hydrogen(Kind kind) {
        return Math.round(cloudMass(kind) * MB_PER_SOLAR_MASS);
    }

    public static long psi(Kind kind, double z) {
        double factor = kind == Kind.NEUTRON_STAR || kind == Kind.BLACK_HOLE ? 4 : 1;
        return Math.round((1L << 20) * cloudMass(kind) * z * factor);
    }

    public static int dust(Kind kind, double z) {
        return (int) Math.ceil(DUST_PER_SOLAR_MASS * cloudMass(kind) * z);
    }

    /** Free-fall time of the cloud collapsing into the star, seconds of play. */
    public static int collapseSeconds(Kind kind) {
        double r = Math.max(0.1, StarPhysics.radius(Kind.YELLOW_DWARF, cloudMass(kind)));
        double t = 60 * Math.sqrt(r * r * r / cloudMass(kind));
        return (int) Math.max(20, Math.min(300, Math.round(t)));
    }

    /** Binding energy the Candle pays, EU in total. */
    public static double igniteEu(Kind kind) {
        double m = cloudMass(kind), r = Math.max(0.1, StarPhysics.radius(Kind.YELLOW_DWARF, m));
        return Math.pow(2, 41) * m * m / r;
    }

    /** Level of the Dimensional era (D-312) for the systems an owner has made: I at 1, II at 3, III at 6. */
    public static int era(int systems) {
        return systems >= 6 ? 3 : systems >= 3 ? 2 : systems >= 1 ? 1 : 0;
    }
}
