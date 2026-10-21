package com.raishxn.gtia.mega.system;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Tells each player the star of the system they stand in, so the sky draws that star (its kind, colour and size)
 * instead of a fixed sun texture. Sent when it changes: a Star Lifter thinning it, a Suppressor collapsing it, a new
 * system made or one taken apart.
 */
public final class SkyStarNetwork {

    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GTIAMod.MOD_ID, "sky_star"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static final Map<UUID, Sky> SENT = new HashMap<>();

    /** What the client draws: the star of {@link #dimension}, or nothing outside the star systems. */
    public static volatile Sky current = Sky.NONE;

    private SkyStarNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, Sky.class, Sky::write, Sky::read, Sky::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.getServer().getTickCount() % 20 != 0) return;
        var systems = StarSystems.get(e.getServer());
        for (ServerPlayer player : e.getServer().getPlayerList().getPlayers()) {
            var dim = player.level().dimension();
            var star = StarSystems.isSystem(dim) ? systems.star(dim) : null;
            var sky = star == null ? Sky.NONE :
                    new Sky(dim.location().toString(), star.kind, (float) star.mass, (float) star.temperature(),
                            star.drawnScale());
            if (!sky.equals(SENT.get(player.getUUID()))) {
                SENT.put(player.getUUID(), sky);
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), sky);
            }
        }
        if (SENT.size() > e.getServer().getPlayerCount() * 2 + 8) SENT.keySet()
                .removeIf(id -> e.getServer().getPlayerList().getPlayer(id) == null);
    }

    public record Sky(String dimension, Kind kind, float mass, float temperature, float scale) {

        public static final Sky NONE = new Sky("", Kind.NONE, 0, 0, 0);

        void write(FriendlyByteBuf buf) {
            buf.writeUtf(dimension);
            buf.writeEnum(kind);
            buf.writeFloat(mass);
            buf.writeFloat(temperature);
            buf.writeFloat(scale);
        }

        static Sky read(FriendlyByteBuf buf) {
            return new Sky(buf.readUtf(), buf.readEnum(Kind.class), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        static void handle(Sky message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> current = message);
            context.get().setPacketHandled(true);
        }
    }
}
