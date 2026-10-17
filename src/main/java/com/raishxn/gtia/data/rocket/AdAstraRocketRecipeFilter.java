package com.raishxn.gtia.data.rocket;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import com.raishxn.gtia.GTIAMod;

import java.util.Set;

/**
 * EN: Drops Ad Astra's own recipes for the tier 1 rocket and its parts (GTIA-D-272): they are made through GT. Runs
 * when the server is up and after each /reload, like the Productive Bees filter.
 * PT: Descarta as receitas do próprio Ad Astra para o foguete tier 1 e suas peças (GTIA-D-272): elas passam pelo GT.
 * Roda com o servidor pronto e após cada /reload, como o filtro do Productive Bees.
 * ZH: 移除 Ad Astra 自带的一级火箭及其部件配方（GTIA-D-272），改由 GT 制作。在服务器启动后及每次 /reload 后执行，
 * 与 Productive Bees 过滤器相同。
 */
public final class AdAstraRocketRecipeFilter {

    static final Set<ResourceLocation> DROPPED = Set.of(
            new ResourceLocation("ad_astra", "nasa_workbench/tier_1_rocket_from_nasa_workbench"),
            new ResourceLocation("ad_astra", "rocket_nose_cone"), new ResourceLocation("ad_astra", "rocket_fin"),
            new ResourceLocation("ad_astra", "steel_tank"), new ResourceLocation("ad_astra", "steel_engine"));

    private AdAstraRocketRecipeFilter() {}

    public static void onServerStarted(ServerStartedEvent event) {
        filter(event.getServer());
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) filter(event.getPlayerList().getServer());
    }

    public static int filter(MinecraftServer server) {
        var manager = server.getRecipeManager();
        var all = manager.getRecipes();
        var kept = all.stream().filter(r -> !DROPPED.contains(r.getId())).toList();
        int dropped = all.size() - kept.size();
        if (dropped > 0) {
            manager.replaceRecipes(kept);
            GTIAMod.LOGGER.info("Dropped {} Ad Astra rocket recipes (GTIA-D-272)", dropped);
        }
        return dropped;
    }
}
