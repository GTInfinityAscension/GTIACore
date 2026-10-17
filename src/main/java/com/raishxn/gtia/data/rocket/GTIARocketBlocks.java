package com.raishxn.gtia.data.rocket;

import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.GTIAMod;
import com.tterrag.registrate.util.entry.BlockEntry;

/**
 * EN: The Rocket Assembly Casing (GTIA-D-274, option 1 chosen by the author). Connected textures come from LDLib: the
 * texture's .mcmeta points at the 32 x 32 `_ctm` sheet, the same format as GTCEu's casings.
 * PT: O casing da Montadora de Foguetes (GTIA-D-274, opção 1 escolhida pelo autor). As texturas conectadas vêm do
 * LDLib: o .mcmeta da textura aponta para a folha `_ctm` 32 x 32, no mesmo formato dos casings do GTCEu.
 * ZH: 火箭装配外壳（GTIA-D-274，作者选择的方案 1）。连接纹理由 LDLib 提供：纹理的 .mcmeta 指向 32 x 32 的 `_ctm`
 * 贴图，格式与 GTCEu 的外壳相同。
 */
public final class GTIARocketBlocks {

    public static final BlockEntry<Block> ROCKET_ASSEMBLY_CASING = GTIAMod.REGISTRATE
            .block("rocket_assembly_casing", Block::new)
            .lang("Rocket Assembly Casing")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .addLayer(() -> RenderType::solid)
            .exBlockstate(GTModels.cubeAllModel(GTIAMod.id("block/casings/rocket_assembly_casing")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();

    private GTIARocketBlocks() {}

    public static void init() {}
}
