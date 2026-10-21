package com.raishxn.gtia.data;

import net.minecraft.world.item.Item;

import com.raishxn.gtia.GTIAMod;
import com.tterrag.registrate.util.entry.ItemEntry;

public final class GTIAItems {

    /**
     * Wears out slowly in the chlor-alkali Electrolyzer recipe (a chanced input). Placeholder texture until the author
     * draws one.
     */
    public static final ItemEntry<Item> ASBESTOS_DIAPHRAGM = GTIAMod.REGISTRATE.item("asbestos_diaphragm", Item::new)
            .lang("Asbestos Diaphragm")
            .model((context, provider) -> provider.generated(context,
                    GTIAMod.id("item/asbestos_diaphragm")))
            .register();

    /**
     * Consumed by the Hall-Heroult recipe in the Electric Blast Furnace: real anodes burn away into CO2. Placeholder
     * texture.
     */
    public static final ItemEntry<Item> CARBON_ANODE = GTIAMod.REGISTRATE.item("carbon_anode", Item::new)
            .lang("Carbon Anode")
            .model((context, provider) -> provider.generated(context,
                    GTIAMod.id("item/carbon_anode")))
            .register();

    /** Records a bedrock vein for the Entangled Miner (GTIA-D-310). Placeholder texture until the author draws one. */
    public static final ItemEntry<com.raishxn.gtia.miner.VeinCardItem> VEIN_CARD = GTIAMod.REGISTRATE
            .item("vein_card", com.raishxn.gtia.miner.VeinCardItem::new)
            .lang("Vein Card")
            .model((context, provider) -> provider.generated(context, GTIAMod.id("item/vein_card")))
            .register();

    private GTIAItems() {}

    public static void init() {}
}
