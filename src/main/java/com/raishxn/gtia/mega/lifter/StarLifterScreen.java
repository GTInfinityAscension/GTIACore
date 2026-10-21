package com.raishxn.gtia.mega.lifter;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtia.mega.common.MegaScreen;
import com.raishxn.gtia.mega.common.client.MegaArt;
import com.raishxn.gtia.mega.common.client.MegaFx;
import com.raishxn.gtia.mega.common.ui.MegaUI;
import com.raishxn.gtia.mega.common.ui.MegaUI.Look;
import com.raishxn.gtia.mega.lifter.LifterPhysics.Element;
import com.raishxn.gtia.mega.lifter.LifterPhysics.Mode;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;

import java.util.Locale;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Star Lifter screen, its own portrait window (the polar axis runs through its top and bottom): the star with the
 * outflow climbing to the chalices; a rate lever, a rotary method switch, ring gauges for the star's mass, its
 * restlessness and the girdle; a spectrometer of the lifted elements; a star sheet and the field manual as overlays.
 */
public final class StarLifterScreen {

    static final String P = "gtia.star_lifter";
    static final Look L = new Look("lifter", 0xFFFF8A3D, 0xFFFFE3C8, 0xFF2A1D18, 0xFF6E3A22, 0xFFB0907E,
            MegaUI.Shape.CHEVRON);
    private static final int MANUAL_LINES = 20, PAGE_LINES = 5;
    private static final String[] SYMBOLS = { "H", "He", "O", "C", "Fe", "Ne", "N", "Si", "Mg", "Ni" };
    private static final int[] BAR = { 0xFFFF7A5A, 0xFFFFD27A, 0xFF7FD8FF, 0xFF9A9AA8, 0xFFC08060, 0xFFFF6AD0,
            0xFF7FA8FF, 0xFFB8B8C8, 0xFFE0E0F0, 0xFF9FE8A0 };

    private StarLifterScreen() {}

    public static ModularUI create(StarLifterMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 236, 284, 12, 10);
        root.addWidget(MegaUI.paint(8, 22, 220, 118, (g, x, y) -> display(g, m, x, y)));
        root.addWidget(MegaUI.lever(L, 12, 148, 58, () -> (m.ratePct() - LifterPhysics.RATE_MIN) /
                (double) (LifterPhysics.RATE_MAX - LifterPhysics.RATE_MIN),
                c -> MegaScreen.act(m, "rate_up", c), c -> MegaScreen.act(m, "rate_down", c), "gtna.ui.o.sl_rate"));
        root.addWidget(MegaUI.paint(30, 148, 22, 10, (g, x, y) -> text(g, m.ratePct() + "%", x, y, L.glow(),
                false)));
        root.addWidget(MegaUI.knob(L, 76, 178, 18, () -> m.mode().ordinal(), Mode.values().length,
                () -> new String[] { MegaScreen.tr(P, "mode.short.thermal"), MegaScreen.tr(P, "mode.short.ring"),
                        MegaScreen.tr(P, "mode.short.deep") },
                c -> MegaScreen.act(m, "mode", c), "gtna.ui.o.sl_mode.tip"));
        root.addWidget(MegaUI.paint(118, 152, 118, 58, (g, x, y) -> gauges(g, m, x, y)));
        root.addWidget(MegaUI.paint(8, 212, 220, 40, (g, x, y) -> spectrum(g, m, x, y)));
        root.addWidget(MegaUI.button(L, 8, 258, 70, 15, "gtna.ui.o.sl_star", () -> "star".equals(nav.open),
                c -> nav.toggle("star")));
        root.addWidget(MegaUI.button(L, 83, 258, 70, 15, "gtna.ui.o.sl_manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 158, 258, 70, 15, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(starSheet(m, nav));
        root.addWidget(MegaUI.manual(L, 6, 20, 224, 232, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(StarLifterMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    // ------------------------------------------------------------------ display

    @OnlyIn(Dist.CLIENT)
    private static void display(GuiGraphics g, StarLifterMachine m, int x, int y) {
        if (!m.isFormed()) {
            centre(g, net.minecraft.network.chat.Component.translatable("gtceu.multiblock.invalid_structure")
                    .getString(), x + 110, y + 55, RED);
            return;
        }
        int phases = m.phases();
        int sx = x + 110, sy = y + 62;
        double kelvin = v(m, 5) > 0 ? v(m, 5) : 5800;
        int r = (int) Math.round(16 * (v(m, 19) > 0 ? v(m, 19) : 1));
        boolean on = v(m, 8) > 0;
        double t = MegaArt.time();
        if (on && m.mode() != Mode.THERMAL) for (int s : new int[] { -1, 1 })
            MegaArt.jetVertical(g, sx, sy + s * r, s, 44 - r, 1, 5, new float[] { 1f, 0.85f, 0.55f },
                    new float[] { 1f, 0.4f, 0.25f });
        MegaArt.star(g, sx, sy, r, kelvin);
        // The girdle edge-on, with its petals beaming at the star.
        ellipse(g, sx, sy, 50, 7, 0xFF8A7A70, 0);
        if (on) for (int k = 0; k < 8; k++) {
            double a = 2 * Math.PI * k / 8 + t * 0.15;
            if (Math.sin(a) < -0.2) continue;
            MegaArt.beam(g, sx + (int) Math.round(Math.cos(a) * 46), sy + (int) Math.round(Math.sin(a) * 6),
                    sx + (int) Math.round(Math.cos(a) * r * 0.9), sy + (int) Math.round(Math.sin(a) * 2), 0xFFB08CFF);
        }
        if (phases >= 2) MegaArt.field(g, sx, sy, 58, on ? 0xFF5AA8FF : 0xFF3A4A6A);
        if (phases >= 3) for (int s : new int[] { -1, 1 }) {
            int cy = sy + s * 46;
            for (int k = -16; k <= 16; k++) px(g, sx + k, cy - s * (int) Math.round(k * k / 40.0), 0xFFC79A54);
        }
        if (phases >= 4) for (int s : new int[] { -1, 1 })
            for (int k = 0; k < 12; k++) g.fill(sx - 1 + (k % 3 == 0 ? -2 : 0), sy + s * (49 + k),
                    sx + 2 + (k % 3 == 0 ? 2 : 0), sy + s * (49 + k) + 1, 0xFF9FE8C0);
        text(g, MegaScreen.tr(P, "mode." + m.mode().name().toLowerCase(Locale.ROOT)), x + 4, y + 4, L.glow(), true);
        MegaUI.digits(g, x + 4, y + 104, String.format(Locale.ROOT, "%.2e", v(m, 0)), on ? L.accent() : L.dim());
        text(g, "kg/s", x + 4 + 7 * 8 + 4, y + 105, L.dim(), false);
        right(g, MegaScreen.tr(P, "power", sci(v(m, 2))), x + 216, y + 4, L.dim());
        right(g, MegaScreen.tr(P, "plasma", String.format("%.0f", v(m, 9)), String.format("%.0f", v(m, 10))),
                x + 216, y + 105, 0xFFFFC890);
        if (!m.inPlace()) centre(g, MegaScreen.tr(P, "no_star"), sx, y + 20, RED);
        else if (m.integrity() <= 0) centre(g, MegaScreen.tr(P, "girdle_broken"), sx, y + 20, RED);
    }

    @OnlyIn(Dist.CLIENT)
    private static void gauges(GuiGraphics g, StarLifterMachine m, int x, int y) {
        double mass = (v(m, 3) - LifterPhysics.MIN_MASS) / (1 - LifterPhysics.MIN_MASS);
        MegaUI.ring(g, L, x + 22, y + 22, 13, mass, 0xFFFFD27A, MegaScreen.tr(P, "g.mass"),
                String.format("%.3f", v(m, 3)));
        MegaUI.ring(g, L, x + 60, y + 22, 13, m.instability() / 1000.0, m.instability() > 600 ? RED : YELLOW,
                MegaScreen.tr(P, "g.instability"), String.format("%.0f%%", m.instability() / 10.0));
        MegaUI.ring(g, L, x + 98, y + 22, 13, m.integrity() / 1000.0, m.integrity() > 300 ? GREEN : RED,
                MegaScreen.tr(P, "g.girdle"), String.format("%.0f%%", m.integrity() / 10.0));
    }

    @OnlyIn(Dist.CLIENT)
    private static void spectrum(GuiGraphics g, StarLifterMachine m, int x, int y) {
        double[] out = new double[Element.values().length];
        for (int i = 0; i < out.length; i++) out[i] = v(m, 9 + i);
        MegaUI.spectrum(g, L, x + 6, y + 4, 208, 24, out, BAR, SYMBOLS);
        if (m.phases() < 4) right(g, MegaScreen.tr(P, "no_spires"), x + 216, y + 3, L.dim());
    }

    // ------------------------------------------------------------------ star sheet

    private static WidgetGroup starSheet(StarLifterMachine m, Nav nav) {
        return MegaUI.overlay(18, 30, 200, 120, () -> "star".equals(nav.open), (g, x, y) -> {
            g.fill(x, y, x + 200, y + 120, 0xFF1A120E);
            g.fill(x, y, x + 200, y + 1, L.accent());
            g.fill(x, y + 119, x + 200, y + 120, L.accent());
            text(g, MegaScreen.tr(P, "s.title"), x + 6, y + 4, L.accent(), true);
            double kelvin = v(m, 5) > 0 ? v(m, 5) : 5800;
            MegaArt.star(g, x + 36, y + 66, (int) Math.round(22 * Math.max(0.5, v(m, 19))), kelvin);
            int lx = x + 76, ly = y + 20;
            String[][] rows = { { MegaScreen.tr(P, "s.mass"), String.format("%.4f M☉", v(m, 3)) },
                    { MegaScreen.tr(P, "s.light"), String.format("%.3f L☉", v(m, 4)) },
                    { MegaScreen.tr(P, "s.temp"), String.format("%.0f K", kelvin) },
                    { MegaScreen.tr(P, "s.fuel"), String.format("%.1f%%", v(m, 7) * 100) },
                    { MegaScreen.tr(P, "s.life"), sci(v(m, 6)) + " " + MegaScreen.tr(P, "years") },
                    { MegaScreen.tr(P, "s.brain"), String.format("×%.3f", Math.pow(Math.max(0, v(m, 4)), 0.75)) } };
            for (var row : rows) {
                text(g, row[0], lx, ly, L.dim(), false);
                right(g, row[1], x + 194, ly, 0xFFE0E0E4);
                ly += 13;
            }
            g.fill(lx, ly + 2, x + 194, ly + 5, MegaArt.rgb(MegaFx.blackbody(kelvin), 1));
        }, group -> group.addWidget(hit(184, 2, 14, 12, c -> nav.open = null)));
    }
}
