package com.raishxn.gtia.mega.common.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A megastructure's render-only effects (stars, jets, beams, force fields, accretion disks, black holes, planets),
 * read from {@code gtia_mega/<name>/fx.json}, written by its generator. Positions and sizes are in grid cells; an
 * effect moves with its {@link #track} (none: fixed).
 */
public final class MegaFx {

    public enum Type {
        STAR, JET, BEAM, FIELD, DISK, HOLE, PLANET, GLOW
    }

    /**
     * One effect. {@code a} and {@code b} are the centre and, for jets and beams, the far end; {@code axis} is the
     * normal of disks and torus fields; {@code r0}/{@code r1} the radii (jets: start and end, disks: inner and outer,
     * torus fields: major and minor); {@code temp} the star temperature (K); {@code colours} and {@code params} as the
     * shader mode takes them.
     */
    public record Effect(String name, Type type, int phase, boolean active, @Nullable String track, float[] a,
                         float[] b, float[] axis, float r0, float r1, float temp, int count, String shape,
                         float[] colourA, float[] colourB, float[] params) {

        public float radius() {
            return r0;
        }
    }

    public final List<Effect> effects = new ArrayList<>();

    private static final Map<String, java.util.Optional<MegaFx>> CACHE = new HashMap<>();

    @Nullable
    public static MegaFx get(String name) {
        return CACHE.computeIfAbsent(name, n -> {
            try (var in = MegaFx.class.getClassLoader()
                    .getResourceAsStream(com.raishxn.gtia.mega.common.MegaShape.dir(n) + "fx.json")) {
                if (in == null) return java.util.Optional.empty();
                var root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                var fx = new MegaFx();
                for (var e : root.getAsJsonArray("effects")) fx.effects.add(read(e.getAsJsonObject()));
                return java.util.Optional.of(fx);
            } catch (Exception e) {
                com.raishxn.gtia.GTIAMod.LOGGER.error("Effects of {} unreadable", n, e);
                return java.util.Optional.empty();
            }
        }).orElse(null);
    }

    private static Effect read(JsonObject o) {
        var type = Type.valueOf(o.get("type").getAsString().toUpperCase(java.util.Locale.ROOT));
        return new Effect(o.get("name").getAsString(), type, num(o, "phase", 1), o.has("active") &&
                o.get("active").getAsBoolean(), o.has("track") && !o.get("track").isJsonNull() ?
                        o.get("track").getAsString() : null,
                vec(o, "a", 0, 0, 0), vec(o, "b", 0, 0, 0), vec(o, "axis", 0, 1, 0), num(o, "r0", 1f),
                num(o, "r1", 0f), num(o, "temp", 5800f), num(o, "count", 0),
                o.has("shape") ? o.get("shape").getAsString() : "sphere",
                vec(o, "colourA", 1, 1, 1, 1), vec(o, "colourB", 1, 1, 1, 1), vec(o, "params", 1, 0, 0, 0));
    }

    private static float num(JsonObject o, String key, float def) {
        return o.has(key) ? o.get(key).getAsFloat() : def;
    }

    private static int num(JsonObject o, String key, int def) {
        return o.has(key) ? o.get(key).getAsInt() : def;
    }

    private static float[] vec(JsonObject o, String key, float... def) {
        if (!o.has(key)) return def;
        JsonArray a = o.getAsJsonArray(key);
        float[] v = new float[Math.max(a.size(), def.length)];
        System.arraycopy(def, 0, v, 0, def.length);
        for (int i = 0; i < a.size(); i++) v[i] = a.get(i).getAsFloat();
        return v;
    }

    /**
     * RGB of a black body at {@code kelvin}, brightest channel 1 (fit of the CIE colour-matching functions,
     * after Tanner Helland's table), for star colours.
     */
    public static float[] blackbody(double kelvin) {
        double t = Math.max(1000, Math.min(40000, kelvin)) / 100;
        double r, g, b;
        r = t <= 66 ? 255 : 329.698727446 * Math.pow(t - 60, -0.1332047592);
        g = t <= 66 ? 99.4708025861 * Math.log(t) - 161.1195681661 : 288.1221695283 * Math.pow(t - 60, -0.0755148492);
        b = t >= 66 ? 255 : t <= 19 ? 0 : 138.5177312231 * Math.log(t - 10) - 305.0447927307;
        float[] c = { (float) clamp(r / 255), (float) clamp(g / 255), (float) clamp(b / 255) };
        float max = Math.max(c[0], Math.max(c[1], c[2]));
        for (int i = 0; i < 3; i++) c[i] /= max;
        return c;
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
