package com.raishxn.gtia.mega.matrioshka.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * The Matrioshka's motion, written by gen_rings_pattern.py: for each turning ring a rotation about the star in grid
 * axes (and a shift, zero for the rings), plus the render-only meshes (star and corona) in grid cells.
 */
public final class MatrioshkaMotion {

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

    /** Triangles in grid cells, moved by {@link #track}, shown from phase {@link #phase}. */
    public record Mesh(String track, int phase, float[] triangles) {}

    public final Map<String, Track> tracks = new HashMap<>();
    public final Map<String, Mesh> meshes = new LinkedHashMap<>();

    private static boolean loaded;
    @Nullable
    private static MatrioshkaMotion motion;

    @Nullable
    public static MatrioshkaMotion get() {
        if (!loaded) {
            loaded = true;
            try {
                motion = load();
            } catch (IOException | RuntimeException e) {
                com.raishxn.gtia.GTIAMod.LOGGER.error("Matrioshka animation data unreadable", e);
            }
        }
        return motion;
    }

    @Nullable
    private static MatrioshkaMotion load() throws IOException {
        var loader = MatrioshkaMotion.class.getClassLoader();
        try (var tracksIn = loader.getResourceAsStream("gtia_matrioshka/tracks.json");
                var meshIn = loader.getResourceAsStream("gtia_matrioshka/render.bin.gz")) {
            if (tracksIn == null || meshIn == null) return null;
            var out = new MatrioshkaMotion();
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
            try (var in = new DataInputStream(new GZIPInputStream(meshIn))) {
                int count = in.readInt();
                for (int m = 0; m < count; m++) {
                    byte[] name = new byte[in.readInt()];
                    in.readFully(name);
                    int phase = in.readInt();
                    float[] tri = new float[in.readInt() * 9];
                    for (int k = 0; k < tri.length; k++) tri[k] = in.readFloat();
                    String track = new String(name, StandardCharsets.UTF_8);
                    out.meshes.put(track, new Mesh(track, phase, tri));
                }
            }
            return out;
        }
    }
}
