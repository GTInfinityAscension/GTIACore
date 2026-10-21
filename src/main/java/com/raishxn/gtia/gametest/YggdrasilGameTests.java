package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Yggdrasil Orchid (GTIA-D-327): the best depth and the growth curve; it harvests a Greenhouse recipe in
 * parallel and gives biomass; without the equatorial shell it does not form. Slots 22, 23.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class YggdrasilGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private YggdrasilGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.YGGDRASIL_DEF != null) return false;
        GTIAMod.LOGGER.warn("Yggdrasil not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "yggdrasil_math", timeoutTicks = 100)
    public static void gardenMath(GameTestHelper helper) {
        if (!skip(helper)) YggdrasilTestRig.gardenMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "yggdrasil_runs", timeoutTicks = 2400)
    public static void harvestsWithBiomass(GameTestHelper helper) {
        if (!skip(helper)) YggdrasilTestRig.harvestsWithBiomass(helper);
    }

    @GameTest(template = TEMPLATE, batch = "yggdrasil_wrong", timeoutTicks = 2400)
    public static void noShellNoOrchid(GameTestHelper helper) {
        if (!skip(helper)) YggdrasilTestRig.noShellNoOrchid(helper);
    }
}
