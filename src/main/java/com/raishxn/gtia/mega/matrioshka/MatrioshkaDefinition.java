package com.raishxn.gtia.mega.matrioshka;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtna.common.machine.multiblock.originals.OriginalLayout;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

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
                .appearanceBlock(() -> block(MatrioshkaShape.Part.STATION.block))
                .pattern(MatrioshkaDefinition::pattern)
                .modelProperty(com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                        com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.IDLE)
                .model(com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/hpca"))
                        .andThen(builder -> builder.addDynamicRenderer(
                                com.raishxn.gtia.mega.matrioshka.client.MatrioshkaRenderer.Render::new)))
                .tooltips(Component.translatable("gtia.machine.matrioshka_brain.tooltip.0"),
                        Component.translatable("gtia.machine.matrioshka_brain.tooltip.1"),
                        Component.translatable("gtia.machine.matrioshka_brain.tooltip.2"))
                .register();
    }

    private static Block block(String id) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
    }

    /** The pattern of all five phases; phases 2 to 5 accept air until they are built. */
    public static BlockPattern pattern(MultiblockMachineDefinition definition) {
        var shape = MatrioshkaShape.get();
        var pattern = FactoryBlockPattern.start();
        for (int i = 0; i < shape.nz; i++) pattern.aisle(shape.aisle(i));
        for (var part : MatrioshkaShape.Part.values()) {
            Block block = block(part.block);
            char letter = part.letter;
            // Every predicate is registered with what it accepts, so GTNA's StructureWatch tests single positions.
            if (part == MatrioshkaShape.Part.HATCH)
                pattern.where(letter, OriginalLayout.register(blocks(block)
                        .or(abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION).setMaxGlobalLimited(2)
                                .setPreviewCount(1)),
                        new OriginalLayout.Matcher(letter, OriginalLayout.Kind.HATCH, block, null)));
            else if (part.phase == 1)
                pattern.where(letter, OriginalLayout.register(blocks(block),
                        new OriginalLayout.Matcher(letter, OriginalLayout.Kind.BLOCK, block, null)));
            else pattern.where(letter, OriginalLayout.register(blocks(block).or(air()),
                    new OriginalLayout.Matcher(letter, OriginalLayout.Kind.BLOCK_OR_AIR, block,
                            part.track != null ? part.track : "phase" + part.phase)));
        }
        return pattern.where('~', OriginalLayout.register(controller(blocks(definition.getBlock())),
                new OriginalLayout.Matcher('~', OriginalLayout.Kind.CONTROLLER, definition.getBlock(), null)))
                .where(' ', any()).build();
    }
}
