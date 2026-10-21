package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Fusion Suppressor (GTIA-D-320): the formulas; a light star only flares; a heavy one, held suppressed, ends as a
 * supernova (neutron star, r-process dust owed) or a black hole; without a pylon it does not form. Slots 12 and 13.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class FusionSuppressorGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private FusionSuppressorGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.FUSION_SUPPRESSOR_DEF != null) return false;
        GTIAMod.LOGGER.warn("Fusion Suppressor not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "fusion_suppressor_math", timeoutTicks = 100)
    public static void suppressorMath(GameTestHelper helper) {
        if (!skip(helper)) FusionSuppressorTestRig.suppressorMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "fusion_suppressor_runs", timeoutTicks = 2400)
    public static void endsAStar(GameTestHelper helper) {
        if (!skip(helper)) FusionSuppressorTestRig.endsAStar(helper);
    }

    @GameTest(template = TEMPLATE, batch = "fusion_suppressor_wrong", timeoutTicks = 2400)
    public static void noPylonNoSuppressor(GameTestHelper helper) {
        if (!skip(helper)) FusionSuppressorTestRig.noPylonNoSuppressor(helper);
    }
}
