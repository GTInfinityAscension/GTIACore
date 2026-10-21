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
        // /gtia_dev_click <pos> <action>: runs a screen button's action on the server copy of the machine.
        event.getDispatcher().register(Commands.literal("gtia_dev_click")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("action", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    var mc = Minecraft.getInstance();
                                    var server = mc.getSingleplayerServer();
                                    if (server == null || mc.player == null) return 0;
                                    var pos = BlockPosArgument.getBlockPos(ctx, "pos");
                                    String action = com.mojang.brigadier.arguments.StringArgumentType
                                            .getString(ctx, "action");
                                    var dim = mc.player.level().dimension();
                                    // A server-side click: what the screen sends, read back on the server.
                                    var buf = new net.minecraft.network.FriendlyByteBuf(
                                            io.netty.buffer.Unpooled.buffer());
                                    new com.lowdragmc.lowdraglib.gui.util.ClickData().writeToBuf(buf);
                                    var click = com.lowdragmc.lowdraglib.gui.util.ClickData.readFromBuf(buf);
                                    server.execute(() -> {
                                        var level = server.getLevel(dim);
                                        if (level != null && MetaMachine.getMachine(level, pos) instanceof
                                                com.raishxn.gtia.mega.common.MegaMachine m)
                                            m.handleDisplayClick(action, click);
                                    });
                                    return 1;
                                }))));
        event.getDispatcher().register(Commands.literal("gtia_fx_debug").then(Commands
                .argument("mask", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0)).executes(ctx -> {
                    MegaFxRenderer.debugMask = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx,
                            "mask");
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
        event.getDispatcher().register(Commands.literal("gtia_dev_fill")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("fluid", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                .then(Commands.argument("mb", com.mojang.brigadier.arguments.IntegerArgumentType
                                        .integer(1)).executes(ctx -> {
                    var mc = Minecraft.getInstance();
                    var server = mc.getSingleplayerServer();
                    if (server == null || mc.player == null) return 0;
                    var pos = BlockPosArgument.getBlockPos(ctx, "pos");
                    var id = ctx.getArgument("fluid", net.minecraft.resources.ResourceLocation.class);
                    int mb = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "mb");
                    var dim = mc.player.level().dimension();
                    server.execute(() -> {
                        var level = server.getLevel(dim);
                        var fluid = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getValue(id);
                        if (level == null || fluid == null) return;
                        if (MetaMachine.getMachine(level, pos) instanceof com.raishxn.gtia.mega.common.MegaMachine m) {
                            long left = m.fillInputForTests(new net.minecraftforge.fluids.FluidStack(fluid, mb));
                            com.raishxn.gtia.GTIAMod.LOGGER.info("gtia_dev_fill {} {} mB, {} did not fit", id, mb, left);
                        }
                    });
                    return 1;
                })))));
    }
}
