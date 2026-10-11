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
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.command.no_world",
                "Join a world to see its GTIA Rules.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.journey",
                "Journey");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.normal",
                "Normal");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.custom",
                "Custom");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.preset.journey.desc",
                "A gentler pace: faster and cheaper recipes, stronger steam, no maintenance and simpler vanilla recipes.");
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
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.value.on",
                "On");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.value.off",
                "Off");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.values.toggle",
                "Journey %s · Normal %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.screen.restart",
                "[restart]");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.warning.restart",
                "This world (%s) uses rules that GTCEu reads when the game starts. They were saved for the next start: restart the game to apply them.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.group.power",
                "Power");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.group.machines",
                "Machines");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.group.tools",
                "Tools");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.group.resources",
                "Resources");
        GTIAMod.REGISTRATE.addRawLang("gtia.rules.group.survival",
                "Survival");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.eu_multiplier",
                "Recipe EU/t");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.eu_multiplier.desc",
                "Multiplies the energy per tick of every GregTech machine recipe. A recipe never moves to another voltage tier.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.casings_per_craft",
                "Casings per craft");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.casings_per_craft.desc",
                "How many machine casings one crafting recipe makes.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.manual_compression",
                "Manual block compression");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.manual_compression.desc",
                "Ingots can be packed into blocks (and blocks back into ingots) in the crafting table.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_rods",
                "Harder rods");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_rods.desc",
                "The lathe makes 1 rod and 2 small piles of dust instead of 2 rods.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_bricks",
                "Harder bricks");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_bricks.desc",
                "Harder crafting recipes for bricks, firebricks, nether bricks and coke oven bricks.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.nerf_wood",
                "Less wood per log");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.nerf_wood.desc",
                "A log makes 2 planks and 2 planks make 2 sticks.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_wood",
                "Hard wood recipes");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_wood.desc",
                "Wooden items (doors, chests, signs…) need GregTech parts.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_iron",
                "Hard iron recipes");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_iron.desc",
                "Iron items (buckets, hoppers, cauldrons…) need plates.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_redstone",
                "Hard redstone recipes");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_redstone.desc",
                "Redstone items need GregTech parts.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_tools_armor",
                "Hard vanilla tools and armor");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_tools_armor.desc",
                "Vanilla tools and armor need plates and GregTech tools.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_misc",
                "Hard misc recipes");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_misc.desc",
                "Other vanilla items get GregTech recipes.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_glass",
                "Hard glass");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_glass.desc",
                "Glass and glass items get harder recipes.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.nerf_paper",
                "Less paper");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.nerf_paper.desc",
                "Paper gets a harder crafting recipe.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_advanced_iron",
                "Hard advanced iron recipes");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_advanced_iron.desc",
                "Iron doors, trapdoors, anvils and similar need iron plates and rods.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_dyes",
                "Hard dyes");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_dyes.desc",
                "Coloring blocks such as concrete or glass is harder.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_charcoal",
                "No furnace charcoal");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_charcoal.desc",
                "Charcoal can't be smelted from logs in a furnace.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.flint_and_steel_steel",
                "Flint and steel needs steel");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.flint_and_steel_steel.desc",
                "The flint and steel needs a steel part.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.remove_vanilla_blocks",
                "No vanilla block compression");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.remove_vanilla_blocks.desc",
                "Removes the crafting table recipes of vanilla blocks; GregTech machines make them.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.remove_vanilla_tnt",
                "No vanilla TNT");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.remove_vanilla_tnt.desc",
                "TNT is made by GregTech machines only.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_circuits",
                "Harder circuits");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.harder_circuits.desc",
                "The first circuit of each set makes 1 instead of 2, and SoC recipes make 2 instead of 4.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_controllers",
                "Harder multiblock controllers");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.recipes.hard_controllers.desc",
                "Multiblock controller recipes cost more.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.boiler_output",
                "Small boiler steam");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.boiler_output.desc",
                "Multiplies the steam made by the small GregTech boilers.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.large_boiler_steam",
                "Large boiler steam per water");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.steam.large_boiler_steam.desc",
                "Multiplies the steam the large boilers make from each unit of water.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.power.fuel_efficiency",
                "Fuel efficiency");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.power.fuel_efficiency.desc",
                "Multiplies how long each fuel lasts in generators and boilers. Higher is better.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.maintenance",
                "Maintenance");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.maintenance.desc",
                "Multiblocks need a maintenance hatch and develop problems over time.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.cleanroom",
                "Cleanroom");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.cleanroom.desc",
                "Some recipes need a cleanroom.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.clean_multiblocks",
                "Multiblocks ignore the cleanroom");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.clean_multiblocks.desc",
                "Multiblocks run cleanroom recipes without a cleanroom.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.explosions_damage_terrain",
                "Explosions break blocks");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.explosions_damage_terrain.desc",
                "Machine explosions destroy the blocks around them.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.weather_explosions",
                "Rain and terrain explosions");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.weather_explosions.desc",
                "Machines left in the rain or in bad spots can explode.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.harmless_active_transformers",
                "Harmless active transformers");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.harmless_active_transformers.desc",
                "Active transformers don't hurt nearby players.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.ordered_assembly_items",
                "Assembly line item order");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.ordered_assembly_items.desc",
                "The assembly line needs its items in recipe order.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.ordered_assembly_fluids",
                "Assembly line fluid order");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.ordered_assembly_fluids.desc",
                "The assembly line needs its fluids in recipe order.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.miner_speed",
                "Miner speed");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.machines.miner_speed.desc",
                "Multiplies the speed of the single-block GregTech miners.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.electric_tool_energy",
                "Electric tool energy use");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.electric_tool_energy.desc",
                "Multiplies the energy electric tools use.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.electric_tool_damage_chance",
                "Electric tool wear (%)");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.electric_tool_damage_chance.desc",
                "Chance in percent that an electric tool loses durability on use.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.prospector_energy",
                "Prospector energy use");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.prospector_energy.desc",
                "Multiplies the energy the prospector uses.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.enchanted_tools",
                "Enchanted GregTech tools");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.tools.enchanted_tools.desc",
                "GregTech tools come with their material's enchantments.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.resources.ore_vein_grid",
                "Ore vein spacing (chunks)");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.resources.ore_vein_grid.desc",
                "One vein per grid cell of this size; smaller means more veins. Only new chunks change.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.resources.void_miner_duration",
                "Void miner duration");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.resources.void_miner_duration.desc",
                "Multiplies the time of each GTNA Electric Void Miner operation.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.survival.ultimine_limit",
                "Ultimine limit");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.survival.ultimine_limit.desc",
                "Most blocks FTB Ultimine breaks at once.");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.survival.keep_inventory",
                "Keep inventory");
        GTIAMod.REGISTRATE.addRawLang("gtia.rule.survival.keep_inventory.desc",
                "Players keep their items on death. Set when the world is created; /gamerule can change it.");
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
