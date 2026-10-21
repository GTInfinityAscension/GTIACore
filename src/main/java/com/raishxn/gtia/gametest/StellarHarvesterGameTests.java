package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Stellar Harvester (GTIA-D-328): fusion exponents, boost, budget and remnants; it burns a star's last fuel into
 * EU and leaves the remnant its mass allows; without the annulus it does not form. Slots 24, 25.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class StellarHarvesterGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private StellarHarvesterGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.STELLAR_HARVESTER_DEF != null) return false;
        GTIAMod.LOGGER.warn("Stellar Harvester not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "harvester_math", timeoutTicks = 100)
    public static void burnMath(GameTestHelper helper) {
        if (!skip(helper)) StellarHarvesterTestRig.burnMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "harvester_runs", timeoutTicks = 2400)
    public static void burnsToARemnant(GameTestHelper helper) {
        if (!skip(helper)) StellarHarvesterTestRig.burnsToARemnant(helper);
    }

    @GameTest(template = TEMPLATE, batch = "harvester_wrong", timeoutTicks = 2400)
    public static void noAnnulusNoHarvester(GameTestHelper helper) {
        if (!skip(helper)) StellarHarvesterTestRig.noAnnulusNoHarvester(helper);
    }
}
