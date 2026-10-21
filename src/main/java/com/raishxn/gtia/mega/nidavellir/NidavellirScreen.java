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
 * Nidavellir Hyperforge screen, an anvil: the ring disc turning round the neutron star, the press arm and the pulsar sweep;
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

    /** The disc seen from above: five rings turning at their own pace, the press arm glowing as it strikes. */
    @OnlyIn(Dist.CLIENT)
    private static void forge(GuiGraphics g, NidavellirMachine m, int x, int y) {
        int cx = x + 89, cy = y + 48;
        double t = MegaArt.time();
        boolean on = v(m, 8) > 0, magnetar = v(m, 6) > 0;
        int[] radii = { 9, 16, 24, 32, 41 };
        double[] periods = { 24, -40, 70, -110, 0 };
        int phases = m.phases();
        for (int i = 0; i < radii.length; i++) {
            boolean built = i == 0 || i == 4 || i == 1 && phases >= 2 || i == 2 && phases >= 4 || i == 3 && phases >= 5;
            int r = radii[i];
            int colour = !built ? 0xFF2A2620 : i == 1 ? 0xFFC08A40 : 0xFF6A7088;
            ellipse(g, cx, cy, r, (int) Math.round(r * 0.55), colour, 0);
            if (!built) continue;
            double turn = periods[i] == 0 ? 0 : t * 2 * Math.PI / periods[i];
            for (int k = 0; k < 14; k++) {
                double a = turn + k * 2 * Math.PI / 14;
                px(g, cx + (int) Math.round(Math.cos(a) * r), cy + (int) Math.round(Math.sin(a) * r * 0.55),
                        0xFFFFE8B0);
            }
        }
        if (phases >= 3) {
            double ax = Math.cos(Math.toRadians(-35)), ay = Math.sin(Math.toRadians(-35)) * 0.55;
            line(g, cx + (int) Math.round(ax * 5), cy + (int) Math.round(ay * 5), cx + (int) Math.round(ax * 41),
                    cy + (int) Math.round(ay * 41), 0xFF9AB8C8);
            for (int k = 0; k < 4; k++) {
                double d = 12 + 8 * k, pulse = on ? Math.max(0, Math.sin(t * 4 - k * 0.9)) : 0;
                int px = cx + (int) Math.round(ax * d), py = cy + (int) Math.round(ay * d);
                g.fill(px - 1, py - 2, px + 2, py + 3, 0xFF6A8A98);
                if (pulse > 0.2) glow(g, px, py, 4, 0xFF80FFF0, pulse);
            }
        }
        if (magnetar) {
            double sweep = t * Math.PI;
            int bx = cx + (int) Math.round(Math.cos(sweep) * 46), by = cy + (int) Math.round(Math.sin(sweep) * 25);
            MegaArt.beam(g, cx, cy, bx, by, 0xFFBFE0FF);
            MegaArt.beam(g, cx, cy, 2 * cx - bx, 2 * cy - by, 0xFFBFE0FF);
        }
        MegaArt.star(g, cx, cy, 4, 600000);
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
