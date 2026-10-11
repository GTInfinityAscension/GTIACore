package com.raishxn.gtia.rules;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registry of every GTIA world rule. Values are read through {@link WorldRules#get(GTIARule)}. */
public final class GTIARules {

    private static final Map<String, GTIARule> RULES = new LinkedHashMap<>();

    /** Multiplies the duration of every GTCEu-built recipe (GTCEu, GTNA, GTIACore). */
    public static final GTIARule RECIPE_DURATION = register(
            new GTIARule("duration_multiplier", "recipes", 0.75, 1.0, 0.1, 10.0));

    /** Multiplies the parallel limit of every GTNA steam multiblock (through GTNASteamBalance). */
    public static final GTIARule STEAM_PARALLELS = register(
            new GTIARule("parallel_multiplier", "steam", 1.5, 1.0, 0.25, 8.0));

    /** Multiplies the recipe duration of every GTNA steam multiblock, on top of the recipe duration rule. */
    public static final GTIARule STEAM_DURATION = register(
            new GTIARule("duration_multiplier_steam", "steam", 0.75, 1.0, 0.1, 10.0));

    private GTIARules() {}

    private static GTIARule register(GTIARule rule) {
        RULES.put(rule.id(), rule);
        return rule;
    }

    public static Map<String, GTIARule> all() {
        return Collections.unmodifiableMap(RULES);
    }
}
