package com.raishxn.gtia.route;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * EN: GTNH-style changes to the GTCEu electrolyzer decompositions (GTIA-D-304). Silicates and clays cost MV
 * (120 EU/t); aluminosilicates and ruby give Alumina instead of Aluminium, which then goes through the Hall-Héroult
 * Cell; bastnasite gives Rare Earth instead of pure cerium. Same formula as GTCEu's DecompositionRecipeHandler.
 * PT: Mudanças no estilo do GTNH nas decomposições do eletrolisador do GTCEu (GTIA-D-304). Silicatos e argilas
 * passam a MV (120 EU/t); aluminossilicatos e rubi dão Alumina no lugar de Alumínio, que segue para a Hall-Héroult;
 * bastnasita dá Rare Earth em vez de cério puro. Mesma fórmula do DecompositionRecipeHandler do GTCEu.
 * ZH: 仿照 GTNH 修改 GTCEu 电解机的分解配方 (GTIA-D-304)。硅酸盐和黏土改为 MV (120 EU/t); 铝硅酸盐和红宝石
 * 产出氧化铝而不是铝, 再送入霍尔-埃鲁电解槽; 氟碳铈矿产出稀土而不是纯铈。公式与 GTCEu 的分解配方相同。
 */
public final class ElectrolyzerRebalance {

    record Entry(Material material, boolean mv, boolean alumina, Material cerium) {}

    static final List<Entry> ENTRIES = List.of(
            silicate(GTMaterials.Andradite), silicate(GTMaterials.Asbestos), silicate(GTMaterials.Ferrosilite),
            silicate(GTMaterials.FullersEarth), silicate(GTMaterials.Obsidian), silicate(GTMaterials.Olivine),
            silicate(GTMaterials.Soapstone), silicate(GTMaterials.Talc), silicate(GTMaterials.Uvarovite),
            aluminosilicate(GTMaterials.Almandine), aluminosilicate(GTMaterials.Alunite),
            aluminosilicate(GTMaterials.Biotite), aluminosilicate(GTMaterials.BlueTopaz),
            aluminosilicate(GTMaterials.Emerald), aluminosilicate(GTMaterials.GlauconiteSand),
            aluminosilicate(GTMaterials.Grossular), aluminosilicate(GTMaterials.Lazurite),
            aluminosilicate(GTMaterials.Mica), aluminosilicate(GTMaterials.Pollucite),
            aluminosilicate(GTMaterials.PotassiumFeldspar), aluminosilicate(GTMaterials.Pyrope),
            aluminosilicate(GTMaterials.Sodalite), aluminosilicate(GTMaterials.Spessartine),
            aluminosilicate(GTMaterials.Spodumene), aluminosilicate(GTMaterials.Topaz),
            aluminosilicate(GTMaterials.Zeolite),
            new Entry(GTMaterials.Ruby, false, true, null),
            new Entry(GTMaterials.Bastnasite, false, false, GTMaterials.RareEarth));

    private ElectrolyzerRebalance() {}

    private static Entry silicate(Material m) {
        return new Entry(m, true, false, null);
    }

    private static Entry aluminosilicate(Material m) {
        return new Entry(m, true, true, null);
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        ENTRIES.forEach(e -> decompose(provider, e));
    }

    private static void decompose(Consumer<FinishedRecipe> provider, Entry e) {
        Material material = e.material();
        long input = 0, aluminium = 0;
        Map<Material, Long> dusts = new LinkedHashMap<>();
        Map<Material, Long> fluids = new LinkedHashMap<>();
        for (MaterialStack c : material.getMaterialComponents()) {
            input += c.amount();
            Material out = c.material() == GTMaterials.Cerium && e.cerium() != null ? e.cerium() : c.material();
            if (out == GTMaterials.Aluminium && e.alumina()) aluminium += c.amount();
            else if (out.hasProperty(PropertyKey.DUST)) dusts.merge(out, c.amount(), Long::sum);
            else if (out.hasProperty(PropertyKey.FLUID)) fluids.merge(out, c.amount() * 1000, Long::sum);
        }
        // EN: 2 Al + 3 O -> 1 Al2O3: an odd aluminium count doubles the batch, and the oxygen goes into the alumina.
        // PT: 2 Al + 3 O -> 1 Al2O3: com alumínio ímpar o lote dobra, e o oxigênio vai para a alumina.
        // ZH: 2 Al + 3 O -> 1 Al2O3: 铝为奇数时批量加倍, 氧进入氧化铝。
        if (aluminium > 0) {
            long scale = aluminium % 2 == 0 ? 1 : 2, oxygen = 1500 * aluminium * scale;
            input *= scale;
            dusts.replaceAll((m, n) -> n * scale);
            fluids.replaceAll((m, mb) -> mb * scale);
            dusts.merge(GTIAAlloys.ALUMINA, oxygen / 3000, Long::sum);
            fluids.computeIfPresent(GTMaterials.Oxygen, (m, mb) -> Math.max(0, mb - oxygen));
            fluids.values().removeIf(mb -> mb == 0);
        }
        // Lowest common divisor of the input, the dusts and the fluids (in buckets), as GTCEu does.
        long divisor = input;
        for (long n : dusts.values()) divisor = gcd(divisor, n);
        for (long mb : fluids.values()) divisor = mb % 1000 == 0 ? gcd(divisor, mb / 1000) : 1;
        final long d = Math.max(1, divisor);

        int components = material.getMaterialComponents().size();
        int eut = e.mv() ? GTValues.VA[GTValues.MV] : components <= 2 ? GTValues.VA[GTValues.LV] :
                2 * GTValues.VA[GTValues.LV];
        List<ItemStack> outItems = new ArrayList<>();
        dusts.forEach((m, n) -> outItems.add(ChemicalHelper.get(TagPrefix.dust, m, (int) (n / d))));
        List<FluidStack> outFluids = new ArrayList<>();
        fluids.forEach((m, mb) -> outFluids.add(m.getFluid((int) (mb / d))));
        GTRecipeTypes.ELECTROLYZER_RECIPES
                .recipeBuilder(GTIAMod.id("decomposition_electrolyzing_" + material.getName()))
                .inputItems(TagPrefix.dust, material, (int) (input / d))
                .outputItems(outItems.toArray(ItemStack[]::new))
                .outputFluids(outFluids.toArray(FluidStack[]::new))
                .duration((int) (material.getProtons() * (input / d) * 2L))
                .EUt(eut)
                .save(provider);
    }

    private static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
