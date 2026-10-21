package com.raishxn.gtia.mega.disintegrator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Solar System Disintegrator; loaded only with GTNA (see GTIAMegastructures). */
public final class DisintegratorDefinition {

    private DisintegratorDefinition() {}

    /** The station gives the dusts and the gases of the system taken apart. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(DisintegratorMachine.SHAPE, DisintegratorMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.EXPORT_ITEMS, 4), new Hatch(PartAbility.EXPORT_FLUIDS, 2));
    }
}
