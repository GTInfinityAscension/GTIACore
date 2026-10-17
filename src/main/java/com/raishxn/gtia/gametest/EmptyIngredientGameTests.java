package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Fails when a GTIA recipe has an item input that resolves to nothing (missing item or empty tag, which Forge shows
 * as a barrier): EMI shows an empty slot and the recipe can never run.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class EmptyIngredientGameTests {

    private EmptyIngredientGameTests() {}

    @GameTest(template = "qa_empty_5x5", batch = "integrity", timeoutTicks = 200)
    public static void noEmptyItemInputs(GameTestHelper helper) {
        List<String> broken = new ArrayList<>();
        for (var recipe : helper.getLevel().getServer().getRecipeManager().getRecipes()) {
            if (!(recipe instanceof GTRecipe gt) || !"gtia".equals(gt.getId().getNamespace())) continue;
            var inputs = gt.getInputContents(ItemRecipeCapability.CAP);
            for (int i = 0; i < inputs.size(); i++) {
                boolean empty = true;
                for (var stack : ItemRecipeCapability.CAP.of(inputs.get(i).content).getItems())
                    if (!stack.isEmpty() && !stack.is(Items.BARRIER)) empty = false;
                if (empty) broken.add(gt.getId() + " slot " + i);
            }
        }
        helper.assertTrue(broken.isEmpty(), broken.size() + " empty item inputs: " + broken);
        helper.succeed();
    }
}
