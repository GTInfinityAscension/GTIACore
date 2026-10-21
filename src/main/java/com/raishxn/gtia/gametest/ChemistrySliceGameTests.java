package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;
import com.raishxn.gtia.data.GTIAItems;

/**
 * The Steam to HV chemistry lines run in GTCEu machines (GTIA-D-335): each test puts the inputs in a real GTCEu
 * machine and checks that the GTIA recipe, not a GTCEu one with the same inputs, is the one that runs.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class ChemistrySliceGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";
    private static final BlockPos MACHINE_POS = new BlockPos(2, 1, 2);

    private ChemistrySliceGameTests() {}

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void lvDistilleryCutsTheWoodCondensate(GameTestHelper h) {
        var still = place(h, GTMachines.DISTILLERY, GTValues.LV, 11);
        still.importFluids.setFluidInTank(0, GTMaterials.CharcoalByproducts.getFluid(1000));
        still.recipeLogic.findAndHandleRecipe();
        h.assertTrue(still.recipeLogic.getDuration() == 200, "circuit 11 cuts the condensate at LV in 10 s");
        still.recipeLogic.onRecipeFinish();
        h.assertTrue(outputAmount(still, GTMaterials.WoodTar) == 250, "1000 mB give 250 mB of wood tar");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void lvChemicalReactorDehydratesEthanol(GameTestHelper h) {
        var reactor = place(h, GTMachines.CHEMICAL_REACTOR, GTValues.LV, 2);
        reactor.importFluids.setFluidInTank(0, GTMaterials.Ethanol.getFluid(225));
        reactor.importFluids.setFluidInTank(1, GTMaterials.Steam.getFluid(100));
        reactor.recipeLogic.findAndHandleRecipe();
        h.assertTrue(reactor.recipeLogic.getDuration() == 200, "steam (circuit 2) halves the time to 10 s");
        reactor.recipeLogic.onRecipeFinish();
        h.assertTrue(outputAmount(reactor, GTMaterials.Ethylene) == 225, "225 mB of ethanol give 225 mB of ethylene");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void lvElectrolyzerRunsTheChlorAlkaliCell(GameTestHelper h) {
        var cell = place(h, GTMachines.ELECTROLYZER, GTValues.LV, 0);
        cell.importFluids.setFluidInTank(0, GTMaterials.SaltWater.getFluid(4000));
        cell.importItems.setStackInSlot(0, GTIAItems.ASBESTOS_DIAPHRAGM.asStack());
        cell.recipeLogic.findAndHandleRecipe();
        h.assertTrue(cell.recipeLogic.getDuration() == 300, "the cell runs 15 s");
        cell.recipeLogic.onRecipeFinish();
        h.assertTrue(outputAmount(cell, GTMaterials.Chlorine) == 2000 &&
                outputAmount(cell, GTIAAlloys.CELL_LIQUOR) == 4000, "2000 mB of chlorine and 4000 mB of cell liquor");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void mvChemicalReactorRunsTheOstwaldBurner(GameTestHelper h) {
        var reactor = place(h, GTMachines.CHEMICAL_REACTOR, GTValues.MV, 1);
        reactor.importItems.setStackInSlot(0, ChemicalHelper.get(TagPrefix.dust, GTMaterials.CobaltOxide, 64));
        reactor.importFluids.setFluidInTank(0, GTMaterials.Ammonia.getFluid(2000));
        reactor.importFluids.setFluidInTank(1, GTMaterials.Oxygen.getFluid(5000));
        reactor.recipeLogic.findAndHandleRecipe();
        reactor.recipeLogic.onRecipeFinish();
        // GTCEu's catalyst-free ammonia burner (2000 mB NH3 -> 2000 mB NO) is removed; ours gives 1000 mB per run.
        h.assertTrue(outputAmount(reactor, GTMaterials.NitricOxide) == 1000, "the GTIA Ostwald burner runs");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void linesKeepTheProposalRatios(GameTestHelper h) {
        int retort = duration(h, "coke_oven/wood_retort_logs");
        int cut = duration(h, "distillery/charcoal_byproducts_tar");
        duration(h, "distillery/charcoal_byproducts_vinegar");
        duration(h, "distillery/wood_vinegar_acetic_acid");
        duration(h, "distillery/wood_tar_creosote");
        duration(h, "coke_oven/quicklime");
        duration(h, "primitive_blast_furnace/steel_fluxed_charcoal");
        duration(h, "primitive_blast_furnace/steel_fluxed_coal");
        duration(h, "fermenter/ethanol_from_sugar");
        int cell = duration(h, "electrolyzer/chlor_alkali");
        int centrifuge = duration(h, "centrifuge/cell_liquor_separation");
        // Three Coke Ovens on logs make exactly what one LV Distillery cuts (1000 mB each).
        h.assertTrue(3 * cut == retort, "3 Coke Ovens : 1 Distillery, got " + retort + "/" + cut);
        // One cell (4000 mB of liquor) feeds one centrifuge (2000 mB per run) exactly.
        h.assertTrue(cell == 2 * centrifuge, "1 Electrolyzer : 1 Centrifuge, got " + cell + "/" + centrifuge);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chemistry", timeoutTicks = 20)
    public static void mvAndHvLinesExist(GameTestHelper h) {
        h.assertTrue(GTIAItems.CARBON_ANODE.isPresent() && GTIAItems.ASBESTOS_DIAPHRAGM.isPresent(),
                "the anode and the diaphragm are registered");
        for (String path : new String[] { "chemical_reactor/bayer_digestion", "chemical_reactor/bayer_precipitation",
                "chemical_reactor/aluminium_hydroxide_seed", "coke_oven/alumina", "coke_oven/soda_ash",
                "chemical_reactor/cryolite", "mixer/carbon_anode", "electric_blast_furnace/hall_heroult_aluminium",
                "chemical_reactor/vanadium_pentoxide", "chemical_reactor/contact_sulfuric_acid",
                "chemical_reactor/haber_bosch_ammonia", "chemical_reactor/solvay_sodium_bicarbonate",
                "chemical_reactor/ostwald_ammonia_oxidation",
                "large_chemical_reactor/ostwald_ammonia_oxidation_platinum",
                "chemical_reactor/ostwald_nitric_oxide_oxidation", "chemical_reactor/ostwald_absorption",
                "chemical_reactor/claus_sulfur_recovery", "chemical_reactor/claus_sulfur_dioxide",
                "large_chemical_reactor/kroll_ilmenite_chlorination",
                "large_chemical_reactor/kroll_rutile_chlorination",
                "chemical_reactor/kroll_magnesium_reduction", "chemical_reactor/siemens_hydrochlorination",
                "chemical_reactor/siemens_deposition" }) {
            duration(h, path);
        }
        // Bauxite to aluminium keeps its atoms: 1 Al2O3 -> 2 Al(OH)3 -> 1 Al2O3 -> 2 Al (4 Al per 2 alumina).
        var hallHeroult = (GTRecipe) h.getLevel().getServer().getRecipeManager()
                .byKey(GTIAMod.id("electric_blast_furnace/hall_heroult_aluminium")).orElseThrow();
        h.assertTrue(hallHeroult.getOutputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP)
                .size() == 1, "Hall-Heroult gives only aluminium (and CO2)");
        h.succeed();
    }

    private static int duration(GameTestHelper h, String path) {
        var recipe = h.getLevel().getServer().getRecipeManager().byKey(GTIAMod.id(path));
        h.assertTrue(recipe.isPresent() && recipe.get() instanceof GTRecipe, "missing recipe gtia:" + path);
        return ((GTRecipe) recipe.get()).duration;
    }

    private static SimpleTieredMachine place(GameTestHelper h, MachineDefinition[] machines, int tier, int circuit) {
        h.setBlock(MACHINE_POS, machines[tier].getBlock());
        var machine = (SimpleTieredMachine) ((MetaMachineBlockEntity) h.getBlockEntity(MACHINE_POS)).getMetaMachine();
        machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
        if (circuit > 0) machine.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(circuit));
        return machine;
    }

    private static int outputAmount(SimpleTieredMachine machine, Material material) {
        FluidStack wanted = material.getFluid(1);
        int amount = 0;
        for (int tank = 0; tank < machine.exportFluids.getTanks(); tank++) {
            var fluid = machine.exportFluids.getFluidInTank(tank);
            if (fluid.isFluidEqual(wanted)) amount += fluid.getAmount();
        }
        return amount;
    }
}
