package com.raishxn.gtia.mega.nidavellir;

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
 * Nidavellir Hyperforge screen, an anvil: the forge with its hammers striking the neutron star and the pulsar sweep;
 * a compression press; the neutronium hopper against what each batch takes; the recipe and the strike count.
 */
public final class NidavellirScreen {

    static final String P = "gtia.nidavellir";
    static final Look L = new Look("nidavellir", 0xFFFFB347, 0xFFFFE8C8, 0xFF1C1712, 0xFF6A4A26, 0xFFB09A80,
            MegaUI.Shape.NOTCH);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4;

    private NidavellirScreen() {}

    public static ModularUI create(NidavellirMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 300, 208, 96, 9);
        root.addWidget(MegaUI.paint(28, 22, 178, 96, (g, x, y) -> forge(g, m, x, y)));
        root.addWidget(MegaUI.lever(L, 214, 22, 96, () -> m.compression() / (double) NidavellirPhysics.COMPRESSION_MAX,
                c -> MegaScreen.act(m, "press", c), c -> MegaScreen.act(m, "ease", c), "gtna.ui.o.nd_press"));
        root.addWidget(MegaUI.paint(238, 22, 32, 96, (g, x, y) -> hopper(g, m, x, y)));
        root.addWidget(MegaUI.paint(28, 124, 242, 14, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 3, 236, ticker(m),
                0xFFFFD8A0)));
        root.addWidget(MegaUI.paint(28, 144, 242, 28, (g, x, y) -> stats(g, m, x, y)));
        root.addWidget(MegaUI.button(L, 28, 180, 116, 16, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 154, 180, 116, 16, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 26, 20, 248, 156, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(NidavellirMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(NidavellirMachine m) {
        if (m.recipe().isEmpty())
            return MegaScreen.tr(P, "idle." + (m.blocked().isEmpty() ? "no_recipe" : m.blocked()));
        return m.recipe().substring(m.recipe().indexOf(':') + 1) + "  ·  " +
                MegaScreen.tr(P, "crafts", String.format("%.0f", v(m, 4)), sci(v(m, 5)));
    }

    /** The ring seen from above, hammers beating in turn on the neutron star, the pulsar beam sweeping round. */
    @OnlyIn(Dist.CLIENT)
    private static void forge(GuiGraphics g, NidavellirMachine m, int x, int y) {
        int cx = x + 89, cy = y + 48;
        double t = MegaArt.time();
        boolean on = v(m, 8) > 0, magnetar = v(m, 6) > 0;
        double sweep = t * Math.PI;
        if (magnetar) {
            int bx = cx + (int) Math.round(Math.cos(sweep) * 46), by = cy + (int) Math.round(Math.sin(sweep) * 46);
            MegaArt.beam(g, cx, cy, bx, by, 0xFFBFE0FF);
            MegaArt.beam(g, cx, cy, 2 * cx - bx, 2 * cy - by, 0xFFBFE0FF);
        }
        MegaArt.star(g, cx, cy, 4, 600000);
        ellipse(g, cx, cy, 40, 40, 0xFF6A5A48, 0);
        ellipse(g, cx, cy, 36, 36, 0xFF4A3E32, 0);
        for (int k = 0; k < 8; k++) {
            double a = 2 * Math.PI * (k + 0.5) / 8;
            double phase = (t / (3.0 + 0.2 * (k % 2)) + k * 0.125) % 1;
            double reach = on ? 22 - 6 * Math.max(0, Math.sin(phase * 2 * Math.PI)) : 22;
            int hx = cx + (int) Math.round(Math.cos(a) * reach), hy = cy + (int) Math.round(Math.sin(a) * reach);
            line(g, cx + (int) Math.round(Math.cos(a) * 36), cy + (int) Math.round(Math.sin(a) * 36), hx, hy,
                    0xFF8A8478);
            g.fill(hx - 2, hy - 2, hx + 3, hy + 3, 0xFFB0A898);
            if (on && reach < 17) glow(g, hx, hy, 5, 0xFFFFB060, 0.8);
        }
        text(g, MegaScreen.tr(P, magnetar ? "magnetar" : "no_magnetar"), x + 3, y + 3, magnetar ? 0xFFBFE0FF :
                L.dim(), false);
        if (v(m, 7) > 0) right(g, MegaScreen.tr(P, "wind"), x + 175, y + 3, GREEN);
    }

    /** Neutronium in the buses, with the line one batch takes. */
    @OnlyIn(Dist.CLIENT)
    private static void hopper(GuiGraphics g, NidavellirMachine m, int x, int y) {
        double have = v(m, 3), per = Math.max(1, v(m, 2));
        double f = Math.min(1, have / (per * 64));
        MegaUI.tank(g, L, x + 4, y + 4, 24, 76, f, 0xFFB8C0D0);
        int line = y + 80 - (int) Math.round(Math.min(1, 1 / 64.0) * 76);
        g.fill(x + 2, line, x + 30, line + 1, RED);
        centre(g, String.format("%.0f", have), x + 16, y + 84, L.glow());
    }

    @OnlyIn(Dist.CLIENT)
    private static void stats(GuiGraphics g, NidavellirMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "parallel"), x + 4, y + 3, L.dim(), false);
        MegaUI.digits(g, x + 4, y + 14, String.format("%.0f", v(m, 0)), L.accent());
        text(g, MegaScreen.tr(P, "speed", String.format("%.0f", v(m, 1))), x + 74, y + 3, L.dim(), false);
        text(g, MegaScreen.tr(P, "per_batch", String.format("%.0f", v(m, 2))), x + 74, y + 15, L.dim(), false);
        right(g, MegaScreen.tr(P, "crafted", m.crafted()), x + 238, y + 3, L.glow());
        right(g, MegaScreen.tr(P, "used", m.neutroniumUsed()), x + 238, y + 15, L.dim());
    }
}
