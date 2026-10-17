package com.raishxn.gtia;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.raishxn.gtia.data.GTIAAlloys;
import com.raishxn.gtia.data.GTIABeeMachines;
import com.raishxn.gtia.data.GTIAGreenhouse;
import com.raishxn.gtia.data.GTIAItems;
import com.raishxn.gtia.data.GTIALang;
import com.raishxn.gtia.data.GTIAMachines;
import com.raishxn.gtia.data.GTIARecipeTypes;
import com.raishxn.gtia.rules.GTIARulesNetwork;
import com.raishxn.gtia.rules.GTIASteamRules;
import com.raishxn.gtia.rules.WorldRules;
import com.raishxn.gtia.tools.RecipeExport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTIAMod.MOD_ID)
@SuppressWarnings("removal")
public final class GTIAMod {

    public static final String MOD_ID = "gtia";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(MOD_ID);

    public GTIAMod() {
        GTIALang.init();
        com.raishxn.gtia.rules.GTIARuleEffects.applyStartupRules();
        GTIAItems.init();
        com.raishxn.gtia.station.GTIAStation.init();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::registerMaterialRegistry);
        modBus.addListener(this::sendCraftingSources);
        modBus.addListener(this::registerMaterials);
        modBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        REGISTRATE.registerRegistrate();
        MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.server.ServerStartedEvent e) -> WorldRules.onServerStarted(e.getServer()));
        MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.data.RecipeAuditCommand::register);
        if (net.minecraftforge.fml.ModList.get().isLoaded("productivebees")) {
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.data.ProductiveBeesRecipeFilter::onServerStarted);
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.data.ProductiveBeesRecipeFilter::onDatapackSync);
        }
        GTIARulesNetwork.register();
        if (net.minecraftforge.fml.ModList.get().isLoaded("gtna")) GTIASteamRules.install();
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.addListener(
                    (net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) -> WorldRules.leaveWorld());
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.client.GTIARulesClient::onRegisterCommands);
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.client.GTIARulesClient::onClientTick);
            if (com.raishxn.gtia.client.GTIARulesSelfTest.ENABLED) {
                MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.client.GTIARulesSelfTest::onClientTick);
            }
        }
        MinecraftForge.EVENT_BUS
                .addListener((net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) -> {
                    if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GTIARulesNetwork.sendTo(player);
                    }
                    String warning = WorldRules.takeWarning();
                    if (warning != null) {
                        e.getEntity().sendSystemMessage(net.minecraft.network.chat.Component.translatable(warning,
                                WorldRules.active().preset().id()).withStyle(net.minecraft.ChatFormatting.GOLD));
                    }
                });
        if (System.getProperty(RecipeExport.PROPERTY) != null) {
            MinecraftForge.EVENT_BUS.addListener(RecipeExport::onServerStarted);
        }
    }

    /**
     * Crafting Chain (optional) crafts from the containers around an open Crafting Station, the same five sides the
     * station shows. Sent over IMC, so GTIACore needs no compile dependency on it.
     */
    private void sendCraftingSources(net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent event) {
        if (!net.minecraftforge.fml.ModList.get().isLoaded("craftingchain")) return;
        net.minecraftforge.fml.InterModComms.sendTo("craftingchain", "sources",
                () -> (java.util.function.Function<net.minecraft.server.level.ServerPlayer, java.util.List<net.minecraftforge.items.IItemHandler>>) player -> player.containerMenu instanceof com.raishxn.gtia.station.CraftingStationMenu station ?
                        station.sides().stream()
                                .map(com.raishxn.gtia.station.CraftingStationMenu.Side::handler)
                                .toList() :
                        java.util.List.of());
    }

    private void registerMaterialRegistry(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(MOD_ID);
    }

    private void registerMaterials(MaterialEvent event) {
        GTIAAlloys.register();
    }

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTIARecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTIAMachines.init();
        GTIABeeMachines.init();
        GTIAGreenhouse.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
