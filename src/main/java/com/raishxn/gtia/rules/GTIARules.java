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

    private GTIARules() {}

    private static GTIARule register(GTIARule rule) {
        RULES.put(rule.id(), rule);
        return rule;
    }

    public static Map<String, GTIARule> all() {
        return Collections.unmodifiableMap(RULES);
    }
}
