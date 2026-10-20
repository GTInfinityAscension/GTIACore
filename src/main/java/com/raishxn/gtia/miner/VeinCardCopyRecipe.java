package com.raishxn.gtia.miner;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** One recorded Vein Card and any blank ones in the crafting grid: every blank card gets the same vein. */
public class VeinCardCopyRecipe extends CustomRecipe {

    public VeinCardCopyRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return source(grid) != null && blanks(grid) > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess access) {
        ItemStack source = source(grid);
        if (source == null) return ItemStack.EMPTY;
        return source.copyWithCount(blanks(grid));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer grid) {
        NonNullList<ItemStack> left = NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (VeinCardItem.read(s) != null) left.set(i, s.copyWithCount(1));
        }
        return left;
    }

    /** The single recorded card in the grid, or null if there is none, more than one, or anything else. */
    private static ItemStack source(CraftingContainer grid) {
        ItemStack found = null;
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (s.isEmpty()) continue;
            if (!(s.getItem() instanceof VeinCardItem)) return null;
            if (VeinCardItem.read(s) != null) {
                if (found != null) return null;
                found = s;
            }
        }
        return found;
    }

    private static int blanks(CraftingContainer grid) {
        int n = 0;
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (s.getItem() instanceof VeinCardItem && VeinCardItem.read(s) == null) n++;
        }
        return n;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GTIAMiners.CARD_COPY.get();
    }
}
