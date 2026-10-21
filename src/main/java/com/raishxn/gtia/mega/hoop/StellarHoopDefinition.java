package com.raishxn.gtia.mega.hoop;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Stellar Hoop; loaded only with GTNA (see GTIAMegastructures). */
public final class StellarHoopDefinition {

    private StellarHoopDefinition() {}

    /** The station takes the recipe inputs and the coolant, and gives the outputs. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(StellarHoopMachine.SHAPE, StellarHoopMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 2), new Hatch(PartAbility.IMPORT_FLUIDS, 2),
                new Hatch(PartAbility.EXPORT_ITEMS, 2), new Hatch(PartAbility.EXPORT_FLUIDS, 2));
    }
}
