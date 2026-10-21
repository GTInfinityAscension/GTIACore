package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.function.Consumer;

/**
 * Agronomica products into the GT lines (GTIA-D-271): oily seed → seed oil (then GTCEu's biodiesel), latex →
 * vulcanised rubber in Steam, biomass bundles → wood pulp and Brewery biomass, seed oil as boiler fuel.
 */
public final class GTIAAgroRecipes {

    private GTIAAgroRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        if (!ModList.get().isLoaded("agronomica")) return;
        Item oilySeed = item("oily_seed"), latex = item("latex"), biomass = item("biomass_bundle");
        if (oilySeed == null || latex == null || biomass == null) return;

        // EN: 4 EU/t so the Steam Extractor runs it; 100 mB is ten times a wheat seed, rapeseed is an oil crop.
        // PT: 4 EU/t para o Steam Extractor rodar; 100 mB é dez vezes a semente de trigo, a colza é planta de óleo.
        // ZH: 4 EU/t 以便蒸汽提取机运行；100 mB 为小麦种子的十倍，油菜是油料作物。
        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder(GTIAMod.id("agro/oily_seed_oil"))
                .inputItems(new ItemStack(oilySeed))
                .outputFluids(GTMaterials.SeedOil.getFluid(100))
                .duration(32).EUt(4).save(provider);

        // EN: Vulcanisation: dearer than GT's resin route (2 latex per ingot), but available in Steam.
        // PT: Vulcanização: mais cara que a rota da resina do GT (2 látex por lingote), mas já no Steam.
        // ZH: 硫化：比 GT 树脂路线更贵（每锭 2 份胶乳），但在蒸汽时代即可使用。
        GTRecipeTypes.ALLOY_SMELTER_RECIPES.recipeBuilder(GTIAMod.id("agro/vulcanised_rubber"))
                .inputItems(new ItemStack(latex, 2))
                .inputItems(TagPrefix.dust, GTMaterials.Sulfur)
                .outputItems(TagPrefix.ingot, GTMaterials.Rubber)
                .duration(200).EUt(4).save(provider);

        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTIAMod.id("agro/biomass_bundle_pulp"))
                .inputItems(new ItemStack(biomass))
                .outputItems(TagPrefix.dust, GTMaterials.Wood, 2)
                .chancedOutput(GTItems.PLANT_BALL.asStack(), 2500, 0)
                .duration(80).EUt(4).save(provider);

        GTRecipeTypes.BREWING_RECIPES.recipeBuilder(GTIAMod.id("agro/biomass_bundle_biomass"))
                .inputItems(new ItemStack(biomass, 4))
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.Biomass.getFluid(500))
                .duration(800).EUt(8).save(provider);

        // EN: Same burn value per mB as creosote (250 mB → 17.5 s): an early fuel, not a better one.
        // PT: Mesmo valor por mB que o creosoto (250 mB → 17,5 s): combustível cedo, não melhor.
        // ZH: 每 mB 燃烧值与杂酚油相同（250 mB → 17.5 秒）：早期燃料，而非更好的燃料。
        greenhouse(provider);
        fertilizers(provider);

        GTRecipeTypes.STEAM_BOILER_RECIPES.recipeBuilder(GTIAMod.id("agro/seed_oil_boiler"))
                .inputFluids(GTMaterials.SeedOil.getFluid(250))
                .duration(350).save(provider);
    }

    /**
     * EN: GTNA Greenhouse (GTIA-D-337): 8 products per 30 s with NPK, 6 with compost (2 per run); the seed stays in the
     * bus.
     * PT: Estufa do GTNA (GTIA-D-337): 8 produtos a cada 30 s com NPK, 6 com composto (2 por ciclo); a semente fica no
     * barramento.
     * ZH: GTNA 温室（GTIA-D-337）：使用 NPK 每 30 秒产出 8 个，使用堆肥（每次 2 个）产出 6 个；种子留在总线中。
     */
    private static void greenhouse(Consumer<FinishedRecipe> provider) {
        Item seeds = item("seeds"), npk = item("fertilizer_npk"), compost = item("compost");
        if (seeds == null || npk == null || compost == null) return;
        String[][] crops = { { "hemp", "hemp_fiber" }, { "rapeseed", "oily_seed" },
                { "elephant_grass", "biomass_bundle" }, { "rubber_bush", "latex" } };
        for (String[] crop : crops) {
            Item product = item(crop[1]);
            if (product == null) continue;
            var tag = new net.minecraft.nbt.CompoundTag();
            tag.putString("species", crop[0]);
            for (boolean rich : new boolean[] { true, false }) {
                GTNARecipeType.GREENHOUSE_RECIPES
                        .recipeBuilder(GTIAMod.id("agro/greenhouse_" + crop[0] + (rich ? "_npk" : "_compost")))
                        .notConsumable(net.minecraftforge.common.crafting.PartialNBTIngredient.of(seeds, tag))
                        .inputItems(new ItemStack(rich ? npk : compost, rich ? 1 : 2))
                        .inputFluids(GTMaterials.Water.getFluid(1000))
                        .outputItems(new ItemStack(product, rich ? 8 : 6))
                        .duration(600).EUt(64).save(provider);
            }
        }
    }

    /**
     * EN: MV fertiliser chemistry: urea (NH3 + CO2), monoammonium phosphate (H3PO4 + NH3), potassium chloride from rock
     * salt, then NPK in the Mixer. This is the route that makes NPK worth 35 each (GTIA-D-271).
     * PT: Química de fertilizantes do MV: ureia (NH3 + CO2), fosfato monoamônico (H3PO4 + NH3), cloreto de potássio do
     * sal-gema e o NPK no Mixer. É a rota que faz o NPK valer 35 de cada (GTIA-D-271).
     * ZH: MV 化肥化工：尿素（NH3 + CO2）、磷酸一铵（H3PO4 + NH3）、岩盐制氯化钾，再在搅拌机中配成 NPK。此路线使 NPK
     * 各项达到 35（GTIA-D-271）。
     */
    private static void fertilizers(Consumer<FinishedRecipe> provider) {
        Item n = item("fertilizer_n"), p = item("fertilizer_p"), k = item("fertilizer_k"), npk = item("fertilizer_npk");
        if (n == null || p == null || k == null || npk == null) return;
        long mv = com.gregtechceu.gtceu.api.GTValues.VA[com.gregtechceu.gtceu.api.GTValues.MV];
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("agro/urea"))
                .inputFluids(GTMaterials.Ammonia.getFluid(2000)).inputFluids(GTMaterials.CarbonDioxide.getFluid(1000))
                .outputItems(new ItemStack(n, 4)).outputFluids(GTMaterials.Water.getFluid(1000))
                .duration(200).EUt(mv).save(provider);
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("agro/monoammonium_phosphate"))
                .inputFluids(GTMaterials.PhosphoricAcid.getFluid(1000)).inputFluids(GTMaterials.Ammonia.getFluid(1000))
                .outputItems(new ItemStack(p, 4))
                .duration(200).EUt(mv).save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTIAMod.id("agro/potassium_chloride"))
                .inputItems(TagPrefix.dust, GTMaterials.RockSalt, 2).inputFluids(GTMaterials.Water.getFluid(500))
                .outputItems(new ItemStack(k, 3))
                .duration(160).EUt(mv).save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTIAMod.id("agro/npk"))
                .inputItems(new ItemStack(n)).inputItems(new ItemStack(p)).inputItems(new ItemStack(k))
                .outputItems(new ItemStack(npk, 3))
                .duration(100).EUt(mv).save(provider);
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("agronomica", path));
        return item == null || item == Items.AIR ? null : item;
    }
}
