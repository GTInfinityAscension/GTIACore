package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.data.worldgen.GTLayerPattern;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.common.data.GTOres;

import net.minecraft.tags.BiomeTags;

import com.raishxn.gtia.GTIAMod;

/** Steam-era source of phase-bearing ore with a visible Overworld indicator. */
public final class GTIAPhaseOres {

    private GTIAPhaseOres() {}

    public static void register() {
        GTOres.create(GTIAMod.id("phase_matrix_deposit"), vein -> vein
                .clusterSize(4).density(0.40f).weight(35)
                .layer(WorldGenLayers.STONE)
                .heightRangeUniform(40, 60)
                .biomes(BiomeTags.IS_OVERWORLD)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(GTOres.OVERWORLD_RULES)
                                .layer(layer -> layer.weight(1).mat(GTIAAlloys.PHASE_MATRIX).size(2, 3))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(GTIAAlloys.PHASE_MATRIX)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));
    }
}
