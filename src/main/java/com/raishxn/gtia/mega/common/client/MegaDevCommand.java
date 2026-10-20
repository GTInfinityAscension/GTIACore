package com.raishxn.gtia.mega.common.client;

import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

/**
 * Development aid for screenshots of a headless client: {@code /gtia_dev_open <pos>} opens the screen of the machine
 * at {@code pos} in the player's dimension, as a right click would; {@code /gtia_dev_power <pos>} makes the player
 * the owner of the megastructure there and fills the player's Nexus network. Only outside production.
 */
public final class MegaDevCommand {

    private MegaDevCommand() {}

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
        event.getDispatcher().register(Commands.literal("gtia_dev_power")
                .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ctx -> {
                    var mc = Minecraft.getInstance();
                    var server = mc.getSingleplayerServer();
                    if (server == null || mc.player == null) return 0;
                    var pos = BlockPosArgument.getBlockPos(ctx, "pos");
                    var uuid = mc.player.getUUID();
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayer(uuid);
                        if (player == null) return;
                        var level = player.serverLevel();
                        if (MetaMachine.getMachine(level, pos) instanceof com.raishxn.gtia.mega.common.MegaMachine m)
                            m.setOwnerForTests(uuid);
                        var network = com.raishxn.gtna.common.data.NexusEnergyNetwork.get(level);
                        var big = com.gtinfinityascension.gtialib.data.Int128
                                .fromString("100000000000000000000000000000");
                        network.setMatrixStats(uuid, 750, 14, 1.0, big, true);
                        network.setMatrixDimension(uuid, level.dimension());
                        com.raishxn.gtna.api.capability.WirelessEnergyManager.addMaxCapacity(level, uuid, big);
                        com.raishxn.gtna.api.capability.WirelessEnergyManager.setEnergy(level, uuid,
                                com.gtinfinityascension.gtialib.data.Int128.fromString("1000000000000000000000000"));
                    });
                    return 1;
                })));
    }
}
