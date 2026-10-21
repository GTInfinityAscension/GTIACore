package com.raishxn.gtia.route;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * AE2 in MV, GTO-style (GTIA-D-349): prints from the MV Laser Engraver, processors in the Circuit Assembler at LV
 * voltage (as in GTO; the engraver is the gate), the network blocks
 * from the MV Assembler; the refinement (wireless, P2P, ExtendedAE's Assembler Matrix and extended providers,
 * storage above 4k) in HV. The Inscriber and ExtendedAE's Circuit Cutter go, so presses only work in the engraver.
 * Recipes that only turn a block into its part (and back) stay.
 */
public final class AE2Gates {

    private static final int MV = GTValues.VA[GTValues.MV];
    private static final int HV = GTValues.VA[GTValues.HV];
    private static final int EV = GTValues.VA[GTValues.EV];

    static final List<String> REMOVED = List.of(
            "ae2:network/blocks/inscribers",
            "ae2:inscriber/calculation_processor_print", "ae2:inscriber/engineering_processor_print",
            "ae2:inscriber/logic_processor_print", "ae2:inscriber/silicon_print",
            "ae2:inscriber/calculation_processor", "ae2:inscriber/engineering_processor",
            "ae2:inscriber/logic_processor",
            "expatternprovider:circuit_cutter", "expatternprovider:cutter/calculation",
            "expatternprovider:cutter/engineering", "expatternprovider:cutter/logic",
            "expatternprovider:cutter/silicon",
            "ae2:materials/annihilationcore", "ae2:materials/formationcore",
            "ae2:network/blocks/controller", "ae2:network/blocks/storage_drive", "ae2:network/blocks/storage_chest",
            "ae2:network/cells/item_cell_housing", "ae2:network/cells/fluid_cell_housing",
            "ae2:network/cells/item_storage_components_cell_1k_part",
            "ae2:network/cells/item_storage_components_cell_4k_part",
            "ae2:network/cells/item_storage_components_cell_16k_part",
            "ae2:network/cells/item_storage_components_cell_64k_part",
            "ae2:network/cells/item_storage_components_cell_256k_part",
            "ae2:network/parts/terminals", "ae2:network/parts/terminals_crafting",
            "ae2:network/parts/terminals_pattern_encoding", "ae2:network/parts/terminals_pattern_access",
            "ae2:network/blocks/pattern_providers_interface", "ae2:network/blocks/interfaces_interface",
            "ae2:network/crafting/molecular_assembler", "ae2:network/crafting/cpu_crafting_unit",
            "ae2:network/wireless_access_point", "ae2:network/wireless_terminal", "ae2:network/parts/tunnels_me",
            "expatternprovider:assembler_matrix_frame", "expatternprovider:assembler_matrix_crafter",
            "expatternprovider:assembler_matrix_pattern", "expatternprovider:assembler_matrix_speed",
            "expatternprovider:epp", "expatternprovider:ei");

    private AE2Gates() {}

    /**
     * EN: AE2 and ExtendedAE datapacks load above GTCEu's pack filter, so the recipes are dropped after loading, like
     * the Ad Astra and Productive Bees filters: when the server is up and after each /reload.
     * PT: Os datapacks do AE2 e do ExtendedAE carregam acima do filtro do GTCEu, então as receitas saem depois da
     * carga, como nos filtros do Ad Astra e do Productive Bees: com o servidor pronto e após cada /reload.
     * ZH: AE2 和 ExtendedAE 的数据包加载顺序高于 GTCEu 的过滤器，因此在加载后移除配方，与 Ad Astra、Productive Bees
     * 过滤器相同：服务器启动后及每次 /reload 后执行。
     */
    public static void onServerStarted(ServerStartedEvent event) {
        filter(event.getServer());
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) filter(event.getPlayerList().getServer());
    }

    public static int filter(MinecraftServer server) {
        Set<ResourceLocation> dropped = Set.copyOf(ids());
        var manager = server.getRecipeManager();
        var all = manager.getRecipes();
        var kept = all.stream().filter(r -> !dropped.contains(r.getId())).toList();
        int count = all.size() - kept.size();
        if (count > 0) {
            manager.replaceRecipes(kept);
            GTIAMod.LOGGER.info("Dropped {} AE2 recipes now made in GT machines (GTIA-D-349)", count);
        }
        return count;
    }

    public static List<ResourceLocation> ids() {
        return REMOVED.stream().map(ResourceLocation::new).toList();
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        if (!ModList.get().isLoaded("ae2")) return;
        processors(provider);
        network(provider);
        storage(provider);
        if (ModList.get().isLoaded("expatternprovider")) extendedAE(provider);
    }

    private static void processors(Consumer<FinishedRecipe> provider) {
        String[][] kinds = { { "calculation", "certus_quartz" }, { "engineering", "diamond" }, { "logic", "gold" },
                { "silicon", "silicon" } };
        for (String[] kind : kinds) {
            String print = kind[0].equals("silicon") ? "ae2:printed_silicon" : "ae2:printed_" + kind[0] + "_processor";
            GTRecipeTypes.LASER_ENGRAVER_RECIPES.recipeBuilder(GTIAMod.id("ae2/printed_" + kind[0]))
                    .inputItems(item("gtceu:" + kind[1] + "_plate", 1))
                    .notConsumable(item("ae2:" + kind[0] + (kind[0].equals("silicon") ? "" : "_processor") + "_press",
                            1))
                    .outputItems(item(print, 1))
                    .duration(200).EUt(MV).save(provider);
            if (kind[0].equals("silicon")) continue;
            GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("ae2/" + kind[0] + "_processor"))
                    .inputItems(item(print, 1))
                    .inputItems(item("ae2:printed_silicon", 1))
                    .inputItems(TagPrefix.dust, GTMaterials.Redstone)
                    .inputItems(CustomTags.LV_CIRCUITS)
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(72))
                    .outputItems(item("ae2:" + kind[0] + "_processor", 1))
                    .duration(200).EUt(GTValues.VA[GTValues.LV]).save(provider);
        }
    }

    private static void network(Consumer<FinishedRecipe> provider) {
        assembler(provider, "annihilation_core", MV, item("ae2:annihilation_core", 2))
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 2).inputItems(item("ae2:logic_processor", 1))
                .inputItems(TagPrefix.dust, GTMaterials.NetherQuartz, 2).inputItems(CustomTags.LV_CIRCUITS)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(72)).save(provider);
        assembler(provider, "formation_core", MV, item("ae2:formation_core", 2))
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 2).inputItems(item("ae2:logic_processor", 1))
                .inputItems(TagPrefix.dust, GTMaterials.CertusQuartz, 2).inputItems(CustomTags.LV_CIRCUITS)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(72)).save(provider);
        assembler(provider, "controller", MV, item("ae2:controller", 1))
                .inputItems(GTMachines.HULL[GTValues.MV].asStack()).inputItems(item("ae2:fluix_crystal", 4))
                .inputItems(item("ae2:engineering_processor", 2)).inputItems(CustomTags.MV_CIRCUITS, 2).save(provider);
        assembler(provider, "drive", MV, item("ae2:drive", 1))
                .inputItems(GTMachines.HULL[GTValues.MV].asStack()).inputItems(item("ae2:engineering_processor", 2))
                .inputItems(item("ae2:fluix_glass_cable", 2)).inputItems(CustomTags.MV_CIRCUITS, 2).save(provider);
        assembler(provider, "terminal", MV, item("ae2:terminal", 1))
                .inputItems(item("ae2:formation_core", 1)).inputItems(item("ae2:annihilation_core", 1))
                .inputItems(item("ae2:logic_processor", 1)).inputItems(item("ae2:quartz_glass", 1))
                .inputItems(CustomTags.MV_CIRCUITS).save(provider);
        assembler(provider, "chest", MV, item("ae2:chest", 1))
                .inputItems(GTMachines.HULL[GTValues.MV].asStack()).inputItems(item("ae2:terminal", 1))
                .inputItems(item("ae2:engineering_processor", 1)).inputItems(CustomTags.MV_CIRCUITS).save(provider);
        assembler(provider, "crafting_terminal", MV, item("ae2:crafting_terminal", 1))
                .inputItems(item("ae2:terminal", 1)).inputItems(item("ae2:calculation_processor", 1))
                .inputItems(new ItemStack(Items.CRAFTING_TABLE)).save(provider);
        assembler(provider, "pattern_encoding_terminal", MV, item("ae2:pattern_encoding_terminal", 1))
                .inputItems(item("ae2:crafting_terminal", 1)).inputItems(item("ae2:engineering_processor", 1))
                .save(provider);
        assembler(provider, "pattern_access_terminal", MV, item("ae2:pattern_access_terminal", 1))
                .inputItems(item("ae2:terminal", 1)).inputItems(item("ae2:engineering_processor", 1))
                .inputItems(CustomTags.MV_CIRCUITS).save(provider);
        assembler(provider, "pattern_provider", MV, item("ae2:pattern_provider", 1))
                .inputItems(new ItemStack(Items.CRAFTING_TABLE, 2)).inputItems(item("ae2:annihilation_core", 1))
                .inputItems(item("ae2:formation_core", 1)).inputItems(TagPrefix.plate, GTMaterials.Aluminium, 4)
                .inputItems(GTItems.ROBOT_ARM_MV.asStack()).save(provider);
        assembler(provider, "interface", MV, item("ae2:interface", 1))
                .inputItems(item("ae2:annihilation_core", 1)).inputItems(item("ae2:formation_core", 1))
                .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 4).inputItems(GTItems.CONVEYOR_MODULE_MV.asStack())
                .save(provider);
        assembler(provider, "molecular_assembler", MV, item("ae2:molecular_assembler", 1))
                .inputItems(item("ae2:quartz_glass", 2)).inputItems(item("ae2:annihilation_core", 1))
                .inputItems(item("ae2:formation_core", 1)).inputItems(new ItemStack(Items.CRAFTING_TABLE))
                .inputItems(GTItems.ROBOT_ARM_MV.asStack()).inputItems(TagPrefix.plate, GTMaterials.Aluminium, 2)
                .save(provider);
        assembler(provider, "crafting_unit", MV, item("ae2:crafting_unit", 1))
                .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 4)
                .inputItems(item("ae2:calculation_processor", 1)).inputItems(item("ae2:logic_processor", 1))
                .inputItems(item("ae2:fluix_glass_cable", 2)).inputItems(CustomTags.MV_CIRCUITS).save(provider);
        // EN: HV refinement: the network leaves the base (wireless, P2P).
        // PT: Refino do HV: a rede sai da base (sem fio, P2P).
        // ZH: HV 精炼：网络走出基地（无线、P2P）。
        assembler(provider, "wireless_access_point", HV, item("ae2:wireless_access_point", 1))
                .inputItems(item("ae2:fluix_glass_cable", 1)).inputItems(GTItems.EMITTER_HV.asStack())
                .inputItems(item("ae2:engineering_processor", 1))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 2).inputItems(CustomTags.HV_CIRCUITS)
                .save(provider);
        assembler(provider, "wireless_terminal", HV, item("ae2:wireless_terminal", 1))
                .inputItems(item("ae2:terminal", 1)).inputItems(GTItems.EMITTER_HV.asStack())
                .inputItems(item("ae2:dense_energy_cell", 1))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 2).inputItems(CustomTags.HV_CIRCUITS)
                .save(provider);
        assembler(provider, "me_p2p_tunnel", HV, item("ae2:me_p2p_tunnel", 1))
                .inputItems(item("ae2:engineering_processor", 1)).inputItems(item("ae2:fluix_crystal", 4))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 2).inputItems(CustomTags.HV_CIRCUITS)
                .save(provider);
    }

    private static void storage(Consumer<FinishedRecipe> provider) {
        assembler(provider, "item_cell_housing", MV, item("ae2:item_cell_housing", 1))
                .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 3).inputItems(item("ae2:quartz_glass", 1))
                .inputItems(TagPrefix.dust, GTMaterials.Redstone, 2).save(provider);
        assembler(provider, "fluid_cell_housing", MV, item("ae2:fluid_cell_housing", 1))
                .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 3).inputItems(item("ae2:quartz_glass", 1))
                .inputItems(GTItems.ELECTRIC_PUMP_LV.asStack()).save(provider);
        assembler(provider, "cell_component_1k", MV, item("ae2:cell_component_1k", 1))
                .inputItems(item("ae2:logic_processor", 1)).inputItems(TagPrefix.dust, GTMaterials.CertusQuartz, 4)
                .inputItems(TagPrefix.dust, GTMaterials.Redstone, 4).inputItems(CustomTags.LV_CIRCUITS)
                .save(provider);
        assembler(provider, "cell_component_4k", MV, item("ae2:cell_component_4k", 1))
                .inputItems(item("ae2:cell_component_1k", 3)).inputItems(item("ae2:calculation_processor", 1))
                .inputItems(CustomTags.MV_CIRCUITS).save(provider);
        // EN: Above 4k is HV refinement (crafting CPU storage is built from these components too).
        // PT: Acima de 4k é refino do HV (o armazenamento das CPUs de crafting também usa esses componentes).
        // ZH: 4k 以上属于 HV 精炼（合成 CPU 的存储也用这些组件制作）。
        assembler(provider, "cell_component_16k", HV, item("ae2:cell_component_16k", 1))
                .inputItems(item("ae2:cell_component_4k", 3)).inputItems(item("ae2:calculation_processor", 2))
                .inputItems(CustomTags.HV_CIRCUITS).save(provider);
        assembler(provider, "cell_component_64k", HV, item("ae2:cell_component_64k", 1))
                .inputItems(item("ae2:cell_component_16k", 3)).inputItems(item("ae2:calculation_processor", 2))
                .inputItems(CustomTags.HV_CIRCUITS, 2).save(provider);
        assembler(provider, "cell_component_256k", EV, item("ae2:cell_component_256k", 1))
                .inputItems(item("ae2:cell_component_64k", 3)).inputItems(item("ae2:calculation_processor", 4))
                .inputItems(CustomTags.EV_CIRCUITS).save(provider);
    }

    private static void extendedAE(Consumer<FinishedRecipe> provider) {
        assembler(provider, "assembler_matrix_frame", HV, item("expatternprovider:assembler_matrix_frame", 4))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4).inputItems(item("ae2:quartz_glass", 1))
                .inputItems(CustomTags.HV_CIRCUITS).save(provider);
        assembler(provider, "assembler_matrix_crafter", HV, item("expatternprovider:assembler_matrix_crafter", 1))
                .inputItems(item("expatternprovider:assembler_matrix_frame", 1))
                .inputItems(item("ae2:molecular_assembler", 4)).inputItems(GTItems.ROBOT_ARM_HV.asStack())
                .inputItems(CustomTags.HV_CIRCUITS).save(provider);
        assembler(provider, "assembler_matrix_pattern", HV, item("expatternprovider:assembler_matrix_pattern", 1))
                .inputItems(item("expatternprovider:assembler_matrix_frame", 1))
                .inputItems(item("ae2:pattern_provider", 2)).inputItems(CustomTags.HV_CIRCUITS).save(provider);
        assembler(provider, "assembler_matrix_speed", HV, item("expatternprovider:assembler_matrix_speed", 1))
                .inputItems(item("expatternprovider:assembler_matrix_frame", 1))
                .inputItems(item("ae2:crafting_unit", 2)).inputItems(CustomTags.HV_CIRCUITS).save(provider);
        assembler(provider, "ex_pattern_provider", HV, item("expatternprovider:ex_pattern_provider", 1))
                .inputItems(item("ae2:pattern_provider", 2)).inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .inputItems(GTItems.ROBOT_ARM_HV.asStack()).inputItems(CustomTags.HV_CIRCUITS).save(provider);
        assembler(provider, "ex_interface", HV, item("expatternprovider:ex_interface", 1))
                .inputItems(item("ae2:interface", 2)).inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .inputItems(GTItems.CONVEYOR_MODULE_HV.asStack()).inputItems(CustomTags.HV_CIRCUITS).save(provider);
    }

    private static GTRecipeBuilder assembler(Consumer<FinishedRecipe> provider, String name, int eu, ItemStack out) {
        return GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("ae2/" + name))
                .outputItems(out).duration(200).EUt(eu);
    }

    private static ItemStack item(String id, int count) {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)), count);
    }
}
