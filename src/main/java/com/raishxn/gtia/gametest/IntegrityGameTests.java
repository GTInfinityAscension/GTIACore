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
        // EN: Since GTIA-D-335 the Rocket Assembler (15 x 15 x 20) is the only GTIA multiblock below megastructure
        // size.
        // PT: Desde a GTIA-D-335 a Montadora de Foguetes (15 x 15 x 20) é o único multibloco menor que uma mega.
        // ZH: 自 GTIA-D-335 起，火箭组装机（15 x 15 x 20）是 GTIA 唯一小于巨构尺寸的多方块。
        Sweeps.multiblocksFormFromPreview(h, GTIAMod.MOD_ID, 24).assertClean(h, 1);
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
