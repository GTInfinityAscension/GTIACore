package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Every GTIA machine can be made, and so can the items the bee line needs from Productive Bees. */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class MachineRecipeGameTests {

    /** Productive Bees' recipes are dropped (GTIA-D-268); these must come from GTIA's own. */
    private static final List<String> BEE_ITEMS = List.of("productivebees:bee_cage", "productivebees:honey_treat");

    private MachineRecipeGameTests() {}

    @GameTest(template = "qa_empty_5x5", batch = "machine_recipes", timeoutTicks = 20)
    public static void everyMachineHasARecipe(GameTestHelper h) {
        var registry = h.getLevel().registryAccess();
        Set<Item> made = new HashSet<>();
        for (Recipe<?> recipe : h.getLevel().getRecipeManager().getRecipes()) {
            if (recipe instanceof GTRecipe gt) {
                RecipeHelper.getOutputItems(gt).forEach(stack -> made.add(stack.getItem()));
            } else {
                made.add(recipe.getResultItem(registry).getItem());
            }
        }
        Set<String> missing = new TreeSet<>();
        for (var definition : GTRegistries.MACHINES.values()) {
            if (GTIAMod.MOD_ID.equals(definition.getId().getNamespace()) && !made.contains(definition.getItem()))
                missing.add(definition.getId().toString());
        }
        for (String id : BEE_ITEMS) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null && item != net.minecraft.world.item.Items.AIR && !made.contains(item)) missing.add(id);
        }
        h.assertTrue(missing.isEmpty(), "no recipe makes: " + missing);
        h.succeed();
    }
}
