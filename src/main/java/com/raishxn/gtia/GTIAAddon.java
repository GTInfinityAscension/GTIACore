package com.raishxn.gtia;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtia.data.GTIAAgroRecipes;
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
        com.raishxn.gtia.chem.ChemLineRecipes.addRecipes(provider);
        GTIAExtraFormRecipes.addRecipes(provider);
        GTIABeeRecipes.addRecipes(provider);
        GTIAAgroRecipes.addRecipes(provider);
        com.raishxn.gtia.data.rocket.GTIARocketRecipes.addRecipes(provider);
        com.raishxn.gtia.data.GTIANexusRecipes.addRecipes(provider);
        com.raishxn.gtia.organ.OrganRecipes.addRecipes(provider);
        com.raishxn.gtia.chem.ChemRecipes.addRecipes(provider);
        com.raishxn.gtia.route.RouteRecipes.addRecipes(provider);
        com.raishxn.gtia.route.ProgressionGates.addRecipes(provider);
        com.raishxn.gtia.route.AE2Gates.addRecipes(provider);
        com.raishxn.gtia.miner.GTIAMiners.addRecipes(provider);
        com.raishxn.gtia.mega.MegaRecipes.addRecipes(provider);
    }

    @Override
    public void removeRecipes(Consumer<ResourceLocation> consumer) {
        com.raishxn.gtia.chem.ChemRouteRemovals.removeRecipes(consumer);
        com.raishxn.gtia.route.RouteRemovals.removeRecipes(consumer);
        com.raishxn.gtia.route.ProgressionGates.removeRecipes(consumer);
        // EN: Normal worlds move steam through pipes; only Journey crafts the GTNA wireless steam hatches (GTIA-D-340).
        // PT: No Normal o vapor vai por canos; só o Journey fabrica os hatches de vapor sem fio do GTNA (GTIA-D-340).
        // ZH: 普通模式下蒸汽走管道；只有旅程模式能合成 GTNA 无线蒸汽仓（GTIA-D-340）。
        if (com.raishxn.gtia.rules.WorldRules.get(com.raishxn.gtia.rules.GTIARules.WIRELESS_STEAM) == 0) {
            for (String id : new String[] { "gtna:wireless_steam_input_hatch", "gtna:wireless_steam_output_hatch",
                    "gtceu:hydraulic_manufacturing/wireless_steam_input_hatch_steel",
                    "gtceu:hydraulic_manufacturing/wireless_steam_output_hatch_steel" })
                consumer.accept(new ResourceLocation(id));
        }
    }

    @Override
    public void registerOreVeins() {
        GTIAPhaseOres.register();
    }

    @Override
    public void registerBedrockOreVeins() {
        com.raishxn.gtia.miner.BedrockVeins.register();
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
