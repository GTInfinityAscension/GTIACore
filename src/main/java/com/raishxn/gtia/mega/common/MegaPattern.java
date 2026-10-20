package com.raishxn.gtia.mega.common;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtna.common.machine.multiblock.originals.OriginalLayout;

import java.util.function.UnaryOperator;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

/** The pattern of a megastructure from its {@link MegaShape}: all five phases, later ones accepting air. */
public final class MegaPattern {

    private MegaPattern() {}

    public static Block block(String id) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
    }

    /**
     * @param hatches adds the hatch abilities to the casing predicate of the {@link MegaShape.Part#HATCH} letter
     */
    public static BlockPattern build(String name, MultiblockMachineDefinition definition,
                                     UnaryOperator<TraceabilityPredicate> hatches) {
        var shape = MegaShape.get(name);
        var pattern = FactoryBlockPattern.start();
        for (int i = 0; i < shape.nz; i++) pattern.aisle(shape.aisle(i));
        for (var part : shape.parts.values()) {
            Block block = block(part.block());
            char letter = part.letter();
            // Every predicate is registered with what it accepts, so GTNA's StructureWatch tests single positions.
            if (letter == MegaShape.Part.HATCH)
                pattern.where(letter, OriginalLayout.register(hatches.apply(blocks(block)),
                        new OriginalLayout.Matcher(letter, OriginalLayout.Kind.HATCH, block, null)));
            // Phase 1 is required, except its moving pieces: they are air while the controller holds them.
            else if (part.phase() == 1 && !part.moving())
                pattern.where(letter, OriginalLayout.register(blocks(block),
                        new OriginalLayout.Matcher(letter, OriginalLayout.Kind.BLOCK, block, null)));
            else pattern.where(letter, OriginalLayout.register(blocks(block).or(air()),
                    new OriginalLayout.Matcher(letter, OriginalLayout.Kind.BLOCK_OR_AIR, block,
                            part.moving() ? part.track() : "phase" + part.phase())));
        }
        return pattern.where('~', OriginalLayout.register(controller(blocks(definition.getBlock())),
                new OriginalLayout.Matcher('~', OriginalLayout.Kind.CONTROLLER, definition.getBlock(), null)))
                .where(' ', any()).build();
    }
}
