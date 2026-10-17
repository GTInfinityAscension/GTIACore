package com.raishxn.gtia.organ;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Organ Modifier: a dark lab panel with a body outline, each organ slot on its place, the wings on the right and a
 * readout of what the current organ set gives.
 */
public class OrganScreen extends AbstractContainerScreen<OrganMenu> {

    private static final int PANEL = 0xFF140A2C, EDGE = 0xFF6A3AA0, LIGHT = 0xFFA978E0, BODY = 0xFF2C1466,
            SLOT_BG = 0xFF0C0620, SLOT_EDGE = 0xFF4B1FA0, TEXT = 0xFFE8DCFF, DIM = 0xFF8F78B8;

    public OrganScreen(OrganMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = OrganMenu.INVENTORY_Y + 82;
        this.inventoryLabelY = OrganMenu.INVENTORY_Y - 11;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, EDGE);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, PANEL);
        g.fill(x + 3, y + 15, x + 107, y + 120, 0xFF0D0624);              // body bay
        g.fill(x + 112, y + 15, x + 154, y + 120, 0xFF0D0624);            // wing bay
        g.fill(x + 159, y + 15, x + imageWidth - 4, y + 120, 0xFF0D0624); // readout
        // Body outline: head, torso, arms and legs.
        g.fill(x + 44, y + 17, x + 66, y + 39, BODY);
        g.fill(x + 24, y + 42, x + 88, y + 88, BODY);
        g.fill(x + 6, y + 44, x + 22, y + 82, BODY);
        g.fill(x + 90, y + 44, x + 106, y + 82, BODY);
        g.fill(x + 30, y + 88, x + 50, y + 116, BODY);
        g.fill(x + 60, y + 88, x + 80, y + 116, BODY);
        // Scan line sweeping the body.
        int scan = (int) (System.currentTimeMillis() / 40 % 100);
        g.fill(x + 4, y + 16 + scan, x + 106, y + 17 + scan, 0x664FE0E8);
        for (Slot slot : menu.slots) {
            int sx = x + slot.x - 1, sy = y + slot.y - 1;
            boolean organ = slot.index < OrganData.SLOTS;
            g.fill(sx, sy, sx + 18, sy + 18, organ ? SLOT_EDGE : 0xFF2C1466);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_BG);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 6, 4, LIGHT, false);
        g.drawString(font, Component.translatable("gtia.organ_type.wing"), 114, 110, DIM, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, DIM, false);
        int set = menu.setTier();
        int line = 18;
        g.drawString(font, Component.translatable("gtia.organ.screen.set_tier", set), 163, line, TEXT, false);
        line += 12;
        int legs = Math.min(menu.tier(OrganType.LEFT_LEG), menu.tier(OrganType.RIGHT_LEG));
        line = stat(g, line, "gtia.organ.screen.speed", Math.round(OrganEffects.legSpeedBonus(legs) * 100), legs > 0);
        line = stat(g, line, "gtia.organ.screen.armor", set * OrganEffects.ARMOR_PER_TIER, set > 0);
        line = stat(g, line, "gtia.organ.screen.reach", menu.tier(OrganType.RIGHT_ARM) >= 2 ? 4 : 0,
                menu.tier(OrganType.RIGHT_ARM) >= 2);
        line = flag(g, line, "gtia.organ.tooltip.liver", menu.tier(OrganType.LIVER) >= 2);
        line = flag(g, line, "gtia.organ.tooltip.lung", menu.tier(OrganType.LUNG) >= 2);
        line = flag(g, line, "gtia.organ.tooltip.saturation", set >= 3);
        flag(g, line, "gtia.organ.tooltip.flight", set >= 4);
    }

    private int stat(GuiGraphics g, int line, String key, long value, boolean on) {
        g.drawString(font, Component.translatable(key, value), 163, line, on ? 0xFF7CF07A : DIM, false);
        return line + 11;
    }

    private int flag(GuiGraphics g, int line, String key, boolean on) {
        var text = Component.literal(on ? "● " : "○ ").append(Component.translatable(key));
        for (var part : font.split(text, imageWidth - 168)) {
            g.drawString(font, part, 163, line, on ? 0xFF7CF07A : DIM, false);
            line += 10;
        }
        return line + 1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        // An empty organ slot names the organ it takes.
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index < OrganData.SLOTS) {
            g.renderTooltip(font, Component.translatable(OrganData.typeOfSlot(hoveredSlot.index).translationKey())
                    .withStyle(ChatFormatting.LIGHT_PURPLE), mouseX, mouseY);
        }
    }
}
