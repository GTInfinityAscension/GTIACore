package com.raishxn.gtia.mega;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Consumer;

/** Recipes of the megastructures' controllers (provisional, until the Ψ era's progression is drawn). */
public final class MegaRecipes {

    private MegaRecipes() {}

    /** Provisional (GTIA-D-330): field generators to switch the binding off, a Suprashell's worth of gravity stars. */
    private static void disintegrator(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("disintegrator"))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(64))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(GTItems.EMITTER_UV.asStack(64))
                .inputItems(CustomTags.UIV_CIRCUITS, 32)
                .inputFluids(GTMaterials.Neutronium.getFluid(73728))
                .outputItems(GTIAMegastructures.DISINTEGRATOR_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-329): a shell needs field generators to hold it static and gravity stars to tether it. */
    private static void suprashell(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("suprashell"))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(64))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(GTItems.SENSOR_UV.asStack(32))
                .inputItems(CustomTags.UIV_CIRCUITS, 32)
                .inputFluids(GTMaterials.Neutronium.getFluid(73728))
                .outputItems(GTIAMegastructures.SUPRASHELL_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-328): emitters to heat a core and the coils of the magnetic mirrors. */
    private static void stellarHarvester(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("stellar_harvester"))
                .inputItems(GTItems.EMITTER_UV.asStack(64))
                .inputItems(com.gregtechceu.gtceu.common.data.GTBlocks.SUPERCONDUCTING_COIL.asStack(64))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(36864))
                .outputItems(GTIAMegastructures.STELLAR_HARVESTER_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-327): greenhouses by the dozen and the field generators that hold the gardens aloft. */
    private static void yggdrasil(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("yggdrasil"))
                .inputItems(com.raishxn.gtia.data.GTIAGreenhouse.GREENHOUSE.asStack(64))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(32))
                .inputItems(GTItems.EMITTER_UV.asStack(32))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(9216))
                .outputItems(GTIAMegastructures.YGGDRASIL_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-326): the yard is assembled from assembly lines and the robot arms that run them. */
    private static void assemblyYard(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("assembly_yard"))
                .inputItems(com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.ASSEMBLY_LINE.asStack(16))
                .inputItems(GTItems.ROBOT_ARM_UV.asStack(64))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(16))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(18432))
                .outputItems(GTIAMegastructures.ASSEMBLY_YARD_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-323): the forge is pressed out of neutronium. */
    private static void nidavellir(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("nidavellir"))
                .inputItems(com.gregtechceu.gtceu.common.data.machines.GCYMMachines.BLAST_ALLOY_SMELTER.asStack(16))
                .inputItems(GTItems.ELECTRIC_PISTON_UV.asStack(64))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(36864))
                .outputItems(GTIAMegastructures.NIDAVELLIR_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-322): superconducting coils for the Penrose ring, sensors for the antennas. */
    private static void hraemc(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("hraemc"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTBlocks.SUPERCONDUCTING_COIL.asStack(64))
                .inputItems(GTItems.SENSOR_UV.asStack(32))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(18432))
                .outputItems(GTIAMegastructures.HRAEMC_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-321): emitters to pour light in, gravity stars to hold the hole. */
    private static void kugelblitz(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("kugelblitz"))
                .inputItems(GTItems.EMITTER_UV.asStack(64))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(32))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(18432))
                .outputItems(GTIAMegastructures.KUGELBLITZ_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-320): field generators to damp the core, sensors to read its neutrinos. */
    private static void fusionSuppressor(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("fusion_suppressor"))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(32))
                .inputItems(GTItems.SENSOR_UV.asStack(32))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(18432))
                .outputItems(GTIAMegastructures.FUSION_SUPPRESSOR_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-319): a forge of stars from fusion reactors, field generators and gravity stars. */
    private static void systemCreator(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("system_creator"))
                .inputItems(com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR[GTValues.UV]
                        .asStack(16))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(32))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UIV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(18432))
                .outputItems(GTIAMegastructures.SYSTEM_CREATOR_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UIV]).duration(12000)
                .save(provider);
    }

    /** Provisional (GTIA-D-318): fusion reactors for the crucibles, shielding for the hoop. */
    private static void stellarHoop(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("stellar_hoop"))
                .inputItems(com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR[GTValues.UV]
                        .asStack(8))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(8))
                .inputItems(GTItems.GRAVI_STAR.asStack(32))
                .inputItems(CustomTags.UEV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(4608))
                .outputItems(GTIAMegastructures.STELLAR_HOOP_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UEV]).duration(6000)
                .save(provider);
    }

    /** Provisional (GTIA-D-317): the ring current is superconducting coils, the petals are field generators. */
    private static void starLifter(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("star_lifter"))
                .inputItems(com.gregtechceu.gtceu.common.data.GTBlocks.SUPERCONDUCTING_COIL.asStack(64))
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(16))
                .inputItems(GTItems.GRAVI_STAR.asStack(32))
                .inputItems(CustomTags.UEV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(4608))
                .outputItems(GTIAMegastructures.STAR_LIFTER_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UEV]).duration(6000)
                .save(provider);
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        // Helium II: liquid helium cooled below the lambda point (2.17 K).
        GTRecipeTypes.VACUUM_RECIPES.recipeBuilder(GTIAMod.id("superfluid_helium"))
                .inputFluids(GTMaterials.Helium.getFluid(
                        com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.LIQUID, 1000))
                .outputFluids(MegaMaterials.SUPERFLUID_HELIUM.getFluid(1000))
                .EUt(GTValues.VA[GTValues.UHV]).duration(200)
                .save(provider);
        if (GTIAMegastructures.STAR_LIFTER_DEF != null) starLifter(provider);
        if (GTIAMegastructures.STELLAR_HOOP_DEF != null) stellarHoop(provider);
        if (GTIAMegastructures.SYSTEM_CREATOR_DEF != null) systemCreator(provider);
        if (GTIAMegastructures.FUSION_SUPPRESSOR_DEF != null) fusionSuppressor(provider);
        if (GTIAMegastructures.KUGELBLITZ_DEF != null) kugelblitz(provider);
        if (GTIAMegastructures.HRAEMC_DEF != null) hraemc(provider);
        if (GTIAMegastructures.NIDAVELLIR_DEF != null) nidavellir(provider);
        if (GTIAMegastructures.ASSEMBLY_YARD_DEF != null) assemblyYard(provider);
        if (GTIAMegastructures.YGGDRASIL_DEF != null) yggdrasil(provider);
        if (GTIAMegastructures.STELLAR_HARVESTER_DEF != null) stellarHarvester(provider);
        if (GTIAMegastructures.SUPRASHELL_DEF != null) suprashell(provider);
        if (GTIAMegastructures.DISINTEGRATOR_DEF != null) disintegrator(provider);
        if (GTIAMegastructures.MATRIOSHKA_BRAIN == null) return;
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("matrioshka_brain"))
                .inputItems(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY.asStack(16))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UEV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(9216))
                .outputItems(GTIAMegastructures.MATRIOSHKA_BRAIN.asStack())
                .EUt(GTValues.VA[GTValues.UEV]).duration(6000)
                .save(provider);
        if (GTIAMegastructures.STELLAR_ENGINE_DEF == null) return;
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("stellar_engine"))
                .inputItems(com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR[GTValues.UV]
                        .asStack(4))
                .inputItems(GTItems.GRAVI_STAR.asStack(64))
                .inputItems(CustomTags.UEV_CIRCUITS, 16)
                .inputFluids(GTMaterials.Neutronium.getFluid(9216))
                .outputItems(GTIAMegastructures.STELLAR_ENGINE_DEF.asStack())
                .EUt(GTValues.VA[GTValues.UEV]).duration(6000)
                .save(provider);
    }
}
