package com.raishxn.gtia.organ;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * The organs inside a player, kept in the persisted part of the player's Forge data so they survive death and the
 * End portal. Slot layout: one slot per {@link OrganType#TIERED} type, then the eight wing slots.
 */
public final class OrganData {

    public static final int WING_START = OrganType.TIERED.length;
    public static final int SLOTS = WING_START + OrganType.WING.slots();
    private static final String KEY = "gtia_organs";

    private OrganData() {}

    public static OrganType typeOfSlot(int slot) {
        return slot < WING_START ? OrganType.TIERED[slot] : OrganType.WING;
    }

    public static ItemStackHandler load(Player player) {
        ItemStackHandler handler = new ItemStackHandler(SLOTS);
        ListTag list = persisted(player).getList(KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot >= 0 && slot < SLOTS) handler.setStackInSlot(slot, ItemStack.of(entry));
        }
        return handler;
    }

    public static void save(Player player, ItemStackHandler handler) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            CompoundTag entry = stack.save(new CompoundTag());
            entry.putInt("Slot", slot);
            list.add(entry);
        }
        CompoundTag persisted = persisted(player);
        persisted.put(KEY, list);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /** Tier of the organ in a type's slot; an empty slot counts as tier 0, like in GTO. */
    public static int tier(ItemStackHandler organs, OrganType type) {
        for (int i = 0; i < WING_START; i++) {
            if (OrganType.TIERED[i] == type && organs.getStackInSlot(i).getItem() instanceof OrganItem organ) {
                return Math.max(0, organ.tier());
            }
        }
        return 0;
    }

    /** The organ set tier: the lowest tier among the nine tiered types. */
    public static int setTier(ItemStackHandler organs) {
        int tier = Integer.MAX_VALUE;
        for (OrganType type : OrganType.TIERED) tier = Math.min(tier, tier(organs, type));
        return tier;
    }

    public static boolean fits(int slot, ItemStack stack) {
        return stack.getItem() instanceof OrganItem organ && organ.type() == typeOfSlot(slot);
    }

    private static CompoundTag persisted(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
