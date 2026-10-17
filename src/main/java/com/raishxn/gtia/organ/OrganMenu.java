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

/** The Organ Modifier screen: the nine tiered organs on the first row, the eight wings on the third. */
public class OrganMenu extends AbstractContainerMenu {

    public static final int ORGAN_ROW_Y = 18;
    public static final int WING_ROW_Y = 54;

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
            boolean wing = slot >= OrganData.WING_START;
            int column = wing ? slot - OrganData.WING_START : slot;
            addSlot(new OrganSlot(slot, 8 + column * 18, wing ? WING_ROW_Y : ORGAN_ROW_Y));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    public int setTier() {
        return OrganData.setTier(organs);
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
