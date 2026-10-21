package com.raishxn.gtia.mega.qso;

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

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Quasi-Stellar Obliterator screen, shaped like the gun: the sight with the quasar and the capacitor filling round it;
 * the charge against the shot and the ceiling; the draw and the shot size; the negentropy against the world's stock.
 */
public final class QsoScreen {

    static final String P = "gtia.qso";
    static final Look L = new Look("qso", 0xFF8FD0FF, 0xFFE4F4FF, 0xFF0A1018, 0xFF2A4A66, 0xFF98B4C8,
            MegaUI.Shape.CHEVRON);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4;

    private QsoScreen() {}

    public static ModularUI create(QsoMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 340, 176, 176, 33);
        root.addWidget(MegaUI.paint(28, 30, 116, 116, (g, x, y) -> sight(g, m, x + 58, y + 58)));
        root.addWidget(MegaUI.paint(160, 52, 96, 14, (g, x, y) -> chargeBar(g, m, x, y)));
        root.addWidget(MegaUI.paint(160, 72, 40, 26, (g, x, y) -> {
            text(g, MegaScreen.tr(P, "draw", m.draw()), x + 2, y + 2, L.dim(), false);
            text(g, MegaScreen.tr(P, "size", m.size()), x + 2, y + 15, L.dim(), false);
        }));
        root.addWidget(MegaUI.button(L, 204, 72, 24, 11, P + ".ui.minus", () -> false,
                c -> MegaScreen.act(m, "draw_down", c)));
        root.addWidget(MegaUI.button(L, 230, 72, 24, 11, P + ".ui.plus", () -> false,
                c -> MegaScreen.act(m, "draw_up", c)));
        root.addWidget(MegaUI.button(L, 204, 86, 24, 11, P + ".ui.minus", () -> false,
                c -> MegaScreen.act(m, "size_down", c)));
        root.addWidget(MegaUI.button(L, 230, 86, 24, 11, P + ".ui.plus", () -> false,
                c -> MegaScreen.act(m, "size_up", c)));
        root.addWidget(MegaUI.button(L, 160, 100, 46, 11, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 210, 100, 46, 11, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.paint(262, 58, 70, 56, (g, x, y) -> negentropy(g, m, x, y)));
        root.addWidget(MegaUI.paint(160, 118, 120, 12, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 2, 114, ticker(m),
                0xFFD8F0FF)));
        root.addWidget(MegaUI.manual(L, 24, 26, 300, 120, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(QsoMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(QsoMachine m) {
        if (m.ended()) return MegaScreen.tr(P, "ended");
        if (v(m, 5) <= 0) return MegaScreen.tr(P, "spent");
        return MegaScreen.tr(P, "status", m.shots(), sci(v(m, 3)));
    }

    /** The sight: the quasar and its disk, the capacitor's charge as a ring closing round it, crosshairs. */
    @OnlyIn(Dist.CLIENT)
    private static void sight(GuiGraphics g, QsoMachine m, int cx, int cy) {
        MegaArt.blackHole(g, cx, cy, 12, 9000);
        double f = Math.min(1, v(m, 0) / Math.max(1, v(m, 1)));
        int steps = 180;
        for (int k = 0; k < steps; k++) {
            double a = -Math.PI / 2 + 2 * Math.PI * k / steps;
            int x = cx + (int) Math.round(Math.cos(a) * 50), y = cy + (int) Math.round(Math.sin(a) * 50);
            g.fill(x - 1, y - 1, x + 2, y + 2, (double) k / steps < f ? L.accent() : 0xFF16222E);
        }
        for (int s : new int[] { -1, 1 }) {
            g.fill(cx + s * 22, cy, cx + s * 44, cy + 1, L.dim());
            g.fill(cx, cy + s * 22, cx + 1, cy + s * 44, L.dim());
        }
        centre(g, MegaScreen.tr(P, "hole", String.format("%.1f", v(m, 7))), cx, cy + 30, L.dim());
    }

    /** Charge against the shot; the dashed line marks the ceiling the input allows. */
    @OnlyIn(Dist.CLIENT)
    private static void chargeBar(GuiGraphics g, QsoMachine m, int x, int y) {
        double shot = Math.max(1, v(m, 1));
        double f = Math.min(1, v(m, 0) / shot), ceil = Math.min(1.2, v(m, 2) / shot);
        g.fill(x + 2, y + 3, x + 94, y + 11, 0xFF16222E);
        g.fill(x + 2, y + 3, x + 2 + (int) Math.round(92 * f), y + 11, ceil < 1 ? 0xFFFF7A5A : L.accent());
        int cxl = x + 2 + (int) Math.round(92 * Math.min(1, ceil));
        for (int yy = y + 2; yy < y + 12; yy += 3) g.fill(cxl, yy, cxl + 1, yy + 2, 0xFFFFFFFF);
    }

    @OnlyIn(Dist.CLIENT)
    private static void negentropy(GuiGraphics g, QsoMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "negentropy"), x + 2, y + 2, L.accent(), false);
        text(g, sci(v(m, 4)), x + 2, y + 13, L.glow(), false);
        text(g, MegaScreen.tr(P, "stock"), x + 2, y + 27, L.dim(), false);
        text(g, String.format("%.0f / %.0f", v(m, 5), v(m, 6)), x + 2, y + 38, v(m, 5) <= 0 ? RED : L.dim(), false);
    }
}
