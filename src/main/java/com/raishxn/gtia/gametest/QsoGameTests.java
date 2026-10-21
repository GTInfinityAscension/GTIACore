package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Quasi-Stellar Obliterator (GTIA-D-332): charge, ceiling, shot sizes and yield; it fires and harvests
 * negentropy, and with the world's stock spent the next shot ends the game; without the cage it does not form.
 * Slots 32, 33.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class QsoGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private QsoGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.QSO_DEF != null) return false;
        GTIAMod.LOGGER.warn("QSO not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "qso_math", timeoutTicks = 100)
    public static void shotMath(GameTestHelper helper) {
        if (!skip(helper)) QsoTestRig.shotMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "qso_runs", timeoutTicks = 2400)
    public static void firesAndEnds(GameTestHelper helper) {
        if (!skip(helper)) QsoTestRig.firesAndEnds(helper);
    }

    @GameTest(template = TEMPLATE, batch = "qso_wrong", timeoutTicks = 2400)
    public static void noCageNoGun(GameTestHelper helper) {
        if (!skip(helper)) QsoTestRig.noCageNoGun(helper);
    }
}
