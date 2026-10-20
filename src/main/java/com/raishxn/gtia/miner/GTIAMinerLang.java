package com.raishxn.gtia.miner;

import com.raishxn.gtia.GTIAMod;

/** English text of the veins with purity and the Entangled Miner (GTIA-D-310); pt_br and zh_cn are in lang. */
public final class GTIAMinerLang {

    private GTIAMinerLang() {}

    public static void init() {
        GTIAMod.REGISTRATE.addRawLang("gtia.entangled_miner",
                "Entangled Miner");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein_card.none",
                "No bedrock vein under this chunk");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein_card.recorded",
                "Recorded: %s (%s)");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein_card.blank",
                "Right-click inside a bedrock vein to record it");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein_card.vein",
                "%s, %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein_card.where",
                "%s, around X %s Z %s");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.purity.impure",
                "impure ×1");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.purity.normal",
                "normal ×2");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.purity.pure",
                "pure ×4");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.entangled_miner.tooltip.0",
                "Mines the bedrock vein on the Vein Card in its input slot, forever. Each operation takes 1, 2 or 4 raw ores by the vein's purity.");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.entangled_miner.tooltip.1",
                "At most %s miners per vein; the next ones wait for a free slot.");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.entangled_miner.tooltip.same_dimension",
                "Only veins in its own dimension (any dimension from EV).");
        GTIAMod.REGISTRATE.addRawLang("gtia.machine.entangled_miner.tooltip.any_dimension",
                "Mines veins in any dimension.");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.iron",
                "Iron Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.copper",
                "Copper Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.tin",
                "Tin Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.coal",
                "Coal Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.lead_silver",
                "Lead and Silver Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.redstone",
                "Redstone Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.nickel",
                "Nickel Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.bauxite",
                "Bauxite Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.gold",
                "Gold Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.salts",
                "Salt Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.nether_quartz",
                "Nether Quartz Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.sulfur",
                "Sulfur Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.pitchblende",
                "Pitchblende Vein");
        GTIAMod.REGISTRATE.addRawLang("gtia.vein.scheelite",
                "Scheelite Vein");
    }
}
