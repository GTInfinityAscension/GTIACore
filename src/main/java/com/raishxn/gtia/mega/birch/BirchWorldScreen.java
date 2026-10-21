package com.raishxn.gtia.mega.birch;

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
 * Birch World screen, a lemniscate: on the left the causal debt against the credit the shells hold; on the right the
 * time loop's clock running backwards round the repayment dial; the hosted work and the paradoxes below.
 */
public final class BirchWorldScreen {

    static final String P = "gtia.birch_world";
    static final Look L = new Look("birch_world", 0xFF7FA8FF, 0xFFE0EAFF, 0xFF0A0E1C, 0xFF2A3A66, 0xFF98A8C8,
            MegaUI.Shape.CAPSULE);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4;

    private BirchWorldScreen() {}

    public static ModularUI create(BirchWorldMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 320, 188, 122, 17);
        root.addWidget(MegaUI.paint(42, 40, 96, 96, (g, x, y) -> debtGauge(g, m, x + 48, y + 48)));
        root.addWidget(MegaUI.paint(182, 40, 96, 96, (g, x, y) -> loopClock(g, m, x + 48, y + 48)));
        root.addWidget(MegaUI.knob(L, 230, 88, 13, m::repay, BirchPhysics.REPAY_MAX + 1,
                () -> new String[] { "0", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "16" },
                c -> MegaScreen.act(m, c.button == 1 ? "less" : "more", c), P + ".ui.repay.tip"));
        root.addWidget(MegaUI.paint(40, 148, 100, 12, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 2, 94, ticker(m),
                0xFFD0DCFF)));
        root.addWidget(MegaUI.paint(180, 148, 100, 12, (g, x, y) -> centre(g, MegaScreen.tr(P, "hosted",
                (int) v(m, 0), (int) v(m, 1)), x + 50, y + 2, L.dim())));
        root.addWidget(MegaUI.button(L, 64, 166, 52, 12, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 204, 166, 52, 12, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 40, 32, 240, 128, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(BirchWorldMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(BirchWorldMachine m) {
        if (v(m, 9) > 0) return MegaScreen.tr(P, "frozen", (int) v(m, 9));
        if (m.recipe().isEmpty())
            return MegaScreen.tr(P, "idle." + (m.blocked().isEmpty() ? "no_recipe" : m.blocked()));
        return m.recipe().substring(m.recipe().indexOf(':') + 1) + "  ·  " +
                MegaScreen.tr(P, "crafts", String.format("%.0f", v(m, 6)), sci(v(m, 7)));
    }

    /** Debt against credit as an arc that reddens toward the paradox. */
    @OnlyIn(Dist.CLIENT)
    private static void debtGauge(GuiGraphics g, BirchWorldMachine m, int cx, int cy) {
        double share = Math.min(1, v(m, 3) / Math.max(1, v(m, 4)));
        int steps = 160;
        for (int k = 0; k < steps; k++) {
            double a = -Math.PI * 1.25 + 1.5 * Math.PI * k / steps;
            boolean lit = (double) k / steps < share;
            double f = (double) k / steps;
            int colour = !lit ? 0xFF1A2236 : f > 0.85 ? 0xFFFF4A4A : f > 0.6 ? 0xFFFFB050 : L.accent();
            int x = cx + (int) Math.round(Math.cos(a) * 42), y = cy + (int) Math.round(Math.sin(a) * 42);
            g.fill(x - 1, y - 1, x + 2, y + 2, colour);
        }
        centre(g, MegaScreen.tr(P, "debt"), cx, cy - 22, L.dim());
        centre(g, sci(v(m, 3)), cx, cy - 11, share > 0.85 ? 0xFFFF6A6A : L.glow());
        centre(g, MegaScreen.tr(P, "credit", sci(v(m, 4))), cx, cy + 4, L.dim());
        centre(g, MegaScreen.tr(P, "paradoxes", m.paradoxes()), cx, cy + 16, m.paradoxes() > 0 ? 0xFFFF9A9A : L.dim());
        centre(g, MegaScreen.tr(P, "era." + (int) v(m, 10)), cx, cy + 28, L.accent());
    }

    /** The loop's clock: twelve marks, the hand running backwards while work goes round the loop. */
    @OnlyIn(Dist.CLIENT)
    private static void loopClock(GuiGraphics g, BirchWorldMachine m, int cx, int cy) {
        for (int k = 0; k < 12; k++) {
            double a = 2 * Math.PI * k / 12;
            int x = cx + (int) Math.round(Math.cos(a) * 40), y = cy + (int) Math.round(Math.sin(a) * 40);
            g.fill(x - 1, y - 1, x + 2, y + 2, L.dim());
        }
        double t = MegaArt.time() * (v(m, 8) > 0 ? -1.6 : -0.1);
        int hx = cx + (int) Math.round(Math.cos(t - Math.PI / 2) * 34), hy = cy + (int) Math.round(Math.sin(t -
                Math.PI / 2) * 34);
        line(g, cx, cy, hx, hy, L.glow());
        glow(g, hx, hy, 4, L.accent(), 0.7);
        centre(g, MegaScreen.tr(P, "repay"), cx, cy - 30, L.dim());
        centre(g, MegaScreen.tr(P, "per_second", sci(BirchPhysics.repayment(m.repay()))), cx, cy + 20, L.glow());
    }
}
