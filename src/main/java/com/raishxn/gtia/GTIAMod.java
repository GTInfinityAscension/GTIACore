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
import com.raishxn.gtia.data.GTIAItems;
import com.raishxn.gtia.data.GTIALang;
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
        com.raishxn.gtia.mega.GTIAMegaLang.init();
        com.raishxn.gtia.mega.common.MegaLang.init();
        com.raishxn.gtia.rules.GTIARuleEffects.applyStartupRules();
        GTIAItems.init();
        com.raishxn.gtia.organ.GTIAOrgans.init();
        MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.rules.GTIASurvivalRules::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.mega.system.SystemWipe::onServerAboutToStart);
        com.raishxn.gtia.station.GTIAStation.init();
        com.raishxn.gtia.data.rocket.GTIARocketBlocks.init();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        com.raishxn.gtia.miner.GTIAMiners.registerSerializers(modBus);
        modBus.addListener(this::registerMaterialRegistry);
        modBus.addListener(this::sendCraftingSources);
        modBus.addListener(this::registerMaterials);
        modBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient() &&
                com.raishxn.gtia.mega.GTIAMegastructures.gtna())
            modBus.addListener(com.raishxn.gtia.mega.common.client.MegaFxShaders::register);
        REGISTRATE.registerRegistrate();
        MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.server.ServerStartedEvent e) -> WorldRules.onServerStarted(e.getServer()));
        MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.data.RecipeAuditCommand::register);
        if (net.minecraftforge.fml.ModList.get().isLoaded("productivebees")) {
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.data.ProductiveBeesRecipeFilter::onServerStarted);
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.data.ProductiveBeesRecipeFilter::onDatapackSync);
        }
        if (net.minecraftforge.fml.ModList.get().isLoaded("ad_astra")) {
            MinecraftForge.EVENT_BUS
                    .addListener(com.raishxn.gtia.data.rocket.AdAstraRocketRecipeFilter::onServerStarted);
            MinecraftForge.EVENT_BUS
                    .addListener(com.raishxn.gtia.data.rocket.AdAstraRocketRecipeFilter::onDatapackSync);
        }
        GTIARulesNetwork.register();
        com.raishxn.gtia.mega.system.SkyStarNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.mega.system.SkyStarNetwork::onServerTick);
        if (net.minecraftforge.fml.ModList.get().isLoaded("gtna")) GTIASteamRules.install();
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.addListener(
                    (net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) -> WorldRules.leaveWorld());
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.client.GTIARulesClient::onRegisterCommands);
            MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.client.GTIARulesClient::onClientTick);
            if (com.raishxn.gtia.mega.GTIAMegastructures.available()) {
                // The dynamic render type has to exist before the machine models load.
                var ignored = com.raishxn.gtia.mega.common.client.MegaRenderer.Render.TYPE;
                MinecraftForge.EVENT_BUS.addListener(
                        com.raishxn.gtia.mega.common.client.MegaRenderer::onRenderLevelStage);
                MinecraftForge.EVENT_BUS.addListener(
                        com.raishxn.gtia.mega.common.client.MegaSkyStar::onRenderLevelStage);
                MinecraftForge.EVENT_BUS.addListener(
                        com.raishxn.gtia.mega.common.client.MegaDevCommand::register);
            }
            // Same test as GTIARulesSelfTest.ENABLED, read here: that class touches EMI, which may be absent.
            if (Boolean.getBoolean("gtia.rulesSelfTest") || java.nio.file.Files.exists(
                    java.nio.file.Path.of("gtia_rules_selftest"))) {
                MinecraftForge.EVENT_BUS.addListener(com.raishxn.gtia.client.GTIARulesSelfTest::onClientTick);
            }
        }
        MinecraftForge.EVENT_BUS
                .addListener((net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) -> {
                    if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                        GTIARulesNetwork.sendTo(player);
                        com.raishxn.gtia.rules.ModeStage.apply(player);
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
        com.raishxn.gtia.chem.ChemMaterials.register();
        com.raishxn.gtia.route.RouteMaterials.register();
        com.raishxn.gtia.mega.MegaMaterials.register();
    }

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTIARecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTIABeeMachines.init();
        com.raishxn.gtia.miner.GTIAMiners.init();
        com.raishxn.gtia.data.rocket.GTIARocketAssembler.init();
        com.raishxn.gtia.mega.GTIAMegastructures.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
