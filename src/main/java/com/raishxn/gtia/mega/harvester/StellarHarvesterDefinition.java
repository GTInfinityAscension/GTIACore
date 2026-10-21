package com.raishxn.gtia.mega.harvester;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import com.raishxn.gtia.mega.common.MegaDefinition;

/** Registration of the Stellar Harvester; loaded only with GTNA (see GTIAMegastructures). */
public final class StellarHarvesterDefinition {

    private StellarHarvesterDefinition() {}

    /** No hatches: the harvest goes to the Nexus network. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(StellarHarvesterMachine.SHAPE, StellarHarvesterMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"));
    }
}
