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

    /** {@code parents} empty: bred from a vanilla bee in the Chemical Reactor; else mutated from those two. */
    private record Species(String id, Material metal, int tier, String... parents) {}

    private static final int HV = GTValues.HV, EV = GTValues.EV;
    // EN: EV species come only from the Mutation Chamber, from two HV parents (GTIA-D-271/272, no RNG).
    // PT: Espécies do EV só saem da Câmara de mutação, de dois pais do HV (GTIA-D-271/272, sem sorte).
    // ZH: EV 物种只能由突变室从两只 HV 亲本获得（GTIA-D-271/272，无随机）。
    private static final Species[] SPECIES = {
            new Species("iron", GTMaterials.Iron, HV), new Species("copper", GTMaterials.Copper, HV),
            new Species("tin", GTMaterials.Tin, HV), new Species("lead", GTMaterials.Lead, HV),
            new Species("silver", GTMaterials.Silver, HV), new Species("nickel", GTMaterials.Nickel, HV),
            new Species("zinc", GTMaterials.Zinc, HV), new Species("gold", GTMaterials.Gold, HV),
            new Species("aluminum", GTMaterials.Aluminium, HV),
            new Species("titanium", GTMaterials.Titanium, EV, "iron", "aluminum"),
            new Species("tungsten", GTMaterials.Tungsten, EV, "titanium", "tin"),
            new Species("molybdenum", GTMaterials.Molybdenum, EV, "copper", "lead"),
            new Species("redstone", GTMaterials.Redstone, EV, "copper", "gold"),
            new Species("lapis", GTMaterials.Lapis, EV, "aluminum", "silver") };

    private static CompoundTag caged(String species) {
        var tag = new CompoundTag();
        tag.putString("entity", "productivebees:configurable_bee");
        tag.putString("type", "productivebees:" + species);
        return tag;
    }

    private GTIABeeRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        if (!ModList.get().isLoaded("productivebees")) return;
        Item cage = item("bee_cage"), comb = item("configurable_honeycomb");
        if (cage == null || comb == null) return;
        var wildBee = new CompoundTag();
        wildBee.putString("entity", "minecraft:bee");
        Item treat = item("honey_treat");
        for (Species s : SPECIES) {
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

            if (s.parents().length == 0) {
                GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTIAMod.id("bees/breed_" + s.id()))
                        .inputItems(PartialNBTIngredient.of(cage, wildBee))
                        .inputItems(TagPrefix.block, s.metal(), 4)
                        .inputFluids(GTMaterials.Biomass.getFluid(1000))
                        .outputItems(caged).duration(600).EUt(GTValues.VA[HV]).save(provider);
            } else if (treat != null) {
                GTIARecipeTypes.MUTATION_CHAMBER.recipeBuilder(GTIAMod.id("bees/mutate_" + s.id()))
                        .notConsumable(PartialNBTIngredient.of(cage, caged(s.parents()[0])))
                        .notConsumable(PartialNBTIngredient.of(cage, caged(s.parents()[1])))
                        .inputItems(PartialNBTIngredient.of(cage, wildBee))
                        .inputItems(new ItemStack(treat, 4))
                        .inputItems(TagPrefix.block, s.metal(), 2)
                        .outputItems(caged).duration(1200).EUt(GTValues.VA[s.tier()]).save(provider);
            }

            // EN: The caged bee is a catalyst; 2 combs per 20 s at HV before overclocks.
            // PT: A abelha engaiolada é catalisador; 2 favos a cada 20 s no HV antes de overclock.
            // ZH: 笼中蜜蜂为催化剂；HV 未超频时每 20 秒产出 2 个蜂巢。
            GTIARecipeTypes.INDUSTRIAL_APIARY.recipeBuilder(GTIAMod.id("bees/apiary_" + s.id()))
                    .notConsumable(PartialNBTIngredient.of(cage, cagedTag))
                    .inputFluids(GTMaterials.Water.getFluid(100))
                    .outputItems(combStack.copyWithCount(2)).duration(400).EUt(GTValues.VA[s.tier()])
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
                    .duration(200).EUt(GTValues.VA[s.tier()]).save(provider);
        }
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("productivebees", path));
        return item == null || item == net.minecraft.world.item.Items.AIR ? null : item;
    }
}
