package com.raishxn.gtia.mega.matrioshka.client;

import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

/**
 * Development aid for screenshots of a headless client: {@code /gtia_dev_open <pos>} opens the screen of the machine
 * at {@code pos} in the player's dimension, as a right click would. Only outside production.
 */
public final class MatrioshkaDevCommand {

    private MatrioshkaDevCommand() {}

    public static void register(RegisterClientCommandsEvent event) {
        if (net.minecraftforge.fml.loading.FMLEnvironment.production) return;
        event.getDispatcher().register(Commands.literal("gtia_dev_open")
                .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ctx -> {
                    var mc = Minecraft.getInstance();
                    var server = mc.getSingleplayerServer();
                    if (server == null || mc.player == null) return 0;
                    var pos = BlockPosArgument.getBlockPos(ctx, "pos");
                    var uuid = mc.player.getUUID();
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayer(uuid);
                        var machine = player == null ? null : MetaMachine.getMachine(player.serverLevel(), pos);
                        if (machine != null) MachineUIFactory.INSTANCE.openUI(machine, player);
                    });
                    return 1;
                })));
    }
}
