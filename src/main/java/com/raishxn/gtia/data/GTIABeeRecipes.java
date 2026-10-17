package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Consumer;

/**
 * HV bee line (GTIA-D-259): catch a vanilla bee in a cage, turn it into a metal bee in the Chemical Reactor, keep it in
 * the Industrial Apiary, and process its combs. Productive Bees' own recipes are dropped by a mixin, so this is the
 * only way in.
 */
public final class GTIABeeRecipes {

    private record Species(String id, Material metal) {}

    private static final Species[] HV_SPECIES = {
            new Species("iron", GTMaterials.Iron), new Species("copper", GTMaterials.Copper),
            new Species("tin", GTMaterials.Tin), new Species("lead", GTMaterials.Lead),
            new Species("silver", GTMaterials.Silver), new Species("nickel", GTMaterials.Nickel),
            new Species("zinc", GTMaterials.Zinc), new Species("gold", GTMaterials.Gold),
            new Species("aluminum", GTMaterials.Aluminium) };

    private GTIABeeRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        if (!ModList.get().isLoaded("productivebees")) return;
        Item cage = item("bee_cage"), comb = item("configurable_honeycomb");
        if (cage == null || comb == null) return;
        var wildBee = new CompoundTag();
        wildBee.putString("entity", "minecraft:bee");
        for (Species s : HV_SPECIES) {
            String type = "productivebees:" + s.id();
            var cagedTag = new CompoundTag();
            cagedTag.putString("entity", "productivebees:configurable_bee");
            cagedTag.putString("type", type);
            var caged = new ItemStack(cage);
            caged.setTag(cagedTag.copy());
            var combTag = new CompoundTag();
            var entityTag = new CompoundTag();
            entityTag.putString("type", type);
            combTag.put("EntityTag", entityTag);
            var combStack = new ItemStack(comb);
            combStack.setTag(combTag);

            GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("bees/breed_" + s.id()))
                    .inputItems(PartialNBTIngredient.of(cage, wildBee))
                    .inputItems(TagPrefix.block, s.metal(), 4)
                    .inputFluids(GTMaterials.Biomass.getFluid(1000))
                    .outputItems(caged).duration(600).EUt(GTValues.VA[GTValues.HV]).save(provider);

            // EN: The caged bee is a catalyst; 2 combs per 20 s at HV before overclocks.
            // PT: A abelha engaiolada é catalisador; 2 favos a cada 20 s no HV antes de overclock.
            // ZH: 笼中蜜蜂为催化剂；HV 未超频时每 20 秒产出 2 个蜂巢。
            GTIARecipeTypes.INDUSTRIAL_APIARY.recipeBuilder(GTIAMod.id("bees/apiary_" + s.id()))
                    .notConsumable(PartialNBTIngredient.of(cage, cagedTag))
                    .inputFluids(GTMaterials.Water.getFluid(100))
                    .outputItems(combStack.copyWithCount(2)).duration(400).EUt(GTValues.VA[GTValues.HV])
                    .save(provider);

            // EN: Direct route is poor (1 small dust per comb); the reactor route gives a purified ore per comb.
            // PT: A rota direta rende pouco (1 pó pequeno por favo); o reator dá 1 minério purificado por favo.
            // ZH: 直接路线收益低（每个蜂巢 1 小撮粉）；反应釜路线每个蜂巢产出 1 个洗净矿石。
            GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(GTIAMod.id("bees/comb_dust_" + s.id()))
                    .inputItems(PartialNBTIngredient.of(comb, combTag))
                    .outputItems(TagPrefix.dustSmall, s.metal())
                    .duration(128).EUt(GTValues.VA[GTValues.MV]).save(provider);
            if (!s.metal().hasProperty(PropertyKey.ORE)) continue;
            GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("bees/comb_ore_" + s.id()))
                    .inputItems(PartialNBTIngredient.of(comb, combTag), 4)
                    .inputFluids(GTMaterials.HydrochloricAcid.getFluid(384))
                    .outputItems(TagPrefix.crushedPurified, s.metal(), 4)
                    .duration(200).EUt(GTValues.VA[GTValues.HV]).save(provider);
        }
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("productivebees", path));
        return item == null || item == net.minecraft.world.item.Items.AIR ? null : item;
    }
}
