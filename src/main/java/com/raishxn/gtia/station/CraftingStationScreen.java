package com.raishxn.gtia.station;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Crafting table on the left, the selected neighbouring container on the right, one tab per container. */
public class CraftingStationScreen extends AbstractContainerScreen<CraftingStationMenu> {

    private static final ResourceLocation CRAFTING = new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final ResourceLocation CHEST = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int PANEL_X = CraftingStationMenu.PANEL_X;

    private Button previous;
    private Button next;

    public CraftingStationScreen(CraftingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_X + 176;
        this.imageHeight = 166;
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        var sides = menu.sides();
        for (int i = 0; i < sides.size(); i++) {
            int index = i;
            var side = sides.get(i);
            Button tab = Button.builder(Component.empty(), b -> click(index))
                    .bounds(leftPos + PANEL_X + i * 24, topPos - 22, 22, 22)
                    .tooltip(Tooltip.create(Component.translatable("gtia.crafting_station.tab", side.name(),
                            Component.translatable("gtia.crafting_station.side." + side.direction().getName()))))
                    .build();
            addRenderableWidget(tab);
        }
        previous = addRenderableWidget(Button.builder(Component.literal("<"),
                b -> click(CraftingStationMenu.BUTTON_PREVIOUS_PAGE))
                .bounds(leftPos + PANEL_X + 120, topPos + 128, 16, 14).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"),
                b -> click(CraftingStationMenu.BUTTON_NEXT_PAGE))
                .bounds(leftPos + PANEL_X + 152, topPos + 128, 16, 14).build());
    }

    private void click(int id) {
        if (minecraft != null && minecraft.gameMode != null && menu.clickMenuButton(minecraft.player, id)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean paged = menu.pageCount() > 1;
        previous.visible = paged;
        next.visible = paged;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(CRAFTING, leftPos, topPos, 0, 0, 176, 166);
        int x = leftPos + PANEL_X;
        // Six chest rows, a plain strip for the page buttons, then the bottom edge of the chest texture.
        int rowsEnd = 17 + CraftingStationMenu.ROWS * 18;
        graphics.blit(CHEST, x, topPos, 0, 0, 176, rowsEnd);
        // The plain strip under the chest title (grey with side borders), repeated down to the bottom edge.
        for (int y = rowsEnd; y < 159; y += 10) {
            graphics.blit(CHEST, x, topPos + y, 0, 5, 176, Math.min(10, 159 - y));
        }
        graphics.blit(CHEST, x, topPos + 159, 0, 215, 176, 7);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 29, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        var sides = menu.sides();
        if (sides.isEmpty()) {
            graphics.drawWordWrap(font, Component.translatable("gtia.crafting_station.empty"), PANEL_X + 8, 30, 160,
                    0x404040);
            return;
        }
        var side = sides.get(Math.min(menu.selectedTab(), sides.size() - 1));
        graphics.drawString(font, side.name(), PANEL_X + 8, 6, 0x404040, false);
        if (menu.pageCount() > 1) {
            String pages = (menu.selectedPage() + 1) + "/" + menu.pageCount();
            graphics.drawString(font, pages, PANEL_X + 144 - font.width(pages) / 2, 131, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        var sides = menu.sides();
        for (int i = 0; i < sides.size(); i++) {
            int tx = leftPos + PANEL_X + i * 24 + 3;
            int ty = topPos - 19;
            graphics.renderItem(sides.get(i).icon(), tx, ty);
            if (i == menu.selectedTab()) graphics.fill(tx - 2, ty + 17, tx + 18, ty + 19, 0xFF55FF55);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
