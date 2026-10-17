package com.raishxn.gtia.rules;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;

/** The extra regeneration rules (GTO: Extra Regeneration), applied every 2.5 seconds. */
public final class GTIASurvivalRules {

    public static final int PERIOD = 50;

    private GTIASurvivalRules() {}

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % PERIOD == 0) regenerate(player);
    }

    /** Heals the player when the food level reaches the rule's threshold; returns the health healed. */
    public static float regenerate(ServerPlayer player) {
        int threshold = (int) WorldRules.get(GTIARules.EXTRA_REGEN_FOOD);
        float amount = (float) WorldRules.get(GTIARules.EXTRA_REGEN_STRENGTH);
        if (threshold > 20 || amount <= 0 || player.isDeadOrDying()) return 0;
        if (player.getFoodData().getFoodLevel() < threshold || player.getHealth() >= player.getMaxHealth()) return 0;
        float before = player.getHealth();
        player.heal(amount);
        return player.getHealth() - before;
    }
}
