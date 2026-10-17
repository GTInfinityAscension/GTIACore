package com.raishxn.gtia.rules;

import net.minecraft.server.level.ServerPlayer;

/**
 * Marks the player with the world's mode as a vanilla tag, which FTB Quests reads as a stage (ftbxmodcompat
 * "vanilla" provider). Mode-only quests depend on the hidden quest that checks this stage.
 */
public final class ModeStage {

    public static final String JOURNEY = "gtia_journey";
    public static final String NORMAL = "gtia_normal";

    private ModeStage() {}

    public static void apply(ServerPlayer player) {
        // EN: Custom starts from Normal, so it gets Normal's quests and rewards (D-254).
        // PT: Custom parte do Normal, então recebe as quests e recompensas do Normal (D-254).
        // ZH: Custom 以 Normal 为基础，因此使用 Normal 的任务与奖励（D-254）。
        boolean journey = WorldRules.active().preset().base() == RulePreset.JOURNEY;
        player.removeTag(journey ? NORMAL : JOURNEY);
        player.addTag(journey ? JOURNEY : NORMAL);
    }
}
