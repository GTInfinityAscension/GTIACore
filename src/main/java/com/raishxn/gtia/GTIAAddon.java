package com.raishxn.gtia;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import com.raishxn.gtia.data.GTIABeeRecipes;
import com.raishxn.gtia.data.GTIAExtraFormRecipes;
import com.raishxn.gtia.data.GTIAExtraForms;
import com.raishxn.gtia.data.GTIAPhaseOres;
import com.raishxn.gtia.data.GTIARecipes;

import java.util.function.Consumer;

@GTAddon
public final class GTIAAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return GTIAMod.REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        GTIAMod.LOGGER.info("GTIACore initialized");
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        GTIAMod.LOGGER.info("Registering GTIA recipes");
        GTIARecipes.addRecipes(provider);
        GTIARecipes.addChemistrySlice01(provider);
        GTIARecipes.addChemistrySlice02(provider);
        GTIAExtraFormRecipes.addRecipes(provider);
        GTIABeeRecipes.addRecipes(provider);
    }

    @Override
    public void registerOreVeins() {
        GTIAPhaseOres.register();
    }

    @Override
    public void registerTagPrefixes() {
        GTIAExtraForms.register();
    }

    @Override
    public String addonModId() {
        return GTIAMod.MOD_ID;
    }
}
