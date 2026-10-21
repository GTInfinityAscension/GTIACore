package com.raishxn.gtia.mega.hraemc;

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
 * HRAE-MC screen, a round Kerr console: the hole with its disk, the innermost stable orbit (it shrinks as the hole
 * spins faster) and the ergosphere; spin and efficiency dials; the three power streams against the Eddington limit;
 * the Penrose share switch.
 */
public final class HraeScreen {

    static final String P = "gtia.hraemc";
    static final Look L = new Look("hraemc", 0xFFFFB070, 0xFFFFE8D0, 0xFF120D18, 0xFF5A3A2E, 0xFFB09A88,
            MegaUI.Shape.CAPSULE);
    private static final int MANUAL_LINES = 15, PAGE_LINES = 5;

    private HraeScreen() {}

    public static ModularUI create(HraeMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 268, 232, 90, 14);
        root.addWidget(MegaUI.paint(82, 40, 104, 104, (g, x, y) -> kerr(g, m, x + 52, y + 52)));
        root.addWidget(MegaUI.paint(18, 66, 52, 60, (g, x, y) -> MegaUI.ring(g, L, x + 26, y + 26, 18,
                v(m, 1) / HraePhysics.MAX_SPIN, 0xFFC78CFF, MegaScreen.tr(P, "spin"),
                String.format("%.3f", v(m, 1)))));
        root.addWidget(MegaUI.paint(198, 66, 52, 60, (g, x, y) -> MegaUI.ring(g, L, x + 26, y + 26, 18,
                v(m, 2) / 0.33, L.accent(), "η", String.format("%.1f%%", v(m, 2) * 100))));
        root.addWidget(MegaUI.paint(56, 156, 156, 40, (g, x, y) -> streams(g, m, x, y)));
        root.addWidget(MegaUI.knob(L, 30, 180, 12, () -> m.penrosePct() / HraePhysics.SHARE_STEP, 11,
                () -> new String[] { "0", "", "", "", "", "", "", "", "", "", "100" },
                c -> MegaScreen.act(m, c.button == 1 ? "less" : "more", c), P + ".ui.penrose.tip"));
        root.addWidget(MegaUI.paint(90, 200, 88, 14, (g, x, y) -> {
            text(g, MegaScreen.tr(P, "research"), x + 3, y + 3, L.dim(), false);
            right(g, String.valueOf(m.research()), x + 85, y + 3, L.glow());
        }));
        root.addWidget(MegaUI.button(L, 214, 160, 44, 14, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 214, 178, 44, 14, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 14, 28, 240, 180, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(HraeMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    /** The hole seen from above: disk from the ISCO out, ergosphere outline, spin shown by the disk's swirl. */
    @OnlyIn(Dist.CLIENT)
    private static void kerr(GuiGraphics g, HraeMachine m, int cx, int cy) {
        if (!m.inPlace()) {
            centre(g, MegaScreen.tr(P, "no_hole"), cx, cy - 4, RED);
            return;
        }
        double t = MegaArt.time(), a = v(m, 1);
        // Gravitational radii to pixels: r = 1 is 4 px; the horizon r+ = 1 + √(1 - a²).
        double scale = 4;
        double rPlus = 1 + Math.sqrt(1 - a * a), isco = Math.max(1, v(m, 3));
        int hole = (int) Math.round(rPlus * scale);
        int inner = (int) Math.round(isco * scale);
        boolean fed = v(m, 4) > 0;
        for (int k = 0; k < 1400; k++) {
            double rr = inner + (k % 40) * 1.0;
            double ang = k * 0.61803 * 2 * Math.PI + t * (1.5 + 2 * a) * Math.pow(inner / rr, 1.5);
            double heat = Math.pow(inner / rr, 0.75);
            int c = MegaArt.rgb(new float[] { 1f, (float) (0.35 + 0.55 * heat), (float) (0.1 + 0.5 * heat) },
                    (fed ? 0.4 + 0.8 * heat : 0.15 + 0.3 * heat));
            px(g, cx + (int) Math.round(Math.cos(ang) * rr), cy + (int) Math.round(Math.sin(ang) * rr), c);
        }
        ellipse(g, cx, cy, 2 * scale, 2 * scale, 0xFF8A6AE0, 2);
        ellipse(g, cx, cy, inner, inner, 0xFFFFE0A0, 3);
        disc(g, cx, cy, hole, 0xFF000000);
        ellipse(g, cx, cy, hole + 1, hole + 1, 0xFFFFD0A0, 0);
        centre(g, MegaScreen.tr(P, "isco", String.format("%.2f", isco)), cx, cy + 42, L.dim());
    }

    /** Accretion, Penrose and jets as stacked bars against the Eddington limit. */
    @OnlyIn(Dist.CLIENT)
    private static void streams(GuiGraphics g, HraeMachine m, int x, int y) {
        double edd = Math.max(1, v(m, 7));
        String[] names = { MegaScreen.tr(P, "accretion"), MegaScreen.tr(P, "penrose"), MegaScreen.tr(P, "jets") };
        int[] colours = { 0xFFFFB070, 0xFFC78CFF, 0xFF8AD0FF };
        for (int i = 0; i < 3; i++) {
            int ly = y + 3 + i * 12;
            text(g, names[i], x + 3, ly, colours[i], false);
            double f = Math.min(1, v(m, 4 + i) / edd);
            g.fill(x + 56, ly + 1, x + 56 + (int) Math.round(f * 60), ly + 7, colours[i]);
            right(g, sci(v(m, 4 + i)), x + 152, ly, L.dim());
        }
        g.fill(x + 116, y + 2, x + 117, y + 38, RED);
    }
}
