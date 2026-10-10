package com.raishxn.gtia.mixin;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;

import com.raishxn.gtia.rules.WorldRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

/**
 * Loads the rules of the world whose data pack repository is being created, before Forge posts
 * {@code AddPackFindersEvent} (where GTCEu generates its recipes).
 */
@Mixin(ServerPacksSource.class)
public abstract class ServerPacksSourceMixin {

    @Inject(method = "createPackRepository(Ljava/nio/file/Path;)Lnet/minecraft/server/packs/repository/PackRepository;",
            at = @At("HEAD"))
    private static void gtia$loadWorldRules(Path datapackDir, CallbackInfoReturnable<PackRepository> cir) {
        WorldRules.prepare(datapackDir);
    }
}
