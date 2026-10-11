package com.raishxn.gtia.rules;

import com.raishxn.gtna.api.steam.GTNASteamBalance;

import java.util.List;

/**
 * Feeds the world's steam rules into GTNA's steam balance, so GTNA steam multiblocks and their tooltips follow them.
 */
public final class GTIASteamRules {

    private GTIASteamRules() {}

    public static void install() {
        GTNASteamBalance.setProvider(new GTNASteamBalance.Provider() {

            @Override
            public GTNASteamBalance.Variant active() {
                return variant(WorldRules.active());
            }

            @Override
            public List<GTNASteamBalance.Variant> tooltipVariants() {
                if (WorldRules.inWorld()) return List.of(active());
                return List.of(variant(WorldRules.Snapshot.of(RulePreset.NORMAL)),
                        variant(WorldRules.Snapshot.of(RulePreset.JOURNEY)));
            }
        });
    }

    private static GTNASteamBalance.Variant variant(WorldRules.Snapshot rules) {
        return new GTNASteamBalance.Variant(rules.preset().displayName(), rules.get(GTIARules.STEAM_PARALLELS),
                rules.get(GTIARules.STEAM_DURATION));
    }
}
