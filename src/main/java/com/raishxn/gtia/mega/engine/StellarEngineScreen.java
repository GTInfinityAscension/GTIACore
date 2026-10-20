package com.raishxn.gtia.mega.engine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtia.mega.engine.EnginePhysics.Region;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Theme;

import java.util.Locale;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Stellar Engine screen, on the GTNA originals' window in its own plasma theme: the engine seen from the side with
 * its jet, thrust, Δv to the next region, power and cage tension; a galaxy map with the route and the regions; a
 * field manual with the formulas.
 */
public final class StellarEngineScreen {

    private static final Theme T = new Theme("gtia_engine", 0xFF7FD8FF, 0xFFE4F6FF, 0xFF0F1418, 0xFF3A5A6E,
            0xFF98AEBC);
    private static final int PLASMA = 0xFFBFE6FF, MANUAL_LINES = 16, PAGE_LINES = 4;

    private StellarEngineScreen() {}

    public static ModularUI create(StellarEngineMachine m, Player player) {
        Nav nav = new Nav();
        var root = window(T, m, player, (g, x, y) -> display(g, m, x, y), (g, x, y) -> {
            chip(g, T, x, y, 52, tr("star"), m.inPlace() ? 0xFFFFD27A : RED);
            chip(g, T, x + 56, y, 52, tr("caplan"), v(m, 6) > 0 ? GREEN : m.caplanOn() ? YELLOW : T.dim());
        });
        root.addWidget(gridButton(T, 0, "minus", "eng_beta_down", () -> false, c -> act(m, "beta_down", c)));
        root.addWidget(gridButton(T, 1, "plus", "eng_beta_up", () -> false, c -> act(m, "beta_up", c)));
        root.addWidget(gridButton(T, 2, "bolt", "eng_caplan", m::caplanOn, c -> act(m, "caplan", c)));
        root.addWidget(gridButton(T, 3, "orbit", "eng_map", () -> "map".equals(nav.open), c -> nav.toggle("map")));
        root.addWidget(gridButton(T, 6, "info", "eng_manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(powerButton(T, 7, m));
        root.addWidget(map(m, nav));
        root.addWidget(manual(nav));
        return ui(root, m, player);
    }

    private static void act(StellarEngineMachine m, String data, ClickData click) {
        if (!click.isRemote) m.handleDisplayClick(data, click);
    }

    static String tr(String key, Object... args) {
        return Component.translatable("gtia.stellar_engine.ui." + key, args).getString();
    }

    static String region(Region r) {
        return tr("region." + r.name().toLowerCase(Locale.ROOT));
    }

    private static double v(StellarEngineMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    // ------------------------------------------------------------------ display

    @OnlyIn(Dist.CLIENT)
    private static void display(GuiGraphics g, StellarEngineMachine m, int x, int y) {
        double time = (net.minecraft.Util.getMillis() % 3_600_000L) / 1000.0;
        int phases = m.phases();
        int sx = x + 40, sy = y + 54;
        // The cage: a teardrop from the mirror around the star to the engine collar.
        int cage = 0xFF5A7184;
        for (int k = 0; k < 4; k++) {
            int dy = 10 + k * 7;
            for (int t = 0; t <= 60; t++) {
                double u = t / 60.0;
                int px = (int) Math.round(sx - 16 + u * 64);
                int py = (int) Math.round(dy * Math.sin(Math.PI * Math.pow(u, 0.8)) * 1.6);
                px(g, px, sy - py, cage);
                px(g, px, sy + py, cage);
            }
        }
        boolean lit = m.inPlace();
        glow(g, sx, sy, 18, lit ? 0xFFFF9A2E : 0xFF2A2030, 0.8);
        disc(g, sx, sy, 8, lit ? 0xFFFFF0C0 : 0xFF4A4050);
        if (phases >= 2) for (int t = -22; t <= 22; t++) {
            // The Shkadov mirror: an arc ahead of the star (to the left on screen).
            int px = sx - 22 + (int) Math.round(t * t / 60.0);
            px(g, px, sy + t, 0xFFCFEFFF);
            px(g, px + 1, sy + t, 0xFF6E9AB4);
        }
        if (phases >= 3) {
            double a = time * 0.8;
            ellipse(g, sx, sy, 4, 24, 0xFF4A86A6, 0);
            int my = sy + (int) Math.round(24 * Math.sin(a));
            g.fill(sx - 1, my - 1, sx + 2, my + 2, 0xFF9FD8FF);
        }
        int ex = sx + 50;
        if (phases >= 4) {
            g.fill(ex, sy - 7, ex + 24, sy + 8, 0xFF6A4A8A);
            for (int k = 0; k < 4; k++) g.fill(ex + 3 + k * 6, sy - 7, ex + 4 + k * 6, sy + 8, 0xFFC08A3A);
        }
        if (phases >= 5) for (int t = 0; t < 8; t++) g.fill(ex + 24 + t, sy - 4 - t, ex + 25 + t, sy + 5 + t, 0xFF8A93A8);
        if (v(m, 6) > 0) {
            // The jet: plasma flowing out of the nozzle, the counter-jet into the star.
            for (int k = 0; k < 40; k++) {
                double f = (k + (time * 12) % 1) / 40.0;
                // The jet stays left of the text column (x0 = x + 140).
                int jx = ex + 32 + (int) (f * 12);
                int r = 2 + (int) (f * 4);
                int c = dim(PLASMA, 1 - f * 0.8);
                g.fill(jx, sy - r, jx + 1, sy + r + 1, c);
            }
            for (int t = 0; t < 30; t += 3) px(g, ex - 2 - t - (int) (time * 20) % 3, sy, 0xFFFF9A6A);
        }
        int x0 = x + 140, w = 146;
        Region here = Region.values()[(int) Math.max(0, Math.min(Region.values().length - 1, v(m, 5)))];
        text(g, region(here), x0, y + 4, T.glow(), true);
        right(g, tr("beta", m.betaPct()), x + 286, y + 4, m.betaPct() > EnginePhysics.BETA_SAFE ? YELLOW : T.dim());
        if (!lit) {
            text(g, tr("no_star"), x0, y + 18, RED, false);
            return;
        }
        double max = EnginePhysics.shkadovThrust(true) + EnginePhysics.caplanThrust(
                EnginePhysics.MDOT_MAX * 1.25, EnginePhysics.BETA_MAX);
        bar(g, T, x0, y + 16, w, tr("thrust"), sci(v(m, 0)) + " N", v(m, 0) / max, T.accent());
        Region next = here.next();
        double dv = v(m, 4);
        double frac = next == here ? 1 : (dv - here.dv) / (next.dv - here.dv);
        bar(g, T, x0, y + 33, w, tr("to_next", region(next)), String.format("%.1f / %.0f km/s", dv, next.dv), frac,
                0xFF9FE8A0);
        bar(g, T, x0, y + 50, w, tr("tension"), String.format("%.0f%% · %s %.0f%%", v(m, 7) * 100, tr("cage"),
                m.integrity() / 10.0), v(m, 7), v(m, 7) > 0 ? RED : GREEN);
        text(g, tr("dv_rate", String.format("%.4f", v(m, 3))), x0, y + 70, T.dim(), false);
        if (v(m, 6) > 0) text(g, tr("plasma", String.format("%.0f", v(m, 9)), String.format("%.0f", v(m, 10))), x0,
                y + 81, PLASMA, false);
        else if (m.caplanOn() && phases >= 4) text(g, tr("no_power"), x0, y + 81, YELLOW, false);
        if (m.integrity() <= 0) text(g, tr("cage_broken"), x0, y + 94, RED, false);
    }

    // ------------------------------------------------------------------ galaxy map

    private static WidgetGroup map(StellarEngineMachine m, Nav nav) {
        return panel(T, nav, "map", "eng_map_title", 380, 170, "panel_wide", (g, x, y) -> {
            int cx = x + 110, cy = y + 92;
            double time = (net.minecraft.Util.getMillis() % 3_600_000L) / 1000.0;
            // Two logarithmic spiral arms of dots and a bright core.
            for (int arm = 0; arm < 2; arm++)
                for (int k = 0; k < 220; k++) {
                    double t = k / 220.0 * 3.2 * Math.PI;
                    double r = 6 * Math.exp(0.22 * t);
                    double a = t + arm * Math.PI + time * 0.02;
                    int px = cx + (int) Math.round(r * Math.cos(a) * 1.25);
                    int py = cy + (int) Math.round(r * Math.sin(a) * 0.8);
                    int scatter = (k * 7919) % 5 - 2;
                    px(g, px + scatter, py, k % 3 == 0 ? 0xFF9FB8D0 : 0xFF4A5E74);
                }
            glow(g, cx, cy, 16, 0xFFFFE2A0, 0.8);
            // Route: the regions from the outer arm to the core.
            Region far = Region.values()[(int) Math.max(0, Math.min(Region.values().length - 1, v(m, 5)))];
            double dv = v(m, 4);
            int[][] pos = new int[Region.values().length][];
            for (Region r : Region.values()) {
                double f = r.ordinal() / (double) (Region.values().length - 1);
                double t = (1 - f) * 3.0 * Math.PI;
                double rad = 6 * Math.exp(0.22 * t) * (r == Region.GALACTIC_CORE ? 0 : 1);
                pos[r.ordinal()] = new int[] { cx + (int) Math.round(rad * Math.cos(t) * 1.25),
                        cy + (int) Math.round(rad * Math.sin(t) * 0.8) };
            }
            for (int i = 0; i + 1 < pos.length; i++)
                line(g, pos[i][0], pos[i][1], pos[i + 1][0], pos[i + 1][1],
                        i < far.ordinal() ? GREEN : 0xFF3A5A6E);
            for (Region r : Region.values()) {
                int[] p = pos[r.ordinal()];
                boolean reached = r.reached(far);
                disc(g, p[0], p[1], 2, reached ? GREEN : T.dim());
            }
            // Where the system is now, between the farthest region and the next.
            Region next = far.next();
            double f = next == far ? 0 : Math.max(0, Math.min(1, (dv - far.dv) / (next.dv - far.dv)));
            int[] a = pos[far.ordinal()], b = pos[next.ordinal()];
            int px = (int) Math.round(a[0] + (b[0] - a[0]) * f), py = (int) Math.round(a[1] + (b[1] - a[1]) * f);
            if ((int) (time * 2) % 2 == 0) g.fill(px - 2, py - 2, px + 3, py + 3, 0xFFFFFFFF);
            // Legend: each region, its Δv and what it gives.
            int lx = x + 222, ly = y + 22;
            for (Region r : Region.values()) {
                boolean reached = r.reached(far);
                text(g, region(r), lx, ly, reached ? GREEN : 0xFFE0E0E4, false);
                right(g, String.format("%.0f km/s", r.dv), x + 372, ly, T.dim());
                var lines = net.minecraft.client.Minecraft.getInstance().font.split(
                        Component.literal(tr("bonus." + r.name().toLowerCase(Locale.ROOT))), 150);
                if (!lines.isEmpty())
                    g.drawString(net.minecraft.client.Minecraft.getInstance().font, lines.get(0), lx + 4, ly + 9,
                            T.dim(), false);
                ly += 23;
            }
        });
    }

    private static WidgetGroup manual(Nav nav) {
        int pages = (MANUAL_LINES + PAGE_LINES - 1) / PAGE_LINES;
        var group = panel(T, nav, "manual", "eng_manual_title", 292, 200, "slate", (g, x, y) -> {
            var font = net.minecraft.client.Minecraft.getInstance().font;
            int page = Math.min(nav.page, pages - 1);
            int ly = y + 20;
            for (int i = page * PAGE_LINES; i < Math.min(MANUAL_LINES, (page + 1) * PAGE_LINES); i++) {
                String s = Component.translatable("gtia.stellar_engine.ui.manual." + i).getString();
                boolean formula = s.startsWith("=");
                if (formula) s = s.substring(1).trim();
                var lines = font.split(Component.literal(s), 272);
                if (formula) g.fill(x + 8, ly - 2, x + 284, ly + lines.size() * 10, 0xFF0A1420);
                for (var l : lines) {
                    if (ly > y + 176) break;
                    g.drawString(font, l, x + 10, ly, formula ? 0xFF9FE8FF : 0xFFE0E0E4, false);
                    ly += 10;
                }
                ly += 3;
            }
            centre(g, "< " + (page + 1) + " / " + pages + " >", x + 146, y + 186, T.accent());
        });
        group.addWidget(hit(110, 182, 22, 14, c -> nav.page = Math.max(0, nav.page - 1)));
        group.addWidget(hit(160, 182, 22, 14, c -> nav.page = Math.min(pages - 1, nav.page + 1)));
        return group;
    }
}
