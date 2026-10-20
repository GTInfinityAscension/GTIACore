package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Stellar Engine (GTIA-D-315): forms, the mirror pushes alone, the Caplan engine needs Nexus power, Δv reaches
 * the regions, an over-long jet strains the cage; without the cage it does not form.
 *
 * <p>
 * Needs GTNA and the engine's data files; without them the engine is not registered and these tests pass empty.
 * Slots 3 and 4: the Matrioshka's tests use 0 to 2.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class StellarEngineGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private StellarEngineGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.STELLAR_ENGINE_DEF != null) return false;
        GTIAMod.LOGGER.warn("Stellar Engine not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "stellar_engine_math", timeoutTicks = 100)
    public static void thrustRegionsAndTension(GameTestHelper helper) {
        if (!skip(helper)) StellarEngineTestRig.thrustRegionsAndTension(helper);
    }

    @GameTest(template = TEMPLATE, batch = "stellar_engine_runs", timeoutTicks = 2400)
    public static void mirrorAndCaplanMoveTheSystem(GameTestHelper helper) {
        if (!skip(helper)) StellarEngineTestRig.mirrorAndCaplanMoveTheSystem(helper);
    }

    @GameTest(template = TEMPLATE, batch = "stellar_engine_wrong", timeoutTicks = 2400)
    public static void noCageNoEngine(GameTestHelper helper) {
        if (!skip(helper)) StellarEngineTestRig.noCageNoEngine(helper);
    }
}
