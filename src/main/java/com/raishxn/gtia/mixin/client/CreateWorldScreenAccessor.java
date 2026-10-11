package com.raishxn.gtia.mixin.client;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;

import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;
import java.util.function.Consumer;

/** What the GTIA Rules screen needs to rebuild the create-world data after the preset changes. */
@Mixin(CreateWorldScreen.class)
public interface CreateWorldScreenAccessor {

    @Accessor("tempDataPackRepository")
    void gtia$setTempDataPackRepository(PackRepository repository);

    @Invoker("getDataPackSelectionSettings")
    Pair<Path, PackRepository> gtia$getDataPackSelectionSettings(WorldDataConfiguration configuration);

    @Invoker("onCreate")
    void gtia$onCreate();

    @Invoker("applyNewPackConfig")
    void gtia$applyNewPackConfig(PackRepository repository, WorldDataConfiguration configuration,
                                 Consumer<WorldDataConfiguration> onFailure);
}
