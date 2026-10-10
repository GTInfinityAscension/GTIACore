package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;

import com.raishxn.gtia.GTIAMod;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_DECOMPOSITION;

/** Draconium materials owned by GTIACore, without external item unification. */
public final class GTIADraconiumMaterials {

    public static Material DRACONIUM;
    public static Material AWAKENED_DRACONIUM;

    private GTIADraconiumMaterials() {}

    public static void register() {
        DRACONIUM = new Material.Builder(GTIAMod.id("draconium"))
                .ingot()
                .liquid(new FluidBuilder().customStill().disableColor())
                .color(0x8A6BAF)
                .iconSet(GTIAMaterialIconSets.ASTRAL_VOID)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        AWAKENED_DRACONIUM = new Material.Builder(GTIAMod.id("awakened_draconium"))
                .ingot()
                .liquid(new FluidBuilder().customStill().disableColor())
                .color(0xE46A27)
                .iconSet(GTIAMaterialIconSets.SOLAR_RUBY)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();
    }
}
