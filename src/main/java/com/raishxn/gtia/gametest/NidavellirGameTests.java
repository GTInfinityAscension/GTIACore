package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Nidavellir Hyperforge (GTIA-D-323): parallels and neutronium by compression; it forges an alloy in parallel,
 * spending neutronium per batch, four times as fast round a neutron star; without neutronium it waits; without the
 * outer ring it does not form. Slots 18, 19.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class NidavellirGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private NidavellirGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.NIDAVELLIR_DEF != null) return false;
        GTIAMod.LOGGER.warn("Nidavellir not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "nidavellir_math", timeoutTicks = 100)
    public static void pressMath(GameTestHelper helper) {
        if (!skip(helper)) NidavellirTestRig.pressMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "nidavellir_runs", timeoutTicks = 2400)
    public static void forgesUnderNeutronium(GameTestHelper helper) {
        if (!skip(helper)) NidavellirTestRig.forgesUnderNeutronium(helper);
    }

    @GameTest(template = TEMPLATE, batch = "nidavellir_wrong", timeoutTicks = 2400)
    public static void noOuterRingNoForge(GameTestHelper helper) {
        if (!skip(helper)) NidavellirTestRig.noOuterRingNoForge(helper);
    }
}
