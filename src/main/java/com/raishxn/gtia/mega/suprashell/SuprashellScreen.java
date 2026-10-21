package com.raishxn.gtia.mega.suprashell;

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
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Suprashell screen, an eye: the gravity well (Flamm's paraboloid) with the shell drawn at its radius and its clocks'
 * rate; the hosted modules; the room left; the recipe ticker.
 */
public final class SuprashellScreen {

    static final String P = "gtia.suprashell";
    static final Look L = new Look("suprashell", 0xFFB08CFF, 0xFFEDE4FF, 0xFF0E0A1A, 0xFF3E2E66, 0xFFA898C8,
            MegaUI.Shape.CAPSULE);
    private static final int MANUAL_LINES = 12, PAGE_LINES = 4;

    private SuprashellScreen() {}

    public static ModularUI create(SuprashellMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 300, 204, 106, 11);
        root.addWidget(MegaUI.paint(26, 38, 124, 92, (g, x, y) -> well(g, m, x, y)));
        root.addWidget(MegaUI.button(L, 26, 136, 60, 14, P + ".ui.closer", () -> false,
                c -> MegaScreen.act(m, "closer", c)));
        root.addWidget(MegaUI.button(L, 90, 136, 60, 14, P + ".ui.farther", () -> false,
                c -> MegaScreen.act(m, "farther", c)));
        root.addWidget(MegaUI.paint(158, 38, 118, 92, (g, x, y) -> modules(g, m, x, y)));
        root.addWidget(MegaUI.paint(158, 136, 118, 14, (g, x, y) -> room(g, m, x, y)));
        root.addWidget(MegaUI.paint(26, 156, 250, 12, (g, x, y) -> MegaUI.ticker(g, L, x + 3, y + 2, 244, ticker(m),
                0xFFD8C8FF)));
        root.addWidget(MegaUI.button(L, 92, 176, 56, 14, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 152, 176, 56, 14, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 24, 30, 252, 140, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    private static double v(SuprashellMachine m, int i) {
        double[] v = m.view();
        return i < v.length ? v[i] : 0;
    }

    private static String ticker(SuprashellMachine m) {
        if (m.recipe().isEmpty())
            return MegaScreen.tr(P, "idle." + (m.blocked().isEmpty() ? "no_recipe" : m.blocked()));
        return m.recipe().substring(m.recipe().indexOf(':') + 1) + "  ·  " +
                MegaScreen.tr(P, "crafts", String.format("%.0f", v(m, 5)), sci(v(m, 6)));
    }

    /**
     * The well seen edge-on: depth z = 2 sqrt(rs (r - rs)) (Flamm), the hole on the left; the shell where the player
     * holds it, its clocks' rate tau beside it.
     */
    @OnlyIn(Dist.CLIENT)
    private static void well(GuiGraphics g, SuprashellMachine m, int x, int y) {
        int x0 = x + 10, y0 = y + 12, w = 108, h = 66;
        double rMax = ShellPhysics.R_MAX;
        int prev = -1;
        for (int i = 0; i <= w; i++) {
            double r = 1 + (rMax - 1) * i / w;
            double z = 2 * Math.sqrt(r - 1) / (2 * Math.sqrt(rMax - 1));
            int py = y0 + h - (int) Math.round(h * z);
            if (prev >= 0) line(g, x0 + i - 1, prev, x0 + i, py, L.line());
            prev = py;
        }
        MegaArt.blackHole(g, x0, y0 + h, 5, 8000);
        double r = m.radius();
        int sx = x0 + (int) Math.round(w * (r - 1) / (rMax - 1));
        int sy = y0 + h - (int) Math.round(h * Math.sqrt(r - 1) / Math.sqrt(rMax - 1));
        g.fill(sx - 1, sy - 6, sx + 2, sy + 2, L.accent());
        glow(g, sx, sy - 2, 5, L.glow(), 0.6);
        text(g, MegaScreen.tr(P, "radius", String.format("%.1f", r)), x + 3, y + 2, L.accent(), false);
        right(g, MegaScreen.tr(P, "tau", String.format("%.3f", ShellPhysics.tau(r))), x + 121, y + 2, L.dim());
        right(g, MegaScreen.tr(P, "lowering", String.format("%.0f%%", ShellPhysics.loweringShare(r) * 100)),
                x + 121, y + 82, L.dim());
    }

    /** The hosted modules, one line a kind. */
    @OnlyIn(Dist.CLIENT)
    private static void modules(GuiGraphics g, SuprashellMachine m, int x, int y) {
        text(g, MegaScreen.tr(P, "hosted"), x + 3, y + 2, L.accent(), false);
        String[] lines = m.modules().isEmpty() ? new String[0] : m.modules().split("\n");
        if (lines.length == 0) {
            text(g, MegaScreen.tr(P, "none"), x + 3, y + 16, L.dim(), false);
            text(g, MegaScreen.tr(P, "none2"), x + 3, y + 26, L.dim(), false);
        }
        for (int i = 0; i < Math.min(7, lines.length); i++) {
            String[] kv = lines[i].split("=");
            String name = Component.translatable(kv[0]).getString();
            if (name.length() > 18) name = name.substring(0, 17) + "…";
            text(g, name, x + 3, y + 14 + i * 10, L.glow(), false);
            right(g, "x" + (kv.length > 1 ? kv[1] : "?"), x + 115, y + 14 + i * 10, L.dim());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void room(GuiGraphics g, SuprashellMachine m, int x, int y) {
        double used = v(m, 0), cap = Math.max(1, v(m, 1));
        g.fill(x + 3, y + 4, x + 115, y + 10, 0xFF1C1430);
        g.fill(x + 3, y + 4, x + 3 + (int) Math.round(112 * Math.min(1, used / cap)), y + 10,
                used > cap ? 0xFFFF6060 : L.accent());
        centre(g, MegaScreen.tr(P, "room", (int) used, (int) cap), x + 59, y + 3, 0xFFFFFFFF);
    }
}
