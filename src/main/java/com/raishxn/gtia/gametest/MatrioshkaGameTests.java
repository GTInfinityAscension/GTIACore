package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.GTIAMegastructures;

/**
 * The Matrioshka Brain (GTIA-D-311): forms with phase 1 and with all five, works only in the Nexus System, sends
 * CWU and turns idle CWU into Ψ, takes its rings in while running; a wrong block keeps it from forming.
 *
 * <p>
 * The brain needs GTNA and its shape file; without them it is not registered and these tests pass empty. Each test
 * has its own batch: the structure is 219 x 93 x 220 and two at once would overlap.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class MatrioshkaGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private MatrioshkaGameTests() {}

    private static boolean skip(GameTestHelper helper) {
        if (GTIAMegastructures.MATRIOSHKA_BRAIN != null) return false;
        GTIAMod.LOGGER.warn("Matrioshka Brain not registered (no GTNA or no shape file): its GameTests are skipped");
        helper.succeed();
        return true;
    }

    @GameTest(template = TEMPLATE, batch = "matrioshka_phase_one", timeoutTicks = 2400)
    public static void phaseOneFormsAndNeedsTheStar(GameTestHelper helper) {
        if (!skip(helper)) MatrioshkaTestRig.phaseOneFormsAndNeedsTheStar(helper);
    }

    @GameTest(template = TEMPLATE, batch = "matrioshka_all_phases", timeoutTicks = 2400)
    public static void allPhasesComputeAndTurn(GameTestHelper helper) {
        if (!skip(helper)) MatrioshkaTestRig.allPhasesComputeAndTurn(helper);
    }

    @GameTest(template = TEMPLATE, batch = "matrioshka_math", timeoutTicks = 100)
    public static void landauerClockAndCoolant(GameTestHelper helper) {
        if (!skip(helper)) MatrioshkaTestRig.landauerClockAndCoolant(helper);
    }

    @GameTest(template = TEMPLATE, batch = "matrioshka_wrong_block", timeoutTicks = 2400)
    public static void wrongBlockDoesNotForm(GameTestHelper helper) {
        if (!skip(helper)) MatrioshkaTestRig.wrongBlockDoesNotForm(helper);
    }
}
