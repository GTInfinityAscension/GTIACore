package com.raishxn.gtia.mixin.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

import com.raishxn.gtia.client.GTIARulesScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the "GTIA Rules: <preset>" button under the vanilla options of the Game tab. */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$GameTab")
public abstract class CreateWorldGameTabMixin extends GridLayoutTab {

    private CreateWorldGameTabMixin(Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtia$addRulesButton(CreateWorldScreen screen, CallbackInfo ci) {
        Button button = Button.builder(GTIARulesScreen.buttonLabel(), b -> GTIARulesScreen.openForNewWorld(screen))
                .width(210)
                .tooltip(net.minecraft.client.gui.components.Tooltip
                        .create(Component.translatable("gtia.rules.screen.button.tooltip")))
                .build();
        this.layout.addChild(button, 4, 0, this.layout.newCellSettings());
    }
}
