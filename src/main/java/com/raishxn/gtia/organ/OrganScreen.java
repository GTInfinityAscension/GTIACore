package com.raishxn.gtia.organ;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Three-row chest look; the middle row is a plain strip with the organ set tier. */
public class OrganScreen extends AbstractContainerScreen<OrganMenu> {

    private static final ResourceLocation CHEST = new ResourceLocation("textures/gui/container/generic_54.png");

    public OrganScreen(OrganMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 166;
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(CHEST, leftPos, topPos, 0, 0, imageWidth, 71);
        graphics.blit(CHEST, leftPos, topPos + 71, 0, 126, imageWidth, 96);
        // The middle slot row becomes a plain strip, and the ninth wing slot is covered (there are eight wings).
        graphics.blit(CHEST, leftPos, topPos + 35, 0, 5, imageWidth, 18);
        graphics.blit(CHEST, leftPos + 151, topPos + 53, 7, 5, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("gtia.organ.screen.set_tier", menu.setTier()), 8, 40,
                0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
