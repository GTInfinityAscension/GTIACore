package com.raishxn.gtia.miner;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.raishxn.gtia.GTIAMod;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * The veins with purity (GTIA-D-310): GTCEu virtual bedrock veins (3 × 3 chunks, one every 16 chunks), never depleted.
 * The GTCEu Bedrock Ore Miner stays off ({@code doBedrockOres: false}); only the Entangled Miner mines them.
 */
public final class BedrockVeins {

    private BedrockVeins() {}

    public static void register() {
        Set<ResourceKey<Level>> overworld = Set.of(Level.OVERWORLD), nether = Set.of(Level.NETHER),
                end = Set.of(Level.END);
        vein("iron", 40, overworld, Magnetite, 3, Iron, 2, VanadiumMagnetite, 1);
        vein("copper", 40, overworld, Chalcopyrite, 3, Copper, 2, Pyrite, 1);
        vein("tin", 30, overworld, Cassiterite, 3, Tin, 2);
        vein("coal", 35, overworld, Coal, 4, Graphite, 1);
        vein("lead_silver", 25, overworld, Galena, 3, Lead, 2, Silver, 1);
        vein("redstone", 25, overworld, Redstone, 3, Ruby, 1, Cinnabar, 1);
        vein("nickel", 20, overworld, Garnierite, 2, Nickel, 2, Cobaltite, 1, Pentlandite, 1);
        vein("bauxite", 20, overworld, Bauxite, 3, Ilmenite, 1);
        vein("gold", 15, overworld, Gold, 2, Magnetite, 1, VanadiumMagnetite, 1);
        vein("salts", 15, overworld, Salt, 2, RockSalt, 2, Lepidolite, 1, Spodumene, 1);
        vein("nether_quartz", 30, nether, NetherQuartz, 3, Quartzite, 1);
        vein("sulfur", 30, nether, Sulfur, 3, Pyrite, 2, Sphalerite, 1);
        vein("pitchblende", 20, end, Pitchblende, 2, Uraninite, 1);
        vein("scheelite", 20, end, Scheelite, 2, Tungstate, 1, Lithium, 1);
    }

    /** {@code materials}: material, weight, material, weight... */
    private static void vein(String name, int weight, Set<ResourceKey<Level>> dimensions, Object... materials) {
        var builder = BedrockOreDefinition.builder(GTIAMod.id(name)).weight(weight).size(3).yield(1, 1)
                .depletionAmount(0).depletionChance(0).depletedYield(1).dimensions(dimensions);
        for (int i = 0; i < materials.length; i += 2) builder.material((Material) materials[i], (int) materials[i + 1]);
        builder.register();
    }

    /** A vein the Vein Card can record: dimension, origin chunk and GTCEu definition. */
    public record Vein(ResourceLocation dimension, int originX, int originZ, BedrockOreDefinition definition) {

        public ResourceLocation id() {
            return GTRegistries.BEDROCK_ORE_DEFINITIONS.getKey(definition);
        }

        public VeinPurity purity(long seed) {
            return VeinPurity.of(seed, dimension.toString(), originX, originZ);
        }
    }

    /** Origin of the vein covering chunk {@code c} on one axis, or {@code Integer.MIN_VALUE} if none can. */
    public static int origin(int c) {
        int distance = ConfigHolder.INSTANCE.worldgen.oreVeins.bedrockOreDistance;
        int o = Math.round((float) c / distance) * distance;
        return Math.abs(c - o) <= BedrockOreVeinSavedData.VEIN_CHUNK_SIZE / 2 ? o : Integer.MIN_VALUE;
    }

    /** The vein under chunk ({@code chunkX}, {@code chunkZ}), or null outside every vein. */
    @Nullable
    public static Vein at(ServerLevel level, int chunkX, int chunkZ) {
        int ox = origin(chunkX), oz = origin(chunkZ);
        if (ox == Integer.MIN_VALUE || oz == Integer.MIN_VALUE) return null;
        return veinAt(level, ox, oz);
    }

    /** The vein whose origin is chunk ({@code originX}, {@code originZ}), or null. */
    @Nullable
    public static Vein veinAt(ServerLevel level, int originX, int originZ) {
        var entry = BedrockOreVeinSavedData.getOrCreate(level).getOreVeinWorldEntry(originX, originZ);
        var definition = entry.getDefinition();
        return definition == null ? null :
                new Vein(level.dimension().location(), originX, originZ, definition);
    }
}
