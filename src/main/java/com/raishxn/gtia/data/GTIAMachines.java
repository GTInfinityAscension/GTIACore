package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.machine.DiaphragmCellMachine;
import com.raishxn.gtia.machine.LimeKilnMachine;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;

public final class GTIAMachines {

    public static final MachineDefinition CATALYTIC_DEHYDRATOR = GTIAMod.REGISTRATE
            .machine("lv_catalytic_dehydrator",
                    holder -> new SimpleTieredMachine(holder, GTValues.LV, GTMachineUtils.defaultTankSizeFunction))
            .tier(GTValues.LV)
            .langValue("Catalytic Dehydrator")
            .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTIAMod.id("catalytic_dehydrator"),
                    GTIARecipeTypes.CATALYTIC_DEHYDRATOR))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.CATALYTIC_DEHYDRATOR)
            .recipeModifier(GTIAMachines::dehydratorRecipeModifier, true)
            .workableTieredHullModel(GTIAMod.id("block/machines/catalytic_dehydrator"))
            .tooltips(Component.translatable("gtia.machine.catalytic_dehydrator.tooltip.purpose"),
                    Component.translatable("gtia.machine.catalytic_dehydrator.tooltip.steam"))
            .tooltips(GTMachineUtils.workableTiered(GTValues.LV, GTValues.V[GTValues.LV],
                    GTValues.V[GTValues.LV] * 64L, GTIARecipeTypes.CATALYTIC_DEHYDRATOR,
                    GTMachineUtils.defaultTankSizeFunction.applyAsInt(GTValues.LV), true))
            .register();

    public static final MachineDefinition[] FERMENTATION_VAT = registerFermentationVats();

    // ------------------------------------------------------------------ chemistry slice 01 (GTIA-D-228/229)
    // Structures approved in GTIA-D-302 (design/propostas/PROPOSTA_ESTRUTURAS_QUIMICA01_v01.html).

    /**
     * EN: Destructive distillation of wood (GTIA-D-302, option B). A wrought-iron retort on a firebrick bench over a
     * bronze firebox; the gas duct rises over the bench into a water-jacketed condenser and a tar/vinegar decanter.
     * A Coke Oven underneath, so Coke Oven Hatches (front of the firebox, decanter) automate it.
     * PT: Destilação seca da madeira (GTIA-D-302, opção B): retorta de ferro no banco de firebrick sobre a fornalha,
     * duto de gás, condensador com camisa d'água e decantador. Por baixo é uma Coke Oven, então aceita o Coke Oven
     * Hatch na frente da fornalha e no decantador.
     * ZH: 木材干馏 (GTIA-D-302, 方案 B)。炉膛上的耐火砖炉台里放着锻铁干馏罐, 煤气管通向水冷冷凝器和焦油/醋分离罐。
     * 底层是焦炉, 可在炉膛前方和分离罐使用焦炉仓。
     */
    public static final MultiblockMachineDefinition WOOD_RETORT = GTIAMod.REGISTRATE
            .multiblock("wood_retort", CokeOvenMachine::new)
            .langValue("Wood Retort")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.WOOD_RETORT)
            .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> {
                var hatch = blocks(GTMachines.COKE_OVEN_HATCH.get()).setMaxGlobalLimited(3);
                return FactoryBlockPattern.start()
                        .aisle(" SSSSSSS SSS", " KFKKKFK QTQ", " KFKKKFK QGQ", " KFKKKFK QGQ", " KFKKKFK QGQ",
                                " KFFFFFK QGQ", " KKKKKKK QGQ", " KK P    QTQ", " KK PPPPPPP ", " KK         ",
                                " KK         ", " II         ")
                        .aisle(" SSSSSSS SSS", "QKFXXXFKQTPT", "QKFRRRFKQGPG", "QKFR#RFKQGPG", "QKFRRRFKQGPG",
                                "QKFFFFFKQGPG", "QKKKKKKKQGPG", "QKK     QTPT", " KK       P ", " KK         ",
                                " KK         ", " II         ")
                        .aisle(" SSSSSSS SSS", " KFXXXFK QTQ", " KFRRRFK QGQ", " KFR#RFK QGQ", " KFRRRFK QGQ",
                                " KFFFFFK QGQ", " KKKKKKK QGQ", "Q       QQTQ", "            ", "            ",
                                "            ", "            ")
                        .aisle(" SSSSSSS    ", " KFXXXFK  P ", " KFRRRFK    ", " KFR#RFK    ", " KFRRRFK    ",
                                " KFFFFFK    ", " KKKKKKK    ", "Q       Q   ", "            ", "            ",
                                "            ", "            ")
                        .aisle(" SSSSSSS TTT", " KFXXXFK TTT", " KFRRRFK TTT", " KFR#RFK    ", " KFRRRFK    ",
                                " KFFFFFK    ", " KKKKKKK    ", "Q       Q   ", "            ", "            ",
                                "            ", "            ")
                        .aisle(" SSSSSSS TTT", " KFXXXFK TTJ", " KFRRRFK TTT", " KFR#RFKPPP ", " KFRRRFK    ",
                                " KFFFFFK    ", " KKKKKKK    ", "Q       Q   ", "            ", "            ",
                                "            ", "            ")
                        .aisle(" SSSSSSS TTT", "QKFXXXFKQTTT", "QKFRRRFKQTTT", "QKFR#RFKQ   ", "QKFRRRFKQ   ",
                                "QKFFFFFKQ   ", "QKKKKKKKQ   ", "Q       Q   ", "            ", "            ",
                                "            ", "            ")
                        .aisle(" SSSSSSS    ", " KFHIHFK    ", " KFRRRFK    ", " KFRCRFK    ", " KFRRRFK    ",
                                " KFFFFFK    ", " KKKKKKK    ", "            ", "            ", "            ",
                                "            ", "            ")
                        .where('C', Predicates.controller(blocks(definition.getBlock())))
                        .where('S', blocks(Blocks.STONE_BRICKS))
                        .where('K', blocks(Blocks.BRICKS))
                        .where('F', blocks(GTBlocks.CASING_PRIMITIVE_BRICKS.get()))
                        .where('X', blocks(GTBlocks.FIREBOX_BRONZE.get()))
                        .where('R', blocks(GTBlocks.STEEL_BRICKS_HULL.get()))
                        .where('P', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                        .where('T', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                        .where('Q', Predicates.frames(GTMaterials.Bronze))
                        .where('G', blocks(Blocks.GLASS))
                        .where('I', blocks(Blocks.IRON_BARS))
                        .where('H', blocks(GTBlocks.FIREBOX_BRONZE.get()).or(hatch))
                        .where('J', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()).or(hatch))
                        .where('#', Predicates.air())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                    GTIAMod.id("block/multiblock/wood_retort"))
            .tooltips(GTIATooltips.multiblock("wood_retort"))
            .register();

    /**
     * EN: A shaft kiln (GTIA-D-302, option B): wrought-iron shaft with bronze bands and a firebrick lining, fire mouths
     * at the burning zone, a discharge grate at the foot, a bronze hoist tower that lifts the charge to the CO2 hood.
     * Also a Coke Oven underneath: Coke Oven Hatches at the hood (charge) and the front foot (discharge).
     * PT: Forno de cuba (GTIA-D-302, opção B): cuba de ferro forjado com cintas de bronze e revestimento de firebrick,
     * bocas de fogo, grelha de descarga, torre de guincho até a coifa de CO2. Coke Oven Hatch na coifa e no pé.
     * ZH: 竖窑 (GTIA-D-302, 方案 B): 青铜箍的锻铁窑身, 耐火砖内衬, 燃烧带的火口, 底部卸料格栅, 青铜提升塔把料送到
     * CO2 烟罩。焦炉仓位于烟罩 (进料) 和前方底部 (出料)。
     */
    public static final MultiblockMachineDefinition LIME_KILN = GTIAMod.REGISTRATE
            .multiblock("lime_kiln", LimeKilnMachine::new)
            .langValue("Lime Kiln")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.LIME_KILN)
            .appearanceBlock(GTBlocks.CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> {
                var hatch = blocks(GTMachines.COKE_OVEN_HATCH.get()).setMaxGlobalLimited(2);
                return FactoryBlockPattern.start()
                        .aisle("SSSSS  ", "RRRRR  ", "RRRRR  ", "NNNNN  ", "RRRRR  ", "RRXRR  ", "RRRRR  ",
                                "NNNNN  ", "RRRRR  ", "RRRRR  ", "       ", "       ", "       ", "       ")
                        .aisle("SSSSS  ", "RFFFR  ", "RFFFR  ", "NFFFN  ", "RFFFR  ", "RFFFR  ", "RFFFR  ",
                                "NFFFN  ", "RFFFR  ", "RFFFR  ", " TTT   ", "       ", "       ", "       ")
                        .aisle("SSSSSSS", "RFIFRQQ", "RF#FRQQ", "NF#FNQQ", "RF#FRQQ", "XF#FXQQ", "RF#FRQQ",
                                "NF#FNQQ", "RF#FRQQ", "RF#FRQQ", " TPJQQQ", "  P QQQ", "  P  WW", "  I    ")
                        .aisle("SSSSS  ", "RFFFR  ", "RFFFR  ", "NFFFN  ", "RFFFR  ", "RFFFR  ", "RFFFR  ",
                                "NFFFN  ", "RFFFR  ", "RFFFR  ", " TTT   ", "       ", "       ", "       ")
                        .aisle("SSSSS  ", "RRHRR  ", "RRRRR  ", "NNNNN  ", "RRXRR  ", "RRCRR  ", "RRRRR  ",
                                "NNNNN  ", "RRRRR  ", "RRRRR  ", "       ", "       ", "       ", "       ")
                        .where('C', Predicates.controller(blocks(definition.getBlock())))
                        .where('S', blocks(Blocks.STONE_BRICKS))
                        .where('R', blocks(GTBlocks.STEEL_BRICKS_HULL.get()))
                        .where('N', blocks(GTBlocks.BRONZE_BRICKS_HULL.get()))
                        .where('F', blocks(GTBlocks.CASING_PRIMITIVE_BRICKS.get()))
                        .where('X', blocks(GTBlocks.FIREBOX_BRONZE.get()))
                        .where('T', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                        .where('P', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                        .where('Q', Predicates.frames(GTMaterials.Bronze))
                        .where('W', blocks(GTBlocks.CASING_BRONZE_GEARBOX.get()))
                        .where('I', blocks(Blocks.IRON_BARS))
                        .where('H', blocks(GTBlocks.STEEL_BRICKS_HULL.get()).or(hatch))
                        .where('J', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()).or(hatch))
                        .where('#', Predicates.air())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                    GTIAMod.id("block/multiblock/lime_kiln"))
            .tooltips(GTIATooltips.multiblock("lime_kiln"))
            .register();

    /** LV still: the early, expensive route for wood chemistry (the MV Distillery is the efficient one). */
    public static final MachineDefinition STILL = GTIAMod.REGISTRATE
            .machine("lv_still",
                    holder -> new SimpleTieredMachine(holder, GTValues.LV, GTMachineUtils.defaultTankSizeFunction))
            .tier(GTValues.LV)
            .langValue("Still")
            .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTIAMod.id("still"), GTIARecipeTypes.STILL))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.STILL)
            .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
            .workableTieredHullModel(GTIAMod.id("block/machines/still"))
            .tooltips(Component.translatable("gtia.machine.still.tooltip.purpose"))
            .tooltips(GTMachineUtils.workableTiered(GTValues.LV, GTValues.V[GTValues.LV],
                    GTValues.V[GTValues.LV] * 64L, GTIARecipeTypes.STILL,
                    GTMachineUtils.defaultTankSizeFunction.applyAsInt(GTValues.LV), true))
            .register();

    /** LV chlor-alkali cell row, 3 to 9 cells (GTIA-D-302, option C). The diaphragm wears out as a chanced input. */
    public static final MultiblockMachineDefinition DIAPHRAGM_CELL = GTIAMod.REGISTRATE
            .multiblock("diaphragm_cell", DiaphragmCellMachine::new)
            .langValue("Diaphragm Cell")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.DIAPHRAGM_CELL)
            .recipeModifiers(DiaphragmCellMachine::cellParallel, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK,
                    GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> DiaphragmCellMachine.pattern(definition, DiaphragmCellMachine.MIN_CELLS))
            .shapeInfos(DiaphragmCellMachine::shapes)
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTIAMod.id("block/multiblock/diaphragm_cell"))
            .tooltips(GTIATooltips.multiblock("diaphragm_cell"))
            .register();

    // ------------------------------------------------------------------ chemistry slice 02 (MV): provisional hollow
    // boxes
    /**
     * MV Bayer Digester: a 5 x 12 x 5 octagonal autoclave tower.
     * Solid steel casing base (with hatches), vertical steel pipe ribs & manifolds,
     * solid steel structural rings, and heatproof invar casing wall panels.
     */
    public static final MultiblockMachineDefinition BAYER_DIGESTER = GTIAMod.REGISTRATE
            .multiblock("bayer_digester", WorkableElectricMultiblockMachine::new)
            .langValue("Bayer Digester")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.BAYER_DIGESTER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> {
                var casing = blocks(GTBlocks.CASING_STEEL_SOLID.get()).setMinGlobalLimited(20);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false));
                var pipe = blocks(GTBlocks.CASING_STEEL_PIPE.get());
                var heatproof = blocks(GTBlocks.CASING_INVAR_HEATPROOF.get());
                return FactoryBlockPattern.start()
                        .aisle(
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " SPS ",
                                " HPH ",
                                " HPH ",
                                " SPS ",
                                " HPH ",
                                " HPH ",
                                " PPP ",
                                " SSS ",
                                "     ")
                        .aisle(
                                "SSSSS",
                                "P###P",
                                "S###S",
                                "S###S",
                                "H###H",
                                "H###H",
                                "S###S",
                                "H###H",
                                "H###H",
                                "P###P",
                                "S###S",
                                " SSS ")
                        .aisle(
                                "SSSSS",
                                "P###P",
                                "S###S",
                                "P###P",
                                "P###P",
                                "P###P",
                                "P###P",
                                "P###P",
                                "P###P",
                                "P###P",
                                "S###S",
                                " S#S ")
                        .aisle(
                                "SSSSS",
                                "P###P",
                                "S###S",
                                "S###S",
                                "H###H",
                                "H###H",
                                "S###S",
                                "H###H",
                                "H###H",
                                "P###P",
                                "S###S",
                                " SSS ")
                        .aisle(
                                " SCS ",
                                " PPP ",
                                " SSS ",
                                " SPS ",
                                " HPH ",
                                " HPH ",
                                " SPS ",
                                " HPH ",
                                " HPH ",
                                " PPP ",
                                " SSS ",
                                "     ")
                        .where('C', Predicates.controller(blocks(definition.getBlock())))
                        .where('S', casing.or(abilities))
                        .where('P', pipe)
                        .where('H', heatproof)
                        .where('#', Predicates.air())
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTIAMod.id("block/multiblock/bayer_digester"))
            .tooltips(GTIATooltips.multiblock("bayer_digester"))
            .register();
    /**
     * MV Hall-Héroult Cell: a 7 x 5 x 7 electrolytic reduction cell.
     * Invar heatproof bath (hearth and walls containing molten cryolite/alumina at 960 °C),
     * solid steel casing hood and corner pillars (with hatches),
     * and 3 parallel overhead steel pipe busbar rails for carbon anodes.
     */
    public static final MultiblockMachineDefinition HALL_HEROULT_CELL = GTIAMod.REGISTRATE
            .multiblock("hall_heroult_cell", WorkableElectricMultiblockMachine::new)
            .langValue("Hall-Heroult Cell")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.HALL_HEROULT)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_INVAR_HEATPROOF)
            .pattern(definition -> {
                var steelCasing = blocks(GTBlocks.CASING_STEEL_SOLID.get()).setMinGlobalLimited(20);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false));
                var heatproof = blocks(GTBlocks.CASING_INVAR_HEATPROOF.get());
                var pipe = blocks(GTBlocks.CASING_STEEL_PIPE.get());
                return FactoryBlockPattern.start()
                        .aisle(" SHHHS ", " SHHHS ", " SHHHS ", " SSSSS ", " P P P ")
                        .aisle("SHHHHHS", "S#####S", "S#####S", "SSSSSSS", " P P P ")
                        .aisle("HHHHHHH", "H#####H", "H#####H", "SSSSSSS", " P P P ")
                        .aisle("HHHHHHH", "H#####H", "H#####H", "SSSSSSS", " P P P ")
                        .aisle("HHHHHHH", "H#####H", "H#####H", "SSSSSSS", " P P P ")
                        .aisle("SHHHHHS", "S#####S", "S#####S", "SSSSSSS", " P P P ")
                        .aisle(" SHHHS ", " SHCHS ", " SHHHS ", " SSSSS ", " P P P ")
                        .where('C', Predicates.controller(blocks(definition.getBlock())))
                        .where('S', steelCasing.or(abilities))
                        .where('H', heatproof)
                        .where('P', pipe)
                        .where('#', Predicates.air())
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTIAMod.id("block/multiblock/hall_heroult_cell"))
            .tooltips(GTIATooltips.multiblock("hall_heroult_cell"))
            .register();

    /**
     * MV Contact Converter: a 5 x 13 x 5 multi-stage catalytic SO2 -> SO3 converter.
     * Solid steel casing base (with hatches) and 4 catalyst bed levels of V2O5,
     * interleaved with steel pipe casing heat exchangers for intercooling and steam extraction.
     */
    public static final MultiblockMachineDefinition CONTACT_CONVERTER = GTIAMod.REGISTRATE
            .multiblock("contact_converter", WorkableElectricMultiblockMachine::new)
            .langValue("Contact Converter")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.CONTACT_CONVERTER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> {
                var steelCasing = blocks(GTBlocks.CASING_STEEL_SOLID.get()).setMinGlobalLimited(20);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false));
                var pipe = blocks(GTBlocks.CASING_STEEL_PIPE.get());
                return FactoryBlockPattern.start()
                        .aisle(
                                " SSS ",
                                " SPS ",
                                " PPP ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ")
                        .aisle(
                                "SSSSS",
                                "S###S",
                                "P###P",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                "  P  ")
                        .aisle(
                                "SSSSS",
                                "P###P",
                                "P###P",
                                " S#S ",
                                " S#S ",
                                " S#S ",
                                " P#P ",
                                " S#S ",
                                " S#S ",
                                " S#S ",
                                " P#P ",
                                " S#S ",
                                " S S ")
                        .aisle(
                                "SSSSS",
                                "S###S",
                                "P###P",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                "  P  ")
                        .aisle(
                                " SCS ",
                                " SPS ",
                                " PPP ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ",
                                "     ")
                        .where('C', Predicates.controller(blocks(definition.getBlock())))
                        .where('S', steelCasing.or(abilities))
                        .where('P', pipe)
                        .where('#', Predicates.air())
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTIAMod.id("block/multiblock/contact_converter"))
            .tooltips(GTIATooltips.multiblock("contact_converter"))
            .register();
    /**
     * MV Ammonia Converter: a 13 x 7 x 7 horizontal forged high-pressure vessel (Haber-Bosch reactor).
     * Solid steel casing cylindrical hull and flanges (with hatches),
     * forged steel firebox casing hemispherical end heads,
     * steel pipe casing intercooling coils, drain feet, and top gas manifolds,
     * and a hollow 11 x 3 x 3 central reaction chamber.
     */
    public static final MultiblockMachineDefinition AMMONIA_CONVERTER = GTIAMod.REGISTRATE
            .multiblock("ammonia_converter", WorkableElectricMultiblockMachine::new)
            .langValue("Ammonia Converter")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.AMMONIA_CONVERTER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> {
                var steelCasing = blocks(GTBlocks.CASING_STEEL_SOLID.get()).setMinGlobalLimited(20);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false));
                var firebox = blocks(GTBlocks.FIREBOX_STEEL.get());
                var pipe = blocks(GTBlocks.CASING_STEEL_PIPE.get());
                return FactoryBlockPattern.start()
                        .aisle(
                                "             ",
                                "             ",
                                "             ",
                                "  P  P P  P  ",
                                "             ",
                                "             ",
                                "             ")
                        .aisle(
                                "             ",
                                "             ",
                                "FFS  S S  SFF",
                                "FFSSSSSSSSSFF",
                                "FFS  SSS  SFF",
                                "             ",
                                "             ")
                        .aisle(
                                "             ",
                                "FFS  S S  SFF",
                                "SSSSSSSSSSSSS",
                                "S###########S",
                                "SSSSSSSSSSSSS",
                                "FFS  SSS  SFF",
                                "             ")
                        .aisle(
                                "  P  P P  P  ",
                                "FFPSSPSPSSPFF",
                                "S###########S",
                                "S###########S",
                                "S###########S",
                                "FFPSSPSSSSPFF",
                                "  P  P P  P  ")
                        .aisle(
                                "             ",
                                "FFS  SSS  SFF",
                                "SSSSSSSSSSSSS",
                                "S###########S",
                                "SSSSSSSSSSSSS",
                                "FFS  SSS  SFF",
                                "             ")
                        .aisle(
                                "             ",
                                "             ",
                                "FFS  SSS  SFF",
                                "FFPSSSCSSSPFF",
                                "FFS  SSS  SFF",
                                "             ",
                                "             ")
                        .aisle(
                                "             ",
                                "             ",
                                "             ",
                                "  P       P  ",
                                "             ",
                                "             ",
                                "             ")
                        .where('C', Predicates.controller(blocks(definition.getBlock())))
                        .where('S', steelCasing.or(abilities))
                        .where('F', firebox)
                        .where('P', pipe)
                        .where('#', Predicates.air())
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTIAMod.id("block/multiblock/ammonia_converter"))
            .tooltips(GTIATooltips.multiblock("ammonia_converter"))
            .register();

    /**
     * MV Solvay Tower: a 5 x 10 x 5 vertical carbonation and absorption column.
     * Octagonal solid steel casing base at ground level (with hatches),
     * column body, external structural fins, upper CO2 vent crown, and top exhaust flange,
     * with steel pipe casing lower distribution ring and 4 internal bubble-cap reaction trays.
     */
    public static final MultiblockMachineDefinition SOLVAY_TOWER = GTIAMod.REGISTRATE
            .multiblock("solvay_tower", WorkableElectricMultiblockMachine::new)
            .langValue("Solvay Tower")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTIARecipeTypes.SOLVAY_TOWER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> {
                var steelCasing = blocks(GTBlocks.CASING_STEEL_SOLID.get()).setMinGlobalLimited(16);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false));
                var pipe = blocks(GTBlocks.CASING_STEEL_PIPE.get());
                return FactoryBlockPattern.start()
                        .aisle(
                                " SSS ",
                                " PPP ",
                                " SSS ",
                                " SSS ",
                                "  S  ",
                                "  S  ",
                                "  S  ",
                                "  S  ",
                                "     ",
                                "     ")
                        .aisle(
                                "SSSSS",
                                "P###P",
                                "S###S",
                                "S###S",
                                " S#S ",
                                " S#S ",
                                " S#S ",
                                " S#S ",
                                "  S  ",
                                "  S  ")
                        .aisle(
                                "SSSSS",
                                "P###P",
                                "S###S",
                                "S###S",
                                " P#P ",
                                " P#P ",
                                " P#P ",
                                " P#P ",
                                " S S ",
                                " S S ")
                        .aisle(
                                "SSSSS",
                                "P###P",
                                "S###S",
                                "S###S",
                                " S#S ",
                                " S#S ",
                                " S#S ",
                                " S#S ",
                                "  S  ",
                                "  S  ")
                        .aisle(
                                " SSS ",
                                " PCP ",
                                " SSS ",
                                " SSS ",
                                "  S  ",
                                "  S  ",
                                "  S  ",
                                "  S  ",
                                "     ",
                                "     ")
                        .where('C', Predicates.controller(blocks(definition.getBlock())))
                        .where('S', steelCasing.or(abilities))
                        .where('P', pipe)
                        .where('#', Predicates.air())
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTIAMod.id("block/multiblock/solvay_tower"))
            .tooltips(GTIATooltips.multiblock("solvay_tower"))
            .register();

    private GTIAMachines() {}

    private static ModifierFunction dehydratorRecipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (GTIAMod.id("catalytic_dehydrator/ethanol_to_ethylene").equals(recipe.getId()) &&
                machine instanceof WorkableTieredMachine workable) {
            for (int tank = 0; tank < workable.importFluids.getTanks(); tank++) {
                var fluid = workable.importFluids.getFluidInTank(tank);
                if (fluid.isFluidEqual(GTMaterials.Steam.getFluid(1)) && fluid.getAmount() >= 100) {
                    // Prefer the faster recipe while enough steam is present; that recipe consumes the steam.
                    return ModifierFunction.NULL;
                }
            }
        }
        return GTRecipeModifiers.OC_NON_PERFECT.getModifier(machine, recipe);
    }

    private static MachineDefinition[] registerFermentationVats() {
        MachineDefinition[] machines = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : GTValues.tiersBetween(GTValues.LV, GTValues.OpV)) {
            String id = GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_fermentation_vat";
            machines[tier] = GTIAMod.REGISTRATE
                    .machine(id, holder -> new SimpleTieredMachine(holder, tier,
                            GTMachineUtils.defaultTankSizeFunction))
                    .tier(tier)
                    .langValue("%s Fermentation Vat %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTIAMod.id("fermentation_vat"),
                            GTIARecipeTypes.FERMENTATION_VAT))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTIARecipeTypes.FERMENTATION_VAT)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullModel(GTIAMod.id("block/machines/primitive_fermentation_vat"))
                    .tooltips(Component.translatable("gtia.machine.fermentation_vat.tooltip.purpose"),
                            Component.translatable("gtia.machine.fermentation_vat.tooltip.yield"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier],
                            GTValues.V[tier] * 64L, GTIARecipeTypes.FERMENTATION_VAT,
                            GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier), true))
                    .register();
        }
        return machines;
    }

    public static void init() {}
}
