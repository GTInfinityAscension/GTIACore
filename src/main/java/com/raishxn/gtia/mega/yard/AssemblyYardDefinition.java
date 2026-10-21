package com.raishxn.gtia.mega.yard;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.raishxn.gtia.mega.common.MegaDefinition;
import com.raishxn.gtia.mega.common.MegaDefinition.Hatch;

/** Registration of the Hyperstructural Assembly Yard; loaded only with GTNA (see GTIAMegastructures). */
public final class AssemblyYardDefinition {

    private AssemblyYardDefinition() {}

    /** The station takes the parts and fluids of the assemblies and gives the finished ones. */
    public static MultiblockMachineDefinition register() {
        return MegaDefinition.register(AssemblyYardMachine.SHAPE, AssemblyYardMachine::new, 3,
                GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                new Hatch(PartAbility.IMPORT_ITEMS, 4), new Hatch(PartAbility.IMPORT_FLUIDS, 2),
                new Hatch(PartAbility.EXPORT_ITEMS, 2));
    }
}
