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

    /** id -> section -> lines (English). "fact" is the optional real-world line on top. */
    private static final Map<String, Map<String, String[]>> TEXT = new LinkedHashMap<>();

    static {
        add("rocket_assembler",
                "The Saturn V was stacked upright in a building 160 m tall, then rolled to the pad",
                new String[] { "Builds Ad Astra rockets from their parts, GregTech components and Rocket Fuel",
                        "The tier 1 rocket takes HV power for 2 minutes" },
                null,
                new String[] { "An Ad Astra Launch Pad (3 x 3) in the middle of the platform",
                        "Hatches go on the casings of the base and the control house in front", OVERCLOCK });
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
