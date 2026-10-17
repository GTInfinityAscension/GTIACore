package com.raishxn.gtia.chem;

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

/** Recipe types of the MV/HV chemistry plants (GTIA-D-281). */
public final class ChemRecipeTypes {

    /** Ostwald process: ammonia burnt over a catalyst gauze, the nitrous gases absorbed into nitric acid. */
    public static final GTRecipeType OSTWALD_CONVERTER = register("ostwald_converter", 2, 0, 2, 3);
    /** Claus process: hydrogen sulfide half burnt, then reacted over alumina into sulfur. */
    public static final GTRecipeType CLAUS_UNIT = register("claus_unit", 1, 3, 2, 2);
    /** Kroll process: titanium ore chlorinated, then the chloride reduced by magnesium under argon. */
    public static final GTRecipeType KROLL_REACTOR = register("kroll_reactor", 3, 2, 2, 3);
    /** Siemens process: silicon to trichlorosilane and back to ultrapure silicon on hot rods. */
    public static final GTRecipeType SIEMENS_REACTOR = register("siemens_reactor", 1, 1, 2, 2);

    private ChemRecipeTypes() {}

    public static void init() {}

    private static GTRecipeType register(String name, int itemIn, int itemOut, int fluidIn, int fluidOut) {
        GTRecipeType type = new GTRecipeType(GTIAMod.id(name), GTRecipeTypes.MULTIBLOCK);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, type.registryName, type);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, type.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(type.registryName, type);
        return type.setMaxIOSize(itemIn, itemOut, fluidIn, fluidOut)
                .setEUIO(IO.IN)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.CHEMICAL);
    }
}
