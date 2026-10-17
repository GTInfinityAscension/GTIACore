package com.raishxn.gtia.organ;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** An organ: a tiered organ of one body type (tier 0 to 4) or a wing (tier -1). */
public class OrganItem extends ComponentItem {

    public static final String[] TIER_NAMES = { "baby", "standard", "reinforced", "tactical", "prototype" };

    private final OrganType type;
    private final int tier;
    /** Flight speed a wing gives; vanilla creative flight is 0.05. */
    private final float flySpeed;

    public OrganItem(Properties properties, OrganType type, int tier, float flySpeed) {
        // EN: Callers set the stack size: durability() already makes it 1 and stacksTo() after it throws.
        // PT: Quem chama define o tamanho da pilha: durability() já o deixa em 1 e stacksTo() depois dele lança erro.
        // ZH: 堆叠数由调用方设置：durability() 已将其设为 1，之后再调用 stacksTo() 会抛异常。
        super(properties.setNoRepair());
        this.type = type;
        this.tier = tier;
        this.flySpeed = flySpeed;
    }

    public OrganType type() {
        return type;
    }

    public int tier() {
        return tier;
    }

    public float flySpeed() {
        return flySpeed;
    }

    public boolean isWing() {
        return type == OrganType.WING;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(type.translationKey()).withStyle(ChatFormatting.GRAY));
        if (isWing()) {
            tooltip.add(Component.translatable("gtia.organ.tooltip.wing").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("gtia.organ.tooltip.fly_speed", String.format("%.2f", flySpeed))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            if (stack.isDamageableItem()) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.durability",
                        stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage())
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("gtia.organ.tooltip.level", tier).withStyle(ChatFormatting.GOLD));
            if (tier >= 1 && (type == OrganType.LEFT_LEG || type == OrganType.RIGHT_LEG)) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.speed",
                        Math.round(OrganEffects.legSpeedBonus(tier) * 100)).withStyle(ChatFormatting.GREEN));
            }
            if (tier >= 2 && type == OrganType.RIGHT_ARM) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.reach", (int) OrganEffects.ARM_REACH)
                        .withStyle(ChatFormatting.YELLOW));
            }
            if (tier >= 2 && type == OrganType.LIVER) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.liver").withStyle(ChatFormatting.GREEN));
            }
            if (tier >= 2 && type == OrganType.LUNG) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.lung").withStyle(ChatFormatting.GREEN));
            }
            tooltip.add(Component.translatable("gtia.organ.tooltip.set_header").withStyle(ChatFormatting.DARK_AQUA));
            if (tier >= 1) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.armor", tier * OrganEffects.ARMOR_PER_TIER)
                        .withStyle(ChatFormatting.YELLOW));
            }
            if (tier >= 3) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.saturation").withStyle(ChatFormatting.GREEN));
            }
            if (tier >= 4) {
                tooltip.add(Component.translatable("gtia.organ.tooltip.flight")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
