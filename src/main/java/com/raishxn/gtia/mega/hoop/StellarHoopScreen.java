package com.raishxn.gtia.mega.hoop;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtia.mega.common.MegaScreen;
import com.raishxn.gtia.mega.common.client.MegaArt;
import com.raishxn.gtia.mega.common.ui.MegaUI;
import com.raishxn.gtia.mega.common.ui.MegaUI.Look;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;

import java.util.Locale;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Stellar Hoop screen, its own arched console: a dive lever beside a profile of the corona (heat and density by
 * height), the hoop sweeping past the star's limb, a coolant tank, a shield ring and the parallel in big digits; the
 * dive chart stays on the console; the recipe runs on a ticker.
 */
public final class StellarHoopScreen {

    static final String P = "gtia.stellar_hoop";
    static final Look L = new Look("hoop", 0xFFFF5A78, 0xFFFFD6DE, 0xFF24141A, 0xFF6E2A3C, 0xFFB08A94,
            MegaUI.Shape.HEX);
    private static final int MANUAL_LINES = 15, PAGE_LINES = 5;
    private static final int[] COOLANT = { 0xFF3A3A44, 0xFF8FD8FF, 0xFFC8F4FF, 0xFFE0B0FF };

    private StellarHoopScreen() {}

    public static ModularUI create(StellarHoopMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 340, 214, 126, 8);
        root.addWidget(MegaUI.paint(10, 40, 10, 140, (g, x, y) -> corona(g, m, x, y)));
        root.addWidget(MegaUI.lever(L, 24, 40, 140, () -> (m.heightPct() - HoopPhysics.HEIGHT_MIN) /
                (double) (HoopPhysics.HEIGHT_MAX - HoopPhysics.HEIGHT_MIN),
                c -> MegaScreen.act(m, "rise", c), c -> MegaScreen.act(m, "dive", c), "gtna.ui.o.sh_lever"));
        root.addWidget(MegaUI.paint(44, 26, 196, 112, (g, x, y) -> display(g, m, x, y)));
        root.addWidget(MegaUI.paint(44, 146, 196, 50, (g, x, y) -> chart(g, m, x, y)));
        root.addWidget(MegaUI.paint(250, 36, 90, 124, (g, x, y) -> side(g, m, x, y)));
        root.addWidget(MegaUI.paint(250, 160, 82, 18, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 5, 76,
                ticker(m), 0xFFFFC0CC)));
        root.addWidget(MegaUI.button(L, 250, 184, 40, 14, "gtna.ui.o.sh_manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 292, 184, 40, 14, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 40, 24, 260, 176, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(StellarHoopMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(StellarHoopMachine m) {
        if (m.recipe().isEmpty())
            return MegaScreen.tr(P, "idle." + (m.blocked().isEmpty() ? "no_recipe" : m.blocked()));
        return m.recipe().substring(m.recipe().indexOf(':') + 1) + "  ·  " +
                MegaScreen.tr(P, "crafts", String.format("%.0f", v(m, 5)), sci(v(m, 6))) + "  ·  " +
                MegaScreen.tr(P, "total", m.crafted());
    }

    /** The corona's profile: heat in colour, density in brightness, the dive height marked. */
    @OnlyIn(Dist.CLIENT)
    private static void corona(GuiGraphics g, StellarHoopMachine m, int x, int y) {
        for (int k = 0; k < 138; k++) {
            int h = HoopPhysics.HEIGHT_MAX - (int) Math.round(k / 137.0 * (HoopPhysics.HEIGHT_MAX -
                    HoopPhysics.HEIGHT_MIN));
            double heat = (HoopPhysics.coronaKelvin(h) - 1e4) / 2e6;
            double dens = Math.exp(-(h - HoopPhysics.HEIGHT_MIN) / 10.0);
            int c = MegaArt.rgb(new float[] { 1f, (float) (0.3 + 0.5 * heat), (float) (0.2 + 0.6 * heat) },
                    0.25 + 0.75 * dens);
            g.fill(x + 1, y + 1 + k, x + 9, y + 2 + k, c);
        }
        int my = y + 1 + (int) Math.round((HoopPhysics.HEIGHT_MAX - m.heightPct()) /
                (double) (HoopPhysics.HEIGHT_MAX - HoopPhysics.HEIGHT_MIN) * 137);
        g.fill(x - 1, my, x + 11, my + 1, 0xFFFFFFFF);
    }

    @OnlyIn(Dist.CLIENT)
    private static void display(GuiGraphics g, StellarHoopMachine m, int x, int y) {
        if (!m.isFormed()) {
            centre(g, net.minecraft.network.chat.Component.translatable("gtceu.multiblock.invalid_structure")
                    .getString(), x + 98, y + 52, RED);
            return;
        }
        double kelvin = v(m, 8) > 0 ? v(m, 8) : 5800;
        boolean on = v(m, 7) > 0;
        double t = MegaArt.time();
        // A big limb of the star from below-left; the hoop's ellipse grazes it at the dive point.
        int sx = x + 30, sy = y + 120, r = 70;
        g.enableScissor(x, y, x + 196, y + 112);
        MegaArt.star(g, sx, sy, r, kelvin);
        g.disableScissor();
        int hx = x + 128, hy = y + 50, rx = 64, ry = 22;
        double tilt = -0.35;
        for (int k = 0; k < 420; k++) {
            double a = 2 * Math.PI * k / 420;
            double ex = Math.cos(a) * rx, ey = Math.sin(a) * ry;
            int px = hx + (int) Math.round(ex * Math.cos(tilt) - ey * Math.sin(tilt));
            int py = hy + (int) Math.round(ex * Math.sin(tilt) + ey * Math.cos(tilt));
            double near = Math.max(0, -Math.cos(a));
            int c = on ? dim(0xFFFF8A60, 0.3 + 0.7 * Math.pow(near, 5)) : 0xFF5A4650;
            px(g, px, py, c);
            px(g, px, py + 1, dim(c, 0.6));
            if (m.phases() >= 2 && (k + (int) (t * 30)) % 35 == 0) g.fill(px - 2, py - 3, px + 2, py, 0xFFC79A54);
        }
        if (on) {
            int dx = hx - (int) Math.round(rx * Math.cos(tilt)), dy = hy - (int) Math.round(rx * Math.sin(tilt));
            glow(g, dx, dy, 10, 0xFFFFB070, 0.9);
            for (int k = 0; k < 3; k++) MegaArt.beam(g, dx - 18 + k * 4, dy + 22, dx, dy, 0xFFFFB070);
        }
        text(g, MegaScreen.tr(P, "height", m.heightPct()), x + 4, y + 4, L.glow(), true);
        right(g, String.format(Locale.ROOT, "%.2f MK", v(m, 1) / 1e6), x + 192, y + 4, 0xFFFFC0A0);
        if (!m.inPlace()) centre(g, MegaScreen.tr(P, "no_star"), x + 98, y + 30, RED);
    }

    /** Parallel and shield load against the dive height, the current height marked. */
    @OnlyIn(Dist.CLIENT)
    private static void chart(GuiGraphics g, StellarHoopMachine m, int x, int y) {
        int gx = x + 6, gy = y + 42, gw = 184, gh = 36;
        int phases = Math.max(1, m.phases());
        double maxP = HoopPhysics.parallel(phases, HoopPhysics.HEIGHT_MIN), maxQ = HoopPhysics.load(phases,
                HoopPhysics.HEIGHT_MIN);
        int[] pp = null, pq = null;
        for (int h = HoopPhysics.HEIGHT_MIN; h <= HoopPhysics.HEIGHT_MAX; h++) {
            int px = gx + (int) Math.round((h - HoopPhysics.HEIGHT_MIN) / 28.0 * gw);
            int[] p = { px, gy - (int) Math.round(HoopPhysics.parallel(phases, h) / maxP * gh) };
            int[] q = { px, gy - (int) Math.round(HoopPhysics.load(phases, h) / maxQ * gh) };
            if (pp != null) {
                line(g, pp[0], pp[1], p[0], p[1], L.accent());
                line(g, pq[0], pq[1], q[0], q[1], 0xFF7FD8FF);
            }
            pp = p;
            pq = q;
        }
        int cx = gx + (int) Math.round((m.heightPct() - HoopPhysics.HEIGHT_MIN) / 28.0 * gw);
        for (int yy = gy - gh; yy < gy; yy += 3) px(g, cx, yy, 0xFFFFFFFF);
        text(g, MegaScreen.tr(P, "chart.parallel"), gx + 100, y + 3, L.accent(), false);
        text(g, MegaScreen.tr(P, "chart.load"), gx + 100, y + 13, 0xFF7FD8FF, false);
    }

    @OnlyIn(Dist.CLIENT)
    private static void side(GuiGraphics g, StellarHoopMachine m, int x, int y) {
        int tier = (int) v(m, 4);
        MegaUI.tank(g, L, x + 7, y + 5, 24, 110, v(m, 3), COOLANT[Math.max(0, Math.min(3, tier))]);
        centre(g, MegaScreen.tr(P, "coolant." + tier), x + 19, y + 116, L.dim());
        MegaUI.ring(g, L, x + 62, y + 30, 15, m.integrity() / 1000.0, m.integrity() > 300 ? GREEN : RED,
                MegaScreen.tr(P, "shields"), String.format("%.0f%%", m.integrity() / 10.0));
        centre(g, MegaScreen.tr(P, "parallel"), x + 62, y + 60, L.dim());
        String par = String.format("%d", (int) v(m, 0));
        MegaUI.digits(g, x + 62 - par.length() * 7 / 2, y + 71, par, L.accent());
        centre(g, MegaScreen.tr(P, "load.short"), x + 62, y + 88, L.dim());
        String load = String.format("%.0f", v(m, 2));
        MegaUI.digits(g, x + 62 - load.length() * 7 / 2, y + 99, load, 0xFF7FD8FF);
    }
}
