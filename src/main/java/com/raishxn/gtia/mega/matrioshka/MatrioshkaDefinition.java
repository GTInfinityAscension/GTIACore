package com.raishxn.gtia.mega.matrioshka;

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

/** Registration and pattern of the Matrioshka Brain; loaded only with GTNA (see GTIAMegastructures). */
public final class MatrioshkaDefinition {

    private MatrioshkaDefinition() {}

    public static MultiblockMachineDefinition register() {
        return GTIAMod.REGISTRATE
                .multiblock("matrioshka_brain", MatrioshkaBrainMachine::new)
                .langValue("Matrioshka Brain")
                .rotationState(RotationState.NON_Y_AXIS)
                // The renderer and the capture assume the pattern stands upright and unmirrored.
                .allowExtendedFacing(false)
                .allowFlip(false)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> MegaPattern.block(
                        MegaShape.get(MatrioshkaBrainMachine.SHAPE).part(MegaShape.Part.STATION).block()))
                .pattern(MatrioshkaDefinition::pattern)
                .modelProperty(com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                        com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.IDLE)
                .model(com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/hpca"))
                        .andThen(builder -> builder.addDynamicRenderer(
                                com.raishxn.gtia.mega.common.client.MegaRenderer.Render::new)))
                .tooltips(Component.translatable("gtia.machine.matrioshka_brain.tooltip.0"),
                        Component.translatable("gtia.machine.matrioshka_brain.tooltip.1"),
                        Component.translatable("gtia.machine.matrioshka_brain.tooltip.2"))
                .register();
    }

    /** The pattern of all five phases; the station's casings take CWU, coolant and repair hatches. */
    public static BlockPattern pattern(MultiblockMachineDefinition definition) {
        return MegaPattern.build(MatrioshkaBrainMachine.SHAPE, definition, casing -> casing
                .or(abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION).setMaxGlobalLimited(2).setPreviewCount(1))
                .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1)));
    }
}
