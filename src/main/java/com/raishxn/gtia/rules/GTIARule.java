package com.raishxn.gtia.rules;

/**
 * One named world rule with a value per preset. Rules only change recipes and numbers, never which items or blocks
 * exist, so a world's rules can be applied when the world loads without restarting the game (GTIA-D-254).
 */
public record GTIARule(String id, String group, double journey, double normal, double min, double max) {

    public double valueFor(RulePreset preset) {
        return preset.base() == RulePreset.JOURNEY ? journey : normal;
    }

    public double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }

    public String translationKey() {
        return "gtia.rule." + group + "." + id;
    }
}
