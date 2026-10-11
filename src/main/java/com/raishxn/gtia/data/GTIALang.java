package com.raishxn.gtia.data;

import com.raishxn.gtia.GTIAMod;

public final class GTIALang {

    private GTIALang() {}

    public static void init() {
        GTIATooltips.registerLang();
        GTIAMod.REGISTRATE.addRawLang("gtia.crafting_station.tab",
                "%s (%s)");
        GTIAMod.REGISTRATE.addRawLang("gtia.crafting_station.empty",
                "Place chests or other containers on the sides or below the station to craft from them.");
        GTIAMod.REGISTRATE.addRawLang("gtia.crafting_station.side.north",
                "north");
        GTIAMod.REGISTRATE.addRawLang("gtia.crafting_station.side.east",
                "east");
        GTIAMod.REGISTRATE.addRawLang("gtia.crafting_station.side.south",
                "south");
        GTIAMod.REGISTRATE.addRawLang("gtia.crafting_station.side.west",
                "west");
        GTIAMod.REGISTRATE.addRawLang("gtia.crafting_station.side.down",
                "below");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.title",
                "GTIA Rules");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.title.world",
                "GTIA Rules of this world");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.button",
                "GTIA Rules: %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.button.tooltip",
                "Difficulty preset of this world: Journey, Normal or Custom. It can't be changed after the world is created.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.search",
                "Search rules...");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.locked",
                "A world's rules are fixed when it is created.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.values",
                "Journey %s · Normal %s · range %s to %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.pause",
                "GTIA Rules");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.journey",
                "Journey");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.normal",
                "Normal");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.custom",
                "Custom");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.journey.desc",
                "A gentler pace: faster recipes and stronger steam machines.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.normal.desc",
                "The intended experience.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.custom.desc",
                "Starts from the selected preset; edit every value below.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.group.recipes",
                "Recipes");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.group.steam",
                "Steam");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.duration_multiplier",
                "Recipe duration");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.duration_multiplier.desc",
                "Multiplies the time of every GregTech machine recipe. Lower is faster.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.parallel_multiplier",
                "Steam multiblock parallels");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.parallel_multiplier.desc",
                "Multiplies how many recipes the GTNA steam multiblocks run at once.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.duration_multiplier_steam",
                "Steam multiblock duration");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.duration_multiplier_steam.desc",
                "Multiplies the recipe time of the GTNA steam multiblocks, on top of the recipe duration rule.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.warning.missing",
                "This world had lost its GTIA rules, so it was set to %s. Back up your saves before moving them around.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.warning.file_mismatch",
                "gtia_rules.json was edited, but a world's rules can't change. This world stays %s.");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_double_ingot", "Double %s Ingot");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_triple_ingot", "Triple %s Ingot");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_quadruple_ingot", "Quadruple %s Ingot");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_quintuple_ingot", "Quintuple %s Ingot");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_triple_plate", "Triple %s Plate");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_quadruple_plate", "Quadruple %s Plate");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_quintuple_plate", "Quintuple %s Plate");
        GTIAMod.REGISTRATE.addRawLang("tagprefix.gtia_superdense_plate", "Superdense %s Plate");
        GTIAMod.REGISTRATE.addRawLang("gtia:phase_matrix_deposit", "Phase Matrix Deposit");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.catalytic_dehydrator.tooltip.purpose",
                "Converts ethanol into ethylene and water.");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.catalytic_dehydrator.tooltip.steam",
                "Steam input halves the processing time.");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.fermentation_vat.tooltip.purpose",
                "Makes biomass from plants and water, then ferments biomass into ethanol.");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.fermentation_vat.tooltip.yield",
                "Accepts several plant sources; higher tiers process the same recipes faster.");
        GTIAMod.REGISTRATE.addRawLang("gtia.catalytic_dehydrator", "Catalytic Dehydrator");
        GTIAMod.REGISTRATE.addRawLang("gtia.fermentation_vat", "Fermentation Vat");
        GTIAMod.REGISTRATE.addRawLang("gtia.bayer_digester",
                "Bayer Digester");
        GTIAMod.REGISTRATE.addRawLang("gtia.hall_heroult",
                "Hall-Heroult Cell");
        GTIAMod.REGISTRATE.addRawLang("gtia.contact_converter",
                "Contact Converter");
        GTIAMod.REGISTRATE.addRawLang("gtia.ammonia_converter",
                "Ammonia Converter");
        GTIAMod.REGISTRATE.addRawLang("gtia.solvay_tower",
                "Solvay Tower");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.still.tooltip.purpose",
                "Separates the wood condensate and its fractions; the circuit picks the cut.");
        GTIAMod.REGISTRATE.addRawLang("gtia.wood_retort",
                "Wood Retort");
        GTIAMod.REGISTRATE.addRawLang("gtia.lime_kiln",
                "Lime Kiln");
        GTIAMod.REGISTRATE.addRawLang("gtia.still",
                "Still");
        GTIAMod.REGISTRATE.addRawLang("gtia.diaphragm_cell",
                "Diaphragm Cell");
    }
}
