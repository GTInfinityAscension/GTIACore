package com.raishxn.gtia.route;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.raishxn.gtia.GTIAMod;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_DECOMPOSITION;

/**
 * EN: Intermediates of the steel and aluminium routes (GTIA-D-306, GTIA-D-308): pig iron and slag from the blast
 * furnace, crude aluminium from the compressor.
 * PT: Intermediários das rotas de aço e alumínio (GTIA-D-306, GTIA-D-308): gusa e escória do alto-forno, alumínio
 * bruto do compressor.
 * ZH: 钢与铝路线的中间产物 (GTIA-D-306, GTIA-D-308): 高炉产出的生铁和炉渣, 压缩机产出的粗铝。
 */
public final class RouteMaterials {

    public static Material PIG_IRON;
    public static Material SLAG;
    public static Material CRUDE_ALUMINIUM;

    private RouteMaterials() {}

    public static void register() {
        PIG_IRON = new Material.Builder(GTIAMod.id("pig_iron"))
                .ingot()
                .color(0x77706A)
                .iconSet(MaterialIconSet.DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Iron, 1)
                .buildAndRegister();
        SLAG = new Material.Builder(GTIAMod.id("slag"))
                .dust()
                .color(0x5E564A)
                .iconSet(MaterialIconSet.ROUGH)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();
        CRUDE_ALUMINIUM = new Material.Builder(GTIAMod.id("crude_aluminium"))
                .ingot()
                .color(0x9AA4AE)
                .iconSet(MaterialIconSet.DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Aluminium, 1)
                .buildAndRegister();
    }
}
