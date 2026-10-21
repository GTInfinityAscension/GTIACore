package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes the first preview of some multiblocks to build/multiblock_shapes.json, for the isometric quest images
 * (modpack/tools/iso_render.py). Only runs in the development GameTest server.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class ShapeDumpGameTests {

    private static final List<String> MACHINES = List.of("gtceu:large_chemical_reactor",
            "gtceu:electric_blast_furnace", "gtceu:vacuum_freezer", "gtceu:distillation_tower", "gtceu:cracker",
            "gtceu:steel_large_boiler", "gtceu:large_steam_turbine", "gtia:rocket_assembler", "gtceu:coke_oven",
            "gtna:greenhouse");

    private ShapeDumpGameTests() {}

    @GameTest(template = "qa_empty_5x5", batch = "shape_dump", timeoutTicks = 20)
    public static void dumpMultiblockShapes(GameTestHelper h) throws Exception {
        JsonObject out = new JsonObject();
        // EN: -Pgtia.shapeDump=a:b,c:d adds machines to study (e.g. GTNA references) without editing the list.
        // PT: -Pgtia.shapeDump=a:b,c:d acrescenta máquinas para estudo (ex.: referências do GTNA) sem editar a lista.
        // ZH: -Pgtia.shapeDump=a:b,c:d 可追加要研究的机器（如 GTNA 参考），无需修改列表。
        var ids = new java.util.LinkedHashSet<>(MACHINES);
        for (String extra : System.getProperty("gtia.shapeDump", "").split(","))
            if (!extra.isBlank()) ids.add(extra.trim());
        for (String id : ids) {
            if (!(GTRegistries.MACHINES.get(
                    new net.minecraft.resources.ResourceLocation(id)) instanceof MultiblockMachineDefinition multi))
                continue;
            var shapes = multi.getMatchingShapes();
            if (shapes.isEmpty()) continue;
            var blocks = shapes.get(0).getBlocks();
            JsonArray list = new JsonArray();
            for (int x = 0; x < blocks.length; x++)
                for (int y = 0; y < blocks[x].length; y++)
                    for (int z = 0; z < blocks[x][y].length; z++) {
                        var info = blocks[x][y][z];
                        if (info == null || info.getBlockState().isAir()) continue;
                        var state = info.getBlockState();
                        JsonArray entry = new JsonArray();
                        entry.add(x);
                        entry.add(y);
                        entry.add(z);
                        entry.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                        entry.add(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ?
                                state.getValue(BlockStateProperties.HORIZONTAL_FACING).getName() :
                                state.hasProperty(BlockStateProperties.FACING) ?
                                        state.getValue(BlockStateProperties.FACING).getName() : "");
                        list.add(entry);
                    }
            out.add(id, list);
        }
        Path file = Path.of("..", "build", "multiblock_shapes.json");
        Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
        h.succeed();
    }
}
