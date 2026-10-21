package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.chem.ChemRouteRemovals;

import java.util.List;

/** The competing direct syntheses are gone (GTIA-D-294) and the GTIA lines that replace them are loaded. */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class ChemRouteGameTests {

    private static final List<String> KEPT = List.of(
            "gtia:chemical_reactor/contact_sulfuric_acid", "gtia:chemical_reactor/haber_bosch_ammonia",
            "gtia:chemical_reactor/ostwald_absorption", "gtia:electrolyzer/chlor_alkali",
            "gtia:centrifuge/cell_liquor_separation", "gtia:coke_oven/quicklime", "gtia:coke_oven/soda_ash",
            "gtia:chemical_reactor/kroll_magnesium_reduction", "gtia:chemical_reactor/siemens_deposition",
            "gtia:electric_blast_furnace/czochralski/silicon_boule",
            "gtia:electric_blast_furnace/hall_heroult_aluminium",
            "gtia:chemical_reactor/ethanol_to_ethylene", "gtia:coke_oven/wood_retort_logs",
            // by-products and the cracking route stay
            "gtceu:electrolyzer/decomposition_electrolyzing_hydrochloric_acid",
            "gtceu:electrolyzer/decomposition_electrolyzing_silicon_dioxide",
            "gtceu:pyrolyse_oven/coal_to_coke_creosote", "gtceu:pyrolyse_oven/log_to_charcoal_byproducts");

    private ChemRouteGameTests() {}

    @GameTest(template = "qa_empty_5x5", batch = "chem_routes", timeoutTicks = 20)
    public static void competingRoutesAreRemovedAndOursRemain(GameTestHelper h) {
        var manager = h.getLevel().getServer().getRecipeManager();
        var present = ChemRouteRemovals.ids().stream().filter(id -> manager.byKey(id).isPresent()).toList();
        var missing = KEPT.stream().map(ResourceLocation::new).filter(id -> manager.byKey(id).isEmpty()).toList();
        h.assertTrue(present.isEmpty(), "competing routes still loaded: " + present);
        h.assertTrue(missing.isEmpty(), "GTIA lines or kept by-products missing: " + missing);
        h.succeed();
    }
}
