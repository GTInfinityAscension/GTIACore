package com.raishxn.gtia.mega.lifter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Star Lifter; loaded only with GTNA (see GTIAMegastructures). */
public final class StarLifterDefinition {

    private StarLifterDefinition() {}

    /** The station takes repair blocks and gives plasma, gases and dusts. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(StarLifterMachine.SHAPE, StarLifterMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 2), new Hatch(PartAbility.EXPORT_FLUIDS, 4),
                new Hatch(PartAbility.EXPORT_ITEMS, 2));
    }
}
