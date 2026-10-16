package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.gtinfinityascension.gtialib.test.Sweeps;
import com.raishxn.gtia.GTIAMod;

/**
 * Sweeps over everything the core registers (GTIA Lib test kit), so a new machine, item or recipe is checked the
 * day it is added: multiblocks form from their own preview, recipes have no empty ingredient, tooltips build, and
 * every name exists in en_us, pt_br and zh_cn.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class IntegrityGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";

    private IntegrityGameTests() {}

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void everyMultiblockFormsFromItsPreview(GameTestHelper h) {
        Sweeps.multiblocksFormFromPreview(h, GTIAMod.MOD_ID, 16).assertClean(h, 8);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void everyRecipeHasRealIngredientsAndOutputs(GameTestHelper h) {
        Sweeps.recipesHaveRealContents(h, GTIAMod.MOD_ID).assertClean(h, 1);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void everyMachineTooltipBuilds(GameTestHelper h) {
        Sweeps.machineTooltipsBuild(GTIAMod.MOD_ID).assertClean(h, 1);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 20)
    public static void everyNameExistsInAllThreeLanguages(GameTestHelper h) {
        Sweeps.namesInAllLanguages(GTIAMod.MOD_ID).assertClean(h, 50);
        h.succeed();
    }
}
