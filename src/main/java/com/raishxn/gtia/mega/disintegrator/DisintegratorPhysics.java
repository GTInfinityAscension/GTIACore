package com.raishxn.gtia.mega.disintegrator;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

/**
 * Physics of the Solar System Disintegrator (GTIA-D-330): a made system taken apart from the outside in.
 *
 * <ul>
 * <li>A body's gravitational binding energy is U = 3GM^2 / 5R (uniform sphere). Peeling it down to radius x costs
 * U (1 - (x/R)^5); in shares of mass taken, f = 1 - (x/R)^3, the energy spent is e(f) = 1 - (1 - f)^(5/3).</li>
 * <li>Layers come off in order: crust, mantle, core, and last the star, whose hydrogen and helium pour out as the
 * system goes dark. The player may stop before the star and keep the system.</li>
 * </ul>
 */
public final class DisintegratorPhysics {

    public static final double EU_PER_SOLAR_MASS_SQ = Math.pow(2, 52), EUT_PER_PHASE = Math.pow(2, 36);
    /** Where each layer ends, as shares of mass taken. */
    public static final double CRUST = 0.25, MANTLE = 0.6, CORE = 0.9;
    public static final Material[] CRUST_YIELD = { GTMaterials.Silicon, GTMaterials.Aluminium, GTMaterials.Calcium },
            MANTLE_YIELD = { GTMaterials.Magnesium, GTMaterials.Iron, GTMaterials.Silicon },
            CORE_YIELD = { GTMaterials.Iron, GTMaterials.Nickel, GTMaterials.Cobalt, GTMaterials.Platinum,
                    GTMaterials.Palladium, GTMaterials.Gold, GTMaterials.Osmium, GTMaterials.Iridium,
                    GTMaterials.Uranium238 };
    /** Dust of each element per 1 % of the system taken, per solar mass, at the Sun's metallicity. */
    public static final double DUST_PER_PERCENT = 4096;
    /** Hydrogen and helium, mB per 1 % of the star taken, per solar mass. */
    public static final double GAS_PER_PERCENT = 1_000_000;

    private DisintegratorPhysics() {}

    /** Binding energy of the system in EU, ~ M^2. */
    public static double binding(double mass) {
        return EU_PER_SOLAR_MASS_SQ * mass * mass;
    }

    public static double energyShare(double f) {
        return 1 - Math.pow(1 - Math.max(0, Math.min(1, f)), 5.0 / 3.0);
    }

    /** Share of mass taken once a share e of the binding energy has been paid. */
    public static double massShare(double e) {
        return 1 - Math.pow(1 - Math.max(0, Math.min(1, e)), 3.0 / 5.0);
    }

    public static double throughput(int phases) {
        return EUT_PER_PHASE * Math.max(1, Math.min(5, phases));
    }

    /** 0 crust, 1 mantle, 2 core, 3 star. */
    public static int layer(double f) {
        return f < CRUST ? 0 : f < MANTLE ? 1 : f < CORE ? 2 : 3;
    }

    public static Material[] yields(int layer) {
        return switch (layer) {
            case 0 -> CRUST_YIELD;
            case 1 -> MANTLE_YIELD;
            case 2 -> CORE_YIELD;
            default -> new Material[0];
        };
    }

    public static long dust(double df, double mass, double metallicity) {
        return Math.round(DUST_PER_PERCENT * df * 100 * mass * metallicity);
    }

    public static long gas(double df, double mass) {
        return Math.round(GAS_PER_PERCENT * df * 100 * mass);
    }
}
