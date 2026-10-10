package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAMachines;

/** Exercises the real recipe logic and fluid tanks without a player or UI. */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class DehydratorGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";
    private static final BlockPos MACHINE_POS = new BlockPos(2, 1, 2);

    private DehydratorGameTests() {}

    @GameTest(template = TEMPLATE, batch = "dehydrator", timeoutTicks = 20)
    public static void baseRecipeWithoutSteam(GameTestHelper helper) {
        WorkableTieredMachine machine = placeDehydrator(helper);
        machine.importFluids.setFluidInTank(0, GTMaterials.Ethanol.getFluid(225));

        machine.recipeLogic.findAndHandleRecipe();

        helper.assertTrue(machine.recipeLogic.getDuration() == 400,
                "Without steam the base recipe must take 400 ticks");
        helper.assertTrue(machine.importFluids.getFluidInTank(0).isEmpty(),
                "Base recipe must consume 225 mB of ethanol");
        machine.recipeLogic.onRecipeFinish();
        helper.assertTrue(outputAmount(machine, GTMaterials.Ethylene) == 225,
                "Base recipe must produce 225 mB of ethylene");
        helper.assertTrue(outputAmount(machine, GTMaterials.Water) == 225,
                "Base recipe must produce 225 mB of water");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "dehydrator", timeoutTicks = 20)
    public static void insufficientSteamUsesBaseRecipe(GameTestHelper helper) {
        WorkableTieredMachine machine = placeDehydrator(helper);
        machine.importFluids.setFluidInTank(0, GTMaterials.Ethanol.getFluid(225));
        machine.importFluids.setFluidInTank(1, GTMaterials.Steam.getFluid(99));

        machine.recipeLogic.findAndHandleRecipe();

        helper.assertTrue(machine.recipeLogic.getDuration() == 400,
                "With 99 mB of steam the base recipe must run");
        helper.assertTrue(machine.importFluids.getFluidInTank(1).getAmount() == 99,
                "The base recipe must leave insufficient steam untouched");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "dehydrator", timeoutTicks = 20)
    public static void steamIsConsumedForEveryAcceleratedBatch(GameTestHelper helper) {
        try {
            WorkableTieredMachine machine = placeDehydrator(helper);
            machine.importFluids.setFluidInTank(0, GTMaterials.Ethanol.getFluid(450));
            machine.importFluids.setFluidInTank(1, GTMaterials.Steam.getFluid(200));

            machine.recipeLogic.findAndHandleRecipe();
            helper.assertTrue(machine.recipeLogic.getDuration() == 200,
                    "With steam the accelerated recipe must take 200 ticks");
            helper.assertTrue(machine.importFluids.getFluidInTank(0).getAmount() == 225,
                    "First batch must consume 225 mB of ethanol");
            helper.assertTrue(machine.importFluids.getFluidInTank(1).getAmount() == 100,
                    "First batch must consume 100 mB of steam");

            machine.recipeLogic.onRecipeFinish();
            helper.assertTrue(outputAmount(machine, GTMaterials.Ethylene) == 225,
                    "First accelerated batch must produce 225 mB of ethylene");
            helper.assertTrue(outputAmount(machine, GTMaterials.Water) == 225,
                    "First accelerated batch must produce 225 mB of water");
            helper.assertTrue(machine.recipeLogic.getDuration() == 200,
                    "Second batch must also select the accelerated recipe");
            helper.assertTrue(machine.importFluids.getFluidInTank(0).isEmpty(),
                    "Second batch must consume the remaining ethanol");
            helper.assertTrue(machine.importFluids.getFluidInTank(1).isEmpty(),
                    "Second batch must consume the remaining 100 mB of steam");
            machine.recipeLogic.onRecipeFinish();
            helper.assertTrue(outputAmount(machine, GTMaterials.Ethylene) == 450,
                    "Two accelerated batches must produce 450 mB of ethylene");
            helper.assertTrue(outputAmount(machine, GTMaterials.Water) == 450,
                    "Two accelerated batches must produce 450 mB of water");
            helper.succeed();
        } catch (RuntimeException exception) {
            GTIAMod.LOGGER.error("Accelerated Dehydrator GameTest failed", exception);
            throw exception;
        }
    }

    private static int outputAmount(WorkableTieredMachine machine, Material material) {
        int amount = 0;
        for (int tank = 0; tank < machine.exportFluids.getTanks(); tank++) {
            var fluid = machine.exportFluids.getFluidInTank(tank);
            if (fluid.isFluidEqual(material.getFluid(1))) amount += fluid.getAmount();
        }
        return amount;
    }

    private static WorkableTieredMachine placeDehydrator(GameTestHelper helper) {
        helper.setBlock(MACHINE_POS, GTIAMachines.CATALYTIC_DEHYDRATOR.getBlock());
        var holder = (MetaMachineBlockEntity) helper.getBlockEntity(MACHINE_POS);
        var machine = (WorkableTieredMachine) holder.getMetaMachine();
        machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
        return machine;
    }
}
