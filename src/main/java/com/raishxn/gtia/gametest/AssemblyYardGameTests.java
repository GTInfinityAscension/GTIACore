package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Hyperstructural Assembly Yard (GTIA-D-326): slipways and parallels by phase, build rate by starlight and rush;
 * it assembles in parallel, waits with every slipway closed; without the hub ring it does not form. Slots 20, 21.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class AssemblyYardGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private AssemblyYardGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.ASSEMBLY_YARD_DEF != null) return false;
        GTIAMod.LOGGER.warn("Assembly Yard not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "yard_math", timeoutTicks = 100)
    public static void slipwayMath(GameTestHelper helper) {
        if (!skip(helper)) AssemblyYardTestRig.slipwayMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "yard_runs", timeoutTicks = 2400)
    public static void assemblesOnSlipways(GameTestHelper helper) {
        if (!skip(helper)) AssemblyYardTestRig.assemblesOnSlipways(helper);
    }

    @GameTest(template = TEMPLATE, batch = "yard_wrong", timeoutTicks = 2400)
    public static void noHubNoYard(GameTestHelper helper) {
        if (!skip(helper)) AssemblyYardTestRig.noHubNoYard(helper);
    }
}
