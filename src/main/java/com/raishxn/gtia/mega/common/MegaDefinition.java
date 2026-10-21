package com.raishxn.gtia.mega.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Function;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;

/**
 * Registration of a megastructure controller from its shape: the station block as appearance, the five-phase pattern,
 * the shared renderer and the station's hatch slots. The English name and the tooltip lines
 * ({@code gtia.machine.<id>.tooltip.<i>}) come from mega_lang.
 */
public final class MegaDefinition {

    /** Hatches the station takes: what, how many at most. */
    public record Hatch(PartAbility ability, int max) {}

    private MegaDefinition() {}

    public static MultiblockMachineDefinition register(String id,
                                                       Function<IMachineBlockEntity, ? extends MegaMachine> factory,
                                                       int tooltips, ResourceLocation casing, Hatch... hatches) {
        return register(id, factory, tooltips, casing, GTRecipeTypes.DUMMY_RECIPES, hatches);
    }

    public static MultiblockMachineDefinition register(String id,
                                                       Function<IMachineBlockEntity, ? extends MegaMachine> factory,
                                                       int tooltips, ResourceLocation casing, GTRecipeType type,
                                                       Hatch... hatches) {
        var tips = new Component[tooltips];
        for (int i = 0; i < tooltips; i++) tips[i] = Component.translatable("gtia.machine." + id + ".tooltip." + i);
        return GTIAMod.REGISTRATE
                .multiblock(id, factory::apply)
                .langValue(MegaLang.english("block.gtia." + id))
                .rotationState(RotationState.NON_Y_AXIS)
                // The renderer and the capture assume the pattern stands upright and unmirrored.
                .allowExtendedFacing(false)
                .allowFlip(false)
                .recipeType(type)
                .appearanceBlock(() -> MegaPattern.block(MegaShape.get(id).part(MegaShape.Part.STATION).block()))
                .pattern(definition -> MegaPattern.build(id, definition, casing0 -> {
                    var p = casing0;
                    for (var h : hatches) p = p.or(abilities(h.ability()).setMaxGlobalLimited(h.max())
                            .setPreviewCount(1));
                    return p;
                }))
                .modelProperty(com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                        com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.IDLE)
                .model(com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel(
                        casing, GTCEu.id("block/multiblock/fusion_reactor"))
                        .andThen(builder -> builder.addDynamicRenderer(
                                com.raishxn.gtia.mega.common.client.MegaRenderer.Render::new)))
                .tooltips(tips)
                .register();
    }
}
