package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAItems;
import com.raishxn.gtia.data.GTIAMachines;

/**
 * Chemistry slice 01 (wood chemistry and lime in Steam, chlor-alkali in LV): the Still runs its cuts for real, and
 * the recipes keep the ratios the proposal promises the player (3 Retorts per Still, 1 Cell per Centrifuge).
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class ChemistrySliceGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";
    private static final BlockPos MACHINE_POS = new BlockPos(2, 1, 2);

    private ChemistrySliceGameTests() {}

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void stillSplitsTheWoodCondensate(GameTestHelper h) {
        SimpleTieredMachine still = placeStill(h, 1);
        still.importFluids.setFluidInTank(0, GTMaterials.CharcoalByproducts.getFluid(1000));
        still.recipeLogic.findAndHandleRecipe();
        h.assertTrue(still.recipeLogic.getDuration() == 200, "circuit 1 separates the condensate in 10 s");
        still.recipeLogic.onRecipeFinish();
        h.assertTrue(outputAmount(still, GTMaterials.WoodVinegar) == 400 &&
                outputAmount(still, GTMaterials.WoodTar) == 250, "1000 mB give 400 mB vinegar and 250 mB tar");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void stillDistilsWoodVinegar(GameTestHelper h) {
        SimpleTieredMachine still = placeStill(h, 2);
        still.importFluids.setFluidInTank(0, GTMaterials.WoodVinegar.getFluid(1000));
        still.recipeLogic.findAndHandleRecipe();
        h.assertTrue(still.recipeLogic.getDuration() == 400, "circuit 2 distils wood vinegar in 20 s");
        still.recipeLogic.onRecipeFinish();
        h.assertTrue(outputAmount(still, GTMaterials.AceticAcid) == 100 &&
                outputAmount(still, GTMaterials.Methanol) == 40 && outputAmount(still, GTMaterials.Water) == 700,
                "wood vinegar is mostly water: 100 acetic acid, 40 methanol, 700 water");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void sliceRecipesKeepTheProposalRatios(GameTestHelper h) {
        h.assertTrue(GTIAMachines.WOOD_RETORT != null && GTIAMachines.LIME_KILN != null &&
                GTIAMachines.STILL != null && GTIAMachines.DIAPHRAGM_CELL != null &&
                GTIAItems.ASBESTOS_DIAPHRAGM.isPresent(), "the slice machines and the diaphragm are registered");
        int retort = duration(h, "wood_retort/logs");
        int separate = duration(h, "still/charcoal_byproducts");
        int vinegar = duration(h, "still/wood_vinegar");
        duration(h, "still/wood_tar");
        for (String fuel : new String[] { "charcoal", "coal", "coke" }) duration(h, "lime_kiln/quicklime_" + fuel);
        duration(h, "primitive_blast_furnace/steel_fluxed_charcoal");
        duration(h, "primitive_blast_furnace/steel_fluxed_coal");
        int cell = duration(h, "diaphragm_cell/chlor_alkali");
        int centrifuge = duration(h, "centrifuge/cell_liquor_separation");

        // Three retorts make exactly what one Still on circuit 1 separates (1000 mB each).
        h.assertTrue(3 * separate == retort, "3 Retorts : 1 Still (circuit 1), got " + retort + "/" + separate);
        // One Still on circuit 2 keeps up with the 400 mB of vinegar per separation of three retorts.
        h.assertTrue(vinegar * 3 * 400 <= retort * 1000, "1 Still on circuit 2 handles 3 Retorts' vinegar");
        // One cell (4000 mB of liquor) feeds one centrifuge (2000 mB per run) exactly.
        h.assertTrue(cell == 2 * centrifuge, "1 Diaphragm Cell : 1 Centrifuge, got " + cell + "/" + centrifuge);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void mvPlantsExistAndCloseTheirLoops(GameTestHelper h) {
        h.assertTrue(GTIAMachines.BAYER_DIGESTER != null && GTIAMachines.HALL_HEROULT_CELL != null &&
                GTIAMachines.CONTACT_CONVERTER != null && GTIAMachines.AMMONIA_CONVERTER != null &&
                GTIAMachines.SOLVAY_TOWER != null && GTIAItems.CARBON_ANODE.isPresent(),
                "the MV plants are registered");
        for (String path : new String[] { "bayer_digester/digestion", "bayer_digester/precipitation",
                "chemical_reactor/aluminium_hydroxide_seed",
                "lime_kiln/alumina_charcoal", "lime_kiln/soda_ash_coke", "chemical_reactor/cryolite",
                "mixer/carbon_anode", "hall_heroult/aluminium", "chemical_reactor/vanadium_pentoxide",
                "contact_converter/sulfuric_acid", "ammonia_converter/ammonia", "solvay_tower/sodium_bicarbonate" }) {
            duration(h, path);
        }
        // Bauxite to aluminium keeps its atoms: 1 Al2O3 -> 2 Al(OH)3 -> 1 Al2O3 -> 2 Al (4 Al per 2 alumina).
        var hallHeroult = (GTRecipe) h.getLevel().getServer().getRecipeManager()
                .byKey(GTIAMod.id("hall_heroult/aluminium")).orElseThrow();
        h.assertTrue(hallHeroult.getOutputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP)
                .size() == 1, "Hall-Heroult gives only aluminium (and CO2)");
        h.succeed();
    }

    private static int duration(GameTestHelper h, String path) {
        var recipe = h.getLevel().getServer().getRecipeManager().byKey(GTIAMod.id(path));
        h.assertTrue(recipe.isPresent() && recipe.get() instanceof GTRecipe, "missing recipe gtia:" + path);
        return ((GTRecipe) recipe.get()).duration;
    }

    private static SimpleTieredMachine placeStill(GameTestHelper h, int circuit) {
        h.setBlock(MACHINE_POS, GTIAMachines.STILL.getBlock());
        var machine = (SimpleTieredMachine) ((MetaMachineBlockEntity) h.getBlockEntity(MACHINE_POS)).getMetaMachine();
        machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
        machine.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(circuit));
        return machine;
    }

    private static int outputAmount(SimpleTieredMachine machine, Material material) {
        int amount = 0;
        for (int tank = 0; tank < machine.exportFluids.getTanks(); tank++) {
            var fluid = machine.exportFluids.getFluidInTank(tank);
            if (fluid.isFluidEqual(material.getFluid(1))) amount += fluid.getAmount();
        }
        return amount;
    }
}
