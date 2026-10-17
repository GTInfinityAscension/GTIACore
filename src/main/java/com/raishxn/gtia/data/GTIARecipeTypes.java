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

    /** Greenhouse (GTIA-D-271): an Agronomica seed of the species is a catalyst; fertiliser and water grow it. */
    public static final GTRecipeType GREENHOUSE = register("greenhouse", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 4, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /** Mutation Chamber (GTIA-D-271): two caged parents (kept) turn a caged vanilla bee into a new species. */
    public static final GTRecipeType MUTATION_CHAMBER = register("mutation_chamber")
            .setMaxIOSize(5, 1, 0, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    /** Rocket Assembler (GTIA-D-274): Ad Astra rocket parts and GT components become a rocket. */
    public static final GTRecipeType ROCKET_ASSEMBLY = register("rocket_assembly", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9, 1, 1, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    public static final GTRecipeType CATALYTIC_DEHYDRATOR = register("catalytic_dehydrator")
            .setMaxIOSize(0, 0, 2, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    public static final GTRecipeType FERMENTATION_VAT = register("fermentation_vat")
            .setMaxIOSize(1, 0, 1, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /** Destructive distillation of wood: logs in, charcoal and a condensate out (a Coke Oven-style multiblock). */
    public static final GTRecipeType WOOD_RETORT = register("wood_retort", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 0, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);

    /** Burning limestone into quicklime: calcite and a fuel in, quicklime out. */
    public static final GTRecipeType LIME_KILN = register("lime_kiln", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 1, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);

    /** A small still: separates the wood condensate and its fractions (circuit chooses the cut). */
    public static final GTRecipeType STILL = register("still")
            .setMaxIOSize(1, 0, 1, 3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /** Chlor-alkali electrolysis in a diaphragm cell. */
    public static final GTRecipeType DIAPHRAGM_CELL = register("diaphragm_cell", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ELECTROLYZER);

    /**
     * Bayer process: bauxite digested in caustic soda, then aluminium hydroxide precipitated (circuit picks the step).
     */
    public static final GTRecipeType BAYER_DIGESTER = register("bayer_digester", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 2, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /** Hall-Heroult: alumina dissolved in molten cryolite, reduced on a carbon anode. */
    public static final GTRecipeType HALL_HEROULT = register("hall_heroult", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 1, 0, 1)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ELECTROLYZER);

    /** Contact process: sulfur burnt and oxidised over vanadium pentoxide, absorbed into sulfuric acid. */
    public static final GTRecipeType CONTACT_CONVERTER = register("contact_converter", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 0, 2, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /** Haber-Bosch: nitrogen and hydrogen over an iron catalyst. */
    public static final GTRecipeType AMMONIA_CONVERTER = register("ammonia_converter", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 2, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    /** Solvay process: ammoniated brine carbonated into sodium bicarbonate. */
    public static final GTRecipeType SOLVAY_TOWER = register("solvay_tower", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 2, 3, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

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
