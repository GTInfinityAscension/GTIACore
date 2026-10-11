package com.raishxn.gtia.station;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * GTIA Crafting Station: a crafting table that also shows up to five containers touching it (the four sides and
 * below, never above), so recipes can be filled from chests without carrying everything.
 */
public class CraftingStationBlock extends Block {

    public CraftingStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            var sides = CraftingStationMenu.findContainers(level, pos);
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, inventory, p) -> new CraftingStationMenu(id, inventory, pos, sides),
                            Component.translatable("block.gtia.crafting_station")),
                    buf -> CraftingStationMenu.writeSides(buf, pos, sides));
        }
        return InteractionResult.CONSUME;
    }
}
