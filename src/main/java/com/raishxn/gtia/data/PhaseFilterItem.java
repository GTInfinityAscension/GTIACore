package com.raishxn.gtia.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PhaseFilterItem extends Item {

    public PhaseFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copyWithCount(1);
    }
}
