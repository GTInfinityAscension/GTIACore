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
import com.raishxn.gtia.data.GTIADraconiumMaterials;
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
        GTIAItems.init();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::registerMaterialRegistry);
        modBus.addListener(this::registerMaterials);
        modBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        REGISTRATE.registerRegistrate();
        MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.server.ServerStartedEvent e) -> WorldRules.onServerStarted(e.getServer()));
        GTIARulesNetwork.register();
        if (net.minecraftforge.fml.ModList.get().isLoaded("gtna")) GTIASteamRules.install();
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.addListener(
                    (net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) -> WorldRules.leaveWorld());
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

    private void registerMaterialRegistry(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(MOD_ID);
    }

    private void registerMaterials(MaterialEvent event) {
        GTIAAlloys.register();
        GTIADraconiumMaterials.register();
    }

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTIARecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTIAMachines.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
