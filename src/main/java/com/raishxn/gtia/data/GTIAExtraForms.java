package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasDustProperty;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasIngotProperty;

/** GTIA-owned versions of GTNA's compound ingot and plate forms. */
public final class GTIAExtraForms {

    public static final TagPrefix DOUBLE_INGOT = ingot("gtiaDoubleIngot", "double_%s_ingot", "double_ingots", 2,
            MaterialIconType.ingotDouble);
    public static final TagPrefix TRIPLE_INGOT = ingot("gtiaTripleIngot", "triple_%s_ingot", "triple_ingots", 3,
            MaterialIconType.ingotTriple);
    public static final TagPrefix QUADRUPLE_INGOT = ingot("gtiaQuadrupleIngot", "quadruple_%s_ingot",
            "quadruple_ingots", 4, MaterialIconType.ingotQuadruple);
    public static final TagPrefix QUINTUPLE_INGOT = ingot("gtiaQuintupleIngot", "quintuple_%s_ingot",
            "quintuple_ingots", 5, MaterialIconType.ingotQuintuple);
    public static final TagPrefix TRIPLE_PLATE = plate("gtiaTriplePlate", "triple_%s_plate", "triple_plates", 3,
            MaterialIconType.plateTriple);
    public static final TagPrefix QUADRUPLE_PLATE = plate("gtiaQuadruplePlate", "quadruple_%s_plate",
            "quadruple_plates", 4, MaterialIconType.plateQuadruple);
    public static final TagPrefix QUINTUPLE_PLATE = plate("gtiaQuintuplePlate", "quintuple_%s_plate",
            "quintuple_plates", 5, MaterialIconType.plateQuintuple);
    public static final TagPrefix SUPERDENSE_PLATE = plate("gtiaSuperdensePlate", "superdense_%s_plate",
            "superdense_plates", 64, sharedSuperdenseIcon());

    private GTIAExtraForms() {}

    // GTNA and GTIA may load in either order; the icon registry is shared by GTCEu.
    private static MaterialIconType sharedSuperdenseIcon() {
        MaterialIconType existing = MaterialIconType.getByName("plate_superdense");
        return existing != null ? existing : new MaterialIconType("plateSuperdense");
    }

    private static boolean isOwnAlloy(com.gregtechceu.gtceu.api.data.chemical.material.Material material) {
        return material == GTIAAlloys.PULSATING_ALLOY || material == GTIAAlloys.CONDUCTIVE_ALLOY;
    }

    private static TagPrefix ingot(String name, String idPattern, String tagPath, int amount,
                                   MaterialIconType iconType) {
        return new TagPrefix(name)
                .idPattern(idPattern)
                .defaultTagPath(tagPath + "/%s")
                .unformattedTagPath(tagPath)
                .langValue(switch (amount) {
                    case 2 -> "Double %s Ingot";
                    case 3 -> "Triple %s Ingot";
                    case 4 -> "Quadruple %s Ingot";
                    default -> "Quintuple %s Ingot";
                })
                .materialAmount(GTValues.M * amount)
                .maxStackSize(32)
                .materialIconType(iconType)
                .unificationEnabled(true)
                .enableRecycling()
                .generateItem(true)
                .generationCondition(hasIngotProperty.and(GTIAExtraForms::isOwnAlloy));
    }

    private static TagPrefix plate(String name, String idPattern, String tagPath, int amount,
                                   MaterialIconType iconType) {
        return new TagPrefix(name)
                .idPattern(idPattern)
                .defaultTagPath(tagPath + "/%s")
                .unformattedTagPath(tagPath)
                .langValue(switch (amount) {
                    case 3 -> "Triple %s Plate";
                    case 4 -> "Quadruple %s Plate";
                    case 5 -> "Quintuple %s Plate";
                    default -> "Superdense %s Plate";
                })
                .materialAmount(GTValues.M * amount)
                .maxStackSize(amount == 64 ? 16 : 32)
                .materialIconType(iconType)
                .unificationEnabled(true)
                .enableRecycling()
                .generateItem(true)
                .generationCondition(hasDustProperty.and(GTIAExtraForms::isOwnAlloy));
    }

    public static void register() {}
}
