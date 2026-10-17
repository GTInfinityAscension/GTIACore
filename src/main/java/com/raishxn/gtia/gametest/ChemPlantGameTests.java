package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.chem.ChemPlants;

/** Each chemistry plant of GTIA-D-281 forms from its preview and stops forming when a key block is missing. */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class ChemPlantGameTests {

    private static final String TEMPLATE = "qa_empty_12";

    private ChemPlantGameTests() {}

    /** Builds the first preview at (1,1,1); with {@code missing} set, leaves out the first block of that kind. */
    private static IMultiController build(GameTestHelper h, MultiblockMachineDefinition plant, Block missing) {
        var shape = plant.getMatchingShapes().get(0).getBlocks();
        IMultiController controller = null;
        boolean removed = false;
        for (int x = 0; x < shape.length; x++)
            for (int y = 0; y < shape[x].length; y++)
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    BlockPos pos = new BlockPos(x + 1, y + 1, z + 1);
                    var state = info == null ? Blocks.AIR.defaultBlockState() : info.getBlockState();
                    if (!removed && missing != null && state.is(missing)) {
                        state = Blocks.AIR.defaultBlockState();
                        removed = true;
                    }
                    h.setBlock(pos, state);
                    if (state.is(plant.getBlock()) && h.getBlockEntity(pos) instanceof IMachineBlockEntity be &&
                            be.getMetaMachine() instanceof IMultiController c)
                        controller = c;
                }
        h.assertTrue(missing == null || removed, "the preview has a " + missing);
        return controller;
    }

    private static void forms(GameTestHelper h, MultiblockMachineDefinition plant, Block key) {
        var formed = build(h, plant, null);
        h.assertTrue(formed != null && formed.checkPattern(), plant.getName() + " must form from its preview");
        var broken = build(h, plant, key);
        h.assertFalse(broken != null && broken.checkPattern(), plant.getName() + " must not form without " + key);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chem_plants", timeoutTicks = 40)
    public static void ostwaldConverterForms(GameTestHelper h) {
        forms(h, ChemPlants.OSTWALD_CONVERTER, GTBlocks.CASING_INVAR_HEATPROOF.get());
    }

    @GameTest(template = TEMPLATE, batch = "chem_plants", timeoutTicks = 40)
    public static void clausUnitForms(GameTestHelper h) {
        forms(h, ChemPlants.CLAUS_UNIT, GTBlocks.CASING_STEEL_PIPE.get());
    }

    @GameTest(template = TEMPLATE, batch = "chem_plants", timeoutTicks = 40)
    public static void krollReactorForms(GameTestHelper h) {
        forms(h, ChemPlants.KROLL_REACTOR, GTBlocks.CASING_INVAR_HEATPROOF.get());
    }

    @GameTest(template = TEMPLATE, batch = "chem_plants", timeoutTicks = 40)
    public static void siemensReactorForms(GameTestHelper h) {
        forms(h, ChemPlants.SIEMENS_REACTOR, GTBlocks.CASING_TEMPERED_GLASS.get());
    }
}
