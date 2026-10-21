package com.raishxn.gtia.mega.harvester;

import com.raishxn.gtia.mega.suppressor.SuppressorPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;

/**
 * Physics of the Stellar Harvester (GTIA-D-328): a star's core heated until it burns its life in hours.
 *
 * <ul>
 * <li>Fusion rate climbs steeply with core temperature: ~T^4 for the proton-proton chain (below ~1.3 M☉), ~T^16 for
 * the CNO cycle above. Heating the core by a share h multiplies the burn by (1 + h)^n.</li>
 * <li>The budget is finite: the core hydrogen holds 0.7 % of a tenth of the star's mass-energy (2^60 EU per M☉ here).
 * When it runs out the star ends by its mass: white dwarf below 8 M☉, neutron star to 20, black hole above.</li>
 * </ul>
 */
public final class HarvesterPhysics {

    public static final double EU_PER_SOLAR_MASS = Math.pow(2, 60), EUT_PER_SUN = Math.pow(2, 30), CNO_MASS = 1.3,
            HEAT_PER_PHASE = 0.3, HEAT_STEP = 0.05, BOOST_MAX = 1e6;

    private HarvesterPhysics() {}

    /** Temperature exponent of the fusion rate: 4 (p-p chain) or 16 (CNO cycle). */
    public static int exponent(double mass) {
        return mass < CNO_MASS ? 4 : 16;
    }

    public static double maxHeat(int phases) {
        return HEAT_PER_PHASE * Math.max(1, Math.min(5, phases));
    }

    public static double boost(double heat, double mass) {
        return Math.min(BOOST_MAX, Math.pow(1 + Math.max(0, heat), exponent(mass)));
    }

    /** EU/t the boosted star gives. */
    public static double power(double luminosity, double boost) {
        return EUT_PER_SUN * Math.max(0, luminosity) * boost;
    }

    /** EU left in the core hydrogen. */
    public static double budget(double mass, double fuel) {
        return EU_PER_SOLAR_MASS * mass * Math.max(0, fuel);
    }

    /** Fuel share burnt in one second at this power. */
    public static double burn(double eut, double mass) {
        return eut * 20 / (EU_PER_SOLAR_MASS * Math.max(1e-6, mass));
    }

    /** Seconds until the core runs out at this power. */
    public static double secondsLeft(double eut, double mass, double fuel) {
        return eut <= 0 ? Double.POSITIVE_INFINITY : budget(mass, fuel) / (eut * 20);
    }

    public static Kind remnant(double mass) {
        return mass < 8 ? Kind.WHITE_DWARF : mass < 20 ? Kind.NEUTRON_STAR : Kind.BLACK_HOLE;
    }

    public static double remnantMass(double mass) {
        return switch (remnant(mass)) {
            case WHITE_DWARF -> 0.6;
            case NEUTRON_STAR -> 1.4;
            default -> SuppressorPhysics.holeMass(mass);
        };
    }

    /** Drawn temperature of the heated photosphere: L ~ T^4 at a fixed radius, so T grows as boost^(1/4). */
    public static double drawnTemperature(double t, double boost) {
        return Math.min(40000, t * Math.pow(Math.max(1, boost), 0.25));
    }
}
