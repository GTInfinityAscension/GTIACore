package com.raishxn.gtia.mega;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import com.raishxn.gtia.mega.matrioshka.MatrioshkaShape;
import org.jetbrains.annotations.Nullable;

/**
 * Megastructures (GTIA-D-311). They reuse GTNA's original-multiblock base (incremental structure checks, moving
 * parts), so they are registered only with GTNA loaded, and the Matrioshka Brain only with its shape file. GTNA's
 * classes are only touched past that check.
 */
public final class GTIAMegastructures {

    @Nullable
    public static MultiblockMachineDefinition MATRIOSHKA_BRAIN;

    private GTIAMegastructures() {}

    public static boolean available() {
        return net.minecraftforge.fml.ModList.get().isLoaded("gtna") && MatrioshkaShape.present();
    }

    public static void init() {
        if (available()) MATRIOSHKA_BRAIN = com.raishxn.gtia.mega.matrioshka.MatrioshkaDefinition.register();
    }
}
