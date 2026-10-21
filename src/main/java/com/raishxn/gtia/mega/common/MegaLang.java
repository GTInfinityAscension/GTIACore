package com.raishxn.gtia.mega.common;

import com.google.gson.JsonParser;
import com.raishxn.gtia.GTIAMod;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Text of the megastructures, one file per megastructure in {@code assets/gtia/mega_lang/<name>.json}: each key maps
 * to [en_us, pt_br, zh_cn]. English goes to the generated en_us through Registrate; tools/mega_lang.py copies the
 * other two into lang/pt_br.json and lang/zh_cn.json.
 */
public final class MegaLang {

    /** Files under mega_lang, without the extension. */
    public static final String[] FILES = { "common", "star_lifter", "stellar_hoop", "system_creator",
            "fusion_suppressor", "kugelblitz", "hraemc", "nidavellir", "assembly_yard", "yggdrasil",
            "stellar_harvester", "suprashell", "disintegrator", "birch_world", "qso" };

    /** English of every key read, for names Registrate needs as a lang value. */
    private static final java.util.Map<String, String> ENGLISH = new java.util.HashMap<>();

    private MegaLang() {}

    public static String english(String key) {
        return ENGLISH.getOrDefault(key, key);
    }

    public static void init() {
        for (String name : FILES) {
            try (var in = MegaLang.class.getClassLoader()
                    .getResourceAsStream("assets/gtia/mega_lang/" + name + ".json")) {
                if (in == null) continue;
                var root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                // Controller names go to Registrate as each machine's lang value (see MegaDefinition).
                for (var e : root.entrySet()) {
                    String en = e.getValue().getAsJsonArray().get(0).getAsString();
                    ENGLISH.put(e.getKey(), en);
                    if (!e.getKey().startsWith("block.gtia.")) GTIAMod.REGISTRATE.addRawLang(e.getKey(), en);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Broken mega_lang/" + name + ".json", e);
            }
        }
    }
}
