package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.data.GTIABeeMachines;
import com.raishxn.gtia.data.GTIARecipeTypes;

/** Industrial Apiary exists from HV to UV and never earlier (GTIA-D-259). */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class BeeMachineGameTests {

    private BeeMachineGameTests() {}

    @GameTest(template = "qa_empty_5x5", batch = "bees", timeoutTicks = 20)
    public static void apiaryStartsAtHv(GameTestHelper helper) {
        for (int tier = GTValues.ULV; tier < GTIABeeMachines.FIRST_TIER; tier++)
            helper.assertTrue(GTIABeeMachines.INDUSTRIAL_APIARY[tier] == null,
                    "no Industrial Apiary before HV, found one at " + GTValues.VN[tier]);
        for (int tier = GTValues.HV; tier <= GTValues.UV; tier++) {
            var machine = GTIABeeMachines.INDUSTRIAL_APIARY[tier];
            helper.assertTrue(machine != null, "missing Industrial Apiary at " + GTValues.VN[tier]);
            helper.assertTrue(machine.getRecipeTypes()[0] == GTIARecipeTypes.INDUSTRIAL_APIARY,
                    "apiary must use the industrial_apiary recipe type");
        }
        helper.succeed();
    }
}
