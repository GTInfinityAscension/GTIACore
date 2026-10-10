package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class PhaseRouteGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";
    private static final BlockPos MACHINE_POS = new BlockPos(2, 1, 2);

    private PhaseRouteGameTests() {}

    @GameTest(template = TEMPLATE, batch = "phase", timeoutTicks = 20)
    public static void phaseOreMaceratesInSteam(GameTestHelper helper) {
        var deposit = GTRegistries.ORE_VEINS.get(GTIAMod.id("phase_matrix_deposit"));
        helper.assertTrue(deposit != null, "Overworld phase deposit must be registered");
        helper.assertTrue(deposit.isForBiome(helper.getLevel().getBiome(helper.absolutePos(MACHINE_POS))) &&
                !deposit.indicatorGenerators().isEmpty(),
                "Phase deposit must be eligible in the Overworld and have a surface indicator");
        var rawOre = ChemicalHelper.get(TagPrefix.rawOre, GTIAAlloys.PHASE_MATRIX);
        var oreBlock = ChemicalHelper.get(TagPrefix.ore, GTIAAlloys.PHASE_MATRIX);
        helper.assertTrue(!rawOre.isEmpty() && !oreBlock.isEmpty(), "Phase ore and its raw form must exist");
        helper.assertTrue(oreBlock.is(ItemTags.create(new ResourceLocation("forge", "ores"))),
                "Phase ore must be recognized by Steam and Electric Miners");
        var orePos = new BlockPos(1, 1, 1);
        helper.setBlock(orePos, ((BlockItem) oreBlock.getItem()).getBlock());
        var drops = Block.getDrops(helper.getBlockState(orePos), helper.getLevel(), helper.absolutePos(orePos), null);
        helper.assertTrue(drops.stream().anyMatch(drop -> ItemStack.isSameItem(drop, rawOre)),
                "Mining phase ore without Silk Touch must yield raw phase ore");
        helper.setBlock(orePos, Blocks.AIR);

        var machine = placeSteamMachine(helper, GTMachines.STEAM_MACERATOR.left().getBlock());
        machine.importItems.setStackInSlot(0, rawOre);
        machine.recipeLogic.findAndHandleRecipe();
        helper.assertTrue(machine.recipeLogic.getDuration() == 800,
                "Steam Macerator must accept raw phase ore at low pressure");
        machine.recipeLogic.onRecipeFinish();
        var output = machine.exportItems.getStackInSlot(0);
        helper.assertTrue(
                ItemStack.isSameItem(output, ChemicalHelper.get(TagPrefix.crushed, GTIAAlloys.PHASE_MATRIX)) &&
                        output.getCount() == 2,
                "Macerator must make two crushed phase matrix items");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "phase", timeoutTicks = 20)
    public static void crushedPhaseMatrixMaceratesToDust(GameTestHelper helper) {
        var machine = placeSteamMachine(helper, GTMachines.STEAM_MACERATOR.left().getBlock());
        machine.importItems.setStackInSlot(0, ChemicalHelper.get(TagPrefix.crushed, GTIAAlloys.PHASE_MATRIX));
        machine.recipeLogic.findAndHandleRecipe();
        helper.assertTrue(machine.recipeLogic.getDuration() == 800,
                "Steam Macerator must accept crushed phase matrix");
        machine.recipeLogic.onRecipeFinish();
        helper.assertTrue(ItemStack.isSameItem(machine.exportItems.getStackInSlot(0),
                ChemicalHelper.get(TagPrefix.dustImpure, GTIAAlloys.PHASE_MATRIX)),
                "Crushed phase matrix must yield impure phase matrix dust");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "phase", timeoutTicks = 20)
    public static void phaseDustMakesPulsatingAlloy(GameTestHelper helper) {
        var machine = placeSteamMachine(helper, GTMachines.STEAM_ALLOY_SMELTER.left().getBlock());
        machine.importItems.setStackInSlot(0, ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Iron));
        machine.importItems.setStackInSlot(1, ChemicalHelper.get(TagPrefix.dustImpure, GTIAAlloys.PHASE_MATRIX));
        machine.recipeLogic.findAndHandleRecipe();
        helper.assertTrue(machine.recipeLogic.getDuration() == 600,
                "Steam Alloy Smelter must accept impure phase matrix dust and iron");
        machine.recipeLogic.onRecipeFinish();
        helper.assertTrue(ItemStack.isSameItem(machine.exportItems.getStackInSlot(0),
                ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)),
                "Phase route must yield GTIA Pulsating Alloy");
        helper.succeed();
    }

    private static SimpleSteamMachine placeSteamMachine(GameTestHelper helper,
                                                        net.minecraft.world.level.block.Block block) {
        helper.setBlock(MACHINE_POS, block);
        var holder = (MetaMachineBlockEntity) helper.getBlockEntity(MACHINE_POS);
        var machine = (SimpleSteamMachine) holder.getMetaMachine();
        machine.steamTank.setFluidInTank(0, GTMaterials.Steam.getFluid(16000));
        return machine;
    }
}
