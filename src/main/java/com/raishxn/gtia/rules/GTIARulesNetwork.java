package com.raishxn.gtia.rules;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.raishxn.gtia.GTIAMod;

import java.util.Optional;
import java.util.function.Supplier;

/** Sends the world's rules to each player on join, so client tooltips show the values of the world they are in. */
public final class GTIARulesNetwork {

    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GTIAMod.MOD_ID, "rules"), () -> VERSION, VERSION::equals, VERSION::equals);

    private GTIARulesNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, Sync.class, Sync::write, Sync::read, Sync::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Sync(WorldRules.activeJson()));
    }

    public record Sync(String json) {

        void write(FriendlyByteBuf buf) {
            buf.writeUtf(json);
        }

        static Sync read(FriendlyByteBuf buf) {
            return new Sync(buf.readUtf());
        }

        static void handle(Sync message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                WorldRules.applyFromServer(message.json());
                String warning = WorldRules.takeWarning();
                if (warning != null) com.raishxn.gtia.client.GTIARulesClient.showWarning(warning);
            });
            context.get().setPacketHandled(true);
        }
    }
}
