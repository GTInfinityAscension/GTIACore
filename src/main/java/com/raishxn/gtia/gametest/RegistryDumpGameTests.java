package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Not a check: it writes the item registry and the periodic table of this runtime to
 * {@code build/reports/registry/} so the quest tools can validate item ids and build the Periodic Table chapter
 * from facts instead of guesses. It always passes.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class RegistryDumpGameTests {

    private RegistryDumpGameTests() {}

    private static String id(ItemStack stack) {
        if (stack.isEmpty()) return "";
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void exportRegistryForTools(GameTestHelper h) throws IOException {
        Path dir = FMLPaths.GAMEDIR.get().resolve("..").resolve("build").resolve("reports").resolve("registry")
                .normalize();
        Files.createDirectories(dir);
        Files.write(dir.resolve("items.txt"),
                ForgeRegistries.ITEMS.getKeys().stream().map(ResourceLocation::toString).sorted().toList());

        List<String[]> rows = new ArrayList<>();
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            Element element = material.getElement();
            if (element == null) continue;
            ResourceLocation materialId = material.getResourceLocation();
            ResourceLocation bucket = new ResourceLocation(materialId.getNamespace(), materialId.getPath() + "_bucket");
            rows.add(new String[] { String.valueOf(element.protons()), element.symbol(), element.name(),
                    materialId.toString(), id(ChemicalHelper.get(TagPrefix.ingot, material)),
                    id(ChemicalHelper.get(TagPrefix.dust, material)), id(ChemicalHelper.get(TagPrefix.gem, material)),
                    ForgeRegistries.ITEMS.containsKey(bucket) ? bucket.toString() : "" });
        }
        rows.sort(Comparator.comparingLong((String[] row) -> Long.parseLong(row[0])).thenComparing(row -> row[3]));
        List<String> lines = new ArrayList<>();
        lines.add("protons\tsymbol\tname\tmaterial\tingot\tdust\tgem\tbucket");
        rows.forEach(row -> lines.add(String.join("\t", row)));
        Files.write(dir.resolve("elements.tsv"), lines);
        h.succeed();
    }
}
