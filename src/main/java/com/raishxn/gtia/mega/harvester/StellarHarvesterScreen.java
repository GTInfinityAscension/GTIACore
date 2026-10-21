package com.raishxn.gtia.mega.harvester;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.client.gui.GuiGraphics;
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
 * Stellar Harvester screen, a flame: the heated star inside a ring of the fuel left; the boost on a log scale; the
 * countdown to the end of the core hydrogen and what the star will become; the heating dial.
 */
public final class StellarHarvesterScreen {

    static final String P = "gtia.stellar_harvester";
    static final Look L = new Look("stellar_harvester", 0xFFFFA040, 0xFFFFE8C8, 0xFF1A0E08, 0xFF6A3A18, 0xFFC0A080,
            MegaUI.Shape.HEX);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4, DIAL = 31;

    private StellarHarvesterScreen() {}

    public static ModularUI create(StellarHarvesterMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 260, 248, 86, 227);
        root.addWidget(MegaUI.paint(78, 60, 104, 104, (g, x, y) -> core(g, m, x + 52, y + 52)));
        root.addWidget(MegaUI.paint(42, 100, 16, 70, (g, x, y) -> boost(g, m, x, y)));
        root.addWidget(MegaUI.paint(188, 100, 40, 70, (g, x, y) -> countdown(g, m, x, y)));
        root.addWidget(MegaUI.paint(44, 178, 172, 12, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 2, 166, ticker(m),
                0xFFFFD8A0)));
        root.addWidget(MegaUI.knob(L, 66, 204, 10, () -> (int) Math.round(m.heat() / HarvesterPhysics.HEAT_STEP), DIAL,
                () -> new String[] { "0", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "1.5" },
                c -> MegaScreen.act(m, c.button == 1 ? "cooler" : "hotter", c), P + ".ui.heat.tip"));
        root.addWidget(MegaUI.button(L, 90, 196, 60, 14, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 156, 196, 54, 14, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 30, 40, 200, 150, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(StellarHarvesterMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(StellarHarvesterMachine m) {
        Kind kind = Kind.values()[Math.max(0, Math.min(Kind.values().length - 1, (int) v(m, 0)))];
        if (!kind.mainSequence()) return MegaScreen.tr(P, "spent." + m.event());
        return MegaScreen.tr(P, "output", sci(v(m, 5)), String.format("%.2f", v(m, 3)), (int) v(m, 6));
    }

    /** The star, hotter as it is pushed, inside an arc of the fuel left (full circle: a fresh core). */
    @OnlyIn(Dist.CLIENT)
    private static void core(GuiGraphics g, StellarHarvesterMachine m, int cx, int cy) {
        double fuel = Math.max(0, Math.min(1, v(m, 2)));
        int steps = 180;
        for (int k = 0; k < steps; k++) {
            double a = -Math.PI / 2 + 2 * Math.PI * k / steps;
            boolean left = (double) k / steps < fuel;
            int x = cx + (int) Math.round(Math.cos(a) * 50), y = cy + (int) Math.round(Math.sin(a) * 50);
            g.fill(x - 1, y - 1, x + 2, y + 2, left ? L.accent() : 0xFF3A2414);
        }
        double t = v(m, 9) > 0 ? v(m, 9) : 5800;
        int r = (int) Math.round(18 + 10 * Math.min(1, Math.log10(Math.max(1, v(m, 4))) / 6));
        if (Kind.values()[Math.max(0, (int) v(m, 0))].mainSequence()) MegaArt.star(g, cx, cy, r, t);
        else MegaArt.blackHole(g, cx, cy, 10, 8000);
        centre(g, MegaScreen.tr(P, "fuel", String.format("%.2f%%", fuel * 100)), cx, cy + 30, L.dim());
        centre(g, MegaScreen.tr(P, "becomes", MegaScreen.tr(P, "remnant." +
                HarvesterPhysics.remnant(v(m, 1)).name().toLowerCase(java.util.Locale.ROOT))), cx, cy + 40, L.accent());
    }

    /** Boost on a log scale, one tick a decade, up to a million. */
    @OnlyIn(Dist.CLIENT)
    private static void boost(GuiGraphics g, StellarHarvesterMachine m, int x, int y) {
        double decades = Math.log10(Math.max(1, v(m, 4)));
        int h = 64, fill = (int) Math.round(h * Math.min(1, decades / 6));
        g.fill(x + 4, y + 3, x + 12, y + 3 + h, 0xFF2A1A10);
        g.fill(x + 4, y + 3 + h - fill, x + 12, y + 3 + h, decades > 4 ? 0xFFFF6040 : L.accent());
        for (int k = 0; k <= 6; k++) g.fill(x + 1, y + 3 + h - h * k / 6, x + 4, y + 4 + h - h * k / 6, L.dim());
    }

    /** Time left for the core at this power, and what the star becomes. */
    @OnlyIn(Dist.CLIENT)
    private static void countdown(GuiGraphics g, StellarHarvesterMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "left"), x + 2, y + 2, L.dim(), false);
        text(g, time(v(m, 7)), x + 2, y + 13, L.glow(), false);
        text(g, String.format("%.2f M☉", v(m, 1)), x + 2, y + 30, L.dim(), false);
    }

    private static String time(double s) {
        if (!Double.isFinite(s)) return "∞";
        if (s < 60) return String.format("%.0f s", s);
        if (s < 3600) return String.format("%.0f min", s / 60);
        if (s < 86400) return String.format("%.1f h", s / 3600);
        return String.format("%.0f d", s / 86400);
    }
}
