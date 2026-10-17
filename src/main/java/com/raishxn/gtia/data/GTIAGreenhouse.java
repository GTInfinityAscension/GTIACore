package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.GTIAMod;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;

/**
 * Greenhouse (MV, GTIA-D-271): a 5 x 4 x 5 glass house on a soil floor. Crops are kept as seed items in the input bus,
 * so a farm of hundreds of plants is one block entity.
 */
public final class GTIAGreenhouse {

    /** Front aisle, then three soil aisles, then the back aisle; each string is one layer from bottom to top. */
    public static final String[][] AISLES = {
            { "CCCCC", "CGSGC", "CGGGC", "CCCCC" },
            { "CDDDC", "G###G", "G###G", "CGGGC" },
            { "CDDDC", "G###G", "G###G", "CGGGC" },
            { "CDDDC", "G###G", "G###G", "CGGGC" },
            { "CCCCC", "CGGGC", "CGGGC", "CCCCC" } };

    public static final MultiblockMachineDefinition GREENHOUSE = GTIAMod.REGISTRATE
            .multiblock("greenhouse", WorkableElectricMultiblockMachine::new)
            .langValue("Greenhouse")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.GREENHOUSE)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> {
                var builder = FactoryBlockPattern.start();
                for (String[] aisle : AISLES) builder.aisle(aisle);
                return builder
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where('C', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(20)
                                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                .or(Predicates.autoAbilities(true, false, false)))
                        .where('G', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()).or(blocks(Blocks.GLASS)))
                        .where('D', blocks(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.FARMLAND, Blocks.MOSS_BLOCK))
                        .where('#', Predicates.air())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/multiblock_workable"))
            .tooltips(Component.translatable("gtia.machine.greenhouse.tooltip.purpose"),
                    Component.translatable("gtia.machine.greenhouse.tooltip.seed"))
            .register();

    private GTIAGreenhouse() {}

    public static void init() {}
}
