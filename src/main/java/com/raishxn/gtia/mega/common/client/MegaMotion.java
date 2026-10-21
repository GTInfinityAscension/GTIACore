package com.raishxn.gtia.mega.common.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A megastructure's motion, written by its generator: for each moving piece (and each moving effect, see
 * {@link MegaFx}) a rotation about the model origin in grid axes and a shift.
 */
public final class MegaMotion {

    /** Samples at a fixed rate over one period: quaternion x y z w, then shift x y z in blocks. */
    public record Track(double period, float[][] samples) {

        /** Rotation and shift at {@code seconds}, interpolated between samples. */
        public void at(double seconds, Quaternionf rotation, Vector3f shift) {
            int n = samples.length;
            double f = ((seconds % period) + period) % period / period * n;
            int i = (int) Math.floor(f) % n;
            float a = (float) (f - Math.floor(f));
            float[] p = samples[i], q = samples[(i + 1) % n];
            rotation.set(p[0], p[1], p[2], p[3]).slerp(new Quaternionf(q[0], q[1], q[2], q[3]), a);
            shift.set(p[4] + (q[4] - p[4]) * a, p[5] + (q[5] - p[5]) * a, p[6] + (q[6] - p[6]) * a);
        }
    }

    public final Map<String, Track> tracks = new HashMap<>();

    private static final Map<String, java.util.Optional<MegaMotion>> MOTIONS = new HashMap<>();

    @Nullable
    public static MegaMotion get(String name) {
        return MOTIONS.computeIfAbsent(name, n -> {
            try {
                return java.util.Optional.ofNullable(load(n));
            } catch (IOException | RuntimeException e) {
                com.raishxn.gtia.GTIAMod.LOGGER.error("Animation data of {} unreadable", n, e);
                return java.util.Optional.empty();
            }
        }).orElse(null);
    }

    @Nullable
    private static MegaMotion load(String name) throws IOException {
        var loader = MegaMotion.class.getClassLoader();
        String dir = com.raishxn.gtia.mega.common.MegaShape.dir(name);
        try (var tracksIn = loader.getResourceAsStream(dir + "tracks.json")) {
            if (tracksIn == null) return null;
            var out = new MegaMotion();
            JsonObject root = JsonParser.parseReader(new InputStreamReader(tracksIn, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            for (var e : root.getAsJsonObject("tracks").entrySet()) {
                var t = e.getValue().getAsJsonObject();
                var list = t.getAsJsonArray("samples");
                float[][] samples = new float[list.size()][];
                for (int i = 0; i < samples.length; i++) {
                    var s = list.get(i).getAsJsonArray();
                    samples[i] = new float[s.size()];
                    for (int k = 0; k < s.size(); k++) samples[i][k] = s.get(k).getAsFloat();
                }
                out.tracks.put(e.getKey(), new Track(t.get("period").getAsDouble(), samples));
            }
            return out;
        }
    }
}
