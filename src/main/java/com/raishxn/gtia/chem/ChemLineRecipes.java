package com.raishxn.gtia.chem;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;
import com.raishxn.gtia.data.GTIAItems;

import java.util.function.Consumer;

/**
 * The Steam to MV chemistry lines (GTIA-D-229) as recipes in GTCEu machines, with no machine of their own
 * (GTIA-D-335): wood and lime in the Coke Oven, wood cuts in the Distillery, chlor-alkali in the Electrolyzer,
 * Bayer, Contact, Haber-Bosch and Solvay in the Chemical Reactor, Hall-Heroult in the Electric Blast Furnace.
 */
public final class ChemLineRecipes {

    private static final int LV = 30;
    private static final int MV = 120;

    private ChemLineRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        steam(provider);
        lv(provider);
        mv(provider);
    }

    private static void steam(Consumer<FinishedRecipe> provider) {
        // EN: Dry distillation of wood: about a third of the wood stays as charcoal. Replaces GTCEu's log → charcoal.
        // PT: Destilação seca da madeira: cerca de um terço fica como carvão. Substitui o tora → carvão do GTCEu.
        // ZH: 木材干馏：约三分之一的木材留作木炭。取代 GTCEu 的原木 → 木炭配方。
        GTRecipeTypes.COKE_OVEN_RECIPES.recipeBuilder(GTIAMod.id("wood_retort_logs"))
                .inputItems(ItemTags.LOGS_THAT_BURN, 4)
                .outputItems(new ItemStack(Items.CHARCOAL, 3))
                .outputFluids(GTMaterials.CharcoalByproducts.getFluid(1000))
                .duration(600).save(provider);
        // CaCO3 -> CaO + CO2 (the CO2 is vented)
        GTRecipeTypes.COKE_OVEN_RECIPES.recipeBuilder(GTIAMod.id("quicklime"))
                .inputItems(TagPrefix.dust, GTMaterials.Calcite, 4)
                .outputItems(TagPrefix.dust, GTMaterials.Quicklime, 4)
                .duration(400).save(provider);

        // Fluxed steel: lime carries sulfur and silica off into the slag
        for (var fuel : new ItemStack[] { new ItemStack(Items.CHARCOAL, 2), new ItemStack(Items.COAL, 2) }) {
            String name = fuel.is(Items.CHARCOAL) ? "charcoal" : "coal";
            GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder(GTIAMod.id("steel_fluxed_" + name))
                    .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron, 2)
                    .inputItems(fuel)
                    .inputItems(TagPrefix.dust, GTMaterials.Quicklime)
                    .outputItems(TagPrefix.ingot, GTMaterials.Steel, 2)
                    .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh)
                    .duration(1000).save(provider);
        }
    }

    private static void lv(Consumer<FinishedRecipe> provider) {
        // EN: LV wood cuts, one fraction per circuit (11+, clear of the MV cuts GTCEu derives from its tower).
        // PT: Cortes da madeira no LV, uma fração por circuito (11+, fora dos cortes MV que o GTCEu tira da torre).
        // ZH: LV 木材馏分，每个电路一种馏分（11 起，避开 GTCEu 从蒸馏塔派生的 MV 馏分）。
        distill(provider, "charcoal_byproducts_tar", 11, GTMaterials.CharcoalByproducts.getFluid(1000),
                GTMaterials.WoodTar.getFluid(250));
        distill(provider, "charcoal_byproducts_vinegar", 12, GTMaterials.CharcoalByproducts.getFluid(1000),
                GTMaterials.WoodVinegar.getFluid(400));
        distill(provider, "wood_vinegar_acetic_acid", 11, GTMaterials.WoodVinegar.getFluid(1000),
                GTMaterials.AceticAcid.getFluid(100));
        distill(provider, "wood_vinegar_methanol", 12, GTMaterials.WoodVinegar.getFluid(1000),
                GTMaterials.Methanol.getFluid(40));
        distill(provider, "wood_tar_creosote", 11, GTMaterials.WoodTar.getFluid(1000),
                GTMaterials.Creosote.getFluid(400));

        // EN: C6H12O6 -> 2 C2H5OH + 2 CO2: the LV ethanol (GTCEu's own comes from MV distillation).
        // PT: C6H12O6 -> 2 C2H5OH + 2 CO2: o etanol do LV (o do GTCEu sai da destilação MV).
        // ZH: C6H12O6 -> 2 C2H5OH + 2 CO2：LV 阶段的乙醇（GTCEu 自带的来自 MV 蒸馏）。
        GTRecipeTypes.FERMENTING_RECIPES.recipeBuilder(GTIAMod.id("ethanol_from_sugar"))
                .inputItems(new ItemStack(Items.SUGAR, 2))
                .inputFluids(GTMaterials.Water.getFluid(100))
                .outputFluids(GTMaterials.Ethanol.getFluid(50))
                .duration(300).EUt(7).save(provider);

        // Catalytic dehydration: C2H5OH -> C2H4 + H2O; steam in the feed halves the time
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("ethanol_to_ethylene"))
                .circuitMeta(1)
                .inputFluids(GTMaterials.Ethanol.getFluid(225))
                .outputFluids(GTMaterials.Ethylene.getFluid(225), GTMaterials.Water.getFluid(225))
                .duration(400).EUt(LV).save(provider);
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("ethanol_to_ethylene_with_steam"))
                .circuitMeta(2)
                .inputFluids(GTMaterials.Ethanol.getFluid(225), GTMaterials.Steam.getFluid(100))
                .outputFluids(GTMaterials.Ethylene.getFluid(225), GTMaterials.Water.getFluid(225))
                .duration(200).EUt(LV).save(provider);

        // Chlor-alkali: 2 NaCl + 2 H2O -> 2 NaOH + Cl2 + H2, the soda leaving as a salty cell liquor
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("asbestos_diaphragm"),
                GTIAItems.ASBESTOS_DIAPHRAGM.asStack(), " A ", "ARA", " A ",
                'A', new MaterialEntry(TagPrefix.dust, GTMaterials.Asbestos),
                'R', new MaterialEntry(TagPrefix.rod, GTMaterials.Steel));
        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(GTIAMod.id("chlor_alkali"))
                .inputFluids(GTMaterials.SaltWater.getFluid(4000))
                .chancedInput(Ingredient.of(GTIAItems.ASBESTOS_DIAPHRAGM.get()), 100, 0)
                .outputFluids(GTMaterials.Chlorine.getFluid(2000), GTMaterials.Hydrogen.getFluid(2000),
                        GTIAAlloys.CELL_LIQUOR.getFluid(4000))
                .duration(300).EUt(LV).save(provider);
        // The salt crystallises out as the soda concentrates; it goes back to the brine
        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(GTIAMod.id("cell_liquor_separation"))
                .inputFluids(GTIAAlloys.CELL_LIQUOR.getFluid(2000))
                .outputItems(TagPrefix.dust, GTMaterials.SodiumHydroxide)
                .outputItems(TagPrefix.dust, GTMaterials.Salt)
                .outputFluids(GTMaterials.Water.getFluid(1000))
                .duration(150).EUt(LV).save(provider);
    }

    private static void mv(Consumer<FinishedRecipe> provider) {
        // Bayer: bauxite (Al2O3) digested in hot caustic soda; the red mud is what stays behind
        var digest = GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("bayer_digestion"))
                .inputItems(TagPrefix.dust, GTMaterials.Bauxite)
                .inputItems(TagPrefix.dust, GTMaterials.SodiumHydroxide)
                .inputFluids(GTMaterials.Water.getFluid(1000), GTMaterials.Steam.getFluid(1000))
                .outputFluids(GTIAAlloys.SODIUM_ALUMINATE.getFluid(1000));
        var redMud = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("gtna", "red_mud"));
        if (redMud != null && redMud != Fluids.EMPTY) digest.outputFluids(new FluidStack(redMud, 250));
        digest.duration(300).EUt(MV).save(provider);
        // The first seed: aluminium dissolves in hot caustic soda, as in a lab (2 Al + 6 H2O -> 2 Al(OH)3 + 3 H2)
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("aluminium_hydroxide_seed"))
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium)
                .notConsumable(TagPrefix.dust, GTMaterials.SodiumHydroxide)
                .inputFluids(GTMaterials.Water.getFluid(3000))
                .outputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE)
                .outputFluids(GTMaterials.Hydrogen.getFluid(3000))
                .duration(200).EUt(LV).save(provider);
        // Seeded precipitation; most of the soda comes back (the rest leaves with the red mud)
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("bayer_precipitation"))
                .notConsumable(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE)
                .inputFluids(GTIAAlloys.SODIUM_ALUMINATE.getFluid(1000))
                .outputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE, 2)
                .chancedOutput(TagPrefix.dust, GTMaterials.SodiumHydroxide, 9000, 0)
                .outputFluids(GTMaterials.Water.getFluid(1000))
                .duration(200).EUt(MV).save(provider);
        // Calcination: 2 Al(OH)3 -> Al2O3 + 3 H2O; 2 NaHCO3 -> Na2CO3 + H2O + CO2
        GTRecipeTypes.COKE_OVEN_RECIPES.recipeBuilder(GTIAMod.id("alumina"))
                .inputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE, 2)
                .outputItems(TagPrefix.dust, GTIAAlloys.ALUMINA)
                .duration(400).save(provider);
        GTRecipeTypes.COKE_OVEN_RECIPES.recipeBuilder(GTIAMod.id("soda_ash"))
                .inputItems(TagPrefix.dust, GTMaterials.SodiumBicarbonate, 2)
                .outputItems(TagPrefix.dust, GTMaterials.SodaAsh)
                .duration(400).save(provider);
        // Cryolite, the molten bath: Al(OH)3 + 3 NaOH + 6 HF
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("cryolite"))
                .inputItems(TagPrefix.dust, GTIAAlloys.ALUMINIUM_HYDROXIDE)
                .inputItems(TagPrefix.dust, GTMaterials.SodiumHydroxide, 3)
                .inputFluids(GTMaterials.HydrofluoricAcid.getFluid(6000))
                .outputItems(TagPrefix.dust, GTIAAlloys.CRYOLITE)
                .outputFluids(GTMaterials.Water.getFluid(6000))
                .duration(400).EUt(MV).save(provider);
        // Carbon anodes: coke bound with wood tar from the Steam Age
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTIAMod.id("carbon_anode"))
                .inputItems(TagPrefix.dust, GTMaterials.Coke, 4)
                .inputFluids(GTMaterials.WoodTar.getFluid(500))
                .outputItems(GTIAItems.CARBON_ANODE.asStack())
                .duration(200).EUt(LV).save(provider);
        // EN: Hall-Heroult: 2 Al2O3 + 3 C -> 4 Al + 3 CO2 in molten cryolite at 960 °C (1,233 K); the bath is slowly
        // lost.
        // PT: Hall-Héroult: 2 Al2O3 + 3 C -> 4 Al + 3 CO2 em criolita fundida a 960 °C (1.233 K); o banho se perde aos
        // poucos.
        // ZH: 霍尔-埃鲁法：2 Al2O3 + 3 C -> 4 Al + 3 CO2，于 960 °C（1233 K）熔融冰晶石中进行；电解浴缓慢损耗。
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTIAMod.id("hall_heroult_aluminium"))
                .inputItems(TagPrefix.dust, GTIAAlloys.ALUMINA, 2)
                .inputItems(GTIAItems.CARBON_ANODE.asStack())
                .chancedInput(Ingredient.of(ChemicalHelper.get(TagPrefix.dust, GTIAAlloys.CRYOLITE)), 500, 0)
                .outputItems(TagPrefix.ingot, GTMaterials.Aluminium, 4)
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(3000))
                .blastFurnaceTemp(1233).duration(600).EUt(MV).save(provider);

        // Vanadium pentoxide, the Contact catalyst
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("vanadium_pentoxide"))
                .inputItems(TagPrefix.dust, GTMaterials.Vanadium, 2)
                .inputFluids(GTMaterials.Oxygen.getFluid(5000))
                .outputItems(TagPrefix.dust, GTIAAlloys.VANADIUM_PENTOXIDE)
                .duration(200).EUt(LV).save(provider);
        // Contact: S + 3 O + H2O -> H2SO4, and the heat leaves as steam
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("contact_sulfuric_acid"))
                .inputItems(TagPrefix.dust, GTMaterials.Sulfur)
                .notConsumable(TagPrefix.dust, GTIAAlloys.VANADIUM_PENTOXIDE)
                .inputFluids(GTMaterials.Oxygen.getFluid(3000), GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.SulfuricAcid.getFluid(1000), ChemRecipes.steam(2000))
                .duration(160).EUt(MV).save(provider);
        // Haber-Bosch: N2 + 3 H2 -> 2 NH3 over iron (magnetite); also exothermic
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("haber_bosch_ammonia"))
                .notConsumable(TagPrefix.dust, GTMaterials.Magnetite)
                .inputFluids(GTMaterials.Nitrogen.getFluid(1000), GTMaterials.Hydrogen.getFluid(3000))
                .outputFluids(GTMaterials.Ammonia.getFluid(1000), ChemRecipes.steam(500))
                .duration(400).EUt(MV).save(provider);
        // Solvay: NaCl + NH3 + CO2 + H2O -> NaHCO3 + NH4Cl (GTCEu turns 2 NH4Cl back into 1000 mB of ammonia with lime)
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("solvay_sodium_bicarbonate"))
                .inputFluids(GTMaterials.SaltWater.getFluid(1000), GTMaterials.Ammonia.getFluid(1000),
                        GTMaterials.CarbonDioxide.getFluid(1000))
                .outputItems(TagPrefix.dust, GTMaterials.SodiumBicarbonate)
                .outputItems(TagPrefix.dust, GTMaterials.AmmoniumChloride, 2)
                .duration(200).EUt(MV).save(provider);
    }

    private static void distill(Consumer<FinishedRecipe> provider, String name, int circuit, FluidStack in,
                                FluidStack out) {
        GTRecipeTypes.DISTILLERY_RECIPES.recipeBuilder(GTIAMod.id(name))
                .circuitMeta(circuit)
                .inputFluids(in)
                .outputFluids(out)
                .duration(200).EUt(LV).save(provider);
    }
}
