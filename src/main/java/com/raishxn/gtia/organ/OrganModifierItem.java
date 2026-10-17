package com.raishxn.gtia.organ;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Right click opens the player's organ slots. */
public class OrganModifierItem extends Item {

    public OrganModifierItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, inventory, p) -> new OrganMenu(GTIAOrgans.MENU.get(), id, inventory, OrganData.load(p)),
                    Component.translatable("item.gtia.organ_modifier")));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int line = 1; line <= 4; line++) {
            tooltip.add(Component.translatable("gtia.organ_modifier.tooltip." + line)
                    .withStyle(line == 4 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY));
        }
    }
}
