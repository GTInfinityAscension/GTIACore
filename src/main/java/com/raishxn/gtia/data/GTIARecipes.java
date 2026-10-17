package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Consumer;

public final class GTIARecipes {

    private GTIARecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("phase_coupler"),
                GTIAItems.PHASE_COUPLER.asStack(),
                " R ", "PVW", " R ",
                'R', new MaterialEntry(TagPrefix.wireGtSingle, GTMaterials.RedAlloy),
                'P', new MaterialEntry(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY),
                'V', GTItems.VACUUM_TUBE.asStack(),
                'W', new MaterialEntry(TagPrefix.plate, GTMaterials.WroughtIron));

        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("lv_catalytic_dehydrator"),
                GTIAMachines.CATALYTIC_DEHYDRATOR.asStack(), "GFG", "CHV", "P P",
                'F', GTItems.FIRECLAY_BRICK.asStack(),
                'P', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze),
                'G', GTItems.GLASS_TUBE.asStack(),
                'C', GTIAItems.PHASE_COUPLER.asStack(),
                'V', GTItems.VACUUM_TUBE.asStack(),
                'H', GTMachines.HULL[GTValues.LV].asStack());

        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(GTIAMod.id("pulsating_alloy_from_ender_pearl"))
                .inputItems(TagPrefix.ingot, GTMaterials.Iron)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)
                .duration(300).EUt(7).save(provider);

        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(GTIAMod.id("pulsating_alloy_from_phase_dust"))
                .inputItems(TagPrefix.ingot, GTMaterials.Iron)
                .inputItems(TagPrefix.dustImpure, GTIAAlloys.PHASE_MATRIX)
                .outputItems(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)
                .duration(300).EUt(7).save(provider);

        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(GTIAMod.id("conductive_alloy_from_pulsating_alloy"))
                .inputItems(TagPrefix.ingot, GTMaterials.RedAlloy)
                .inputItems(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)
                .outputItems(TagPrefix.ingot, GTIAAlloys.CONDUCTIVE_ALLOY, 2)
                .duration(300).EUt(7).save(provider);

        for (int tier : GTValues.tiersBetween(GTValues.LV, GTValues.OpV)) {
            var vat = GTIAMachines.FERMENTATION_VAT[tier];
            VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id(vat.getName()), vat.asStack(),
                    "GPG", "PMP", "WCW",
                    'G', GTCraftingComponents.GLASS.get(tier),
                    'P', GTCraftingComponents.PUMP.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier));
        }

        // EN: Farm machines (D-259, D-271, D-273) in GT's tiered pattern: the Apiary watches the bees (sensor) and
        // pumps water, the Mutation Chamber drives mutations with emitters, the Seed Analyzer is a small scanner.
        // PT: Máquinas de fazenda (D-259, D-271, D-273) no padrão por tier do GT: o Apiário observa as abelhas
        // (sensor) e bombeia água, a Câmara de mutação força a mutação com emissores, o Analisador é um scanner
        // pequeno.
        // ZH: 农场机器（D-259、D-271、D-273）采用 GT 分级配方：工业蜂箱用传感器观察蜜蜂并泵水，突变室用发射器驱动突变，
        // 种子分析仪是小型扫描仪。
        for (int tier : GTValues.tiersBetween(GTIABeeMachines.FIRST_TIER, GTValues.UV)) {
            VanillaRecipeHelper.addShapedRecipe(provider, true,
                    GTIAMod.id(GTIABeeMachines.INDUSTRIAL_APIARY[tier].getName()),
                    GTIABeeMachines.INDUSTRIAL_APIARY[tier].asStack(), "GSG", "PMP", "WCW",
                    'G', GTCraftingComponents.GLASS.get(tier),
                    'S', GTCraftingComponents.SENSOR.get(tier),
                    'P', GTCraftingComponents.PUMP.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier));
            VanillaRecipeHelper.addShapedRecipe(provider, true,
                    GTIAMod.id(GTIABeeMachines.MUTATION_CHAMBER[tier].getName()),
                    GTIABeeMachines.MUTATION_CHAMBER[tier].asStack(), "GEG", "CMC", "WPW",
                    'G', GTCraftingComponents.GLASS.get(tier),
                    'E', GTCraftingComponents.EMITTER.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier),
                    'P', GTCraftingComponents.PUMP.get(tier));
        }
        for (int tier : GTValues.tiersBetween(GTValues.LV, GTValues.HV)) {
            VanillaRecipeHelper.addShapedRecipe(provider, true,
                    GTIAMod.id(GTIABeeMachines.SEED_ANALYZER[tier].getName()),
                    GTIABeeMachines.SEED_ANALYZER[tier].asStack(), "SCS", "EMG", "WCW",
                    'S', GTCraftingComponents.SENSOR.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier),
                    'E', GTCraftingComponents.EMITTER.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'G', GTCraftingComponents.GLASS.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier));
        }

        GTIARecipeTypes.FERMENTATION_VAT.recipeBuilder(GTIAMod.id("biomass_from_sapling"))
                .inputItems(ItemTags.SAPLINGS)
                .inputFluids(GTMaterials.Water.getFluid(100))
                .outputFluids(GTMaterials.Biomass.getFluid(100))
                .duration(1600).EUt(7).save(provider);

        GTIARecipeTypes.FERMENTATION_VAT.recipeBuilder(GTIAMod.id("biomass_from_sugar_cane"))
                .inputItems(Items.SUGAR_CANE)
                .inputFluids(GTMaterials.Water.getFluid(20))
                .outputFluids(GTMaterials.Biomass.getFluid(20))
                .duration(320).EUt(7).save(provider);

        addPlantBiomass(provider, "potato", Items.POTATO);
        addPlantBiomass(provider, "carrot", Items.CARROT);
        addPlantBiomass(provider, "cactus", Items.CACTUS);
        addPlantBiomass(provider, "beetroot", Items.BEETROOT);
        addPlantBiomass(provider, "kelp", Items.KELP);
        addPlantBiomass(provider, "sea_pickle", Items.SEA_PICKLE);
        addPlantBiomass(provider, "sweet_berries", Items.SWEET_BERRIES);
        addPlantBiomass(provider, "brown_mushroom", Items.BROWN_MUSHROOM);
        addPlantBiomass(provider, "red_mushroom", Items.RED_MUSHROOM);
        addPlantBiomass(provider, "crimson_fungus", Items.CRIMSON_FUNGUS);
        addPlantBiomass(provider, "warped_fungus", Items.WARPED_FUNGUS);
        addPlantBiomass(provider, "glow_berries", Items.GLOW_BERRIES);
        addPlantBiomass(provider, "pitcher_pod", Items.PITCHER_POD);
        addPlantBiomass(provider, "torchflower_seeds", Items.TORCHFLOWER_SEEDS);

        GTIARecipeTypes.FERMENTATION_VAT.recipeBuilder(GTIAMod.id("biomass_from_bio_chaff"))
                .inputItems(GTItems.BIO_CHAFF)
                .inputFluids(GTMaterials.Water.getFluid(750))
                .outputFluids(GTMaterials.Biomass.getFluid(750))
                .duration(256).EUt(7).save(provider);

        GTIARecipeTypes.FERMENTATION_VAT.recipeBuilder(GTIAMod.id("ethanol_from_biomass"))
                .inputFluids(GTMaterials.Biomass.getFluid(100))
                .outputFluids(GTMaterials.Ethanol.getFluid(50))
                .duration(600).EUt(7).save(provider);

        GTIARecipeTypes.CATALYTIC_DEHYDRATOR.recipeBuilder(GTIAMod.id("ethanol_to_ethylene"))
                .inputFluids(GTMaterials.Ethanol.getFluid(225))
                .outputFluids(GTMaterials.Ethylene.getFluid(225))
                .outputFluids(GTMaterials.Water.getFluid(225))
                .duration(400).EUt(30).save(provider);

        GTIARecipeTypes.CATALYTIC_DEHYDRATOR.recipeBuilder(GTIAMod.id("ethanol_to_ethylene_with_steam"))
                .inputFluids(GTMaterials.Ethanol.getFluid(225))
                .inputFluids(GTMaterials.Steam.getFluid(100))
                .outputFluids(GTMaterials.Ethylene.getFluid(225))
                .outputFluids(GTMaterials.Water.getFluid(225))
                .duration(200).EUt(30).save(provider);
    }

    /** Chemistry slice 01: wood chemistry and lime in Steam, chlor-alkali in LV (GTIA-D-229). */
    public static void addChemistrySlice01(Consumer<FinishedRecipe> provider) {
        // Machines
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("wood_retort"),
                GTIAMachines.WOOD_RETORT.asStack(), "PFP", "FBF", "PFP",
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Bronze),
                'F', GTBlocks.CASING_PRIMITIVE_BRICKS.asStack(),
                'B', new ItemStack(Items.BUCKET));
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("lime_kiln"),
                GTIAMachines.LIME_KILN.asStack(), "PFP", "FUF", "PFP",
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.WroughtIron),
                'F', GTBlocks.CASING_PRIMITIVE_BRICKS.asStack(),
                'U', new ItemStack(Items.FURNACE));
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("lv_still"),
                GTIAMachines.STILL.asStack(), "PGP", "CHC", "PTP",
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Bronze),
                'G', GTItems.GLASS_TUBE.asStack(),
                'C', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Copper),
                'T', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze),
                'H', GTMachines.HULL[GTValues.LV].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("diaphragm_cell"),
                GTIAMachines.DIAPHRAGM_CELL.asStack(), "WCW", "PHP", "WCW",
                'W', new MaterialEntry(TagPrefix.cableGtSingle, GTMaterials.Tin),
                'C', CustomTags.LV_CIRCUITS,
                'P', GTItems.ELECTRIC_PUMP_LV.asStack(),
                'H', GTMachines.HULL[GTValues.LV].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("asbestos_diaphragm"),
                GTIAItems.ASBESTOS_DIAPHRAGM.asStack(), " A ", "ARA", " A ",
                'A', new MaterialEntry(TagPrefix.dust, GTMaterials.Asbestos),
                'R', new MaterialEntry(TagPrefix.rod, GTMaterials.Steel));

        // Wood Retort: dry distillation; about a third of the wood stays as charcoal
        GTIARecipeTypes.WOOD_RETORT.recipeBuilder(GTIAMod.id("logs"))
                .inputItems(ItemTags.LOGS_THAT_BURN, 4)
                .outputItems(new ItemStack(Items.CHARCOAL, 3))
                .outputFluids(GTMaterials.CharcoalByproducts.getFluid(1000))
                .duration(600).save(provider);

        // Still: the circuit picks the cut
        GTIARecipeTypes.STILL.recipeBuilder(GTIAMod.id("charcoal_byproducts"))
                .circuitMeta(1)
                .inputFluids(GTMaterials.CharcoalByproducts.getFluid(1000))
                .outputFluids(GTMaterials.WoodVinegar.getFluid(400))
                .outputFluids(GTMaterials.WoodTar.getFluid(250))
                .duration(200).EUt(30).save(provider);
        GTIARecipeTypes.STILL.recipeBuilder(GTIAMod.id("wood_vinegar"))
                .circuitMeta(2)
                .inputFluids(GTMaterials.WoodVinegar.getFluid(1000))
                .outputFluids(GTMaterials.AceticAcid.getFluid(100))
                .outputFluids(GTMaterials.Methanol.getFluid(40))
                .outputFluids(GTMaterials.Water.getFluid(700))
                .duration(400).EUt(30).save(provider);
        GTIARecipeTypes.STILL.recipeBuilder(GTIAMod.id("wood_tar"))
                .circuitMeta(3)
                .inputFluids(GTMaterials.WoodTar.getFluid(1000))
                .outputFluids(GTMaterials.Creosote.getFluid(400))
                .duration(200).EUt(30).save(provider);

        // Lime Kiln: CaCO3 -> CaO + CO2 (the CO2 is vented)
        addLimeKiln(provider, "charcoal", new ItemStack(Items.CHARCOAL));
        addLimeKiln(provider, "coal", new ItemStack(Items.COAL));
        addLimeKiln(provider, "coke", ChemicalHelper.get(TagPrefix.gem, GTMaterials.Coke));

        // Fluxed steel: lime carries sulfur and silica off into the slag
        GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder(GTIAMod.id("steel_fluxed_charcoal"))
                .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron, 2)
                .inputItems(new ItemStack(Items.CHARCOAL, 2))
                .inputItems(TagPrefix.dust, GTMaterials.Quicklime)
                .outputItems(TagPrefix.ingot, GTMaterials.Steel, 2)
                .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh)
                .duration(1000).save(provider);
        GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder(GTIAMod.id("steel_fluxed_coal"))
                .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron, 2)
                .inputItems(new ItemStack(Items.COAL, 2))
                .inputItems(TagPrefix.dust, GTMaterials.Quicklime)
                .outputItems(TagPrefix.ingot, GTMaterials.Steel, 2)
                .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh)
                .duration(1000).save(provider);

        // Diaphragm Cell: 2 NaCl + 2 H2O -> 2 NaOH + Cl2 + H2, the soda leaving as a salty cell liquor
        GTIARecipeTypes.DIAPHRAGM_CELL.recipeBuilder(GTIAMod.id("chlor_alkali"))
                .inputFluids(GTMaterials.SaltWater.getFluid(4000))
                .chancedInput(Ingredient.of(GTIAItems.ASBESTOS_DIAPHRAGM.get()), 100, 0)
                .outputFluids(GTMaterials.Chlorine.getFluid(2000))
                .outputFluids(GTMaterials.Hydrogen.getFluid(2000))
                .outputFluids(GTIAAlloys.CELL_LIQUOR.getFluid(4000))
                .duration(300).EUt(30).save(provider);
        // The salt crystallises out as the soda concentrates; it goes back to the brine
        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(GTIAMod.id("cell_liquor_separation"))
                .inputFluids(GTIAAlloys.CELL_LIQUOR.getFluid(2000))
                .outputItems(TagPrefix.dust, GTMaterials.SodiumHydroxide)
                .outputItems(TagPrefix.dust, GTMaterials.Salt)
                .outputFluids(GTMaterials.Water.getFluid(1000))
                .duration(150).EUt(30).save(provider);
    }

    /** Chemistry slice 02 (MV): Bayer + Hall-Heroult, Contact, Haber-Bosch and Solvay, the efficient routes. */
    public static void addChemistrySlice02(Consumer<FinishedRecipe> provider) {
        // Machines: an MV hull, MV circuits and what each plant is built around
        plant(provider, GTIAMachines.BAYER_DIGESTER.asStack(), GTItems.ELECTRIC_PUMP_MV.asStack(),
                new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel));
        plant(provider, GTIAMachines.HALL_HEROULT_CELL.asStack(), GTBlocks.CASING_INVAR_HEATPROOF.asStack(),
                new MaterialEntry(TagPrefix.cableGtDouble, GTMaterials.Copper));
        plant(provider, GTIAMachines.CONTACT_CONVERTER.asStack(), GTItems.ELECTRIC_PUMP_MV.asStack(),
                new MaterialEntry(TagPrefix.rotor, GTMaterials.Steel));
        plant(provider, GTIAMachines.AMMONIA_CONVERTER.asStack(), GTItems.ELECTRIC_PISTON_MV.asStack(),
                new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Steel));
        plant(provider, GTIAMachines.SOLVAY_TOWER.asStack(), GTItems.ELECTRIC_PUMP_MV.asStack(),
                new MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Bronze));

        // Bayer: bauxite (Al2O3) digested in hot caustic soda; the red mud is what stays behind
        var digest = GTIARecipeTypes.BAYER_DIGESTER.recipeBuilder(GTIAMod.id("digestion"))
                .circuitMeta(1)
                .inputItems(TagPrefix.dust, GTMaterials.Bauxite)
                .inputItems(TagPrefix.dust, GTMaterials.SodiumHydroxide)
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .inputFluids(GTMaterials.Steam.getFluid(1000))
                .outputFluids(GTIAAlloys.SODIUM_ALUMINATE.getFluid(1000));
        var redMud = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("gtna", "red_mud"));
        if (redMud != null && redMud != Fluids.EMPTY) digest.outputFluids(new FluidStack(redMud, 250));
        digest.duration(300).EUt(120).save(provider);
        // The first seed: aluminium dissolves in hot caustic soda, as in a lab (2 Al + 6 H2O -> 2 Al(OH)3 + 3 H2)
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("aluminium_hydroxide_seed"))
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium)
                .notConsumable(TagPrefix.dust, GTMaterials.SodiumHydroxide)
                .inputFluids(GTMaterials.Water.getFluid(3000))
                .outputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE)
                .outputFluids(GTMaterials.Hydrogen.getFluid(3000))
                .duration(200).EUt(30).save(provider);
        // Seeded precipitation; most of the soda comes back (the rest leaves with the red mud)
        GTIARecipeTypes.BAYER_DIGESTER.recipeBuilder(GTIAMod.id("precipitation"))
                .circuitMeta(2)
                .notConsumable(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE)
                .inputFluids(GTIAAlloys.SODIUM_ALUMINATE.getFluid(1000))
                .outputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE, 2)
                .chancedOutput(TagPrefix.dust, GTMaterials.SodiumHydroxide, 9000, 0)
                .outputFluids(GTMaterials.Water.getFluid(1000))
                .duration(200).EUt(120).save(provider);
        // Calcination in the Lime Kiln: 2 Al(OH)3 -> Al2O3; 2 NaHCO3 -> Na2CO3
        for (String fuel : new String[] { "charcoal", "coal", "coke" }) {
            ItemStack fuelStack = fuelStack(fuel);
            GTIARecipeTypes.LIME_KILN.recipeBuilder(GTIAMod.id("alumina_" + fuel))
                    .inputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE, 2)
                    .inputItems(fuelStack)
                    .outputItems(TagPrefix.dust, GTIAAlloys.ALUMINA)
                    .duration(400).save(provider);
            GTIARecipeTypes.LIME_KILN.recipeBuilder(GTIAMod.id("soda_ash_" + fuel))
                    .inputItems(TagPrefix.dust, GTMaterials.SodiumBicarbonate, 2)
                    .inputItems(fuelStack)
                    .outputItems(TagPrefix.dust, GTMaterials.SodaAsh)
                    .duration(400).save(provider);
        }
        // Cryolite, the molten bath: Al(OH)3 + 3 NaOH + 6 HF
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("cryolite"))
                .inputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE)
                .inputItems(TagPrefix.dust, GTMaterials.SodiumHydroxide, 3)
                .inputFluids(GTMaterials.HydrofluoricAcid.getFluid(6000))
                .outputItems(TagPrefix.dust, GTIAAlloys.CRYOLITE)
                .outputFluids(GTMaterials.Water.getFluid(6000))
                .duration(400).EUt(120).save(provider);
        // Carbon anodes: coke bound with wood tar from the Steam Age
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTIAMod.id("carbon_anode"))
                .inputItems(TagPrefix.dust, GTMaterials.Coke, 4)
                .inputFluids(GTMaterials.WoodTar.getFluid(500))
                .outputItems(GTIAItems.CARBON_ANODE.asStack())
                .duration(200).EUt(30).save(provider);
        // Hall-Heroult: 2 Al2O3 + 3 C -> 4 Al + 3 CO2; the bath is slowly lost
        GTIARecipeTypes.HALL_HEROULT.recipeBuilder(GTIAMod.id("aluminium"))
                .inputItems(TagPrefix.dust, GTIAAlloys.ALUMINA, 2)
                .inputItems(GTIAItems.CARBON_ANODE.asStack())
                .chancedInput(Ingredient.of(ChemicalHelper.get(TagPrefix.dust, GTIAAlloys.CRYOLITE)), 500, 0)
                .outputItems(TagPrefix.ingot, GTMaterials.Aluminium, 4)
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(3000))
                .duration(600).EUt(120).save(provider);

        // Vanadium pentoxide, the Contact catalyst
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("vanadium_pentoxide"))
                .inputItems(TagPrefix.dust, GTMaterials.Vanadium, 2)
                .inputFluids(GTMaterials.Oxygen.getFluid(5000))
                .outputItems(TagPrefix.dust, GTIAAlloys.VANADIUM_PENTOXIDE)
                .duration(200).EUt(30).save(provider);
        // Contact: S + 3 O + H2O -> H2SO4, and the heat leaves as steam
        GTIARecipeTypes.CONTACT_CONVERTER.recipeBuilder(GTIAMod.id("sulfuric_acid"))
                .inputItems(TagPrefix.dust, GTMaterials.Sulfur)
                .notConsumable(TagPrefix.dust, GTIAAlloys.VANADIUM_PENTOXIDE)
                .inputFluids(GTMaterials.Oxygen.getFluid(3000))
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.SulfuricAcid.getFluid(1000))
                .outputFluids(GTMaterials.Steam.getFluid(2000))
                .duration(160).EUt(120).save(provider);
        // Haber-Bosch: N2 + 3 H2 -> 2 NH3 over iron (magnetite); also exothermic
        GTIARecipeTypes.AMMONIA_CONVERTER.recipeBuilder(GTIAMod.id("ammonia"))
                .notConsumable(TagPrefix.dust, GTMaterials.Magnetite)
                .inputFluids(GTMaterials.Nitrogen.getFluid(1000))
                .inputFluids(GTMaterials.Hydrogen.getFluid(3000))
                .outputFluids(GTMaterials.Ammonia.getFluid(1000))
                .outputFluids(GTMaterials.Steam.getFluid(500))
                .duration(400).EUt(120).save(provider);
        // Solvay: NaCl + NH3 + CO2 + H2O -> NaHCO3 + NH4Cl (GTCEu turns 2 NH4Cl back into 1000 mB of ammonia with lime)
        GTIARecipeTypes.SOLVAY_TOWER.recipeBuilder(GTIAMod.id("sodium_bicarbonate"))
                .inputFluids(GTMaterials.SaltWater.getFluid(1000))
                .inputFluids(GTMaterials.Ammonia.getFluid(1000))
                .inputFluids(GTMaterials.CarbonDioxide.getFluid(1000))
                .outputItems(TagPrefix.dust, GTMaterials.SodiumBicarbonate)
                .outputItems(TagPrefix.dust, GTMaterials.AmmoniumChloride, 2)
                .duration(200).EUt(120).save(provider);
    }

    private static void plant(Consumer<FinishedRecipe> provider, ItemStack controller, Object core, Object part) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(controller.getItem()).getPath()),
                controller, "PCP", "KHK", "PCP",
                'P', part,
                'C', CustomTags.MV_CIRCUITS,
                'K', core,
                'H', GTMachines.HULL[GTValues.MV].asStack());
    }

    private static ItemStack fuelStack(String fuel) {
        return switch (fuel) {
            case "charcoal" -> new ItemStack(Items.CHARCOAL);
            case "coal" -> new ItemStack(Items.COAL);
            default -> ChemicalHelper.get(TagPrefix.gem, GTMaterials.Coke);
        };
    }

    private static void addLimeKiln(Consumer<FinishedRecipe> provider, String fuel, ItemStack fuelStack) {
        GTIARecipeTypes.LIME_KILN.recipeBuilder(GTIAMod.id("quicklime_" + fuel))
                .inputItems(TagPrefix.dust, GTMaterials.Calcite, 4)
                .inputItems(fuelStack)
                .outputItems(TagPrefix.dust, GTMaterials.Quicklime, 4)
                .duration(400).save(provider);
    }

    private static void addPlantBiomass(Consumer<FinishedRecipe> provider, String name, ItemLike plant) {
        GTIARecipeTypes.FERMENTATION_VAT.recipeBuilder(GTIAMod.id("biomass_from_" + name))
                .inputItems(plant)
                .inputFluids(GTMaterials.Water.getFluid(20))
                .outputFluids(GTMaterials.Biomass.getFluid(20))
                .duration(320).EUt(7).save(provider);
    }
}
