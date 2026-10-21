package com.raishxn.gtia.mega.yggdrasil;

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
 * Yggdrasil screen, a leaf: the gas giant with its band of gardens at the chosen depth; the growth curve over depth
 * (light falling, carbon rising) with the chosen and the best depth marked; the harvest counters and biomass.
 */
public final class YggdrasilScreen {

    static final String P = "gtia.yggdrasil";
    static final Look L = new Look("yggdrasil", 0xFF7FE0A0, 0xFFE0FFE8, 0xFF0C1A12, 0xFF2E5A3E, 0xFF9AC0A8,
            MegaUI.Shape.CAPSULE);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4;

    private YggdrasilScreen() {}

    public static ModularUI create(YggdrasilMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 296, 214, 98, 8);
        root.addWidget(MegaUI.paint(20, 46, 100, 100, (g, x, y) -> planet(g, m, x + 50, y + 50)));
        root.addWidget(MegaUI.paint(130, 26, 148, 88, (g, x, y) -> chart(g, m, x, y)));
        root.addWidget(MegaUI.button(L, 132, 118, 70, 14, P + ".ui.higher", () -> false,
                c -> MegaScreen.act(m, "higher", c)));
        root.addWidget(MegaUI.button(L, 206, 118, 70, 14, P + ".ui.deeper", () -> false,
                c -> MegaScreen.act(m, "deeper", c)));
        root.addWidget(MegaUI.paint(130, 138, 148, 30, (g, x, y) -> counters(g, m, x, y)));
        root.addWidget(MegaUI.paint(22, 174, 252, 14, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 3, 246, ticker(m),
                0xFFC8F0D0)));
        root.addWidget(MegaUI.button(L, 70, 194, 74, 14, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 152, 194, 74, 14, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 24, 24, 248, 146, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(YggdrasilMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(YggdrasilMachine m) {
        if (m.recipe().isEmpty())
            return MegaScreen.tr(P, "idle." + (m.blocked().isEmpty() ? "no_recipe" : m.blocked()));
        return m.recipe().substring(m.recipe().indexOf(':') + 1) + "  ·  " +
                MegaScreen.tr(P, "crafts", String.format("%.0f", v(m, 5)), sci(v(m, 6)));
    }

    /** The planet with a ring of gardens: higher gardens float farther out, deeper ones closer to the clouds. */
    @OnlyIn(Dist.CLIENT)
    private static void planet(GuiGraphics g, YggdrasilMachine m, int cx, int cy) {
        MegaArt.planet(g, cx, cy, 34, 0xFF5AB080, 0xFF1E4A70);
        double t = MegaArt.time();
        int r = 46 - (int) Math.round(m.depth() * 1.4);
        boolean on = v(m, 7) > 0;
        for (int k = 0; k < 48; k++) {
            double a = 2 * Math.PI * k / 48 + t * 0.15;
            int px = cx + (int) Math.round(Math.cos(a) * r), py = cy + (int) Math.round(Math.sin(a) * r * 0.32);
            if (Math.sin(a) < 0 && Math.abs(px - cx) < 32) continue;
            g.fill(px, py, px + 2, py + 2, on && (k + (int) (t * 4)) % 6 == 0 ? 0xFFFFFFFF : L.accent());
        }
    }

    /** Growth over depth for this star; the chosen depth solid, the best one dashed. */
    @OnlyIn(Dist.CLIENT)
    private static void chart(GuiGraphics g, YggdrasilMachine m, int x, int y) {
        int x0 = x + 18, y0 = y + 74, w = 124, h = 62;
        g.fill(x0, y0, x0 + w, y0 + 1, L.dim());
        g.fill(x0, y0 - h, x0 + 1, y0, L.dim());
        double light = v(m, 3), peak = 1e-6;
        for (int i = 0; i <= w; i++) peak = Math.max(peak, YggdrasilPhysics.growth(light,
                YggdrasilPhysics.DEPTH_MAX * i / w));
        int prev = -1;
        for (int i = 0; i <= w; i++) {
            double gr = YggdrasilPhysics.growth(light, YggdrasilPhysics.DEPTH_MAX * i / w);
            int py = y0 - 1 - (int) Math.round((h - 4) * gr / peak);
            if (prev >= 0) line(g, x0 + i - 1, prev, x0 + i, py, L.accent());
            prev = py;
        }
        int best = x0 + (int) Math.round(w * v(m, 4) / YggdrasilPhysics.DEPTH_MAX);
        for (int yy = y0 - h; yy < y0; yy += 4) g.fill(best, yy, best + 1, yy + 2, 0xFFFFFFFF);
        int at = x0 + (int) Math.round(w * m.depth() / YggdrasilPhysics.DEPTH_MAX);
        g.fill(at - 1, y0 - h, at + 1, y0, 0xFFFFD27A);
        text(g, MegaScreen.tr(P, "growth"), x + 2, y + 2, L.dim(), false);
        right(g, MegaScreen.tr(P, "depth", String.format("%.1f", m.depth())), x + 146, y + 2, 0xFFFFD27A);
        text(g, "0", x0 - 2, y0 + 3, L.dim(), false);
        right(g, MegaScreen.tr(P, "deep"), x0 + w, y0 + 3, L.dim());
        centre(g, MegaScreen.tr(P, "best", String.format("%.1f", v(m, 4))), x0 + w / 2, y0 + 3, 0xFFFFFFFF);
    }

    @OnlyIn(Dist.CLIENT)
    private static void counters(GuiGraphics g, YggdrasilMachine m, int x, int y) {
        MegaUI.digits(g, x + 3, y + 4, String.format("%.0f", v(m, 0)), L.accent());
        text(g, MegaScreen.tr(P, "speed", String.format("%.2f", v(m, 1)), String.format("%.0f%%", v(m, 2) * 100)),
                x + 52, y + 2, L.dim(), false);
        text(g, MegaScreen.tr(P, "harvest", m.crafted()), x + 52, y + 11, L.glow(), false);
        text(g, MegaScreen.tr(P, "biomass", sci(m.biomass())), x + 52, y + 20, L.glow(), false);
    }
}
