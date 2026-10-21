package com.raishxn.gtia.mega.common.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Curved effect meshes (POSITION_TEX, UV = around, along), built once and kept as vertex buffers: tubes along a
 * Catmull-Rom curve (streams), dipole field lines round a star (arcs) and the grid of a gravity well. Only the model
 * matrix changes between frames.
 */
final class MegaFxCurves {

    private static final Map<String, VertexBuffer> CACHE = new HashMap<>();
    private static final int PER_SPAN = 24, RADIAL = 16;

    private MegaFxCurves() {}

    /**
     * A tube through {@code points} (relative to the first), its radius going from {@code r0} to {@code r1} (fast at
     * first, so a stream leaves its nozzle as a drop and thins out).
     */
    static VertexBuffer stream(String key, float[][] points, float r0, float r1) {
        return CACHE.computeIfAbsent("stream/" + key, k -> {
            List<Vector3f> path = catmullRom(points);
            float[] radius = new float[path.size()];
            for (int i = 0; i < radius.length; i++) {
                float s = (float) i / (radius.length - 1);
                radius[i] = r0 + (r1 - r0) * (float) Math.pow(s, 0.6);
            }
            var b = MegaFxMeshes.begin((path.size() - 1) * RADIAL * 6);
            tube(b, path, radius, 0, 1);
            return MegaFxMeshes.upload(b);
        });
    }

    /**
     * {@code lines} dipole field lines about +Y with the field-line constant L = 1: r = sin^2(theta), each from the
     * star's surface ({@code starShare} = star radius / L) back to it in the other hemisphere; tube radius
     * {@code thick} (also in units of L).
     */
    static VertexBuffer arcs(int lines, float starShare, float thick) {
        String key = "arcs/" + lines + "/" + Math.round(starShare * 1000) + "/" + Math.round(thick * 1000);
        return CACHE.computeIfAbsent(key, k -> {
            // EN: In a dipole field a line keeps r / sin^2(theta) constant; it leaves the surface where
            // sin^2(theta) = R / L.
            // PT: Num campo dipolar uma linha mantém r / sin^2(theta) constante; ela sai da superfície onde
            // sin^2(theta) = R / L.
            // ZH: 偶极场中一条磁力线保持 r / sin^2(theta) 不变；它在 sin^2(theta) = R / L 处离开表面。
            double t0 = Math.asin(Math.sqrt(Math.min(0.95, Math.max(0.01, starShare))));
            int steps = 64;
            var b = MegaFxMeshes.begin(lines * steps * RADIAL * 6);
            for (int l = 0; l < lines; l++) {
                double phi = 2 * Math.PI * l / lines;
                List<Vector3f> path = new ArrayList<>();
                for (int i = 0; i <= steps; i++) {
                    double th = t0 + (Math.PI - 2 * t0) * i / steps, r = Math.sin(th) * Math.sin(th);
                    path.add(new Vector3f((float) (r * Math.sin(th) * Math.cos(phi)), (float) (r * Math.cos(th)),
                            (float) (r * Math.sin(th) * Math.sin(phi))));
                }
                float[] radius = new float[path.size()];
                java.util.Arrays.fill(radius, thick);
                tube(b, path, radius, 0, 1);
            }
            return MegaFxMeshes.upload(b);
        });
    }

    /**
     * Grid of half-size 1 in the XZ plane sinking {@code depth} at the centre: y = -depth m / sqrt(r^2 + m^2), the
     * embedding-diagram funnel of a point mass, flattened far out.
     */
    static VertexBuffer well(float depth) {
        return CACHE.computeIfAbsent("well/" + Math.round(depth * 1000), k -> {
            int n = 96;
            double m = 0.12;
            var b = MegaFxMeshes.begin(n * n * 6);
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++) {
                    float[][] q = new float[4][];
                    int[][] c = { { i, j }, { i + 1, j }, { i + 1, j + 1 }, { i, j + 1 } };
                    for (int v = 0; v < 4; v++) {
                        double x = 2.0 * c[v][0] / n - 1, z = 2.0 * c[v][1] / n - 1;
                        double y = -depth * m / Math.sqrt(x * x + z * z + m * m);
                        q[v] = new float[] { (float) x, (float) y, (float) z, (float) c[v][0] / n, (float) c[v][1] / n };
                    }
                    MegaFxMeshes.quad(b, q);
                }
            return MegaFxMeshes.upload(b);
        });
    }

    // ------------------------------------------------------------------ building

    /** Uniform Catmull-Rom through the points (relative to the first), PER_SPAN samples a span. */
    static List<Vector3f> catmullRom(float[][] pts) {
        int n = pts.length;
        List<Vector3f> p = new ArrayList<>();
        for (float[] q : pts) p.add(new Vector3f(q[0] - pts[0][0], q[1] - pts[0][1], q[2] - pts[0][2]));
        List<Vector3f> out = new ArrayList<>();
        if (n < 2) {
            out.add(new Vector3f());
            out.add(new Vector3f(0, 1, 0));
            return out;
        }
        // Mirrored end points stand in for the missing neighbours of the first and last spans.
        Vector3f first = new Vector3f(p.get(0)).mul(2).sub(p.get(1));
        Vector3f last = new Vector3f(p.get(n - 1)).mul(2).sub(p.get(n - 2));
        for (int s = 0; s < n - 1; s++) {
            Vector3f a = s == 0 ? first : p.get(s - 1), b = p.get(s), c = p.get(s + 1),
                    d = s + 2 < n ? p.get(s + 2) : last;
            for (int i = 0; i < PER_SPAN; i++) out.add(point(a, b, c, d, (float) i / PER_SPAN));
        }
        out.add(new Vector3f(p.get(n - 1)));
        return out;
    }

    private static Vector3f point(Vector3f a, Vector3f b, Vector3f c, Vector3f d, float t) {
        float t2 = t * t, t3 = t2 * t;
        // 0.5 * (2b + (c - a) t + (2a - 5b + 4c - d) t^2 + (3b - a - 3c + d) t^3)
        return new Vector3f(
                0.5f * (2 * b.x + (c.x - a.x) * t + (2 * a.x - 5 * b.x + 4 * c.x - d.x) * t2 +
                        (3 * b.x - a.x - 3 * c.x + d.x) * t3),
                0.5f * (2 * b.y + (c.y - a.y) * t + (2 * a.y - 5 * b.y + 4 * c.y - d.y) * t2 +
                        (3 * b.y - a.y - 3 * c.y + d.y) * t3),
                0.5f * (2 * b.z + (c.z - a.z) * t + (2 * a.z - 5 * b.z + 4 * c.z - d.z) * t2 +
                        (3 * b.z - a.z - 3 * c.z + d.z) * t3));
    }

    /**
     * Rings of RADIAL vertices round each path point, framed by parallel transport (the normal is carried from point
     * to point instead of being recomputed, so the tube never twists); V runs from {@code v0} to {@code v1}.
     */
    private static void tube(BufferBuilder b, List<Vector3f> path, float[] radius, float v0, float v1) {
        int n = path.size();
        Vector3f[] tan = new Vector3f[n];
        for (int i = 0; i < n; i++) {
            var t = new Vector3f(path.get(Math.min(n - 1, i + 1))).sub(path.get(Math.max(0, i - 1)));
            tan[i] = t.lengthSquared() < 1e-10f ? new Vector3f(0, 1, 0) : t.normalize();
        }
        Vector3f normal = Math.abs(tan[0].y) < 0.9f ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
        normal.sub(new Vector3f(tan[0]).mul(normal.dot(tan[0]))).normalize();
        Vector3f[][] ring = new Vector3f[n][RADIAL + 1];
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                normal.sub(new Vector3f(tan[i]).mul(normal.dot(tan[i])));
                if (normal.lengthSquared() < 1e-10f) normal.set(Math.abs(tan[i].y) < 0.9f ? 0 : 1, Math.abs(tan[i].y) <
                        0.9f ? 1 : 0, 0);
                normal.normalize();
            }
            Vector3f bin = new Vector3f(tan[i]).cross(normal).normalize();
            for (int j = 0; j <= RADIAL; j++) {
                double a = 2 * Math.PI * j / RADIAL;
                ring[i][j] = new Vector3f(path.get(i)).add(new Vector3f(normal).mul((float) Math.cos(a) * radius[i]))
                        .add(new Vector3f(bin).mul((float) Math.sin(a) * radius[i]));
            }
        }
        for (int i = 0; i < n - 1; i++) {
            float va = v0 + (v1 - v0) * i / (n - 1), vb = v0 + (v1 - v0) * (i + 1) / (n - 1);
            for (int j = 0; j < RADIAL; j++) {
                float ua = (float) j / RADIAL, ub = (float) (j + 1) / RADIAL;
                MegaFxMeshes.quad(b, new float[][] { vtx(ring[i][j], ua, va), vtx(ring[i + 1][j], ua, vb),
                        vtx(ring[i + 1][j + 1], ub, vb), vtx(ring[i][j + 1], ub, va) });
            }
        }
    }

    private static float[] vtx(Vector3f p, float u, float v) {
        return new float[] { p.x, p.y, p.z, u, v };
    }
}
