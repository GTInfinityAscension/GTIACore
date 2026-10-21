package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Solar System Disintegrator (GTIA-D-330): binding energy by layers; it takes a made system's crust and keeps the
 * system, then takes the star and erases it, pouring out dust and gas; without the support ring it does not form.
 * Slots 28, 29.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class DisintegratorGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private DisintegratorGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.DISINTEGRATOR_DEF != null) return false;
        GTIAMod.LOGGER.warn("Disintegrator not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "disintegrator_math", timeoutTicks = 100)
    public static void bindingMath(GameTestHelper helper) {
        if (!skip(helper)) DisintegratorTestRig.bindingMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "disintegrator_runs", timeoutTicks = 2400)
    public static void takesASystemApart(GameTestHelper helper) {
        if (!skip(helper)) DisintegratorTestRig.takesASystemApart(helper);
    }

    @GameTest(template = TEMPLATE, batch = "disintegrator_wrong", timeoutTicks = 2400)
    public static void noRingNoDisintegrator(GameTestHelper helper) {
        if (!skip(helper)) DisintegratorTestRig.noRingNoDisintegrator(helper);
    }
}
