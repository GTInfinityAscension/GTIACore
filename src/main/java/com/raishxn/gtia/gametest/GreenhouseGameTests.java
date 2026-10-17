package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.data.GTIAGreenhouse;

/** Greenhouse forms from its own preview and breaks when a glass pane is missing. */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class GreenhouseGameTests {

    private GreenhouseGameTests() {}

    /** Builds the first preview shape at (1,1,1) and returns the controller. */
    static IMultiController build(GameTestHelper h) {
        var shape = GTIAGreenhouse.GREENHOUSE.getMatchingShapes().get(0).getBlocks();
        IMultiController controller = null;
        for (int x = 0; x < shape.length; x++)
            for (int y = 0; y < shape[x].length; y++)
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    BlockPos pos = new BlockPos(x + 1, y + 1, z + 1);
                    h.setBlock(pos, info == null ? Blocks.AIR.defaultBlockState() : info.getBlockState());
                    if (info != null && info.getBlockState().is(GTIAGreenhouse.GREENHOUSE.getBlock()) &&
                            h.getBlockEntity(pos) instanceof IMachineBlockEntity be &&
                            be.getMetaMachine() instanceof IMultiController c)
                        controller = c;
                }
        return controller;
    }

    @GameTest(template = "qa_empty_12", batch = "greenhouse", timeoutTicks = 40)
    public static void greenhouseForms(GameTestHelper h) {
        var controller = build(h);
        h.assertTrue(controller != null, "the preview has a controller");
        h.assertTrue(controller.checkPattern(), "the preview structure must form");
        h.succeed();
    }

    @GameTest(template = "qa_empty_12", batch = "greenhouse", timeoutTicks = 40)
    public static void greenhouseNeedsItsGlass(GameTestHelper h) {
        var controller = build(h);
        var glass = GTIAGreenhouse.GREENHOUSE.getMatchingShapes().get(0).getBlocks();
        // EN: Break the first glass block found; the structure must stop matching.
        // PT: Quebra o primeiro vidro encontrado; a estrutura deve deixar de casar.
        // ZH: 破坏找到的第一块玻璃；结构必须不再匹配。
        outer:
        for (int x = 0; x < glass.length; x++)
            for (int y = 0; y < glass[x].length; y++)
                for (int z = 0; z < glass[x][y].length; z++) {
                    var info = glass[x][y][z];
                    if (info != null && (info.getBlockState().is(Blocks.GLASS) ||
                            info.getBlockState().is(com.gregtechceu.gtceu.common.data.GTBlocks.CASING_TEMPERED_GLASS
                                    .get()))) {
                        h.setBlock(new BlockPos(x + 1, y + 1, z + 1), Blocks.AIR);
                        break outer;
                    }
                }
        h.assertFalse(controller.checkPattern(), "a greenhouse with a hole must not form");
        h.succeed();
    }
}
