package com.raishxn.gtia.mega.suppressor;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtia.mega.common.MegaScreen;
import com.raishxn.gtia.mega.common.client.MegaArt;
import com.raishxn.gtia.mega.common.ui.MegaUI;
import com.raishxn.gtia.mega.common.ui.MegaUI.Look;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Fusion Suppressor screen, an observatory console: an oscilloscope of the neutrino flux (it flattens as fusion is
 * damped), a dome gauge of the core's pressure deficit, a big suppression switch, the star in a round well, and the
 * two endings that light up only when the core of a heavy star gives way.
 */
public final class FusionSuppressorScreen {

    static final String P = "gtia.fusion_suppressor";
    static final Look L = new Look("suppressor", 0xFF4EE6FF, 0xFFD6FAFF, 0xFF0F1E24, 0xFF2A5866, 0xFF88B8C4,
            MegaUI.Shape.CAPSULE);
    private static final int MANUAL_LINES = 15, PAGE_LINES = 5;

    private FusionSuppressorScreen() {}

    public static ModularUI create(FusionSuppressorMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 320, 212, 116, 9);
        root.addWidget(MegaUI.paint(10, 22, 206, 88, (g, x, y) -> scope(g, m, x, y)));
        root.addWidget(MegaUI.paint(230, 26, 72, 72, (g, x, y) -> star(g, m, x + 36, y + 36)));
        root.addWidget(MegaUI.paint(10, 116, 98, 54, (g, x, y) -> dome(g, m, x, y)));
        root.addWidget(MegaUI.knob(L, 160, 142, 20, () -> m.suppressionPct() / SuppressorPhysics.STEP,
                SuppressorPhysics.MAX / SuppressorPhysics.STEP + 1, () -> new String[] { "0", "", "", "", "", "50",
                        "", "", "", "", "100" },
                c -> MegaScreen.act(m, c.button == 1 ? "less" : "more", c), P + ".ui.knob.tip"));
        root.addWidget(MegaUI.paint(214, 114, 96, 58, (g, x, y) -> readout(g, m, x, y)));
        root.addWidget(MegaUI.button(L, 14, 182, 94, 18, P + ".ui.supernova", m::ready,
                c -> MegaScreen.act(m, "supernova", c)));
        root.addWidget(MegaUI.button(L, 114, 182, 94, 18, P + ".ui.collapse", m::ready,
                c -> MegaScreen.act(m, "collapse", c)));
        root.addWidget(MegaUI.button(L, 216, 182, 46, 18, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 266, 182, 46, 18, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 10, 22, 300, 154, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(FusionSuppressorMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    /** Neutrino flux trace: amplitude falls with suppression, with noise; the deficit bends the baseline. */
    @OnlyIn(Dist.CLIENT)
    private static void scope(GuiGraphics g, FusionSuppressorMachine m, int x, int y) {
        double t = MegaArt.time();
        for (int k = 0; k <= 4; k++) g.fill(x + 1, y + 8 + k * 18, x + 205, y + 9 + k * 18, 0xFF13303A);
        for (int k = 0; k <= 8; k++) g.fill(x + 1 + k * 25, y + 4, x + 2 + k * 25, y + 84, 0xFF13303A);
        boolean main = m.starKind().mainSequence();
        double amp = main ? 28 * (1 - 0.85 * m.suppressionPct() / 100.0) : m.starKind() == Kind.NEUTRON_STAR ? 6 : 1;
        int prev = -1;
        for (int i = 0; i < 204; i++) {
            double ph = (i + t * 60) * 0.11;
            double noise = Math.sin(ph * 3.7) * 0.3 + Math.sin(ph * 7.3 + 1.1) * 0.15;
            double burst = m.event() == 1 && m.eventSeconds() < 10 ? 2.5 * Math.exp(-m.eventSeconds() / 3.0) : 1;
            int yy = y + 44 + (int) Math.round((Math.sin(ph) + noise) * amp * burst / 1.45);
            if (prev >= 0) line(g, x + i, prev, x + i + 1, yy, L.accent());
            prev = yy;
        }
        text(g, MegaScreen.tr(P, "flux"), x + 4, y + 2, L.dim(), false);
        right(g, String.format("%.0f%%", main ? 100 - 85 * m.suppressionPct() / 100.0 : 0), x + 202, y + 2,
                L.glow());
        if (!m.inPlace()) centre(g, MegaScreen.tr(P, "no_star"), x + 103, y + 40, RED);
        else if (m.event() > 0 && m.eventSeconds() < 20)
            centre(g, MegaScreen.tr(P, "event." + m.event()), x + 103, y + 74, m.event() == 3 ? YELLOW : 0xFFFFD27A);
    }

    @OnlyIn(Dist.CLIENT)
    private static void star(GuiGraphics g, FusionSuppressorMachine m, int cx, int cy) {
        Kind k = m.starKind();
        if (k == Kind.BLACK_HOLE) MegaArt.blackHole(g, cx, cy, 9, 9000);
        else if (k == Kind.NEUTRON_STAR) MegaArt.star(g, cx, cy, 4, 600000);
        else if (k != Kind.NONE)
            MegaArt.star(g, cx, cy, (int) Math.round(Math.max(8, Math.min(26, 14 * v(m, 7)))), v(m, 3));
        if (m.event() == 1 && m.eventSeconds() < 30) MegaArt.field(g, cx, cy, 10 + m.eventSeconds(), 0xFFFFB070);
    }

    /** A half-dial of the core's pressure deficit: needle from left (0) to right (full). */
    @OnlyIn(Dist.CLIENT)
    private static void dome(GuiGraphics g, FusionSuppressorMachine m, int x, int y) {
        int cx = x + 49, cy = y + 44, r = 36;
        for (int k = 0; k <= 60; k++) {
            double a = Math.PI * (1 - k / 60.0);
            int c = k > 48 ? RED : k > 30 ? YELLOW : L.line();
            px(g, cx + (int) Math.round(Math.cos(a) * r), cy - (int) Math.round(Math.sin(a) * r), c);
            if (k % 10 == 0) px(g, cx + (int) Math.round(Math.cos(a) * (r - 3)), cy -
                    (int) Math.round(Math.sin(a) * (r - 3)), c);
        }
        double f = m.deficit() / (double) SuppressorPhysics.DEFICIT_FULL;
        double a = Math.PI * (1 - f);
        line(g, cx, cy, cx + (int) Math.round(Math.cos(a) * (r - 4)), cy - (int) Math.round(Math.sin(a) * (r - 4)),
                L.glow());
        disc(g, cx, cy, 2, L.accent());
        centre(g, MegaScreen.tr(P, "deficit") + String.format(" %.0f%%", f * 100), cx, y + 2, L.dim());
    }

    @OnlyIn(Dist.CLIENT)
    private static void readout(GuiGraphics g, FusionSuppressorMachine m, int x, int y) {
        Kind k = m.starKind();
        text(g, Component.translatable(FusionSuppressorMachine.kindKey(k)).getString(), x + 4, y + 3, L.glow(),
                false);
        text(g, String.format("%.2f M☉ · %.0f K", v(m, 1), v(m, 3)), x + 4, y + 13, L.dim(), false);
        boolean heavy = v(m, 1) >= SuppressorPhysics.COLLAPSE_MASS;
        text(g, MegaScreen.tr(P, heavy ? "heavy" : "light"), x + 4, y + 23, heavy ? 0xFFFFD27A : L.dim(), false);
        text(g, MegaScreen.tr(P, "research"), x + 4, y + 35, L.dim(), false);
        MegaUI.digits(g, x + 4, y + 45, String.valueOf(m.research()), L.accent());
        right(g, String.format("+%.0f/s", v(m, 4)), x + 92, y + 46, L.dim());
        if (m.owed() > 0) right(g, MegaScreen.tr(P, "owed", m.owed()), x + 92, y + 35, 0xFFFFD27A);
    }
}
