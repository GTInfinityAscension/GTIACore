package com.raishxn.gtia.chem;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.WorldRules;

import java.util.function.Consumer;

/** Recipes of the Ostwald, Claus, Kroll and Siemens lines (GTIA-D-281), with real stoichiometry. */
public final class ChemRecipes {

    private static final int MV = GTValues.VA[GTValues.MV];
    private static final int HV = GTValues.VA[GTValues.HV];

    private ChemRecipes() {}

    /** A catalyst: kept, or worn out now and then under the catalyst wear rule (GTIA-D-284). */
    static GTRecipeBuilder catalyst(GTRecipeBuilder builder, TagPrefix prefix, Material material) {
        int wear = (int) WorldRules.get(GTIARules.CATALYST_WEAR);
        if (wear <= 0) return builder.notConsumable(prefix, material);
        return builder.chancedInput(ChemicalHelper.get(prefix, material), wear, 0);
    }

    /** Steam recovered from a plant's heat, scaled by the heat recovery rule. */
    static FluidStack steam(int amount) {
        return GTMaterials.Steam
                .getFluid(Math.max(0, (int) Math.round(amount * WorldRules.get(GTIARules.HEAT_RECOVERY))));
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        controllers(provider);
        ostwald(provider);
        claus(provider);
        kroll(provider);
        siemens(provider);
    }

    private static void controllers(Consumer<FinishedRecipe> provider) {
        plant(provider, ChemPlants.OSTWALD_CONVERTER.asStack(), GTValues.MV, CustomTags.MV_CIRCUITS,
                GTItems.ELECTRIC_PUMP_MV.asStack(), new MaterialEntry(TagPrefix.wireFine, GTMaterials.Cobalt));
        plant(provider, ChemPlants.CLAUS_UNIT.asStack(), GTValues.HV, CustomTags.HV_CIRCUITS,
                GTItems.ELECTRIC_PUMP_HV.asStack(), new MaterialEntry(TagPrefix.pipeNormalFluid,
                        GTMaterials.StainlessSteel));
        plant(provider, ChemPlants.KROLL_REACTOR.asStack(), GTValues.HV, CustomTags.HV_CIRCUITS,
                GTItems.ELECTRIC_PISTON_HV.asStack(), new MaterialEntry(TagPrefix.plateDouble,
                        GTMaterials.StainlessSteel));
        plant(provider, ChemPlants.SIEMENS_REACTOR.asStack(), GTValues.HV, CustomTags.HV_CIRCUITS,
                GTItems.ELECTRIC_PUMP_HV.asStack(), new MaterialEntry(TagPrefix.cableGtDouble, GTMaterials.Gold));
    }

    private static void ostwald(Consumer<FinishedRecipe> provider) {
        // EN: 4 NH3 + 5 O2 -> 4 NO + 6 H2O over a cobalt oxide gauze; the burner's heat leaves as steam.
        // PT: 4 NH3 + 5 O2 -> 4 NO + 6 H2O sobre tela de óxido de cobalto; o calor do queimador sai como vapor.
        // ZH: 4 NH3 + 5 O2 -> 4 NO + 6 H2O，经氧化钴网催化；燃烧器的热量以蒸汽形式输出。
        catalyst(ChemRecipeTypes.OSTWALD_CONVERTER.recipeBuilder(GTIAMod.id("ostwald/ammonia_oxidation")),
                TagPrefix.dust, GTMaterials.CobaltOxide)
                .circuitMeta(1)

                .inputFluids(GTMaterials.Ammonia.getFluid(1000), GTMaterials.Oxygen.getFluid(1250))
                .outputFluids(GTMaterials.NitricOxide.getFluid(1000), GTMaterials.Water.getFluid(1500),
                        steam(1000))
                .duration(200).EUt(MV).save(provider);
        // EN: The HV route: the classic platinum gauze, after the Moon's platinum. Faster, and more heat recovered.
        // PT: A rota HV: a tela de platina clássica, depois da platina da Lua. Mais rápida e recupera mais calor.
        // ZH: HV 路线：经典铂网，需月球铂。更快，回收更多热量。
        catalyst(ChemRecipeTypes.OSTWALD_CONVERTER.recipeBuilder(GTIAMod.id("ostwald/ammonia_oxidation_platinum")),
                TagPrefix.wireFine, GTMaterials.Platinum)
                .circuitMeta(4)

                .inputFluids(GTMaterials.Ammonia.getFluid(1000), GTMaterials.Oxygen.getFluid(1250))
                .outputFluids(GTMaterials.NitricOxide.getFluid(1000), GTMaterials.Water.getFluid(1500),
                        steam(2000))
                .duration(80).EUt(HV).save(provider);
        ChemRecipeTypes.OSTWALD_CONVERTER.recipeBuilder(GTIAMod.id("ostwald/nitric_oxide_oxidation"))
                .circuitMeta(2)
                .inputFluids(GTMaterials.NitricOxide.getFluid(1000), GTMaterials.Oxygen.getFluid(500))
                .outputFluids(GTMaterials.NitrogenDioxide.getFluid(1000))
                .duration(100).EUt(MV).save(provider);
        // EN: 3 NO2 + H2O -> 2 HNO3 + NO: a third of the gas comes back for circuit 2, the loop of every real plant.
        // PT: 3 NO2 + H2O -> 2 HNO3 + NO: um terço do gás volta para o circuito 2, o laço de toda planta real.
        // ZH: 3 NO2 + H2O -> 2 HNO3 + NO：三分之一的气体回到电路 2，真实工厂都有这个循环。
        ChemRecipeTypes.OSTWALD_CONVERTER.recipeBuilder(GTIAMod.id("ostwald/absorption"))
                .circuitMeta(3)
                .inputFluids(GTMaterials.NitrogenDioxide.getFluid(3000), GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.NitricAcid.getFluid(2000), GTMaterials.NitricOxide.getFluid(1000))
                .duration(200).EUt(MV).save(provider);
    }

    private static void claus(Consumer<FinishedRecipe> provider) {
        // EN: 3 H2S + 3/2 O2 -> 3 S + 3 H2O: a third burnt to SO2, the rest reacts with it over alumina (Bayer).
        // PT: 3 H2S + 3/2 O2 -> 3 S + 3 H2O: um terço queima a SO2 e o resto reage com ele sobre alumina (Bayer).
        // ZH: 3 H2S + 3/2 O2 -> 3 S + 3 H2O：三分之一燃烧成 SO2，其余在氧化铝（拜耳法）上与之反应。
        catalyst(ChemRecipeTypes.CLAUS_UNIT.recipeBuilder(GTIAMod.id("claus/sulfur_recovery")), TagPrefix.dust,
                GTIAAlloys.ALUMINA)
                .circuitMeta(1)

                .inputFluids(GTMaterials.HydrogenSulfide.getFluid(3000), GTMaterials.Oxygen.getFluid(1500))
                .outputItems(TagPrefix.dust, GTMaterials.Sulfur, 3)
                .outputFluids(GTMaterials.Water.getFluid(3000), steam(2000))
                .duration(160).EUt(HV).save(provider);
        catalyst(ChemRecipeTypes.CLAUS_UNIT.recipeBuilder(GTIAMod.id("claus/sulfur_dioxide")), TagPrefix.dust,
                GTIAAlloys.ALUMINA)
                .circuitMeta(2)

                .inputFluids(GTMaterials.HydrogenSulfide.getFluid(2000), GTMaterials.SulfurDioxide.getFluid(1000))
                .outputItems(TagPrefix.dust, GTMaterials.Sulfur, 3)
                .outputFluids(GTMaterials.Water.getFluid(2000))
                .duration(120).EUt(HV).save(provider);
    }

    private static void kroll(Consumer<FinishedRecipe> provider) {
        // EN: 2 FeTiO3 + 7 Cl2 + 6 C -> 2 TiCl4 + 2 FeCl3 + 6 CO: the Moon's ilmenite, chlorinated in a fluid bed.
        // PT: 2 FeTiO3 + 7 Cl2 + 6 C -> 2 TiCl4 + 2 FeCl3 + 6 CO: a ilmenita da Lua, clorada em leito fluidizado.
        // ZH: 2 FeTiO3 + 7 Cl2 + 6 C -> 2 TiCl4 + 2 FeCl3 + 6 CO：月球钛铁矿在流化床中氯化。
        ChemRecipeTypes.KROLL_REACTOR.recipeBuilder(GTIAMod.id("kroll/ilmenite_chlorination"))
                .circuitMeta(1)
                .inputItems(TagPrefix.dust, GTMaterials.Ilmenite, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon, 6)
                .inputFluids(GTMaterials.Chlorine.getFluid(7000))
                .outputFluids(GTMaterials.TitaniumTetrachloride.getFluid(2000),
                        GTMaterials.Iron3Chloride.getFluid(2000), GTMaterials.CarbonMonoxide.getFluid(6000))
                .duration(400).EUt(HV).save(provider);
        ChemRecipeTypes.KROLL_REACTOR.recipeBuilder(GTIAMod.id("kroll/rutile_chlorination"))
                .circuitMeta(2)
                .inputItems(TagPrefix.dust, GTMaterials.Rutile)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon, 2)
                .inputFluids(GTMaterials.Chlorine.getFluid(4000))
                .outputFluids(GTMaterials.TitaniumTetrachloride.getFluid(1000),
                        GTMaterials.CarbonMonoxide.getFluid(2000))
                .duration(200).EUt(HV).save(provider);
        // EN: TiCl4 + 2 Mg -> Ti + 2 MgCl2 under argon; the electrolyzer turns the MgCl2 back into Mg and Cl2.
        // PT: TiCl4 + 2 Mg -> Ti + 2 MgCl2 sob argônio; o eletrolisador devolve o MgCl2 em Mg e Cl2.
        // ZH: TiCl4 + 2 Mg -> Ti + 2 MgCl2，在氩气中进行；电解机把 MgCl2 还原为 Mg 和 Cl2。
        ChemRecipeTypes.KROLL_REACTOR.recipeBuilder(GTIAMod.id("kroll/magnesium_reduction"))
                .circuitMeta(3)
                .inputItems(TagPrefix.dust, GTMaterials.Magnesium, 2)
                .inputFluids(GTMaterials.TitaniumTetrachloride.getFluid(1000), GTMaterials.Argon.getFluid(50))
                .outputItems(TagPrefix.dust, GTMaterials.Titanium)
                .outputItems(TagPrefix.dust, GTMaterials.MagnesiumChloride, 2)
                .duration(400).EUt(HV).save(provider);
    }

    private static void siemens(Consumer<FinishedRecipe> provider) {
        // EN: Si + 3 HCl -> SiHCl3 + H2, then SiHCl3 + H2 -> Si + 3 HCl on hot rods: the HCl goes round and round.
        // PT: Si + 3 HCl -> SiHCl3 + H2, depois SiHCl3 + H2 -> Si + 3 HCl em barras quentes: o HCl gira em círculo.
        // ZH: Si + 3 HCl -> SiHCl3 + H2，再在热棒上 SiHCl3 + H2 -> Si + 3 HCl：HCl 循环使用。
        ChemRecipeTypes.SIEMENS_REACTOR.recipeBuilder(GTIAMod.id("siemens/hydrochlorination"))
                .circuitMeta(1)
                .inputItems(TagPrefix.dust, GTMaterials.Silicon)
                .inputFluids(GTMaterials.HydrochloricAcid.getFluid(3000))
                .outputFluids(ChemMaterials.TRICHLOROSILANE.getFluid(1000), GTMaterials.Hydrogen.getFluid(1000))
                .duration(200).EUt(HV).save(provider);
        ChemRecipeTypes.SIEMENS_REACTOR.recipeBuilder(GTIAMod.id("siemens/deposition"))
                .circuitMeta(2)
                .inputFluids(ChemMaterials.TRICHLOROSILANE.getFluid(1000), GTMaterials.Hydrogen.getFluid(1000))
                .outputItems(TagPrefix.dust, ChemMaterials.POLYSILICON)
                .outputFluids(GTMaterials.HydrochloricAcid.getFluid(3000))
                .duration(300).EUt(HV).save(provider);
        // EN: Czochralski with ultrapure silicon: half the silicon and half the time of the GTCEu boule.
        // PT: Czochralski com silício ultrapuro: metade do silício e do tempo do boule do GTCEu.
        // ZH: 用超纯硅进行直拉法：硅用量和时间都是 GTCEu 单晶锭的一半。
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTIAMod.id("czochralski/silicon_boule"))
                .circuitMeta(3)
                .inputItems(TagPrefix.dust, ChemMaterials.POLYSILICON, 16)
                .inputItems(TagPrefix.dustSmall, GTMaterials.GalliumArsenide)
                .outputItems(GTItems.SILICON_BOULE.asStack())
                .blastFurnaceTemp(1784).duration(4500).EUt(MV).save(provider);
    }

    private static void plant(Consumer<FinishedRecipe> provider, ItemStack controller, int tier, TagKey<Item> circuit,
                              ItemStack core, MaterialEntry part) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(controller.getItem()).getPath()),
                controller, "PCP", "KHK", "PCP",
                'P', part, 'C', circuit, 'K', core, 'H', GTMachines.HULL[tier].asStack());
    }
}
