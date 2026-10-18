package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;

import java.util.ArrayList;
import java.util.List;

/** Every GTIA multiblock controller faces out of its structure, so the player can reach it. */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTIAMod.MOD_ID)
public final class ControllerFacingGameTests {

    private ControllerFacingGameTests() {}

    @GameTest(template = "qa_empty_5x5", batch = "controller_facing", timeoutTicks = 20)
    public static void controllersFaceOutOfTheirStructure(GameTestHelper h) {
        List<String> inward = new ArrayList<>();
        int checked = 0;
        for (var definition : GTRegistries.MACHINES.values()) {
            if (!(definition instanceof MultiblockMachineDefinition multi) ||
                    !GTIAMod.MOD_ID.equals(multi.getId().getNamespace()))
                continue;
            var shapes = multi.getMatchingShapes();
            if (shapes.isEmpty()) continue;
            var blocks = shapes.get(0).getBlocks();
            // EN: GTCEu builds the preview with the controller facing north (towards -z); the block in front of it must
            // be outside the shape or empty. The pattern's last aisle is the front one.
            // PT: O GTCEu monta a prévia com o controlador virado para o norte (-z); o bloco na frente dele precisa
            // estar fora da forma ou vazio. A última aisle do pattern é a da frente.
            // ZH: GTCEu 生成预览时控制器朝北（-z）；它前方的方块必须在结构之外或为空。图案的最后一层 aisle 是正面。
            for (int x = 0; x < blocks.length; x++)
                for (int y = 0; y < blocks[x].length; y++)
                    for (int z = 0; z < blocks[x][y].length; z++) {
                        var info = blocks[x][y][z];
                        if (info == null || !(info.getBlockState().getBlock() instanceof MetaMachineBlock block) ||
                                block.getDefinition() != multi)
                            continue;
                        checked++;
                        var front = z == 0 ? null : blocks[x][y][z - 1];
                        if (front != null && !front.getBlockState().isAir()) inward.add(multi.getId().toString());
                    }
        }
        h.assertTrue(checked > 0, "no GTIA multiblock controller was checked");
        h.assertTrue(inward.isEmpty(), "controllers facing into their structure: " + inward);
        h.succeed();
    }
}
