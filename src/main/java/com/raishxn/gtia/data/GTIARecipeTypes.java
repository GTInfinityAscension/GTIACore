package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import net.minecraft.core.registries.BuiltInRegistries;

import com.raishxn.gtia.GTIAMod;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public final class GTIARecipeTypes {

    /**
     * Industrial Apiary (GTIA-D-259): a caged Productive Bees bee is a catalyst item, combs come out. The pack's
     * KubeJS adds the species; no bee entity ever exists in the world.
     */
    public static final GTRecipeType INDUSTRIAL_APIARY = register("industrial_apiary")
            .setMaxIOSize(3, 6, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /** Seed Analyzer: reveals an Agronomica seed's stats; the recipe is built per seed (custom logic). */
    public static final GTRecipeType SEED_ANALYZER = register("seed_analyzer")
            .setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    /** Mutation Chamber (GTIA-D-271): two caged parents (kept) turn a caged vanilla bee into a new species. */
    public static final GTRecipeType MUTATION_CHAMBER = register("mutation_chamber")
            .setMaxIOSize(5, 1, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    /** Entangled Miner (GTIA-D-310): the recipe is built per operation from the Vein Card (custom logic). */
    public static final GTRecipeType ENTANGLED_MINER = register("entangled_miner")
            .setMaxIOSize(1, 4, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MINER);

    /** Rocket Assembler (GTIA-D-274): Ad Astra rocket parts and GT components become a rocket. */
    public static final GTRecipeType ROCKET_ASSEMBLY = register("rocket_assembly", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9, 1, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    private GTIARecipeTypes() {}

    public static void init() {}

    private static GTRecipeType register(String name) {
        return register(name, GTRecipeTypes.ELECTRIC);
    }

    private static GTRecipeType register(String name, String group) {
        GTRecipeType type = new GTRecipeType(GTIAMod.id(name), group);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, type.registryName, type);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, type.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(type.registryName, type);
        return type;
    }
}
