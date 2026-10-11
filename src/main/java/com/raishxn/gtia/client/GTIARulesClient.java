package com.raishxn.gtia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;

import com.raishxn.gtia.rules.WorldRules;

/** Client command {@code /gtia rules}, which shows the read-only rules of the world being played. */
public final class GTIARulesClient {

    private static boolean openRequested;

    private GTIARulesClient() {}

    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gtia").then(Commands.literal("rules").executes(ctx -> {
            if (!WorldRules.inWorld()) {
                ctx.getSource().sendFailure(Component.translatable("gtia.rules.command.no_world"));
                return 0;
            }
            // The chat screen closes after the command runs, so the rules screen opens on the next tick.
            openRequested = true;
            return 1;
        })));
    }

    /** A server's rules arrived and need the player's attention (for example, a restart). */
    public static void showWarning(String key) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.translatable(key, WorldRules.active().preset().id())
                    .withStyle(net.minecraft.ChatFormatting.GOLD), false);
        }
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !openRequested) return;
        openRequested = false;
        if (WorldRules.inWorld()) GTIARulesScreen.openForCurrentWorld(Minecraft.getInstance().screen);
    }
}
