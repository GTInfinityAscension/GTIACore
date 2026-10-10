package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.raishxn.gtia.GTIAMod;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_DECOMPOSITION;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FRAME;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;

public final class GTIAAlloys {

    public static Material PULSATING_ALLOY;
    public static Material CONDUCTIVE_ALLOY;
    public static Material PHASE_MATRIX;
    /** What leaves a diaphragm cell: dilute caustic soda still carrying salt. */
    public static Material CELL_LIQUOR;
    // Chemistry slice 02 (MV): Bayer and Hall-Heroult, the Contact process
    public static Material SODIUM_ALUMINATE;
    public static Material ALUMINIUM_HYDROXIDE;
    public static Material ALUMINA;
    public static Material CRYOLITE;
    public static Material VANADIUM_PENTOXIDE;

    private GTIAAlloys() {}

    public static void register() {
        PHASE_MATRIX = new Material.Builder(GTIAMod.id("phase_matrix"))
                .dust()
                .ore()
                .color(0x53BFC0)
                .iconSet(GTIAMaterialIconSets.PHASE_CRYSTAL)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        PULSATING_ALLOY = new Material.Builder(GTIAMod.id("pulsating_alloy"))
                .ingot()
                .liquid(new FluidBuilder().customStill().disableColor())
                .color(0x6AE26E)
                .iconSet(MaterialIconSet.METALLIC)
                .flags(DISABLE_DECOMPOSITION, GENERATE_PLATE, GENERATE_FRAME)
                .buildAndRegister();

        CONDUCTIVE_ALLOY = new Material.Builder(GTIAMod.id("conductive_alloy"))
                .ingot()
                .liquid(new FluidBuilder().customStill().disableColor())
                .color(0xF7B29B)
                .iconSet(MaterialIconSet.METALLIC)
                .cableProperties(GTValues.V[GTValues.ULV], 4, 0, true)
                .flags(DISABLE_DECOMPOSITION, GENERATE_PLATE, GENERATE_FRAME)
                .buildAndRegister();

        CELL_LIQUOR = new Material.Builder(GTIAMod.id("cell_liquor"))
                .liquid()
                .color(0xA9C9D3)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        SODIUM_ALUMINATE = new Material.Builder(GTIAMod.id("sodium_aluminate"))
                .liquid()
                .color(0xC9C2A8)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Sodium, 1, GTMaterials.Aluminium, 1, GTMaterials.Oxygen, 2)
                .buildAndRegister();
        ALUMINIUM_HYDROXIDE = new Material.Builder(GTIAMod.id("aluminium_hydroxide"))
                .dust()
                .color(0xEDEDE4)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Aluminium, 1, GTMaterials.Oxygen, 3, GTMaterials.Hydrogen, 3)
                .buildAndRegister();
        ALUMINA = new Material.Builder(GTIAMod.id("alumina"))
                .dust()
                .color(0xF4F1EA)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Aluminium, 2, GTMaterials.Oxygen, 3)
                .buildAndRegister();
        CRYOLITE = new Material.Builder(GTIAMod.id("cryolite"))
                .dust()
                .color(0xD6E8EE)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Sodium, 3, GTMaterials.Aluminium, 1, GTMaterials.Fluorine, 6)
                .buildAndRegister();
        VANADIUM_PENTOXIDE = new Material.Builder(GTIAMod.id("vanadium_pentoxide"))
                .dust()
                .color(0xC98A16)
                .flags(DISABLE_DECOMPOSITION)
                .components(GTMaterials.Vanadium, 2, GTMaterials.Oxygen, 5)
                .buildAndRegister();
    }
}
