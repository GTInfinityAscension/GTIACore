package com.raishxn.gtia.mega.kugelblitz;

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
 * Kugelblitz screen, an hourglass shaped like the silo: the hole and its glow on top; the charge-rate and mass-target
 * switches at the waist; below, log columns of the mass (critical zone in red) and of the Hawking power, the energy
 * held and how long the hole would last unfed.
 */
public final class KugelblitzScreen {

    static final String P = "gtia.kugelblitz";
    static final Look L = new Look("kugelblitz", 0xFFC78CFF, 0xFFF0E0FF, 0xFF160F1C, 0xFF4A2E66, 0xFFA890C0,
            MegaUI.Shape.HEX);
    private static final int MANUAL_LINES = 15, PAGE_LINES = 5;

    private KugelblitzScreen() {}

    public static ModularUI create(KugelblitzMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 236, 236, 74, 8);
        root.addWidget(MegaUI.paint(14, 20, 208, 80, (g, x, y) -> display(g, m, x, y)));
        root.addWidget(MegaUI.knob(L, 86, 121, 12, m::rateIndex, KugelblitzPhysics.RATES.length,
                () -> new String[] { "0", "", "", "", "2⁴⁸" }, c -> MegaScreen.act(m, "rate", c), P + ".ui.rate.tip"));
        root.addWidget(MegaUI.knob(L, 150, 121, 12, m::targetIndex, KugelblitzPhysics.TARGETS.length,
                () -> new String[] { "10³", "", "", "", "", "", "10⁹" },
                c -> MegaScreen.act(m, c.button == 1 ? "target_down" : "target_up", c), P + ".ui.target.tip"));
        root.addWidget(MegaUI.paint(12, 146, 24, 66, (g, x, y) -> massColumn(g, m, x, y)));
        root.addWidget(MegaUI.paint(200, 146, 24, 66, (g, x, y) -> powerColumn(g, m, x, y)));
        root.addWidget(MegaUI.paint(44, 146, 148, 40, (g, x, y) -> readout(g, m, x, y)));
        root.addWidget(MegaUI.paint(44, 212, 148, 14, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 3, 142,
                ticker(m), L.dim())));
        root.addWidget(MegaUI.button(L, 44, 192, 72, 15, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 120, 192, 72, 15, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 8, 20, 220, 190, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(KugelblitzMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(KugelblitzMachine m) {
        if (m.integrity() <= 0) return MegaScreen.tr(P, "breached");
        if (!m.hole()) return MegaScreen.tr(P, "gathering", String.format(Locale.ROOT, "%.3g", m.mass()),
                String.format(Locale.ROOT, "%.3g", KugelblitzPhysics.FORM_MASS));
        double life = KugelblitzPhysics.lifetimeSeconds(m.mass());
        return MegaScreen.tr(P, "life", life > 86400 * 3 ? String.format("%.0f d", life / 86400) :
                life > 3600 ? String.format("%.1f h", life / 3600) : String.format("%.0f s", life)) + "  ·  " +
                MegaScreen.tr(P, "temp", sci(KugelblitzPhysics.hawkingKelvin(m.mass()))) + "  ·  r_s " +
                sci(KugelblitzPhysics.schwarzschildMetres(m.mass())) + " m";
    }

    @OnlyIn(Dist.CLIENT)
    private static void display(GuiGraphics g, KugelblitzMachine m, int x, int y) {
        int cx = x + 104, cy = y + 40;
        if (m.breachSeconds() < 8) {
            glow(g, cx, cy, 40 - m.breachSeconds() * 4, 0xFFFFFFFF, 1.0);
            centre(g, MegaScreen.tr(P, "flash"), cx, y + 68, RED);
            return;
        }
        if (m.hole()) {
            double t = MegaArt.time();
            int r = (int) Math.round(6 + 2 * Math.log10(Math.max(1, m.mass() / 1000)));
            glow(g, cx, cy, r * 3, 0xFF8A9CFF, Math.min(1, Math.log10(1 + v(m, 0)) / 12));
            MegaArt.blackHole(g, cx, cy, r, 7000);
            if (v(m, 2) > 0) for (int k = 0; k < 8; k++) {
                double a = 2 * Math.PI * k / 8 + t * 0.3;
                MegaArt.beam(g, cx + (int) (Math.cos(a) * 36), cy + (int) (Math.sin(a) * 30), cx, cy, 0xFFE08CFF);
            }
        } else {
            // Light gathering towards the point where it will collapse.
            double f = Math.min(1, m.mass() / KugelblitzPhysics.FORM_MASS);
            glow(g, cx, cy, (int) (6 + 20 * f), 0xFFE08CFF, 0.3 + 0.7 * f);
            centre(g, MegaScreen.tr(P, "no_hole"), cx, y + 68, L.dim());
        }
        text(g, MegaScreen.tr(P, "rate", sci(KugelblitzPhysics.RATES[m.rateIndex()])), x + 4, y + 4, L.dim(),
                false);
        right(g, MegaScreen.tr(P, "target", sci(KugelblitzPhysics.TARGETS[m.targetIndex()])), x + 204, y + 4,
                L.dim());
    }

    /** Log scale 1 kg (bottom) to 10⁹ kg (top); red under the critical mass, a tick at the target. */
    @OnlyIn(Dist.CLIENT)
    private static void massColumn(GuiGraphics g, KugelblitzMachine m, int x, int y) {
        int h = 62, top = y + 2;
        int crit = top + h - (int) Math.round(Math.log10(m.critical()) / 9 * h);
        g.fill(x + 3, crit, x + 21, top + h, 0xFF3A1018);
        double f = m.mass() <= 1 ? 0 : Math.min(1, Math.log10(m.mass()) / 9);
        int level = top + h - (int) Math.round(f * h);
        g.fill(x + 5, level, x + 19, top + h, m.hole() ? 0xFF9A6AE0 : 0xFFE08CFF);
        int tgt = top + h - (int) Math.round(Math.log10(KugelblitzPhysics.TARGETS[m.targetIndex()]) / 9 * h);
        g.fill(x + 1, tgt, x + 23, tgt + 1, L.glow());
        g.fill(x + 1, crit, x + 23, crit + 1, RED);
    }

    @OnlyIn(Dist.CLIENT)
    private static void powerColumn(GuiGraphics g, KugelblitzMachine m, int x, int y) {
        int h = 62, top = y + 2;
        double f = Math.min(1, Math.log10(1 + v(m, 0)) / 16);
        int level = top + h - (int) Math.round(f * h);
        g.fill(x + 5, level, x + 19, top + h, m.phases() >= 5 ? 0xFF8AD0FF : 0xFF4A5A70);
    }

    @OnlyIn(Dist.CLIENT)
    private static void readout(GuiGraphics g, KugelblitzMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "mass"), x + 4, y + 3, L.dim(), false);
        MegaUI.digits(g, x + 4, y + 13, String.format(Locale.ROOT, "%.3e", m.mass()).replace("e+", "e"),
                L.accent());
        right(g, "kg", x + 144, y + 14, L.dim());
        text(g, MegaScreen.tr(P, "held", sci(m.mass() * KugelblitzPhysics.EU_PER_KG)), x + 4, y + 27, L.dim(),
                false);
        right(g, MegaScreen.tr(P, m.phases() >= 5 ? "caught" : "lost", sci(m.phases() >= 5 ? v(m, 1) : v(m, 0))),
                x + 144, y + 27, m.phases() >= 5 ? 0xFF8AD0FF : YELLOW);
    }
}
