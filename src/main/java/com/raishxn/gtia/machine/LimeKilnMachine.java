package com.raishxn.gtia.machine;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;

import net.minecraft.world.entity.player.Player;

/**
 * The Lime Kiln: calcite and a fuel in, quicklime out. It is a Coke Oven underneath, so the Coke Oven Hatch
 * automates it the same way; only the screen changes, to show the second (fuel) slot.
 */
public class LimeKilnMachine extends CokeOvenMachine {

    public LimeKilnMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.PRIMITIVE_BACKGROUND)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(new SlotWidget(importItems.storage, 0, 52, 20, true, true)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT))
                .widget(new SlotWidget(importItems.storage, 1, 52, 44, true, true)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
                .widget(new ProgressWidget(recipeLogic::getProgressPercent, 77, 32, 20, 15,
                        GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR))
                .widget(new SlotWidget(exportItems.storage, 0, 104, 32, true, false)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.PRIMITIVE_SLOT, 7, 84,
                        true));
    }
}
