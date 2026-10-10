package com.raishxn.gtia.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.raishxn.gtia.GTIAMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Multiblock tooltips in the GTNA layout: an optional real fact, then "Main Function", "Auxiliary Module" (only when
 * the structure has one) and "Running Requirements". English text lives here and is registered as lang keys
 * (gtia.machine.&lt;id&gt;.tooltip.&lt;section&gt;.&lt;n&gt;); pt_br.json translates the same keys.
 */
public final class GTIATooltips {

    private static final String[] SECTIONS = { "main", "auxiliary", "requirements" };
    private static final String OVERCLOCK = "Overclocks like a GregTech machine and supports Batch Mode";
    private static final String COKE_OVEN_HATCH = "Accepts up to 5 Coke Oven Hatches for automation";

    /** id -> section -> lines (English). "fact" is the optional real-world line on top. */
    private static final Map<String, Map<String, String[]>> TEXT = new LinkedHashMap<>();

    static {
        add("wood_retort",
                "Until the 1920s, most of the world's methanol came from cooking wood",
                new String[] { "Cooks 4 logs into 3 Charcoal and 1,000 mB of Charcoal Byproducts",
                        "Burns its own wood gas: no fuel, no power" },
                null,
                new String[] { COKE_OVEN_HATCH, "Three Retorts keep one Still busy on circuit 1" });
        add("lime_kiln",
                "Lime kilns are older than writing",
                new String[] { "Burns 4 Calcite into 4 Quicklime",
                        "Also calcines Aluminium Hydroxide into Alumina and Sodium Bicarbonate into Soda Ash" },
                null,
                new String[] { "One Charcoal, Coal or Coke per batch, in the second slot", COKE_OVEN_HATCH });
        add("diaphragm_cell",
                "Chlorine and caustic soda always come out together, in the same ratio",
                new String[] {
                        "Splits 4,000 mB of Salt Water into 2,000 mB Chlorine, 2,000 mB Hydrogen and 4,000 mB Cell Liquor",
                        "A Centrifuge turns the Cell Liquor into Sodium Hydroxide and returns half the salt" },
                null,
                new String[] { "An Asbestos Diaphragm in an Input Bus: 1% chance to wear out per run",
                        "One Centrifuge keeps up with one Cell", OVERCLOCK });
        add("bayer_digester",
                "Almost all of the world's alumina still comes from this 1888 process",
                new String[] { "Circuit 1: Bauxite and Sodium Hydroxide in steam become Sodium Aluminate and Red Mud",
                        "Circuit 2: Sodium Aluminate precipitates Aluminium Hydroxide and gives the soda back" },
                null,
                new String[] { "Circuit 1 also burns 1,000 mB of Steam per run",
                        "Circuit 2 needs one Aluminium Hydroxide as seed (not consumed)", OVERCLOCK });
        add("hall_heroult_cell",
                "Before 1886, aluminium cost more than gold",
                new String[] { "Reduces 2 Alumina into 4 Aluminium Ingots in molten Cryolite",
                        "Vents 3,000 mB of Carbon Dioxide per run" },
                null,
                new String[] { "Burns one Carbon Anode per run",
                        "Cryolite is the bath: 5% chance to be used up per run", OVERCLOCK });
        add("contact_converter",
                "Sulfuric acid is the most produced chemical on Earth",
                new String[] { "Burns Sulfur with Oxygen and Water into 1,000 mB of Sulfuric Acid",
                        "Gives off 2,000 mB of Steam per run: send it to a turbine or the Bayer Digester" },
                null,
                new String[] { "Vanadium Pentoxide catalyst in an Input Bus (not consumed)", OVERCLOCK });
        add("ammonia_converter",
                "Half of the nitrogen in your body passed through a Haber-Bosch reactor",
                new String[] { "Nitrogen and Hydrogen (1:3) into 1,000 mB of Ammonia",
                        "Gives off 500 mB of Steam per run" },
                null,
                new String[] { "Magnetite catalyst in an Input Bus (not consumed)", OVERCLOCK });
        add("solvay_tower",
                "Ernest Solvay's 1861 process retired the dirty Leblanc soda works",
                new String[] { "Salt Water, Ammonia and Carbon Dioxide into Sodium Bicarbonate and Ammonium Chloride",
                        "Calcine the bicarbonate into Soda Ash in the Lime Kiln" },
                null,
                new String[] { "Recover the Ammonia from Ammonium Chloride with Quicklime in a Chemical Reactor",
                        OVERCLOCK });
    }

    private GTIATooltips() {}

    private static void add(String id, String fact, String[] main, String[] auxiliary, String[] requirements) {
        Map<String, String[]> sections = new LinkedHashMap<>();
        if (fact != null) sections.put("fact", new String[] { fact });
        sections.put("main", main);
        if (auxiliary != null) sections.put("auxiliary", auxiliary);
        sections.put("requirements", requirements);
        TEXT.put(id, sections);
    }

    /** The tooltip lines of a multiblock registered above. */
    public static Component[] multiblock(String id) {
        Map<String, String[]> sections = TEXT.get(id);
        if (sections == null) throw new IllegalArgumentException("no tooltip for " + id);
        List<Component> lines = new ArrayList<>();
        if (sections.containsKey("fact")) {
            lines.add(Component.translatable(key(id, "fact", 0)).withStyle(ChatFormatting.GRAY,
                    ChatFormatting.ITALIC));
        }
        for (String section : SECTIONS) {
            String[] text = sections.get(section);
            if (text == null) continue;
            lines.add(Component.literal("- ").append(Component.translatable("gtia.tooltip.section." + section))
                    .withStyle(ChatFormatting.GOLD));
            for (int i = 0; i < text.length; i++) {
                lines.add(Component.literal("∘ ").append(Component.translatable(key(id, section, i)))
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        return lines.toArray(Component[]::new);
    }

    static void registerLang() {
        GTIAMod.REGISTRATE.addRawLang("gtia.tooltip.section.main", "Main Function");
        GTIAMod.REGISTRATE.addRawLang("gtia.tooltip.section.auxiliary", "Auxiliary Module");
        GTIAMod.REGISTRATE.addRawLang("gtia.tooltip.section.requirements", "Running Requirements");
        TEXT.forEach((id, sections) -> sections.forEach((section, text) -> {
            for (int i = 0; i < text.length; i++) GTIAMod.REGISTRATE.addRawLang(key(id, section, i), text[i]);
        }));
    }

    private static String key(String id, String section, int line) {
        return "gtia.machine." + id + ".tooltip." + section + "." + line;
    }
}
