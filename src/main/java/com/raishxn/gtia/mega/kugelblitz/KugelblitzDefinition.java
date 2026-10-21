package com.raishxn.gtia.mega.kugelblitz;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Kugelblitz Containment Silo; loaded only with GTNA (see GTIAMegastructures). */
public final class KugelblitzDefinition {

    private KugelblitzDefinition() {}

    /** The station's hatches feed the vault (items and fluids) and the chamber's repair blocks. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(KugelblitzMachine.SHAPE, KugelblitzMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 4), new Hatch(PartAbility.IMPORT_FLUIDS, 4));
    }
}
