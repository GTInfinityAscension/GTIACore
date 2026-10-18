package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;
import com.raishxn.gtia.route.RouteRemovals;

import java.util.List;

/** Block 1 routes (GTIA-D-303 to GTIA-D-308): the replaced recipes are gone and the new ones carry the new numbers. */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class RouteGameTests {

    private static final List<String> ADDED = List.of(
            "gtia:electrolyzer/decomposition_electrolyzing_mica",
            "gtia:electrolyzer/decomposition_electrolyzing_bastnasite",
            "gtia:electrolyzer/decomposition_electrolyzing_talc",
            "gtia:primitive_blast_furnace/pig_iron_hematite_coke",
            "gtia:electric_blast_furnace/pig_iron_magnetite",
            "gtia:arc_furnace/steel_from_pig_iron",
            "gtia:mixer/concrete_from_slag",
            "gtia:electric_blast_furnace/aluminium_from_dust",
            "gtia:compressor/crude_aluminium",
            "gtia:electric_blast_furnace/aluminium_from_crude",
            "gtia:electric_blast_furnace/aluminium_from_alumina",
            // recycling decompositions stay
            "gtceu:electrolyzer/decomposition_electrolyzing_magnalium",
            "gtceu:electrolyzer/decomposition_electrolyzing_monazite");

    private RouteGameTests() {}

    private static GTRecipe recipe(GameTestHelper h, String id) {
        var r = h.getLevel().getServer().getRecipeManager().byKey(new ResourceLocation(id));
        h.assertTrue(r.isPresent() && r.get() instanceof GTRecipe, "missing recipe " + id);
        return (GTRecipe) r.get();
    }

    private static boolean gives(GTRecipe recipe, Material material) {
        var dust = ChemicalHelper.get(TagPrefix.dust, material);
        return RecipeHelper.getOutputItems(recipe).stream().anyMatch(s -> s.is(dust.getItem()));
    }

    @GameTest(template = "qa_empty_5x5", batch = "routes", timeoutTicks = 20)
    public static void replacedRecipesAreGoneAndNewOnesLoaded(GameTestHelper h) {
        var manager = h.getLevel().getServer().getRecipeManager();
        var present = RouteRemovals.ids().stream().filter(id -> manager.byKey(id).isPresent()).toList();
        var missing = ADDED.stream().map(ResourceLocation::new).filter(id -> manager.byKey(id).isEmpty()).toList();
        h.assertTrue(present.isEmpty(), "replaced recipes still loaded: " + present);
        h.assertTrue(missing.isEmpty(), "new route recipes missing: " + missing);
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", batch = "routes", timeoutTicks = 20)
    public static void routesCarryTheApprovedNumbers(GameTestHelper h) {
        // GTNA only loads in dev runs with -PgtnaDevJar.
        if (ModList.get().isLoaded("gtna"))
            h.assertTrue(recipe(h, "gtia:assembly_line/integrated_ore_processor").getInputEUt().getTotalEU() == 7680,
                    "IOP controller at IV");
        var mica = recipe(h, "gtia:electrolyzer/decomposition_electrolyzing_mica");
        h.assertTrue(mica.getInputEUt().getTotalEU() == 120, "mica at MV");
        h.assertTrue(gives(mica, GTIAAlloys.ALUMINA) && !gives(mica, GTMaterials.Aluminium), "mica gives alumina");
        var bastnasite = recipe(h, "gtia:electrolyzer/decomposition_electrolyzing_bastnasite");
        h.assertTrue(gives(bastnasite, GTMaterials.RareEarth) && !gives(bastnasite, GTMaterials.Cerium),
                "bastnasite gives rare earth");
        h.assertTrue(recipe(h, "gtia:electric_blast_furnace/aluminium_from_dust").getInputEUt().getTotalEU() == 480,
                "aluminium dust smelts at HV");
        h.assertTrue(recipe(h, "gtia:electric_blast_furnace/aluminium_from_crude").getInputEUt().getTotalEU() == 120,
                "crude aluminium smelts at MV");
        h.succeed();
    }
}
