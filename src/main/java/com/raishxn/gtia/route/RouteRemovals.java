package com.raishxn.gtia.route;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * EN: GTCEu and GTNA recipes that the Block 1 routes replace (GTIA-D-303 to GTIA-D-308). Each one comes back in
 * {@link RouteRecipes} or {@link ElectrolyzerRebalance} with the new tier or outputs, except the dust-to-fluid
 * extractions of EBF-only materials, which skipped the blast furnace.
 * PT: Receitas do GTCEu e do GTNA trocadas pelas rotas do Bloco 1 (GTIA-D-303 a GTIA-D-308). Cada uma volta em
 * {@link RouteRecipes} ou {@link ElectrolyzerRebalance} com o tier ou as saídas novas, menos as extrações pó → fluido
 * dos materiais de EBF, que pulavam o alto-forno.
 * ZH: 第一阶段路线替换的 GTCEu 与 GTNA 配方 (GTIA-D-303 至 GTIA-D-308)。除了跳过电力高炉的粉 → 流体提取,
 * 其余都以新的电压或产物在 RouteRecipes 或 ElectrolyzerRebalance 中重新加入。
 */
public final class RouteRemovals {

    /** Materials only the EBF makes, whose dust melted straight into fluid (GTIA-D-305). */
    static final List<String> EBF_ONLY_DUSTS = List.of("advanced_nitinol", "astral_titanium",
            "atomic_separation_catalyst", "celestial_tungsten", "chronomatic_glass", "nickel_zinc_ferrite",
            "polycaprolactam", "quantum", "shijima", "superconductor_uiv_base", "superconductor_umv_base",
            "tengam_purified", "transcendent_metal");

    static final List<String> REMOVED = build();

    private RouteRemovals() {}

    private static List<String> build() {
        List<String> ids = new ArrayList<>();
        // IOP controller: Assembler (EV) -> Assembly Line (IV)
        ids.add("gtceu:assembler/integrated_ore_processor");
        // Electrolyzer: silicates to MV, aluminium to alumina, bastnasite to rare earth
        ElectrolyzerRebalance.ENTRIES.forEach(e -> ids.add("gtceu:electrolyzer/decomposition_electrolyzing_" +
                e.material().getName()));
        for (String m : EBF_ONLY_DUSTS) {
            ids.add("gtceu:extractor/extract_" + m + "_dust");
            ids.add("gtceu:liquefaction_furnace/extract_" + m + "_dust");
        }
        // Aluminium dust in the EBF: 120 -> 480 EU/t
        ids.add("gtceu:electric_blast_furnace/blast_aluminium");
        ids.add("gtceu:electric_blast_furnace/blast_aluminium_gas");
        return List.copyOf(ids);
    }

    public static void removeRecipes(Consumer<ResourceLocation> consumer) {
        REMOVED.forEach(id -> consumer.accept(new ResourceLocation(id)));
    }

    public static List<ResourceLocation> ids() {
        return REMOVED.stream().map(ResourceLocation::new).toList();
    }
}
