package com.raishxn.gtia.rules;

import java.util.Locale;

/** World difficulty presets (GTIA-D-205, D-254). Custom starts from Normal and lets each rule be edited. */
public enum RulePreset {

    JOURNEY,
    NORMAL,
    CUSTOM;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The preset whose values a Custom world starts from. */
    public RulePreset base() {
        return this == CUSTOM ? NORMAL : this;
    }

    /** Name shown in tooltips (a proper noun, the same in every language). */
    public String displayName() {
        return switch (this) {
            case JOURNEY -> "Journey";
            case NORMAL -> "Normal";
            case CUSTOM -> "Custom";
        };
    }

    public static RulePreset byId(String id) {
        for (RulePreset preset : values()) {
            if (preset.id().equals(id)) return preset;
        }
        return NORMAL;
    }
}
