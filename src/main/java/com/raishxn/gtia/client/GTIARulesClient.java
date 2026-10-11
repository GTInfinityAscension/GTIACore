package com.raishxn.gtia.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;

import com.raishxn.gtia.rules.WorldRules;

/** Client hooks of the GTIA Rules screen. */
public final class GTIARulesClient {

    private GTIARulesClient() {}

    /** The pause menu gets a small "GTIA Rules" button that shows the rules of the world being played. */
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen pause && WorldRules.inWorld()) {
            event.addListener(Button.builder(Component.translatable("gtia.rules.screen.pause"),
                    b -> GTIARulesScreen.openForCurrentWorld(pause)).bounds(4, 4, 98, 20).build());
        }
    }
}
