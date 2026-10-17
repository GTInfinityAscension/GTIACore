package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

/** A fresh survival player for GameTests; the vanilla mock player needs a network channel the test server lacks. */
final class TestPlayers {

    private TestPlayers() {}

    static ServerPlayer survival(GameTestHelper helper) {
        UUID id = UUID.randomUUID();
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(id, "gtia_test_" + id));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        return player;
    }
}
