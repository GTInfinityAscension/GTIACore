package com.raishxn.gtia.mega.yard;

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
 * Assembly Yard screen, a dock board shaped like a hull: a radar of the ring with its ninety slipways (lit when open,
 * flashing as hulls are finished), a slipway lever, the build counters, the starlight that powers the lasers, the
 * recipe ticker and the rush-shift switch.
 */
public final class AssemblyYardScreen {

    static final String P = "gtia.assembly_yard";
    static final Look L = new Look("assembly_yard", 0xFF7FC4FF, 0xFFE0F0FF, 0xFF0F1620, 0xFF2E4A66, 0xFF8FA8C0,
            MegaUI.Shape.CHEVRON);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4;

    private AssemblyYardScreen() {}

    public static ModularUI create(AssemblyYardMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 312, 204, 106, 7);
        root.addWidget(MegaUI.paint(18, 32, 124, 124, (g, x, y) -> radar(g, m, x + 62, y + 62)));
        root.addWidget(MegaUI.lever(L, 152, 22, 128, () -> m.active() / (double) YardPhysics.slipways(5),
                c -> MegaScreen.act(m, "open", c), c -> MegaScreen.act(m, "close", c), P + ".ui.lever.tip"));
        root.addWidget(MegaUI.paint(180, 22, 108, 56, (g, x, y) -> counters(g, m, x, y)));
        root.addWidget(MegaUI.paint(180, 84, 108, 66, (g, x, y) -> starlight(g, m, x, y)));
        root.addWidget(MegaUI.paint(14, 160, 274, 14, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 3, 268, ticker(m),
                0xFFBFE0FF)));
        root.addWidget(MegaUI.button(L, 14, 182, 84, 14, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 108, 182, 84, 14, P + ".ui.rush", m::rush,
                c -> MegaScreen.act(m, "rush", c)));
        root.addWidget(MegaUI.button(L, 202, 182, 84, 14, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 14, 20, 284, 156, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(AssemblyYardMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(AssemblyYardMachine m) {
        if (m.recipe().isEmpty())
            return MegaScreen.tr(P, "idle." + (m.blocked().isEmpty() ? "no_recipe" : m.blocked()));
        return m.recipe().substring(m.recipe().indexOf(':') + 1) + "  ·  " +
                MegaScreen.tr(P, "crafts", String.format("%.0f", v(m, 4)), sci(v(m, 5)));
    }

    /** The ring from above: the star, the hub, twelve spokes, and the ninety slipways round the rim. */
    @OnlyIn(Dist.CLIENT)
    private static void radar(GuiGraphics g, AssemblyYardMachine m, int cx, int cy) {
        double t = MegaArt.time();
        boolean on = v(m, 8) > 0;
        ellipse(g, cx, cy, 52, 52, 0xFF2E4A66, 0);
        ellipse(g, cx, cy, 16, 16, 0xFF5A7A9A, 1);
        for (int k = 0; k < 12; k++) {
            double a = 2 * Math.PI * k / 12;
            line(g, cx + (int) Math.round(Math.cos(a) * 17), cy + (int) Math.round(Math.sin(a) * 17),
                    cx + (int) Math.round(Math.cos(a) * 51), cy + (int) Math.round(Math.sin(a) * 51), 0xFF24384E);
        }
        int open = (int) v(m, 2), built = (int) v(m, 3), total = YardPhysics.slipways(5);
        // A sweep runs round the open slipways while hulls are being finished.
        int sweep = on ? (int) (t * 30) % Math.max(1, open) : -1;
        for (int i = 0; i < total; i++) {
            double a = 2 * Math.PI * i / total - Math.PI / 2;
            int px = cx + (int) Math.round(Math.cos(a) * 45), py = cy + (int) Math.round(Math.sin(a) * 45);
            int colour = i >= built ? 0xFF1A2430 : i >= open ? 0xFF3A5068 : L.accent();
            if (on && i < open && Math.abs(i - sweep) < 3) colour = 0xFFFFFFFF;
            g.fill(px - 1, py - 1, px + 2, py + 2, colour);
        }
        MegaArt.star(g, cx, cy, 7, Math.max(3000, v(m, 7)));
        centre(g, MegaScreen.tr(P, "slipways", open, built), cx, cy + 55, L.dim());
    }

    @OnlyIn(Dist.CLIENT)
    private static void counters(GuiGraphics g, AssemblyYardMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "parallel"), x + 4, y + 3, L.dim(), false);
        MegaUI.digits(g, x + 4, y + 14, String.format("%.0f", v(m, 0)), L.accent());
        text(g, MegaScreen.tr(P, "speed", String.format("%.1f", v(m, 1))), x + 4, y + 33, L.dim(), false);
        text(g, MegaScreen.tr(P, "crafted", m.crafted()), x + 4, y + 44, L.glow(), false);
    }

    /** The light the lasers run on: luminosity, temperature, the build rate against its ceiling. */
    @OnlyIn(Dist.CLIENT)
    private static void starlight(GuiGraphics g, AssemblyYardMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "starlight"), x + 4, y + 3, L.accent(), false);
        text(g, MegaScreen.tr(P, "luminosity", sci(v(m, 6))), x + 4, y + 15, L.dim(), false);
        text(g, MegaScreen.tr(P, "temperature", String.format("%.0f", v(m, 7))), x + 4, y + 26, L.dim(), false);
        double cap = YardPhysics.SPEED_MAX * YardPhysics.RUSH_SPEED;
        g.fill(x + 4, y + 40, x + 104, y + 46, 0xFF1A2430);
        g.fill(x + 4, y + 40, x + 4 + (int) Math.round(100 * Math.min(1, v(m, 1) / cap)), y + 46,
                m.rush() ? 0xFFFFB060 : L.accent());
        text(g, MegaScreen.tr(P, m.rush() ? "rush_on" : "rush_off"), x + 4, y + 52, m.rush() ? 0xFFFFB060 : L.dim(),
                false);
    }
}
