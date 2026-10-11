package com.raishxn.gtia.integration.emi;

import net.minecraft.world.inventory.Slot;

import com.raishxn.gtia.station.CraftingStationMenu;
import com.raishxn.gtia.station.GTIAStation;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;

import java.util.ArrayList;
import java.util.List;

/** EMI: the Crafting Station fills recipes from the player and from the containers next to it. */
@EmiEntrypoint
public class GTIAEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(GTIAStation.BLOCK.get()));
        registry.addRecipeHandler(GTIAStation.MENU.get(), new StationHandler());
    }

    /** The station's recipe handler; also used by the client self-test to press "+" the same way EMI does. */
    public static class StationHandler implements StandardRecipeHandler<CraftingStationMenu> {

        @Override
        public List<Slot> getInputSources(CraftingStationMenu menu) {
            List<Slot> sources = new ArrayList<>(menu.slots.subList(10, 46));
            sources.addAll(menu.slots.subList(menu.firstSideSlot(), menu.slots.size()));
            return sources;
        }

        @Override
        public List<Slot> getCraftingSlots(CraftingStationMenu menu) {
            return menu.slots.subList(1, 10);
        }

        @Override
        public Slot getOutputSlot(CraftingStationMenu menu) {
            return menu.slots.get(0);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
        }
    }
}
