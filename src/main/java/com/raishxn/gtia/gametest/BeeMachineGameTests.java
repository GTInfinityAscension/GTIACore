package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
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

    @GameTest(template = "qa_empty_5x5", batch = "bees", timeoutTicks = 20)
    public static void beesOnlyComeFromTheGtiaLine(GameTestHelper helper) {
        if (!ModList.get().isLoaded("productivebees")) {
            helper.succeed();
            return;
        }
        var recipes = helper.getLevel().getServer().getRecipeManager().getRecipes();
        long pb = recipes.stream().filter(r -> "productivebees".equals(r.getId().getNamespace())).count();
        helper.assertTrue(pb == 0, pb + " Productive Bees recipes are still loaded");
        for (String step : new String[] { "breed_iron", "apiary_iron", "comb_dust_iron", "comb_ore_iron" })
            helper.assertTrue(
                    recipes.stream()
                            .anyMatch(r -> "gtia".equals(r.getId().getNamespace()) &&
                                    r.getId().getPath().endsWith("bees/" + step)),
                    "missing GTIA bee recipe " + step);
        helper.succeed();
    }
}
