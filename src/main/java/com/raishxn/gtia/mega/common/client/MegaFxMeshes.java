package com.raishxn.gtia.mega.common.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.HashMap;
import java.util.Map;

/** Unit meshes of the megastructure effects (POSITION_TEX), built once on the render thread. */
final class MegaFxMeshes {

    private static VertexBuffer sphere, tube, square, quad;
    private static final Map<Integer, VertexBuffer> PARTICLES = new HashMap<>();
    private static final Map<Integer, VertexBuffer> TORI = new HashMap<>();
    private static final Map<Integer, VertexBuffer> PROMINENCES = new HashMap<>();

    private MegaFxMeshes() {}

    /** Unit sphere, UV = longitude, latitude. */
    static VertexBuffer sphere() {
        if (sphere != null) return sphere;
        int n = 72, m = 48;
        var b = begin(n * m * 6);
        for (int i = 0; i < m; i++) {
            double p0 = Math.PI * i / m - Math.PI / 2, p1 = Math.PI * (i + 1) / m - Math.PI / 2;
            for (int j = 0; j < n; j++) {
                double t0 = 2 * Math.PI * j / n, t1 = 2 * Math.PI * (j + 1) / n;
                float[][] q = { sp(p0, t0, j, i, n, m), sp(p1, t0, j, i + 1, n, m), sp(p1, t1, j + 1, i + 1, n, m),
                        sp(p0, t1, j + 1, i, n, m) };
                quad(b, q);
            }
        }
        return sphere = upload(b);
    }

    private static float[] sp(double lat, double lon, int j, int i, int n, int m) {
        return new float[] { (float) (Math.cos(lat) * Math.cos(lon)), (float) Math.sin(lat),
                (float) (Math.cos(lat) * Math.sin(lon)), (float) j / n, (float) i / m };
    }

    /** Unit cylinder of radius 1 along +Y from 0 to 1, open ends; UV = angle, height. */
    static VertexBuffer tube() {
        if (tube != null) return tube;
        int seg = 40, rings = 64;
        var b = begin(seg * rings * 6);
        for (int i = 0; i < rings; i++) {
            float y0 = (float) i / rings, y1 = (float) (i + 1) / rings;
            for (int j = 0; j < seg; j++) {
                double a0 = 2 * Math.PI * j / seg, a1 = 2 * Math.PI * (j + 1) / seg;
                float u0 = (float) j / seg, u1 = (float) (j + 1) / seg;
                quad(b, new float[][] { { (float) Math.cos(a0), y0, (float) Math.sin(a0), u0, y0 },
                        { (float) Math.cos(a0), y1, (float) Math.sin(a0), u0, y1 },
                        { (float) Math.cos(a1), y1, (float) Math.sin(a1), u1, y1 },
                        { (float) Math.cos(a1), y0, (float) Math.sin(a1), u1, y0 } });
            }
        }
        return tube = upload(b);
    }

    /** Square of side 2 in the XZ plane, UV 0..1 (disks). */
    static VertexBuffer square() {
        if (square != null) return square;
        var b = begin(6);
        quad(b, new float[][] { { -1, 0, -1, 0, 0 }, { 1, 0, -1, 1, 0 }, { 1, 0, 1, 1, 1 }, { -1, 0, 1, 0, 1 } });
        return square = upload(b);
    }

    /** Billboard corners -1..1 in Position.xy. */
    static VertexBuffer quad() {
        if (quad != null) return quad;
        var b = begin(6);
        quad(b, new float[][] { { -1, -1, 0, 0, 0 }, { 1, -1, 0, 1, 0 }, { 1, 1, 0, 1, 1 }, { -1, 1, 0, 0, 1 } });
        return quad = upload(b);
    }

    /** {@code count} particle sprites: Position = (seed, corner x, corner y). */
    static VertexBuffer particles(int count) {
        return PARTICLES.computeIfAbsent(count, n -> {
            var b = begin(n * 6);
            for (int i = 0; i < n; i++) {
                float s = i + 0.5f;
                quad(b, new float[][] { { s, -1, -1, 0, 0 }, { s, 1, -1, 1, 0 }, { s, 1, 1, 1, 1 },
                        { s, -1, 1, 0, 1 } });
            }
            return upload(b);
        });
    }

    /** Torus of major radius 1 and minor radius {@code minorPermille / 1000}, in the XZ plane. */
    static VertexBuffer torus(int minorPermille) {
        return TORI.computeIfAbsent(minorPermille, k -> {
            double r = k / 1000.0;
            int n = 160, m = 28;
            var b = begin(n * m * 6);
            for (int i = 0; i < n; i++)
                for (int j = 0; j < m; j++) {
                    float[][] q = new float[4][];
                    int[][] c = { { i, j }, { i + 1, j }, { i + 1, j + 1 }, { i, j + 1 } };
                    for (int v = 0; v < 4; v++) {
                        double a = 2 * Math.PI * c[v][0] / n, t = 2 * Math.PI * c[v][1] / m;
                        double rr = 1 + r * Math.cos(t);
                        q[v] = new float[] { (float) (rr * Math.cos(a)), (float) (r * Math.sin(t)),
                                (float) (rr * Math.sin(a)), (float) c[v][0] / n, (float) c[v][1] / m };
                    }
                    quad(b, q);
                }
            return upload(b);
        });
    }

    /**
     * Prominences on the unit sphere: {@code count} arched loops, each two crossed ribbons; UV = across, along.
     * The seed picks where they stand.
     */
    static VertexBuffer prominences(int count, int seed) {
        return PROMINENCES.computeIfAbsent(count * 1000 + seed, k -> {
            var rnd = new java.util.Random(seed * 7919L + 13);
            int steps = 40;
            var b = begin(count * steps * 12);
            for (int p = 0; p < count; p++) {
                // Foot points either side of a belt, an arch up to 0.15 - 0.45 radii high.
                double lat = (rnd.nextDouble() - 0.5) * 1.6, lon = rnd.nextDouble() * 2 * Math.PI;
                double span = 0.12 + rnd.nextDouble() * 0.22, height = 0.15 + rnd.nextDouble() * 0.3;
                double tilt = rnd.nextDouble() * Math.PI;
                var centre = new org.joml.Vector3d(Math.cos(lat) * Math.cos(lon), Math.sin(lat),
                        Math.cos(lat) * Math.sin(lon));
                var east = new org.joml.Vector3d(0, 1, 0).cross(centre, new org.joml.Vector3d()).normalize();
                var north = new org.joml.Vector3d(centre).cross(east, new org.joml.Vector3d()).normalize();
                var along = new org.joml.Vector3d(east).mul(Math.cos(tilt)).add(new org.joml.Vector3d(north)
                        .mul(Math.sin(tilt)));
                var side = new org.joml.Vector3d(centre).cross(along, new org.joml.Vector3d()).normalize();
                double width = 0.02 + rnd.nextDouble() * 0.025;
                for (int s = 0; s < steps; s++) {
                    double t0 = (double) s / steps, t1 = (double) (s + 1) / steps;
                    var a0 = arch(centre, along, span, height, t0);
                    var a1 = arch(centre, along, span, height, t1);
                    for (var w : new org.joml.Vector3d[] { side, centre }) {
                        var d = new org.joml.Vector3d(w).mul(width);
                        quad(b, new float[][] { pt(a0, d, -1, 0, t0), pt(a1, d, -1, 0, t1), pt(a1, d, 1, 1, t1),
                                pt(a0, d, 1, 1, t0) });
                    }
                }
            }
            return upload(b);
        });
    }

    private static org.joml.Vector3d arch(org.joml.Vector3d centre, org.joml.Vector3d along, double span,
                                          double height, double t) {
        double x = (t * 2 - 1) * span;
        double up = 1 + height * Math.sin(Math.PI * t);
        return new org.joml.Vector3d(centre).add(new org.joml.Vector3d(along).mul(x)).normalize().mul(up);
    }

    private static float[] pt(org.joml.Vector3d p, org.joml.Vector3d d, int sign, float u, double v) {
        return new float[] { (float) (p.x + d.x * sign), (float) (p.y + d.y * sign), (float) (p.z + d.z * sign), u,
                (float) v };
    }

    // ------------------------------------------------------------------ building

    static BufferBuilder begin(int vertices) {
        var b = new BufferBuilder(vertices * DefaultVertexFormat.POSITION_TEX.getVertexSize() + 64);
        b.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        return b;
    }

    static void quad(BufferBuilder b, float[][] q) {
        for (int k : new int[] { 0, 1, 2, 0, 2, 3 }) b.vertex(q[k][0], q[k][1], q[k][2]).uv(q[k][3], q[k][4]).endVertex();
    }

    static VertexBuffer upload(BufferBuilder b) {
        var vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vb.bind();
        vb.upload(b.end());
        VertexBuffer.unbind();
        return vb;
    }
}
