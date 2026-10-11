package com.raishxn.gtia.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Crafting grid plus the containers next to the station. Every container slot is a real menu slot (so shift-click,
 * EMI recipe filling and normal dragging all work); only the selected tab and page are active on screen.
 */
public class CraftingStationMenu extends AbstractContainerMenu {

    /** Sides checked, in tab order: the four horizontal sides and below, never above. */
    public static final Direction[] SIDES = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST,
            Direction.DOWN };
    public static final int COLUMNS = 9;
    public static final int ROWS = 6;
    public static final int PAGE_SIZE = COLUMNS * ROWS;
    /** Large storages show their first slots only; keeps the menu small enough to sync. */
    public static final int MAX_SLOTS_PER_SIDE = PAGE_SIZE * 10;
    public static final int PANEL_X = 180;

    public static final int BUTTON_PREVIOUS_PAGE = 10;
    public static final int BUTTON_NEXT_PAGE = 11;

    public record Side(Direction direction, IItemHandler handler, ItemStack icon, Component name) {}

    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final Player player;
    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final List<Side> sides;
    private final DataSlot tab = DataSlot.standalone();
    private final DataSlot page = DataSlot.standalone();
    private final int firstSideSlot;

    /** Server side. */
    public CraftingStationMenu(int id, Inventory inventory, BlockPos pos, List<Side> sides) {
        this(GTIAStation.MENU.get(), id, inventory, pos, sides);
    }

    protected CraftingStationMenu(MenuType<?> type, int id, Inventory inventory, BlockPos pos, List<Side> sides) {
        super(type, id);
        this.player = inventory.player;
        this.pos = pos;
        this.sides = sides;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
        addSlot(new ResultSlot(inventory.player, craftSlots, resultSlots, 0, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftSlots, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        firstSideSlot = slots.size();
        for (int s = 0; s < sides.size(); s++) {
            IItemHandler handler = sides.get(s).handler();
            int count = Math.min(handler.getSlots(), MAX_SLOTS_PER_SIDE);
            for (int i = 0; i < count; i++) {
                int inPage = i % PAGE_SIZE;
                addSlot(new SideSlot(handler, i, PANEL_X + 8 + (inPage % COLUMNS) * 18, 18 + (inPage / COLUMNS) * 18,
                        s, i / PAGE_SIZE));
            }
        }
        addDataSlot(tab);
        addDataSlot(page);
    }

    /** Client side, from the open-screen packet. */
    public static CraftingStationMenu fromNetwork(MenuType<CraftingStationMenu> type, int id, Inventory inventory,
                                                  FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int count = buf.readVarInt();
        List<Side> sides = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Direction direction = buf.readEnum(Direction.class);
            int slots = buf.readVarInt();
            ItemStack icon = buf.readItem();
            Component name = buf.readComponent();
            sides.add(new Side(direction, new ItemStackHandler(slots), icon, name));
        }
        return new CraftingStationMenu(type, id, inventory, pos, sides);
    }

    public static void writeSides(FriendlyByteBuf buf, BlockPos pos, List<Side> sides) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(sides.size());
        for (Side side : sides) {
            buf.writeEnum(side.direction());
            buf.writeVarInt(Math.min(side.handler().getSlots(), MAX_SLOTS_PER_SIDE));
            buf.writeItem(side.icon());
            buf.writeComponent(side.name());
        }
    }

    /** Containers with an item handler on the sides of the station; a double chest touching twice counts once. */
    public static List<Side> findContainers(Level level, BlockPos pos) {
        List<Side> found = new ArrayList<>();
        List<BlockPos> seenChests = new ArrayList<>();
        for (Direction direction : SIDES) {
            BlockPos at = pos.relative(direction);
            BlockEntity entity = level.getBlockEntity(at);
            if (entity == null) continue;
            var state = level.getBlockState(at);
            if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE) &&
                    state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos other = at.relative(ChestBlock.getConnectedDirection(state));
                if (seenChests.contains(other)) continue;
                seenChests.add(at);
            }
            entity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).resolve()
                    .filter(handler -> handler.getSlots() > 0)
                    .ifPresent(handler -> found.add(new Side(direction, handler,
                            new ItemStack(state.getBlock().asItem()), state.getBlock().getName())));
        }
        return found;
    }

    public List<Side> sides() {
        return sides;
    }

    public int selectedTab() {
        return tab.get();
    }

    public int selectedPage() {
        return page.get();
    }

    public int pageCount() {
        if (sides.isEmpty()) return 0;
        int slotsOnTab = Math.min(sides.get(Math.min(tab.get(), sides.size() - 1)).handler().getSlots(),
                MAX_SLOTS_PER_SIDE);
        return Math.max(1, (slotsOnTab + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < sides.size()) {
            tab.set(id);
            page.set(0);
            return true;
        }
        if (id == BUTTON_PREVIOUS_PAGE && page.get() > 0) {
            page.set(page.get() - 1);
            return true;
        }
        if (id == BUTTON_NEXT_PAGE && page.get() + 1 < pageCount()) {
            page.set(page.get() + 1);
            return true;
        }
        return false;
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        access.execute((level, at) -> updateResult(level));
    }

    private void updateResult(Level level) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;
        ItemStack result = ItemStack.EMPTY;
        var recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots, level);
        if (recipe.isPresent()) {
            CraftingRecipe crafting = recipe.get();
            if (resultSlots.setRecipeUsed(level, serverPlayer, crafting)) {
                ItemStack assembled = crafting.assemble(craftSlots, level.registryAccess());
                if (assembled.isItemEnabled(level.enabledFeatures())) result = assembled;
            }
        }
        resultSlots.setItem(0, result);
        setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, result));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int gridEnd = 10, inventoryEnd = 46;
        if (index == 0) {
            access.execute((level, at) -> stack.getItem().onCraftedBy(stack, level, player));
            if (!moveItemStackTo(stack, 10, inventoryEnd, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copy);
        } else if (index < gridEnd) {
            if (!moveItemStackTo(stack, 10, inventoryEnd, false)) return ItemStack.EMPTY;
        } else if (index < inventoryEnd) {
            // From the player: into the container shown on screen, else into the grid.
            int[] range = activeSideRange();
            boolean moved = range != null && moveItemStackTo(stack, range[0], range[1], false);
            if (!moved && !moveItemStackTo(stack, 1, gridEnd, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 10, inventoryEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        if (index == 0) player.drop(stack, false);
        return copy;
    }

    /** Menu slot range of the selected container, or null when there is none. */
    private int[] activeSideRange() {
        if (sides.isEmpty()) return null;
        int start = firstSideSlot;
        for (int s = 0; s < tab.get() && s < sides.size(); s++) {
            start += Math.min(sides.get(s).handler().getSlots(), MAX_SLOTS_PER_SIDE);
        }
        int size = Math.min(sides.get(Math.min(tab.get(), sides.size() - 1)).handler().getSlots(),
                MAX_SLOTS_PER_SIDE);
        return new int[] { start, start + size };
    }

    /** First menu index of the container slots (grid and player inventory come before). */
    public int firstSideSlot() {
        return firstSideSlot;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, at) -> clearContainer(player, craftSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, GTIAStation.BLOCK.get());
    }

    public BlockPos pos() {
        return pos;
    }

    /** A slot of a neighbouring container, shown only on its tab and page. */
    public final class SideSlot extends SlotItemHandler {

        private final int sideIndex;
        private final int pageIndex;

        SideSlot(IItemHandler handler, int index, int x, int y, int sideIndex, int pageIndex) {
            super(handler, index, x, y);
            this.sideIndex = sideIndex;
            this.pageIndex = pageIndex;
        }

        @Override
        public boolean isActive() {
            return tab.get() == sideIndex && page.get() == pageIndex;
        }
    }
}
