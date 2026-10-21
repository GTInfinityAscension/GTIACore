package com.raishxn.gtia.mega.disintegrator;

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
 * Disintegrator screen, a shard: the target system cut away, its layers peeled from the outside in as they are taken;
 * the target picker; where to stop; what is still pouring out.
 */
public final class DisintegratorScreen {

    static final String P = "gtia.disintegrator";
    static final Look L = new Look("disintegrator", 0xFFE08CFF, 0xFFF6E4FF, 0xFF120A14, 0xFF4E2A5A, 0xFFB898C0,
            MegaUI.Shape.NOTCH);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4;
    private static final int[] LAYER = { 0xFF8A6A4A, 0xFFD2782E, 0xFFFFD27A, 0xFFFFF4D0 };

    private DisintegratorScreen() {}

    public static ModularUI create(DisintegratorMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 296, 220, 104, 43);
        root.addWidget(MegaUI.paint(106, 62, 84, 84, (g, x, y) -> cutaway(g, m, x + 42, y + 42)));
        root.addWidget(MegaUI.paint(52, 86, 44, 38, (g, x, y) -> targetPanel(g, m, x, y)));
        root.addWidget(MegaUI.button(L, 52, 128, 20, 12, P + ".ui.prev", () -> false, c -> MegaScreen.act(m, "prev", c)));
        root.addWidget(MegaUI.button(L, 76, 128, 20, 12, P + ".ui.next", () -> false, c -> MegaScreen.act(m, "next", c)));
        root.addWidget(MegaUI.paint(200, 86, 44, 38, (g, x, y) -> stopPanel(g, m, x, y)));
        root.addWidget(MegaUI.button(L, 200, 128, 44, 12, P + ".ui.stop", () -> m.stopAt() == 3,
                c -> MegaScreen.act(m, "stop", c)));
        root.addWidget(MegaUI.paint(88, 152, 120, 12, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 2, 114, ticker(m),
                0xFFF0D8FF)));
        root.addWidget(MegaUI.button(L, 86, 172, 58, 14, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 152, 172, 58, 14, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 40, 56, 216, 120, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(DisintegratorMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(DisintegratorMachine m) {
        if (v(m, 0) < 0) return MegaScreen.tr(P, m.erased() > 0 ? "erased" : "no_target", m.erased());
        return MegaScreen.tr(P, "pouring", sci(v(m, 5)), sci(v(m, 8)), sci(v(m, 10)));
    }

    /**
     * The system in section: star, core, mantle and crust as rings; what is taken fades out from the edge in. The
     * radius left follows the mass left: x/R = (1 - f)^(1/3).
     */
    @OnlyIn(Dist.CLIENT)
    private static void cutaway(GuiGraphics g, DisintegratorMachine m, int cx, int cy) {
        if (v(m, 0) < 0) {
            centre(g, MegaScreen.tr(P, "pick"), cx, cy - 4, L.dim());
            return;
        }
        double f = v(m, 1);
        double[] edges = { 1, 1 - DisintegratorPhysics.CRUST, 1 - DisintegratorPhysics.MANTLE,
                1 - DisintegratorPhysics.CORE };
        int rMax = 38;
        int left = (int) Math.round(rMax * Math.cbrt(Math.max(0, 1 - f)));
        for (int l = 0; l < 4; l++) {
            int r = (int) Math.round(rMax * Math.cbrt(edges[l]));
            int colour = r > left ? 0xFF241828 : LAYER[l];
            disc(g, cx, cy, r, colour);
        }
        if (left > 0) ellipse(g, cx, cy, left, left, L.glow(), 0);
        double t = MegaArt.time();
        for (int k = 0; k < 24 && v(m, 7) > 0; k++) {
            double a = k * 0.2618 + t * 1.3, rr = left + 2 + (t * 9 + k * 3) % 10;
            px(g, cx + (int) Math.round(Math.cos(a) * rr), cy + (int) Math.round(Math.sin(a) * rr), L.accent());
        }
        centre(g, String.format("%.1f%%", f * 100), cx, cy + 40, L.glow());
    }

    @OnlyIn(Dist.CLIENT)
    private static void targetPanel(GuiGraphics g, DisintegratorMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "target"), x + 2, y + 2, L.dim(), false);
        if (v(m, 0) < 0) {
            text(g, "—", x + 2, y + 14, L.dim(), false);
            return;
        }
        text(g, MegaScreen.tr(P, "slot", (int) v(m, 0) + 1), x + 2, y + 14, L.glow(), false);
        text(g, String.format("%.1f M☉", v(m, 3)), x + 2, y + 25, L.dim(), false);
    }

    @OnlyIn(Dist.CLIENT)
    private static void stopPanel(GuiGraphics g, DisintegratorMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "until"), x + 2, y + 2, L.dim(), false);
        int s = m.stopAt();
        text(g, MegaScreen.tr(P, "layer." + s), x + 2, y + 14, LAYER[s], false);
        if (s == 3) text(g, MegaScreen.tr(P, "erases"), x + 2, y + 25, RED, false);
    }
}
