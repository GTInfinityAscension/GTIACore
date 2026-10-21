package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Star Lifter (GTIA-D-317): the lifting formulas; with the ring current it lifts, the star gets lighter and the
 * ring is captured; before the spires only plasma comes out; deep stirring makes flares; without the girdle it does
 * not form. Needs GTNA and the shape files (otherwise these pass empty). Slots 5 to 7.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class StarLifterGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private StarLifterGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.STAR_LIFTER_DEF != null) return false;
        GTIAMod.LOGGER.warn("Star Lifter not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "star_lifter_math", timeoutTicks = 100)
    public static void liftingMath(GameTestHelper helper) {
        if (!skip(helper)) StarLifterTestRig.liftingMath(helper);
    }

    @GameTest(template = TEMPLATE, batch = "star_lifter_runs", timeoutTicks = 2400)
    public static void liftsAndShrinksTheStar(GameTestHelper helper) {
        if (!skip(helper)) StarLifterTestRig.liftsAndShrinksTheStar(helper);
    }

    @GameTest(template = TEMPLATE, batch = "star_lifter_plasma", timeoutTicks = 2400)
    public static void plasmaOnlyBeforeTheSpires(GameTestHelper helper) {
        if (!skip(helper)) StarLifterTestRig.plasmaOnlyBeforeTheSpires(helper);
    }

    @GameTest(template = TEMPLATE, batch = "star_lifter_wrong", timeoutTicks = 2400)
    public static void noGirdleNoLifter(GameTestHelper helper) {
        if (!skip(helper)) StarLifterTestRig.noGirdleNoLifter(helper);
    }
}
