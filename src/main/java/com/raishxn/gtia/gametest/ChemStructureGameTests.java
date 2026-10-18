package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.data.GTIAMachines;
import com.raishxn.gtia.machine.DiaphragmCellMachine;

/** The chemistry slice 01 structures of GTIA-D-302 form from their preview and not without a key block. */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class ChemStructureGameTests {

    private static final String TEMPLATE = "qa_empty_24";

    private ChemStructureGameTests() {}

    private static void forms(GameTestHelper h, MultiblockMachineDefinition machine, Block key) {
        var formed = ChemPlantGameTests.build(h, machine, null);
        h.assertTrue(formed != null && formed.checkPattern(), machine.getName() + " must form from its preview");
        var broken = ChemPlantGameTests.build(h, machine, key);
        h.assertFalse(broken != null && broken.checkPattern(), machine.getName() + " must not form without " + key);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "chem_structures", timeoutTicks = 40)
    public static void woodRetortForms(GameTestHelper h) {
        forms(h, GTIAMachines.WOOD_RETORT, Blocks.GLASS);
    }

    @GameTest(template = TEMPLATE, batch = "chem_structures", timeoutTicks = 40)
    public static void limeKilnForms(GameTestHelper h) {
        forms(h, GTIAMachines.LIME_KILN, GTBlocks.CASING_BRONZE_GEARBOX.get());
    }

    @GameTest(template = TEMPLATE, batch = "chem_structures", timeoutTicks = 40)
    public static void diaphragmCellForms(GameTestHelper h) {
        forms(h, GTIAMachines.DIAPHRAGM_CELL, Blocks.IRON_BARS);
    }

    /** Three cells give three parallels; every row length from 3 to 9 is a different pattern. */
    @GameTest(template = TEMPLATE, batch = "chem_structures", timeoutTicks = 40)
    public static void diaphragmCellCountsCells(GameTestHelper h) {
        var cell = (DiaphragmCellMachine) ChemPlantGameTests.build(h, GTIAMachines.DIAPHRAGM_CELL, null);
        h.assertTrue(cell != null && cell.checkPattern(), "the 3-cell row must form");
        h.assertTrue(cell.getCells() == 3, "3 cells expected, got " + cell.getCells());
        h.assertTrue(GTIAMachines.DIAPHRAGM_CELL.getMatchingShapes().size() == 3, "previews for 3, 5 and 9 cells");
        for (int n = DiaphragmCellMachine.MIN_CELLS; n < DiaphragmCellMachine.MAX_CELLS; n++)
            h.assertTrue(DiaphragmCellMachine.pattern(GTIAMachines.DIAPHRAGM_CELL, n) !=
                    DiaphragmCellMachine.pattern(GTIAMachines.DIAPHRAGM_CELL, n + 1), "one pattern per count");
        h.succeed();
    }
}
