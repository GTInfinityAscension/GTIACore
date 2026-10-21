package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Stellar Hoop (GTIA-D-318): the corona model; it runs a real Electric Blast Furnace recipe in parallel from its
 * hatches, the hoop is captured, coolant keeps the shields whole and without it they wear; without a clamp it does
 * not form. Needs GTNA and the shape files. Slots 8 and 9.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class StellarHoopGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private StellarHoopGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.STELLAR_HOOP_DEF != null) return false;
        GTIAMod.LOGGER.warn("Stellar Hoop not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "stellar_hoop_math", timeoutTicks = 100)
    public static void coronaModel(GameTestHelper helper) {
        if (!skip(helper)) StellarHoopTestRig.coronaModel(helper);
    }

    @GameTest(template = TEMPLATE, batch = "stellar_hoop_runs", timeoutTicks = 2400)
    public static void forgesInTheCorona(GameTestHelper helper) {
        if (!skip(helper)) StellarHoopTestRig.forgesInTheCorona(helper);
    }

    @GameTest(template = TEMPLATE, batch = "stellar_hoop_wrong", timeoutTicks = 2400)
    public static void noClampNoHoop(GameTestHelper helper) {
        if (!skip(helper)) StellarHoopTestRig.noClampNoHoop(helper);
    }
}
