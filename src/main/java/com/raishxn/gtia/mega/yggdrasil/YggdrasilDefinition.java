package com.raishxn.gtia.mega.yggdrasil;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Yggdrasil Orchid; loaded only with GTNA (see GTIAMegastructures). */
public final class YggdrasilDefinition {

    private YggdrasilDefinition() {}

    /** The station takes seeds and water and gives the harvest and the biomass. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(YggdrasilMachine.SHAPE, YggdrasilMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 2), new Hatch(PartAbility.IMPORT_FLUIDS, 2),
                new Hatch(PartAbility.EXPORT_ITEMS, 2), new Hatch(PartAbility.EXPORT_FLUIDS, 1));
    }
}
