package com.raishxn.gtia.mega.nidavellir;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Nidavellir Hyperforge; loaded only with GTNA (see GTIAMegastructures). */
public final class NidavellirDefinition {

    private NidavellirDefinition() {}

    /** The station takes the alloys' inputs and the neutronium, and gives the alloys. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(NidavellirMachine.SHAPE, NidavellirMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 2), new Hatch(PartAbility.IMPORT_FLUIDS, 2),
                new Hatch(PartAbility.EXPORT_ITEMS, 2), new Hatch(PartAbility.EXPORT_FLUIDS, 2));
    }
}
