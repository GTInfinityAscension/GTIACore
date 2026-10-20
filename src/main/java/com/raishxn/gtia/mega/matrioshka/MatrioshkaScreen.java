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

    /** The Stellar Chamber's art, in the brain's amber. */
    private static final Theme T = new Theme("stellar", 0xFFFFB347, 0xFFFFE6B0, 0xFF0A0E1C, 0xFF3A548C, 0xFF8498C4);
    /** Ring tilts of the display, one per phase (the build's ±15°, 0°, 90°, ±30°, ±4°). */
    private static final double[] TILT = { 15, 0, 90, 30, 4 };
    private static final int MANUAL_LINES = 14;

    private MatrioshkaScreen() {}

    public static ModularUI create(MatrioshkaBrainMachine m, Player player) {
        Nav nav = new Nav();
        var root = window(T, m, player, (g, x, y) -> display(g, m, x, y), (g, x, y) -> {
            chip(g, T, x, y, 52, tr("star"), m.inNexusSystem() ? 0xFFFFD27A : RED);
            chip(g, T, x + 56, y, 52, tr("cool"), m.coolant() > 0.99 ? GREEN : m.coolant() > 0 ? YELLOW : T.dim());
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

    private static boolean hot(MatrioshkaBrainMachine m, int phase) {
        double[] v = m.view();
        return v.length > 5 && (((int) v[5]) & (1 << (phase - 1))) != 0;
    }

    /** Blue at 200 K, amber at 400 K, red at the limit. */
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
            int colour = !built ? 0xFF2A3044 :
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
        double[] v = m.view();
        double demand = v.length > 7 ? v[7] : 0;
        bar(g, T, x0, y + 33, w, tr("coolant"), String.format("%.0f%% · %.0f mB/s", m.coolant() * 100, demand),
                m.coolant(), 0xFF7ED8FF);
        double hottest = 0;
        for (int p = 1; p <= m.phases(); p++) hottest = Math.max(hottest, temp(m, p));
        bar(g, T, x0, y + 50, w, tr("hottest"), String.format("%.0f / %.0f K", hottest, MatrioshkaShells.T_LIMIT),
                hottest / MatrioshkaShells.T_LIMIT, heat(hottest));
        text(g, tr("psi", sci(m.psi())), x0, y + 70, VIOLET, false);
        text(g, tr("psi_rate", sci(v.length > 6 ? v[6] : 0)), x0, y + 81, T.dim(), false);
        int overheated = v.length > 5 ? (int) v[5] : 0;
        if (overheated != 0) text(g, tr("overheat_warning", Integer.bitCount(overheated)), x0, y + 94, RED, false);
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
            int[] col = { 10, 58, 118, 170, 240 };
            String[] head = { "col_shell", "col_radius", "col_temp", "col_cwu", "col_state" };
            for (int c = 0; c < col.length; c++) text(g, tr(head[c]), x + col[c], y + 20, T.accent(), false);
            g.fill(x + 8, y + 30, x + 284, y + 31, T.line());
            var shape = MatrioshkaShape.get();
            for (int p = 1; p <= MatrioshkaShells.PHASES; p++) {
                int ry = y + 22 + p * 14;
                boolean built = p <= m.phases();
                double r = shape == null ? 0 : shape.radius[p - 1];
                double t = built ? temp(m, p) : MatrioshkaShells.temperature(r, m.clockPct(), m.coolant());
                long cwu = MatrioshkaShells.shellCwut(p, r, m.clockPct(), m.coolant());
                int c = built ? 0xFFE0E0E4 : T.dim();
                text(g, String.valueOf(p), x + col[0] + 4, ry, c, false);
                text(g, String.format("%.0f", r), x + col[1], ry, c, false);
                text(g, String.format("%.0f K", t), x + col[2], ry, heat(t), false);
                text(g, built ? sci(cwu) : "-", x + col[3], ry, c, false);
                String state = !built ? tr("not_built") : MatrioshkaShells.overheated(t) ? tr("overheat") : tr("ok");
                text(g, state, x + col[4], ry, !built ? T.dim() : MatrioshkaShells.overheated(t) ? RED : GREEN, false);
            }
            text(g, tr("shells_hint"), x + 10, y + 118, T.dim(), false);
        });
    }

    /** Manual lines per page: two pages of seven. */
    private static final int PAGE_LINES = 7;

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
