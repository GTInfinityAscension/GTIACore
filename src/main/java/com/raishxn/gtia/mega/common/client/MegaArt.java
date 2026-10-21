package com.raishxn.gtia.mega.common.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Pixel art of the megastructure screens, animated: stars with limb darkening and moving granules, black holes with
 * a tilted accretion disk and photon ring, jets with a hot core, force-field arcs and gas giants.
 */
@OnlyIn(Dist.CLIENT)
public final class MegaArt {

    private MegaArt() {}

    public static double time() {
        return (net.minecraft.Util.getMillis() % 3_600_000L) / 1000.0;
    }

    public static int rgb(float[] c, double f) {
        int r = (int) Math.max(0, Math.min(255, c[0] * 255 * f)), g = (int) Math.max(0, Math.min(255, c[1] * 255 * f)),
                b = (int) Math.max(0, Math.min(255, c[2] * 255 * f));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static double hash(int x, int y, int t) {
        long h = x * 374761393L + y * 668265263L + t * 2147483647L;
        h = (h ^ (h >> 13)) * 1274126177L;
        return ((h ^ (h >> 16)) & 0xFFFF) / 65535.0;
    }

    /** A star of {@code kelvin}, radius r: limb darkening, granules that boil, a soft corona around it. */
    public static void star(GuiGraphics g, int cx, int cy, int r, double kelvin) {
        if (r <= 0) return;
        float[] hot = MegaFx.blackbody(kelvin), limb = MegaFx.blackbody(kelvin * 0.62);
        glow(g, cx, cy, (int) (r * 2.2), rgb(hot, 0.9), 0.55);
        int t = (int) (time() * 3);
        for (int y = -r; y <= r; y++)
            for (int x = -r; x <= r; x++) {
                double d2 = (x * x + y * y) / (double) (r * r);
                if (d2 > 1) continue;
                double mu = Math.sqrt(1 - d2);
                double ld = 1 - 0.6 * (1 - mu) - 0.2 * (1 - mu) * (1 - mu);
                double gr = 0.86 + 0.22 * hash((x + 64) / 2, (y + 64) / 2, t / 2 + (x * 7 + y * 3) % 5);
                float[] c = new float[3];
                for (int k = 0; k < 3; k++) c[k] = (float) (limb[k] + (hot[k] - limb[k]) * Math.pow(mu, 0.45));
                px(g, cx + x, cy + y, rgb(c, ld * gr));
            }
    }

    /** A black hole: dark shadow, an accretion disk seen tilted (brighter on the approaching left), photon ring. */
    public static void blackHole(GuiGraphics g, int cx, int cy, int r, double kelvin) {
        double t = time();
        float[] hot = MegaFx.blackbody(kelvin), cool = MegaFx.blackbody(kelvin * 0.35);
        for (int pass = 0; pass < 2; pass++) {
            for (int k = 0; k < 900; k++) {
                double rr = r * (1.4 + 1.8 * (k % 30) / 30.0);
                double a = k * 0.61803 * 2 * Math.PI + t * 2.2 * Math.pow(r / rr, 1.5);
                double x = Math.cos(a) * rr, y = Math.sin(a) * rr * 0.28;
                boolean back = y < 0;
                if ((pass == 0) != back) continue;
                double heat = Math.pow(1.4 * r / rr, 0.75);
                double beam = 1 + 0.6 * -Math.sin(a);
                float[] c = new float[3];
                for (int i = 0; i < 3; i++) c[i] = (float) (cool[i] + (hot[i] - cool[i]) * heat);
                int px = cx + (int) Math.round(x), py = cy + (int) Math.round(y) - (back ? (int) (r * 0.35) : 0);
                px(g, px, py, rgb(c, Math.min(1.4, heat * beam)));
            }
            if (pass == 0) {
                disc(g, cx, cy, r, 0xFF000000);
                ellipse(g, cx, cy, r + 1, r + 1, rgb(hot, 1.1), 0);
            }
        }
    }

    /** A jet from (x, y) to the right of length {@code len}: white-hot core, coloured sheath, drifting knots. */
    public static void jet(GuiGraphics g, int x, int y, int len, int w0, int w1, float[] hot, float[] cool,
                           boolean right) {
        double t = time();
        for (int i = 0; i < len; i++) {
            double f = i / (double) len;
            int w = (int) Math.round(w0 + (w1 - w0) * f);
            double knot = 0.75 + 0.35 * Math.sin(f * 40 - t * 14) * Math.exp(-f * 3);
            int xx = right ? x + i : x - i;
            for (int j = -w; j <= w; j++) {
                double core = 1 - Math.abs(j) / (double) (w + 1);
                float[] c = new float[3];
                for (int k = 0; k < 3; k++)
                    c[k] = (float) (cool[k] + (hot[k] - cool[k]) * Math.exp(-f * 2.4) + Math.pow(core, 6) * 0.6);
                double a = Math.pow(core, 0.8) * (1 - f) * knot;
                if (a > 0.08 && hash(xx, y + j, (int) (t * 20)) < a + 0.2) px(g, xx, y + j, rgb(c, a));
            }
        }
    }

    /** A jet going up ({@code dir} -1) or down (+1) from (x, y): hot core, coloured sheath, drifting knots. */
    public static void jetVertical(GuiGraphics g, int x, int y, int dir, int len, int w0, int w1, float[] hot,
                                   float[] cool) {
        double t = time();
        for (int i = 0; i < len; i++) {
            double f = i / (double) len;
            int w = (int) Math.round(w0 + (w1 - w0) * f);
            double knot = 0.7 + 0.4 * Math.sin(f * 30 - t * 12);
            int yy = y + dir * i;
            for (int j = -w; j <= w; j++) {
                double core = 1 - Math.abs(j) / (double) (w + 1);
                float[] c = new float[3];
                for (int k = 0; k < 3; k++)
                    c[k] = (float) (cool[k] + (hot[k] - cool[k]) * Math.exp(-f * 2.4) + Math.pow(core, 6) * 0.6);
                double a = Math.pow(core, 0.8) * (1 - f) * knot;
                if (a > 0.06 && hash(x + j, yy, (int) (t * 20)) < a + 0.25) px(g, x + j, yy, rgb(c, a));
            }
        }
    }

    /** A beam between two points with moving bright bands. */
    public static void beam(GuiGraphics g, int x0, int y0, int x1, int y1, int colour) {
        double t = time();
        int n = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int i = 0; i <= n; i++) {
            double f = i / (double) Math.max(1, n);
            double band = 0.6 + 0.4 * Math.sin(f * 30 - t * 9);
            px(g, x0 + (int) Math.round((x1 - x0) * f), y0 + (int) Math.round((y1 - y0) * f), dim(colour, band));
        }
    }

    /** A hexagonal force-field circle: dotted rim with a pulse running round it. */
    public static void field(GuiGraphics g, int cx, int cy, int r, int colour) {
        double t = time();
        int n = (int) (2 * Math.PI * r);
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            double pulse = Math.max(0, Math.cos(a - t * 1.5));
            if (i % 3 == 0 || pulse > 0.9)
                px(g, cx + (int) Math.round(Math.cos(a) * r), cy + (int) Math.round(Math.sin(a) * r),
                        dim(colour, 0.45 + 0.55 * pulse));
        }
    }

    /** A banded gas giant lit from the left. */
    public static void planet(GuiGraphics g, int cx, int cy, int r, int light, int dark) {
        double t = time();
        for (int y = -r; y <= r; y++)
            for (int x = -r; x <= r; x++) {
                if (x * x + y * y > r * r) continue;
                double band = 0.5 + 0.5 * Math.sin(y * 0.9 + Math.sin(x * 0.2 + t * 0.4) * 0.8);
                double lit = Math.max(0.15, 1 - (x + r) / (2.4 * r));
                int c = band > 0.5 ? light : dark;
                px(g, cx + x, cy + y, dim(c, 0.35 + 0.65 * (1 - lit) * (0.8 + 0.2 * band)));
            }
    }
}
