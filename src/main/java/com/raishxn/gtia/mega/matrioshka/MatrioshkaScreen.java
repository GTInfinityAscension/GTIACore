package com.raishxn.gtia.mega.matrioshka;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Theme;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Matrioshka Brain screen, on the GTNA originals' window: the star with its five shells coloured by temperature,
 * CWU, coolant, hottest shell and Ψ; clock buttons; a shell table and a field manual with the formulas.
 */
public final class MatrioshkaScreen {

    /**
     * Its own art (assets/gtna/textures/gui/originals_v2/gtia_matrioshka, the GTNA kit's folder): dark warm grey with
     * amber accents and circuit traces on the display.
     */
    private static final Theme T = new Theme("gtia_matrioshka", 0xFFFFB347, 0xFFFFE6B0, 0xFF16130F, 0xFF5A4A34,
            0xFFB0A088);
    /** Ring tilts of the display, one per phase (the build's ±15°, 0°, 90°, ±30°, ±4°). */
    private static final double[] TILT = { 15, 0, 90, 30, 4 };
    private static final int MANUAL_LINES = 20;

    private MatrioshkaScreen() {}

    public static ModularUI create(MatrioshkaBrainMachine m, Player player) {
        Nav nav = new Nav();
        var root = window(T, m, player, (g, x, y) -> display(g, m, x, y), (g, x, y) -> {
            chip(g, T, x, y, 52, tr("star"), m.inNexusSystem() ? 0xFFFFD27A : RED);
            var tier = m.coolantTier();
            chip(g, T, x + 56, y, 52, tier == MatrioshkaShells.Coolant.NONE ? tr("cool") : tr("coolant_short." +
                    tier.name().toLowerCase(java.util.Locale.ROOT)),
                    tier == MatrioshkaShells.Coolant.NONE ? T.dim() : m.coolant() > 0.99 ? GREEN : YELLOW);
        });
        root.addWidget(gridButton(T, 0, "minus", "mb_clock_down", () -> false, c -> act(m, "clock_down", c)));
        root.addWidget(gridButton(T, 1, "plus", "mb_clock_up", () -> false, c -> act(m, "clock_up", c)));
        root.addWidget(gridButton(T, 2, "chart", "mb_shells", () -> "shells".equals(nav.open),
                c -> nav.toggle("shells")));
        root.addWidget(gridButton(T, 6, "info", "mb_manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(powerButton(T, 7, m));
        root.addWidget(shells(m, nav));
        root.addWidget(manual(nav));
        return ui(root, m, player);
    }

    private static void act(MatrioshkaBrainMachine m, String data, ClickData click) {
        if (!click.isRemote) m.handleDisplayClick(data, click);
    }

    static String tr(String key, Object... args) {
        return Component.translatable("gtia.matrioshka.ui." + key, args).getString();
    }

    private static double temp(MatrioshkaBrainMachine m, int phase) {
        double[] v = m.view();
        return phase - 1 < v.length ? v[phase - 1] : 0;
    }

    private static double v(MatrioshkaBrainMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static boolean hot(MatrioshkaBrainMachine m, int phase) {
        return (((int) v(m, 5)) & (1 << (phase - 1))) != 0;
    }

    private static boolean fractured(MatrioshkaBrainMachine m, int phase) {
        return (((int) v(m, 6)) & (1 << (phase - 1))) != 0;
    }

    private static double limit(MatrioshkaBrainMachine m) {
        double l = v(m, 14);
        return l > 0 ? l : MatrioshkaShells.T_LIMIT;
    }

    /** Blue at 200 K, amber halfway, red at the limit. */
    static int heat(double t) {
        double f = Math.max(0, Math.min(1, (t - 200) / (MatrioshkaShells.T_LIMIT - 200)));
        int r = (int) (80 + 175 * f), gr = (int) (170 + 40 * Math.sin(Math.PI * f) - 120 * f),
                b = (int) (255 * (1 - f));
        return 0xFF000000 | r << 16 | Math.max(0, gr) << 8 | Math.max(0, b);
    }

    // ------------------------------------------------------------------ display

    @OnlyIn(Dist.CLIENT)
    private static void display(GuiGraphics g, MatrioshkaBrainMachine m, int x, int y) {
        int cx = x + 58, cy = y + 54;
        double time = (net.minecraft.Util.getMillis() % 3_600_000L) / 1000.0;
        boolean star = m.inNexusSystem();
        glow(g, cx, cy, 20, star ? 0xFFFF9A2E : 0xFF2A2030, 0.8);
        glow(g, cx, cy, 12, star ? 0xFFFFD27A : 0xFF3A3040, 0.9);
        disc(g, cx, cy, 6, star ? 0xFFFFF0C0 : 0xFF4A4050);
        for (int p = 1; p <= MatrioshkaShells.PHASES; p++) {
            boolean built = p <= m.phases();
            int r = 12 + p * 8;
            int colour = !built ? 0xFF2A3044 : fractured(m, p) ? 0xFF5A1A1A :
                    hot(m, p) && (int) (time * 3) % 2 == 0 ? RED : heat(temp(m, p));
            double spin = m.isRunning() ? time * (0.25 + 0.12 * p) * (p % 2 == 0 ? -1 : 1) : 0;
            ring(g, cx, cy, r, TILT[p - 1], spin, colour, built ? 0 : 3);
        }
        int x0 = x + 122, w = 164;
        text(g, tr("phases", m.phases(), MatrioshkaShells.PHASES), x0, y + 4, T.glow(), true);
        right(g, tr("clock", m.clockPct()), x + 286, y + 4, m.clockPct() > 100 ? YELLOW : T.dim());
        if (!star) {
            text(g, tr("no_star"), x0, y + 18, RED, false);
            return;
        }
        double max = m.maxCwut();
        bar(g, T, x0, y + 16, w, tr("cwu"), sci(m.usedCwut()) + " / " + sci(max),
                max > 0 ? m.usedCwut() / max : 0, T.accent());
        var tier = m.coolantTier();
        String coolantLabel = tier == MatrioshkaShells.Coolant.NONE ? tr("coolant") :
                tr("coolant_name." + tier.name().toLowerCase(java.util.Locale.ROOT));
        bar(g, T, x0, y + 33, w, coolantLabel, String.format("%.0f%% · %.0f mB/s", m.coolant() * 100, v(m, 8)),
                m.coolant(), 0xFF7ED8FF);
        double hottest = 0;
        for (int p = 1; p <= m.phases(); p++) hottest = Math.max(hottest, temp(m, p));
        double lim = limit(m);
        bar(g, T, x0, y + 50, w, tr("hottest"), String.format("%.0f / %.0f K", hottest, lim), hottest / lim,
                heat(hottest * MatrioshkaShells.T_LIMIT / lim));
        text(g, tr("psi", sci(m.psi())), x0, y + 70, VIOLET, false);
        text(g, tr("psi_rate", sci(v(m, 7))), x0, y + 81, T.dim(), false);
        int broken = (int) v(m, 6), overheated = (int) v(m, 5);
        if (broken != 0) text(g, tr("fracture_warning", Integer.bitCount(broken)), x0, y + 94, RED, false);
        else if (overheated != 0)
            text(g, tr("overheat_warning", Integer.bitCount(overheated)), x0, y + 94, RED, false);
    }

    /** A ring seen from the side: an ellipse tilted by {@code tilt}, with a marker turning along it. */
    @OnlyIn(Dist.CLIENT)
    private static void ring(GuiGraphics g, int cx, int cy, int r, double tilt, double spin, int colour, int dotted) {
        double t = Math.toRadians(tilt);
        double ry = r * 0.32;
        int n = (int) (2 * Math.PI * r * 1.4);
        for (int k = 0; k < n; k++) {
            if (dotted > 0 && (k / dotted) % 2 == 1) continue;
            double a = 2 * Math.PI * k / n;
            double ex = r * Math.cos(a), ey = ry * Math.sin(a);
            px(g, (int) Math.round(cx + ex * Math.cos(t) - ey * Math.sin(t)),
                    (int) Math.round(cy + ex * Math.sin(t) + ey * Math.cos(t)), colour);
        }
        if (dotted > 0) return;
        double ex = r * Math.cos(spin), ey = ry * Math.sin(spin);
        int mx = (int) Math.round(cx + ex * Math.cos(t) - ey * Math.sin(t));
        int my = (int) Math.round(cy + ex * Math.sin(t) + ey * Math.cos(t));
        g.fill(mx - 1, my - 1, mx + 2, my + 2, lighter(colour));
    }

    // ------------------------------------------------------------------ panels

    private static WidgetGroup shells(MatrioshkaBrainMachine m, Nav nav) {
        return panel(T, nav, "shells", "mb_shells_title", (g, x, y) -> {
            int[] col = { 10, 50, 98, 150, 200, 240 };
            String[] head = { "col_shell", "col_radius", "col_temp", "col_cwu", "col_integrity", "col_state" };
            for (int c = 0; c < col.length; c++) text(g, tr(head[c]), x + col[c], y + 20, T.accent(), false);
            g.fill(x + 8, y + 30, x + 284, y + 31, T.line());
            var shape = MatrioshkaShape.get();
            double lim = limit(m);
            for (int p = 1; p <= MatrioshkaShells.PHASES; p++) {
                int ry = y + 22 + p * 14;
                boolean built = p <= m.phases();
                double r = shape == null ? 0 : shape.radius[p - 1];
                double t = built ? temp(m, p) : MatrioshkaShells.target(r, m.clockPct(), m.coolantTier(), m.coolant());
                boolean off = built && (hot(m, p) || fractured(m, p));
                long cwu = off ? 0 : MatrioshkaShells.shellCwut(p, t, m.clockPct());
                int c = built ? 0xFFE0E0E4 : T.dim();
                text(g, String.valueOf(p), x + col[0] + 4, ry, c, false);
                text(g, String.format("%.0f", r), x + col[1], ry, c, false);
                text(g, String.format("%.0f K", t), x + col[2], ry, heat(t * MatrioshkaShells.T_LIMIT / lim), false);
                text(g, built ? sci(cwu) : "-", x + col[3], ry, c, false);
                double integ = v(m, 8 + p) / 10;
                text(g, built ? String.format("%.0f%%", integ) : "-", x + col[4], ry,
                        !built ? T.dim() : integ < 25 ? RED : integ < 75 ? YELLOW : GREEN, false);
                String state = !built ? tr("not_built") : fractured(m, p) ? tr("fractured") :
                        hot(m, p) ? tr("overheat") : tr("ok");
                text(g, state, x + col[5], ry, !built ? T.dim() : off ? RED : GREEN, false);
            }
            text(g, tr("shells_hint"), x + 10, y + 112, T.dim(), false);
            text(g, tr("limit_hint", String.format("%.0f", lim)), x + 10, y + 124, T.dim(), false);
        });
    }

    /** Manual lines per page: four pages of five. */
    private static final int PAGE_LINES = 5;

    private static WidgetGroup manual(Nav nav) {
        int pages = (MANUAL_LINES + PAGE_LINES - 1) / PAGE_LINES;
        var group = panel(T, nav, "manual", "mb_manual_title", 292, 200, "slate", (g, x, y) -> {
            var font = net.minecraft.client.Minecraft.getInstance().font;
            int page = Math.min(nav.page, pages - 1);
            int ly = y + 20;
            for (int i = page * PAGE_LINES; i < Math.min(MANUAL_LINES, (page + 1) * PAGE_LINES); i++) {
                String s = Component.translatable("gtia.matrioshka.ui.manual." + i).getString();
                boolean formula = s.startsWith("=");
                if (formula) s = s.substring(1).trim();
                var lines = font.split(Component.literal(s), 272);
                if (formula) g.fill(x + 8, ly - 2, x + 284, ly + lines.size() * 10, 0xFF0A1A0E);
                for (var l : lines) {
                    if (ly > y + 176) break;
                    g.drawString(font, l, x + 10, ly, formula ? GREEN : 0xFFE0E0E4, false);
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
