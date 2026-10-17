package com.raishxn.gtia.organ;

/**
 * Body slots of the organ system, ported from GTO 0.6 (GTIA-D-279). Wings take eight slots and are used one after the
 * other; every other type takes one organ, whose tier counts for the organ set.
 */
public enum OrganType {

    WING("wing", 8),
    EYE("eye", 1),
    SPINE("spine", 1),
    LUNG("lung", 1),
    LIVER("liver", 1),
    HEART("heart", 1),
    LEFT_ARM("left_arm", 1),
    RIGHT_ARM("right_arm", 1),
    LEFT_LEG("left_leg", 1),
    RIGHT_LEG("right_leg", 1);

    /** The nine types that have tiered organs; the organ set tier is the lowest of them. */
    public static final OrganType[] TIERED = { EYE, SPINE, LUNG, LIVER, HEART, LEFT_ARM, RIGHT_ARM, LEFT_LEG,
            RIGHT_LEG };

    private final String key;
    private final int slots;

    OrganType(String key, int slots) {
        this.key = key;
        this.slots = slots;
    }

    public String key() {
        return key;
    }

    public int slots() {
        return slots;
    }

    public String translationKey() {
        return "gtia.organ_type." + key;
    }
}
