package com.raishxn.gtia.mega;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.raishxn.gtia.GTIAMod;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_DECOMPOSITION;

/** Materials of the megastructures. */
public final class MegaMaterials {

    /**
     * Helium II below the lambda point (2.17 K): a superfluid that carries heat far better than liquid helium. The
     * Matrioshka Brain's second coolant (GTIA-D-314).
     */
    public static Material SUPERFLUID_HELIUM;

    private MegaMaterials() {}

    public static void register() {
        SUPERFLUID_HELIUM = new Material.Builder(GTIAMod.id("superfluid_helium"))
                .liquid(new FluidBuilder().temperature(2))
                .color(0xC8F4FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Helium, 1)
                .buildAndRegister();
    }
}
