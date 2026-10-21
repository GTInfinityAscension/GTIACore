package com.raishxn.gtia.mega.suppressor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Fusion Suppressor; loaded only with GTNA (see GTIAMegastructures). */
public final class FusionSuppressorDefinition {

    private FusionSuppressorDefinition() {}

    /** The station takes repair blocks and gives the supernova's dust. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(FusionSuppressorMachine.SHAPE, FusionSuppressorMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 2), new Hatch(PartAbility.EXPORT_ITEMS, 6));
    }
}
