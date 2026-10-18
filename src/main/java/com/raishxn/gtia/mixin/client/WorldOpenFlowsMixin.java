package com.raishxn.gtia.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;

import com.raishxn.gtia.client.GTIARestartScreen;
import com.raishxn.gtia.rules.WorldRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Opening a saved world whose restart rules differ from this session's asks for a restart first. */
@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Inject(method = "loadLevel", at = @At("HEAD"), cancellable = true)
    private void gtia$restartForRules(Screen lastScreen, String levelName, CallbackInfo ci) {
        var world = Minecraft.getInstance().getLevelSource().getBaseDir().resolve(levelName);
        WorldOpenFlows self = (WorldOpenFlows) (Object) this;
        if (GTIARestartScreen.interceptExistingWorld(lastScreen, WorldRules.readWorld(world),
                () -> self.loadLevel(lastScreen, levelName)))
            ci.cancel();
    }
}
