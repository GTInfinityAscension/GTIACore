package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.raishxn.gtia.GTIAMod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Sweeps over everything the core registers, so a new machine, item or recipe is checked the day it is added
 * instead of when someone opens it by hand: multiblocks form from their own preview, recipes have no empty
 * ingredient, tooltips build, and every name exists in en_us, pt_br and zh_cn.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class IntegrityGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";
    private static final String[] LANGS = { "en_us", "pt_br", "zh_cn" };

    private IntegrityGameTests() {}

    private static boolean ours(ResourceLocation id) {
        return "gtia".equals(id.getNamespace());
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void everyMultiblockFormsFromItsPreview(GameTestHelper h) {
        List<String> failures = new ArrayList<>();
        int checked = 0;
        BlockPos origin = new BlockPos(0, 1, 0);
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            if (!(machine instanceof MultiblockMachineDefinition def) || !ours(def.getId())) continue;
            var shapes = def.getMatchingShapes();
            if (shapes.isEmpty()) {
                failures.add(def.getId().getPath() + " (no preview)");
                continue;
            }
            var shape = shapes.get(0).getBlocks();
            MultiblockControllerMachine controller = null;
            List<BlockPos> placed = new ArrayList<>();
            try {
                for (int x = 0; x < shape.length; x++)
                    for (int y = 0; y < shape[x].length; y++)
                        for (int z = 0; z < shape[x][y].length; z++) {
                            var info = shape[x][y][z];
                            if (info == null || info.getBlockState().isAir()) continue;
                            BlockPos pos = origin.offset(x, y, z);
                            h.setBlock(pos, info.getBlockState());
                            placed.add(pos);
                            if (info.getBlockState().is(def.getBlock()) &&
                                    h.getBlockEntity(pos) instanceof IMachineBlockEntity be &&
                                    be.getMetaMachine() instanceof MultiblockControllerMachine m)
                                controller = m;
                        }
                checked++;
                if (controller == null) failures.add(def.getId().getPath() + " (no controller in preview)");
                else if (!controller.checkPatternWithLock()) {
                    var error = controller.getMultiblockState().error;
                    failures.add(def.getId().getPath() + " (" +
                            (error == null ? "?" : error.getErrorInfo().getString() + " at " + error.getPos()) + ")");
                }
            } catch (RuntimeException e) {
                failures.add(def.getId().getPath() + " (preview threw " + e + ")");
            } finally {
                for (BlockPos pos : placed) h.setBlock(pos, Blocks.AIR);
            }
        }
        GTIAMod.LOGGER.info("[GTIA integrity] {} multiblocks checked, failures: {}", checked, failures);
        h.assertTrue(checked > 0, "no GTIA multiblock was checked");
        h.assertTrue(failures.isEmpty(), "multiblocks that do not form from their preview: " + failures);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void everyRecipeHasRealIngredientsAndOutputs(GameTestHelper h) {
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (var recipe : h.getLevel().getRecipeManager().getRecipes()) {
            if (!(recipe instanceof GTRecipe gt) || !ours(gt.id)) continue;
            checked++;
            String id = gt.id.getPath();
            for (var content : gt.getInputContents(ItemRecipeCapability.CAP)) {
                var items = ItemRecipeCapability.CAP.of(content.content).getItems();
                if (items.length == 0 || items[0].isEmpty()) problems.add(id + ": empty item input");
            }
            for (var content : gt.getInputContents(FluidRecipeCapability.CAP)) {
                var fluids = FluidRecipeCapability.CAP.of(content.content).getStacks();
                if (fluids.length == 0 || fluids[0].isEmpty()) problems.add(id + ": empty fluid input");
            }
            for (var content : gt.getOutputContents(ItemRecipeCapability.CAP)) {
                var items = ItemRecipeCapability.CAP.of(content.content).getItems();
                if (items.length == 0 || items[0].isEmpty()) problems.add(id + ": empty item output");
            }
            if (gt.duration <= 0) problems.add(id + ": duration " + gt.duration);
        }
        h.assertTrue(checked > 0, "no GTIA GT recipe was found");
        h.assertTrue(problems.isEmpty(), checked + " recipes checked; " + String.join("; ", problems));
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void everyMachineTooltipBuilds(GameTestHelper h) {
        List<String> failures = new ArrayList<>();
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            if (!ours(definition.getId())) continue;
            BiConsumer<ItemStack, List<Component>> builder = definition.getTooltipBuilder();
            if (builder == null) continue;
            List<Component> tooltip = new ArrayList<>();
            try {
                builder.accept(ItemStack.EMPTY, tooltip);
                for (Component line : tooltip)
                    if (line.getString().startsWith("gtia.")) failures.add(definition.getId() + ": raw key " + line.getString());
            } catch (RuntimeException e) {
                failures.add(definition.getId() + " threw " + e);
            }
        }
        h.assertTrue(failures.isEmpty(), String.join("; ", failures));
        h.succeed();
    }

    private static JsonObject lang(String code) throws Exception {
        String path = "/assets/gtia/lang/" + code + ".json";
        try (InputStream in = IntegrityGameTests.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("missing " + path);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    /** Every pack mod ships en_us, pt_br and zh_cn together; a name missing in one language shows a raw key. */
    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void everyNameExistsInAllThreeLanguages(GameTestHelper h) throws Exception {
        JsonObject[] langs = new JsonObject[LANGS.length];
        for (int i = 0; i < LANGS.length; i++) langs[i] = lang(LANGS[i]);
        List<String> missing = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        ForgeRegistries.ITEMS.getEntries().forEach(e -> {
            if (ours(e.getKey().location())) keys.add(e.getValue().getDescriptionId());
        });
        ForgeRegistries.BLOCKS.getEntries().forEach(e -> {
            if (ours(e.getKey().location())) keys.add(e.getValue().getDescriptionId());
        });
        // GTCEu material items share runtime-built names (tagprefix.*, item.gtceu.bucket); only our own keys count.
        for (String key : keys.stream().filter(k -> k.contains(".gtia.")).distinct().sorted().toList())
            for (int i = 0; i < LANGS.length; i++)
                if (!langs[i].has(key)) missing.add(LANGS[i] + ":" + key);
        for (String key : langs[0].keySet())
            for (int i = 1; i < LANGS.length; i++)
                if (!langs[i].has(key)) missing.add(LANGS[i] + ":" + key);
        h.assertTrue(missing.isEmpty(), missing.size() + " missing translations: " +
                String.join(", ", missing.subList(0, Math.min(40, missing.size()))));
        h.succeed();
    }
}
