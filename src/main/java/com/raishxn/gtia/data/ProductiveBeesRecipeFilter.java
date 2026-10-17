package com.raishxn.gtia.data;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import com.raishxn.gtia.GTIAMod;

/**
 * EN: Drops every Productive Bees recipe (GTIA-D-259) so bees only enter through GTIA's HV line. PB adds recipes after
 * the datapack load, so the filter runs once the server is up and again after each /reload.
 * PT: Descarta todas as receitas do Productive Bees (GTIA-D-259); abelhas só entram pela linha HV do GTIA. O PB
 * acrescenta receitas depois do carregamento, então o filtro roda com o servidor pronto e após cada /reload.
 * ZH: 移除 Productive Bees 的全部配方（GTIA-D-259），蜜蜂只能通过 GTIA 的 HV 线路获得。PB 在数据包加载后才添加配方，
 * 因此在服务器启动后及每次 /reload 后执行过滤。
 */
public final class ProductiveBeesRecipeFilter {

    private ProductiveBeesRecipeFilter() {}

    public static void onServerStarted(ServerStartedEvent event) {
        filter(event.getServer());
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) filter(event.getPlayerList().getServer());
    }

    public static int filter(MinecraftServer server) {
        var manager = server.getRecipeManager();
        var all = manager.getRecipes();
        var kept = all.stream().filter(r -> !"productivebees".equals(r.getId().getNamespace())).toList();
        int dropped = all.size() - kept.size();
        if (dropped > 0) {
            manager.replaceRecipes(kept);
            GTIAMod.LOGGER.info("Dropped {} Productive Bees recipes (GTIA-D-259)", dropped);
        }
        return dropped;
    }
}
