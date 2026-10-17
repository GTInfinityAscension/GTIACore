package com.raishxn.gtia.chem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIATooltips;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;

/**
 * The MV/HV chemistry plants (GTIA-D-281). Aisle rows go from the bottom up; the controller sits on the front aisle.
 * C controller, X main casing (takes the hatches), H heat-proof casing, P steel pipe casing, G tempered glass.
 */
public final class ChemPlants {

    private static final ResourceLocation STEEL = GTCEu.id("block/casings/solid/machine_casing_solid_steel");
    private static final ResourceLocation STAINLESS = GTCEu
            .id("block/casings/solid/machine_casing_clean_stainless_steel");

    /** Ostwald (MV): a burner of two heat-proof rings over the base, a steel absorption column on top. */
    public static final MultiblockMachineDefinition OSTWALD_CONVERTER = plant("ostwald_converter", "Ostwald Converter",
            ChemRecipeTypes.OSTWALD_CONVERTER, GTBlocks.CASING_STEEL_SOLID::get, STEEL, 10, new String[][] {
                    { "XXX", "HHH", "HHH", "XXX", "XPX", "XPX", " P " },
                    { "XXX", "H#H", "H#H", "X#X", "P#P", "P#P", "PPP" },
                    { "XCX", "HHH", "HHH", "XXX", "XPX", "XPX", " P " } });

    /** Claus (HV): a heat-proof thermal stage, a pipe neck and a stainless catalytic stage, lying down. */
    public static final MultiblockMachineDefinition CLAUS_UNIT = plant("claus_unit", "Claus Unit",
            ChemRecipeTypes.CLAUS_UNIT, GTBlocks.CASING_STAINLESS_CLEAN::get, STAINLESS, 10, new String[][] {
                    { "HHH", "HHH", "HHH" },
                    { "HHH", "H#H", "HHH" },
                    { "HHH", "HHH", "HHH" },
                    { " P ", "PPP", " P " },
                    { "XXX", "XXX", "XXX" },
                    { "XXX", "X#X", "XXX" },
                    { "XXX", "XCX", "XXX" } });

    /** Kroll (HV): a sealed retort, three heat-proof layers between stainless lids, an argon pipe on top. */
    public static final MultiblockMachineDefinition KROLL_REACTOR = plant("kroll_reactor", "Kroll Reactor",
            ChemRecipeTypes.KROLL_REACTOR, GTBlocks.CASING_STAINLESS_CLEAN::get, STAINLESS, 8, new String[][] {
                    { "XXX", "HHH", "HHH", "HHH", "XXX" },
                    { "XXX", "H#H", "H#H", "H#H", "XPX" },
                    { "XCX", "HHH", "HHH", "HHH", "XXX" } });

    /** Siemens (HV): two glass bell jars over hot silicon rods, fed by pipes from below. */
    public static final MultiblockMachineDefinition SIEMENS_REACTOR = plant("siemens_reactor", "Siemens Reactor",
            ChemRecipeTypes.SIEMENS_REACTOR, GTBlocks.CASING_STAINLESS_CLEAN::get, STAINLESS, 12, new String[][] {
                    { "XXXXX", "XGXGX", "XGXGX", "XXXXX" },
                    { "XPXPX", "G#X#G", "G#X#G", "XXXXX" },
                    { "XXCXX", "XGXGX", "XGXGX", "XXXXX" } });

    private ChemPlants() {}

    public static void init() {}

    private static MultiblockMachineDefinition plant(String id, String name, GTRecipeType type,
                                                     Supplier<? extends Block> casing, ResourceLocation casingModel,
                                                     int minCasings, String[][] aisles) {
        return GTIAMod.REGISTRATE.multiblock(id, WorkableElectricMultiblockMachine::new)
                .langValue(name)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(type)
                .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
                .appearanceBlock(casing)
                .pattern(definition -> {
                    TraceabilityPredicate main = blocks(casing.get()).setMinGlobalLimited(minCasings)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false));
                    var pattern = FactoryBlockPattern.start();
                    for (String[] aisle : aisles) pattern.aisle(aisle);
                    return pattern
                            .where('C', Predicates.controller(blocks(definition.getBlock())))
                            .where('X', main)
                            .where('H', blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                            .where('P', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                            .where('G', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                            .where('#', Predicates.air())
                            .where(' ', Predicates.any())
                            .build();
                })
                .workableCasingModel(casingModel, GTIAMod.id("block/multiblock/" + id))
                .tooltips(GTIATooltips.multiblock(id))
                .register();
    }
}
