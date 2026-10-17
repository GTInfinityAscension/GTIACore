package com.raishxn.gtia.organ;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * The Organ Modifier screen: each organ slot sits on its place in a body outline, the eight wings in a column on the
 * right, the player inventory below.
 */
public class OrganMenu extends AbstractContainerMenu {

    /** Slot positions on the body outline, in {@link OrganType#TIERED} order. */
    public static final int[][] ORGAN_POS = { { 47, 20 }, { 47, 66 }, { 26, 46 }, { 68, 66 }, { 68, 46 },
            { 8, 62 }, { 86, 62 }, { 32, 96 }, { 62, 96 } };
    public static final int WING_X = 116;
    public static final int INVENTORY_Y = 140;

    private final Player player;
    private final ItemStackHandler organs;

    public OrganMenu(MenuType<?> type, int id, Inventory inventory) {
        this(type, id, inventory, new ItemStackHandler(OrganData.SLOTS));
    }

    public OrganMenu(MenuType<?> type, int id, Inventory inventory, ItemStackHandler organs) {
        super(type, id);
        this.player = inventory.player;
        this.organs = organs;
        for (int slot = 0; slot < OrganData.SLOTS; slot++) {
            if (slot < OrganData.WING_START) {
                addSlot(new OrganSlot(slot, ORGAN_POS[slot][0], ORGAN_POS[slot][1]));
            } else {
                int wing = slot - OrganData.WING_START;
                addSlot(new OrganSlot(slot, WING_X + (wing % 2) * 18, 20 + (wing / 2) * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, INVENTORY_Y + 58));
    }

    public int setTier() {
        return OrganData.setTier(organs);
    }

    public int tier(OrganType type) {
        return OrganData.tier(organs, type);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < OrganData.SLOTS) {
            if (!moveItemStackTo(stack, OrganData.SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, OrganData.SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    private void changed() {
        if (player instanceof ServerPlayer serverPlayer) {
            OrganData.save(serverPlayer, organs);
            OrganEffects.apply(serverPlayer, organs);
        }
    }

    private class OrganSlot extends SlotItemHandler {

        OrganSlot(int index, int x, int y) {
            super(organs, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return OrganData.fits(getSlotIndex(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            changed();
        }
    }
}
