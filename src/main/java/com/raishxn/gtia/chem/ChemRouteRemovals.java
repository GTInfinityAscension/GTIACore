package com.raishxn.gtia.chem;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

/**
 * EN: GTCEu and GTNA direct syntheses that made the GTIA chemistry lines pointless (GTIA-D-294). Only direct
 * syntheses go; by-products of distillation, ore processing, recycling and decomposition of other materials stay.
 * Removed in both modes. The GTCEu silicon boule stays: the Siemens Reactor needs HV circuits, which need wafers,
 * so without it the line would be a loop; the Czochralski boule is the cheaper upgrade. The list and the
 * reason for each line are in design/propostas/REMOCAO_ROTAS_QUIMICAS.md.
 * PT: Sínteses diretas do GTCEu e do GTNA que tornavam inúteis as linhas químicas do GTIA (GTIA-D-294). Só saem
 * as sínteses diretas; subprodutos de destilação, minério, reciclagem e decomposição de outros materiais ficam.
 * Removidas nos dois modos. O boule de silício do GTCEu fica: o Siemens pede circuitos HV, que pedem wafers.
 * ZH: 移除使 GTIA 化工线失去意义的 GTCEu 与 GTNA 直接合成配方（GTIA-D-294）。只移除直接合成；蒸馏、矿石处理、
 * 回收和其他材料分解的副产物保留。两种模式均移除。GTCEu 的硅单晶锭保留：Siemens 反应器需要 HV 电路，而 HV 电路需要晶圆。
 */
public final class ChemRouteRemovals {

    static final List<String> REMOVED = List.of(
            // Aluminium: Bayer + Hall-Heroult
            "gtceu:electrolyzer/bauxite_electrolysis",
            "gtceu:electrolyzer/decomposition_electrolyzing_sapphire",
            "gtceu:electrolyzer/decomposition_electrolyzing_green_sapphire",
            "gtceu:electrolyzer/decomposition_electrolyzing_aluminium_sulfite",
            "gtceu:electrolyzer/decomposition_electrolyzing_kyanite",
            "gtceu:electrolyzer/decomposition_electrolyzing_clay",
            "gtceu:large_chemical_reactor/bauxite_sludge_from_slurry",
            // Sulfuric acid: Contact Converter
            "gtceu:chemical_reactor/sulfuric_acid_from_trioxide",
            "gtceu:large_chemical_reactor/sulfuric_acid_from_trioxide",
            "gtceu:large_chemical_reactor/sulfuric_acid_from_sulfur",
            "gtceu:chemical_reactor/sulfuric_acid_from_sulfide",
            "gtceu:large_chemical_reactor/sulfuric_acid_from_sulfide",
            // Ammonia: Ammonia Converter
            "gtceu:chemical_reactor/ammonia_from_elements",
            "gtceu:large_chemical_reactor/ammonia_from_elements",
            "gtceu:large_chemical_reactor/ammonia_shortcut",
            // Nitric acid: Ostwald Converter
            "gtceu:large_chemical_reactor/nitric_acid_from_elements",
            "gtceu:large_chemical_reactor/nitric_acid_from_ammonia",
            "gtceu:chemical_reactor/nitric_acid_from_dioxide_1",
            "gtceu:large_chemical_reactor/nitric_acid_from_dioxide_1",
            "gtceu:chemical_reactor/nitric_acid_from_dioxide_2",
            "gtceu:large_chemical_reactor/nitric_acid_from_dioxide_2",
            // Chlorine and sodium hydroxide: Diaphragm Cell
            "gtceu:electrolyzer/salt_water_electrolysis",
            // Soda ash: Lime Kiln (Solvay)
            "gtceu:chemical_reactor/soda_ash_from_carbon_dioxide",
            "gtceu:large_chemical_reactor/soda_ash_from_carbon_dioxide",
            "gtceu:electric_blast_furnace/soda_ash_from_calcite",
            "gtna:godforge_molten/godforge_molten/gtceu/soda_ash_from_calcite",
            // Titanium: Kroll Reactor
            "gtceu:chemical_reactor/titanium_tetrachloride",
            "gtceu:large_chemical_reactor/titanium_tetrachloride",
            "gtceu:electric_blast_furnace/titanium_from_tetrachloride",
            "gtna:godforge_molten/godforge_molten/gtceu/titanium_from_tetrachloride",
            // Quicklime: Lime Kiln
            "gtceu:chemical_reactor/quicklime_from_calcite",
            "gtceu:large_chemical_reactor/quicklime_from_calcite",
            // Ethylene: Catalytic Dehydrator (cracking stays)
            "gtceu:chemical_reactor/ethylene_from_ethanol",
            "gtceu:large_chemical_reactor/ethylene_from_ethanol",
            "gtceu:chemical_reactor/ethylene_from_glycerol",
            "gtceu:large_chemical_reactor/ethylene_from_glycerol",
            // Wood: Wood Retort + Still (coal pyrolysis stays)
            "gtceu:pyrolyse_oven/log_to_charcoal_byproducts",
            "gtceu:pyrolyse_oven/log_to_wood_tar",
            "gtceu:pyrolyse_oven/log_to_wood_tar_nitrogen",
            "gtceu:pyrolyse_oven/log_to_wood_vinegar",
            "gtceu:pyrolyse_oven/log_to_wood_vinegar_nitrogen",
            "gtceu:pyrolyse_oven/log_to_wood_gas",
            "gtceu:pyrolyse_oven/log_to_wood_gas_nitrogen",
            "gtceu:pyrolyse_oven/log_to_creosote",
            "gtceu:pyrolyse_oven/log_to_creosote_nitrogen",
            "gtceu:pyrolyse_oven/log_to_heavy_oil",
            "gtceu:pyrolyse_oven/log_to_coal_gas");

    private ChemRouteRemovals() {}

    public static void removeRecipes(Consumer<ResourceLocation> consumer) {
        REMOVED.forEach(id -> consumer.accept(new ResourceLocation(id)));
    }

    public static List<ResourceLocation> ids() {
        return REMOVED.stream().map(ResourceLocation::new).toList();
    }
}
