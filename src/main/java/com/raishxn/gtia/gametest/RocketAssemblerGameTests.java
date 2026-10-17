package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.data.rocket.GTIARocketAssembler;

/** Rocket Assembler forms from its own preview and breaks when a gearbox of a service tower is missing. */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class RocketAssemblerGameTests {

    private RocketAssemblerGameTests() {}

    static IMultiController build(GameTestHelper h) {
        var shape = GTIARocketAssembler.ROCKET_ASSEMBLER.getMatchingShapes().get(0).getBlocks();
        IMultiController controller = null;
        for (int x = 0; x < shape.length; x++)
            for (int y = 0; y < shape[x].length; y++)
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    BlockPos pos = new BlockPos(x + 1, y + 1, z + 1);
                    h.setBlock(pos, info == null ? Blocks.AIR.defaultBlockState() : info.getBlockState());
                    if (info != null && info.getBlockState().is(GTIARocketAssembler.ROCKET_ASSEMBLER.getBlock()) &&
                            h.getBlockEntity(pos) instanceof IMachineBlockEntity be &&
                            be.getMetaMachine() instanceof IMultiController c)
                        controller = c;
                }
        return controller;
    }

    @GameTest(template = "qa_empty_24", batch = "rocket_assembler", timeoutTicks = 60)
    public static void rocketAssemblerForms(GameTestHelper h) {
        var controller = build(h);
        h.assertTrue(controller != null, "the preview has a controller");
        h.assertTrue(controller.checkPattern(), "the preview structure must form");
        h.succeed();
    }

    @GameTest(template = "qa_empty_24", batch = "rocket_assembler", timeoutTicks = 60)
    public static void rocketAssemblerNeedsItsTowers(GameTestHelper h) {
        var controller = build(h);
        var shape = GTIARocketAssembler.ROCKET_ASSEMBLER.getMatchingShapes().get(0).getBlocks();
        // EN: Remove one stainless gearbox from a service tower; the complex must stop matching.
        // PT: Tira uma caixa de engrenagens de aço inox de uma torre de serviço; o complexo deve deixar de casar.
        // ZH: 从服务塔上移除一个不锈钢齿轮箱外壳；结构必须不再匹配。
        outer:
        for (int x = 0; x < shape.length; x++)
            for (int y = 0; y < shape[x].length; y++)
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    if (info != null && info.getBlockState().is(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get())) {
                        h.setBlock(new BlockPos(x + 1, y + 1, z + 1), Blocks.AIR);
                        break outer;
                    }
                }
        h.assertFalse(controller.checkPattern(), "a rocket assembler with a missing gearbox must not form");
        h.succeed();
    }
}
