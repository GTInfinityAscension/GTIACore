package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Kugelblitz Containment Silo (GTIA-D-321): Hawking's ratios; lasers make a hole and the petals give EU back; a
 * hole under the critical mass flashes out; without the chamber it does not form. Slots 14 and 15.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class KugelblitzGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private KugelblitzGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.KUGELBLITZ_DEF != null) return false;
        GTIAMod.LOGGER.warn("Kugelblitz not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "kugelblitz_math", timeoutTicks = 100)
    public static void hawkingMath(GameTestHelper helper) {
        if (!skip(helper)) KugelblitzTestRig.hawkingMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "kugelblitz_runs", timeoutTicks = 2400)
    public static void lightIntoAHole(GameTestHelper helper) {
        if (!skip(helper)) KugelblitzTestRig.lightIntoAHole(helper);
    }

    @GameTest(template = TEMPLATE, batch = "kugelblitz_wrong", timeoutTicks = 2400)
    public static void noChamberNoSilo(GameTestHelper helper) {
        if (!skip(helper)) KugelblitzTestRig.noChamberNoSilo(helper);
    }
}
