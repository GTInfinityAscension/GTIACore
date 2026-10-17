package com.raishxn.gtia.chem;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.raishxn.gtia.GTIAMod;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_DECOMPOSITION;

/** Materials of the HV chemistry lines (GTIA-D-281). */
public final class ChemMaterials {

    public static Material TRICHLOROSILANE;
    public static Material POLYSILICON;

    private ChemMaterials() {}

    public static void register() {
        TRICHLOROSILANE = new Material.Builder(GTIAMod.id("trichlorosilane"))
                .liquid()
                .color(0xD8E4E0)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Silicon, 1, GTMaterials.Hydrogen, 1, GTMaterials.Chlorine, 3)
                .buildAndRegister();
        POLYSILICON = new Material.Builder(GTIAMod.id("polysilicon"))
                .dust()
                .color(0x5A6470)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Silicon, 1)
                .buildAndRegister();
    }
}
