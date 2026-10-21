package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Fortune multiplies the drops of GregTech ores with the vanilla ore formula (GTIA-D-348). */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class OreFortuneGameTests {

    private static final int BREAKS = 400;

    private OreFortuneGameTests() {}

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 40)
    public static void fortuneMultipliesGregTechOres(GameTestHelper h) {
        Block ore = ChemicalHelper.getBlock(TagPrefix.ore, GTMaterials.Iron);
        h.assertTrue(ore != null, "GTCEu iron ore block exists");
        ItemStack raw = ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron);
        ItemStack plain = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack fortune = new ItemStack(Items.DIAMOND_PICKAXE);
        fortune.enchant(Enchantments.BLOCK_FORTUNE, 3);
        ItemStack silk = new ItemStack(Items.DIAMOND_PICKAXE);
        silk.enchant(Enchantments.SILK_TOUCH, 1);
        double withoutFortune = average(h, ore, raw, plain);
        double withFortune = average(h, ore, raw, fortune);
        h.assertTrue(withoutFortune == 1.0, "no Fortune: exactly one raw ore, got " + withoutFortune);
        // Vanilla Fortune III averages 2.2; 400 breaks keep the mean well inside 1.9-2.5.
        h.assertTrue(withFortune > 1.9 && withFortune < 2.5, "Fortune III averages about 2.2, got " + withFortune);
        h.assertTrue(average(h, ore, raw, silk) == 0.0, "Silk Touch still drops the ore block itself");
        h.succeed();
    }

    private static double average(GameTestHelper h, Block ore, ItemStack raw, ItemStack tool) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 1, 2));
        int total = 0;
        for (int i = 0; i < BREAKS; i++) {
            for (ItemStack drop : Block.getDrops(ore.defaultBlockState(), h.getLevel(), pos, null, null, tool)) {
                if (ItemStack.isSameItem(drop, raw)) total += drop.getCount();
            }
        }
        return total / (double) BREAKS;
    }
}
