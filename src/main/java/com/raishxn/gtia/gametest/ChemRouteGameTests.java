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
            "gtia:contact_converter/sulfuric_acid", "gtia:ammonia_converter/ammonia",
            "gtia:ostwald_converter/ostwald/absorption", "gtia:diaphragm_cell/chlor_alkali",
            "gtia:centrifuge/cell_liquor_separation", "gtia:lime_kiln/quicklime_coal", "gtia:lime_kiln/soda_ash_coal",
            "gtia:kroll_reactor/kroll/magnesium_reduction", "gtia:siemens_reactor/siemens/deposition",
            "gtia:electric_blast_furnace/czochralski/silicon_boule", "gtia:hall_heroult/aluminium",
            "gtia:catalytic_dehydrator/ethanol_to_ethylene", "gtia:wood_retort/logs",
            // by-products and the cracking route stay
            "gtceu:electrolyzer/decomposition_electrolyzing_hydrochloric_acid",
            "gtceu:electrolyzer/decomposition_electrolyzing_silicon_dioxide",
            "gtceu:pyrolyse_oven/coal_to_coke_creosote");

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
