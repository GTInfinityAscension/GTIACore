package com.raishxn.gtia.rules;

/**
 * One named world rule with a value per preset. Most rules only change recipes and numbers, never which items or blocks
 * exist, so a world's rules can be applied when the world loads without restarting the game (GTIA-D-254). Rules marked
 * {@link #restart()} are read by GTCEu while it registers its machines and items; they never add or remove anything,
 * but a new value only takes effect after the game restarts (GTIA-D-256).
 * <p>
 * Every value is stored as a double: toggles are 0 (off) or 1 (on).
 */
public record GTIARule(String id, String group, Kind kind, double journey, double normal, double min, double max,
                       boolean restart) {

    public enum Kind {
        /** Shown as ×1.5. */
        MULTIPLIER,
        /** A whole number, shown as is. */
        NUMBER,
        /** On or off. */
        TOGGLE
    }

    public static GTIARule multiplier(String id, String group, double journey, double normal, double min,
                                      double max) {
        return new GTIARule(id, group, Kind.MULTIPLIER, journey, normal, min, max, false);
    }

    public static GTIARule number(String id, String group, int journey, int normal, int min, int max) {
        return new GTIARule(id, group, Kind.NUMBER, journey, normal, min, max, false);
    }

    public static GTIARule toggle(String id, String group, boolean journey, boolean normal) {
        return new GTIARule(id, group, Kind.TOGGLE, journey ? 1 : 0, normal ? 1 : 0, 0, 1, false);
    }

    /** The same rule, read when the game starts. */
    public GTIARule needsRestart() {
        return new GTIARule(id, group, kind, journey, normal, min, max, true);
    }

    public double valueFor(RulePreset preset) {
        return preset.base() == RulePreset.JOURNEY ? journey : normal;
    }

    public double clamp(double value) {
        double clamped = Math.max(min, Math.min(max, value));
        return kind == Kind.MULTIPLIER ? clamped : Math.round(clamped);
    }

    public String translationKey() {
        return "gtia.rule." + group + "." + id;
    }
}
