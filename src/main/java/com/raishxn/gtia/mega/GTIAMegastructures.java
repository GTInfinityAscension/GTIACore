package com.raishxn.gtia.mega;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import com.raishxn.gtia.mega.common.MegaShape;
import org.jetbrains.annotations.Nullable;

/**
 * Megastructures (GTIA-D-311, D-315, D-316). They reuse GTNA's original-multiblock base (incremental structure
 * checks, moving parts), so they are registered only with GTNA loaded, each one only with its data files. GTNA's
 * classes are only touched past that check.
 */
public final class GTIAMegastructures {

    public static final String MATRIOSHKA = "matrioshka_brain", STELLAR_ENGINE = "stellar_engine";

    @Nullable
    public static MultiblockMachineDefinition MATRIOSHKA_BRAIN, STELLAR_ENGINE_DEF;

    private GTIAMegastructures() {}

    public static boolean gtna() {
        return net.minecraftforge.fml.ModList.get().isLoaded("gtna");
    }

    public static boolean available(String name) {
        return gtna() && MegaShape.present(name);
    }

    /** Any megastructure can be registered: the client then needs the shared renderer. */
    public static boolean available() {
        return available(MATRIOSHKA) || available(STELLAR_ENGINE);
    }

    public static void init() {
        if (available(MATRIOSHKA))
            MATRIOSHKA_BRAIN = com.raishxn.gtia.mega.matrioshka.MatrioshkaDefinition.register();
        if (available(STELLAR_ENGINE))
            STELLAR_ENGINE_DEF = com.raishxn.gtia.mega.engine.StellarEngineDefinition.register();
    }
}
