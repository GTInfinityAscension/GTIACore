package com.raishxn.gtia.machine.logic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.data.GTIARecipeTypes;
import org.jetbrains.annotations.Nullable;

/**
 * Seed Analyzer (GTIA-D-271): an Agronomica seed in, the same seed out with {@code analyzed:1}, so its Growth, Gain and
 * Resistance show. Built per stack because each seed carries its own stats.
 */
public enum SeedAnalyzerLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    public static final ResourceLocation SEEDS = new ResourceLocation("agronomica", "seeds");
    public static final String ANALYZED = "analyzed";

    public static boolean isUnanalyzedSeed(ItemStack stack) {
        return !stack.isEmpty() && SEEDS.equals(ForgeRegistries.ITEMS.getKey(stack.getItem())) &&
                stack.hasTag() && !stack.getTag().getBoolean(ANALYZED);
    }

    public static ItemStack analyzed(ItemStack seed) {
        ItemStack out = seed.copyWithCount(1);
        out.getOrCreateTag().putBoolean(ANALYZED, true);
        return out;
    }

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        for (var list : holder.getCapabilitiesForIO(IO.IN)) {
            for (var handler : list.getCapability(ItemRecipeCapability.CAP)) {
                for (var content : handler.getContents()) {
                    if (content instanceof ItemStack stack && isUnanalyzedSeed(stack)) return build(stack);
                }
            }
        }
        return null;
    }

    private static GTRecipe build(ItemStack seed) {
        // EN: 5 s at 16 EU/t: cheap on purpose, the cost of breeding is in the crops, not the reading.
        // PT: 5 s a 16 EU/t: barato de propósito; o custo do melhoramento está nas plantas, não na leitura.
        // ZH: 5 秒、16 EU/t：故意便宜，育种成本在作物本身而不在分析。
        return GTIARecipeTypes.SEED_ANALYZER.recipeBuilder("analyze_" + Integer.toHexString(seed.getTag().hashCode()))
                .inputItems(seed.copyWithCount(1)).outputItems(analyzed(seed))
                .duration(100).EUt(16).buildRawRecipe();
    }

    @Override
    public void buildRepresentativeRecipes() {
        var item = ForgeRegistries.ITEMS.getValue(SEEDS);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return;
        var seed = new ItemStack(item);
        seed.getOrCreateTag().putString("species", "hemp");
        GTRecipe recipe = build(seed);
        recipe.setId(recipe.getId().withPrefix("/"));
        GTIARecipeTypes.SEED_ANALYZER.addToMainCategory(recipe);
    }

    public static final int FIRST_TIER = GTValues.LV;
}
