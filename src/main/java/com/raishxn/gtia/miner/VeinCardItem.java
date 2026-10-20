package com.raishxn.gtia.miner;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.WeightedMaterial;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Vein Card (GTIA-D-310): right-click inside a bedrock vein to record it. The card keeps the dimension, the vein
 * origin, the vein type and its purity; an Entangled Miner with the card mines that vein from wherever it may.
 */
public class VeinCardItem extends Item {

    public static final String TAG = "gtia_vein";

    public VeinCardItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    /** The recorded vein of a card: dimension, origin chunk, vein id and purity. */
    public record Record(ResourceLocation dimension, int originX, int originZ, ResourceLocation vein,
                         VeinPurity purity) {

        public String key() {
            return dimension + "@" + originX + "," + originZ;
        }

        @Nullable
        public ServerLevel level(ServerLevel any) {
            return any.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        }
    }

    @Nullable
    public static Record read(ItemStack stack) {
        if (!(stack.getItem() instanceof VeinCardItem) || !stack.hasTag() || !stack.getTag().contains(TAG)) return null;
        CompoundTag t = stack.getTag().getCompound(TAG);
        int p = Math.max(0, Math.min(VeinPurity.values().length - 1, t.getInt("purity")));
        return new Record(new ResourceLocation(t.getString("dim")), t.getInt("x"), t.getInt("z"),
                new ResourceLocation(t.getString("vein")), VeinPurity.values()[p]);
    }

    public static void write(ItemStack stack, BedrockVeins.Vein vein, VeinPurity purity) {
        CompoundTag t = new CompoundTag();
        t.putString("dim", vein.dimension().toString());
        t.putInt("x", vein.originX());
        t.putInt("z", vein.originZ());
        t.putString("vein", vein.id().toString());
        t.putInt("purity", purity.ordinal());
        stack.getOrCreateTag().put(TAG, t);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        var chunk = player.chunkPosition();
        var vein = BedrockVeins.at(server, chunk.x, chunk.z);
        if (vein == null) {
            player.displayClientMessage(Component.translatable("gtia.vein_card.none"), true);
            return InteractionResultHolder.fail(stack);
        }
        var purity = vein.purity(server.getSeed());
        // A stack records one card and keeps the rest blank, so blank cards are not spent by accident.
        ItemStack card = stack.getCount() > 1 ? stack.split(1) : stack;
        write(card, vein, purity);
        if (card != stack && !player.getInventory().add(card)) player.drop(card, false);
        player.displayClientMessage(Component.translatable("gtia.vein_card.recorded", veinName(vein.id()),
                Component.translatable(purity.langKey())), true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static Component veinName(ResourceLocation vein) {
        return Component.translatable("gtia.vein." + vein.getPath());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Record r = read(stack);
        if (r == null) {
            tooltip.add(Component.translatable("gtia.vein_card.blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("gtia.vein_card.vein", veinName(r.vein()),
                Component.translatable(r.purity().langKey())).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("gtia.vein_card.where", r.dimension().toString(), r.originX() * 16 + 8,
                r.originZ() * 16 + 8).withStyle(ChatFormatting.GRAY));
        var definition = GTRegistries.BEDROCK_ORE_DEFINITIONS.get(r.vein());
        if (definition == null) return;
        int total = definition.materials().stream().mapToInt(WeightedMaterial::weight).sum();
        for (var m : definition.materials())
            tooltip.add(Component.literal(" - ").append(m.material().getLocalizedName())
                    .append(" " + Math.round(100.0 * m.weight() / total) + "%").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return read(stack) != null;
    }
}
