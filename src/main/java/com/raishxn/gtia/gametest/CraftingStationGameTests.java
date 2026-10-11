package com.raishxn.gtia.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.station.CraftingStationMenu;
import com.raishxn.gtia.station.GTIAStation;

@GameTestHolder(GTIAMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingStationGameTests {

    private CraftingStationGameTests() {}

    /** Finds a chest north and one double chest west (touching on one half) and nothing above. */
    @GameTest(template = "qa_empty_5x5", batch = "station", timeoutTicks = 20)
    public static void stationFindsSideContainersOnce(GameTestHelper helper) {
        BlockPos station = new BlockPos(2, 1, 2);
        helper.setBlock(station, GTIAStation.BLOCK.get());
        helper.setBlock(station.north(), Blocks.CHEST);
        helper.setBlock(station.above(), Blocks.CHEST);
        // Double chest along the west side: both halves touch the station? Only one does; place a pair west.
        BlockPos west = station.west();
        helper.setBlock(west, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.RIGHT)
                .setValue(ChestBlock.FACING, Direction.EAST));
        helper.setBlock(west.north(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.LEFT)
                .setValue(ChestBlock.FACING, Direction.EAST));
        var sides = CraftingStationMenu.findContainers(helper.getLevel(), helper.absolutePos(station));
        helper.assertTrue(sides.size() == 2, "expected the north chest and the west double chest, got " +
                sides.stream().map(s -> s.direction() + ":" + s.handler().getSlots()).toList());
        helper.assertTrue(sides.stream().noneMatch(s -> s.direction() == Direction.UP), "never above");
        helper.assertTrue(sides.stream().anyMatch(s -> s.handler().getSlots() == 54), "double chest has 54 slots");
        helper.succeed();
    }

    /** Crafts from the grid and shift-clicks player items into the container on screen. */
    @GameTest(template = "qa_empty_5x5", batch = "station", timeoutTicks = 20)
    public static void stationCraftsAndMovesItemsToTheChest(GameTestHelper helper) {
        BlockPos station = new BlockPos(2, 1, 2);
        helper.setBlock(station, GTIAStation.BLOCK.get());
        helper.setBlock(station.south(), Blocks.CHEST);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.getInventory().clearContent();
        var pos = helper.absolutePos(station);
        player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        var sides = CraftingStationMenu.findContainers(helper.getLevel(), pos);
        var menu = new CraftingStationMenu(1, player.getInventory(), pos, sides);
        for (int slot : new int[] { 1, 2, 4, 5 }) menu.getSlot(slot).set(new ItemStack(Items.OAK_PLANKS));
        menu.slotsChanged(null);
        helper.assertTrue(menu.getSlot(0).getItem().is(Items.CRAFTING_TABLE),
                "2x2 planks should give a crafting table, got " + menu.getSlot(0).getItem());
        player.getInventory().setItem(9, new ItemStack(Items.COBBLESTONE, 32));
        menu.quickMoveStack(player, 10);
        var chest = sides.get(0).handler();
        int moved = 0;
        for (int i = 0; i < chest.getSlots(); i++) {
            if (chest.getStackInSlot(i).is(Items.COBBLESTONE)) moved += chest.getStackInSlot(i).getCount();
        }
        helper.assertTrue(moved == 32, "shift-click should move 32 cobblestone into the chest, moved " + moved);
        helper.succeed();
    }
}
