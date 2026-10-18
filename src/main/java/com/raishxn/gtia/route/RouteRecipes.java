package com.raishxn.gtia.route;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;

import java.util.function.Consumer;

/**
 * EN: Block 1 routes: the Integrated Ore Processor on the IV Assembly Line (GTIA-D-303), pig iron and oxygen steel
 * (GTIA-D-306) and three aluminium routes (GTIA-D-308). No new machine: PBF, EBF, Arc Furnace, Compressor, Mixer.
 * PT: Rotas do Bloco 1: Integrated Ore Processor no Assembly Line IV (GTIA-D-303), gusa e aço a oxigênio
 * (GTIA-D-306) e três rotas de alumínio (GTIA-D-308). Nenhuma máquina nova: PBF, EBF, Arc Furnace, Compressor, Mixer.
 * ZH: 第一阶段路线: IV 装配线上的集成矿石处理厂 (GTIA-D-303), 生铁与吹氧炼钢 (GTIA-D-306), 三条铝路线
 * (GTIA-D-308)。不加新机器: 土高炉、电力高炉、电弧炉、压缩机、搅拌机。
 */
public final class RouteRecipes {

    private static final int LV = GTValues.VA[GTValues.LV];
    private static final int MV = GTValues.VA[GTValues.MV];
    private static final int HV = GTValues.VA[GTValues.HV];

    private RouteRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        oreProcessor(provider);
        steel(provider);
        aluminium(provider);
        ElectrolyzerRebalance.addRecipes(provider);
    }

    private static void oreProcessor(Consumer<FinishedRecipe> provider) {
        // GTNA is not loaded in datagen.
        var controller = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtna", "integrated_ore_processor"));
        if (controller == null || controller == Items.AIR) return;
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTIAMod.id("integrated_ore_processor"))
                .inputItems(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.asStack(4))
                .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel, 4)
                .inputItems(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.asStack(2))
                .inputItems(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.asStack(2))
                .inputItems(GTItems.ELECTRIC_MOTOR_IV.asStack(4))
                .inputItems(GTItems.ELECTRIC_PUMP_IV.asStack(2))
                .inputItems(GTItems.CONVEYOR_MODULE_IV.asStack(2))
                .inputItems(CustomTags.IV_CIRCUITS, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(new ItemStack(controller))
                .duration(600).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }

    private static void steel(Consumer<FinishedRecipe> provider) {
        // EN: Fe2O3/Fe3O4 + C, limestone as flux: pig iron and slag. Oxygen then burns the carbon out of the pig iron.
        // PT: Fe2O3/Fe3O4 + C, calcário como fundente: gusa e escória. Depois o oxigênio queima o carbono da gusa.
        // ZH: Fe2O3/Fe3O4 + C, 石灰石作熔剂: 生铁和炉渣。随后用氧气烧掉生铁中的碳。
        for (Material ore : new Material[] { GTMaterials.Hematite, GTMaterials.Magnetite }) {
            String name = ore.getName();
            GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder(GTIAMod.id("pig_iron_" + name + "_coke"))
                    .inputItems(TagPrefix.dust, ore, 2)
                    .inputItems(TagPrefix.gem, GTMaterials.Coke)
                    .inputItems(TagPrefix.dust, GTMaterials.Calcite)
                    .outputItems(TagPrefix.ingot, RouteMaterials.PIG_IRON, 2)
                    .outputItems(TagPrefix.dust, RouteMaterials.SLAG)
                    .duration(1000).save(provider);
            GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder(GTIAMod.id("pig_iron_" + name + "_charcoal"))
                    .inputItems(TagPrefix.dust, ore, 2)
                    .inputItems(TagPrefix.gem, GTMaterials.Charcoal, 2)
                    .inputItems(TagPrefix.dust, GTMaterials.Calcite)
                    .outputItems(TagPrefix.ingot, RouteMaterials.PIG_IRON, 2)
                    .outputItems(TagPrefix.dust, RouteMaterials.SLAG)
                    .duration(1200).save(provider);
            GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTIAMod.id("pig_iron_" + name))
                    .inputItems(TagPrefix.dust, ore, 2)
                    .inputItems(TagPrefix.gem, GTMaterials.Coke)
                    .inputItems(TagPrefix.dust, GTMaterials.Calcite)
                    .outputItems(TagPrefix.ingot, RouteMaterials.PIG_IRON, 2)
                    .outputItems(TagPrefix.dust, RouteMaterials.SLAG)
                    .outputFluids(GTMaterials.CarbonMonoxide.getFluid(1000))
                    .blastFurnaceTemp(1200).duration(240).EUt(MV).save(provider);
        }
        GTRecipeTypes.ARC_FURNACE_RECIPES.recipeBuilder(GTIAMod.id("steel_from_pig_iron"))
                .inputItems(TagPrefix.ingot, RouteMaterials.PIG_IRON)
                .inputFluids(GTMaterials.Oxygen.getFluid(100))
                .outputItems(TagPrefix.ingot, GTMaterials.Steel)
                .duration(100).EUt(LV).save(provider);
        // Slag cement: ground slag in place of stone dust.
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTIAMod.id("concrete_from_slag"))
                .inputItems(TagPrefix.dust, RouteMaterials.SLAG, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Calcite)
                .inputItems(TagPrefix.dust, GTMaterials.Gypsum)
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.Concrete.getFluid(1152))
                .duration(40).EUt(16).save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTIAMod.id("concrete_from_slag_clay"))
                .inputItems(TagPrefix.dust, GTMaterials.Clay)
                .inputItems(TagPrefix.dust, RouteMaterials.SLAG, 3)
                .inputFluids(GTMaterials.Water.getFluid(500))
                .outputFluids(GTMaterials.Concrete.getFluid(576))
                .duration(20).EUt(16).save(provider);
    }

    private static void aluminium(Consumer<FinishedRecipe> provider) {
        // HV: the GTCEu dust smelt, now at 480 EU/t (same time and heat).
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTIAMod.id("aluminium_from_dust"))
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium)
                .outputItems(TagPrefix.ingot, GTMaterials.Aluminium)
                .blastFurnaceTemp(1700).duration(884).EUt(HV).save(provider);
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTIAMod.id("aluminium_from_dust_nitrogen"))
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium)
                .inputFluids(GTMaterials.Nitrogen.getFluid(1000))
                .outputItems(TagPrefix.ingot, GTMaterials.Aluminium)
                .blastFurnaceTemp(1700).duration(592).EUt(HV).save(provider);
        // MV, slow: pressed dust melts at a lower heat.
        GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder(GTIAMod.id("crude_aluminium"))
                .inputItems(TagPrefix.dust, GTMaterials.Aluminium)
                .outputItems(TagPrefix.ingot, RouteMaterials.CRUDE_ALUMINIUM)
                .duration(300).EUt(2).save(provider);
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTIAMod.id("aluminium_from_crude"))
                .inputItems(TagPrefix.ingot, RouteMaterials.CRUDE_ALUMINIUM)
                .outputItems(TagPrefix.ingot, GTMaterials.Aluminium)
                .blastFurnaceTemp(1300).duration(1500).EUt(MV).save(provider);
        // EN: MV, chemical: Al2O3 + 3 C -> 2 Al + 3 CO, but half the aluminium is lost as carbide; the Hall-Héroult
        // Cell gets all of it. 1700 K, not GTNH's 2054 K: Kanthal coils need aluminium.
        // PT: MV, química: Al2O3 + 3 C -> 2 Al + 3 CO, mas metade do alumínio vira carbeto; a Hall-Héroult aproveita
        // tudo. 1700 K, não os 2054 K do GTNH: a bobina de Kanthal pede alumínio.
        // ZH: MV 化学路线: Al2O3 + 3 C -> 2 Al + 3 CO, 但一半的铝变成碳化物; 霍尔-埃鲁电解槽全部回收。
        // 用 1700 K 而不是 GTNH 的 2054 K: 康铝线圈需要铝。
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTIAMod.id("aluminium_from_alumina"))
                .inputItems(TagPrefix.dust, GTIAAlloys.ALUMINA)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon, 3)
                .outputItems(TagPrefix.ingot, GTMaterials.Aluminium)
                .outputFluids(GTMaterials.CarbonMonoxide.getFluid(3000))
                .blastFurnaceTemp(1700).duration(1200).EUt(MV).save(provider);
    }
}
