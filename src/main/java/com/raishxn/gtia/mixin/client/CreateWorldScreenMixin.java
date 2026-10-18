package com.raishxn.gtia.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

import com.raishxn.gtia.client.GTIARestartScreen;
import com.raishxn.gtia.rules.WorldRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A fresh create-world screen starts from the default preset, not from the rules of the last world played; creating a
 * world whose rules need a restart asks for it first (as GTO does with its difficulty).
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Inject(method = "openFresh", at = @At("HEAD"))
    private static void gtia$resetRules(Minecraft minecraft, Screen lastScreen, CallbackInfo ci) {
        WorldRules.beginNewWorld();
    }

    @Inject(method = "onCreate", at = @At("HEAD"), cancellable = true)
    private void gtia$restartForRules(CallbackInfo ci) {
        if (GTIARestartScreen.interceptNewWorld((Screen) (Object) this, WorldRules.pendingOrDefault())) ci.cancel();
    }
}
