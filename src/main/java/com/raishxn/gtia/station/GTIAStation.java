package com.raishxn.gtia.station;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.GTIAMod;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.MenuEntry;

/** Registration of the GTIA Crafting Station. */
public final class GTIAStation {

    public static final BlockEntry<CraftingStationBlock> BLOCK = GTIAMod.REGISTRATE
            .block("crafting_station", CraftingStationBlock::new)
            .initialProperties(() -> Blocks.CRAFTING_TABLE)
            .lang("Crafting Station")
            // Model, blockstate and recipe are hand-written resources (vanilla crafting table textures).
            .blockstate((ctx, prov) -> {})
            .loot((tables, block) -> tables.dropSelf(block))
            .item()
            .model((ctx, prov) -> {})
            .build()
            .register();

    public static final MenuEntry<CraftingStationMenu> MENU = GTIAMod.REGISTRATE
            .menu("crafting_station", (MenuType<CraftingStationMenu> type, int id,
                                       net.minecraft.world.entity.player.Inventory inventory,
                                       net.minecraft.network.FriendlyByteBuf buf) -> CraftingStationMenu
                                               .fromNetwork(type, id, inventory, buf),
                    () -> CraftingStationScreen::new)
            .register();

    private GTIAStation() {}

    public static void init() {}
}
