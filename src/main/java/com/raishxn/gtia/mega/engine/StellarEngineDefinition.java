package com.raishxn.gtia.mega.engine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.common.MegaPattern;
import com.raishxn.gtia.mega.common.MegaShape;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;

/** Registration and pattern of the Stellar Engine; loaded only with GTNA (see GTIAMegastructures). */
public final class StellarEngineDefinition {

    private StellarEngineDefinition() {}

    public static MultiblockMachineDefinition register() {
        return GTIAMod.REGISTRATE
                .multiblock("stellar_engine", StellarEngineMachine::new)
                .langValue("Stellar Engine")
                .rotationState(RotationState.NON_Y_AXIS)
                // The renderer and the capture assume the pattern stands upright and unmirrored.
                .allowExtendedFacing(false)
                .allowFlip(false)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> MegaPattern.block(
                        MegaShape.get(StellarEngineMachine.SHAPE).part(MegaShape.Part.STATION).block()))
                .pattern(StellarEngineDefinition::pattern)
                .modelProperty(com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                        com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.IDLE)
                .model(com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel(
                        GTCEu.id("block/casings/fusion/fusion_casing_mk3"),
                        GTCEu.id("block/multiblock/fusion_reactor"))
                        .andThen(builder -> builder.addDynamicRenderer(
                                com.raishxn.gtia.mega.common.client.MegaRenderer.Render::new)))
                .tooltips(Component.translatable("gtia.machine.stellar_engine.tooltip.0"),
                        Component.translatable("gtia.machine.stellar_engine.tooltip.1"),
                        Component.translatable("gtia.machine.stellar_engine.tooltip.2"))
                .register();
    }

    /** All five phases; the station's casings take repair blocks and give plasma and neutronium. */
    public static BlockPattern pattern(MultiblockMachineDefinition definition) {
        return MegaPattern.build(StellarEngineMachine.SHAPE, definition, casing -> casing
                .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1)));
    }
}
