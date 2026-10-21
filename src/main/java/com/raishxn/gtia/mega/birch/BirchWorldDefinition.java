package com.raishxn.gtia.mega.birch;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Birch World; loaded only with GTNA (see GTIAMegastructures). */
public final class BirchWorldDefinition {

    private BirchWorldDefinition() {}

    /** The station takes the module controllers and the inputs of their recipes, and gives the outputs. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(BirchWorldMachine.SHAPE, BirchWorldMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 4), new Hatch(PartAbility.IMPORT_FLUIDS, 2),
                new Hatch(PartAbility.EXPORT_ITEMS, 2), new Hatch(PartAbility.EXPORT_FLUIDS, 2));
    }
}
