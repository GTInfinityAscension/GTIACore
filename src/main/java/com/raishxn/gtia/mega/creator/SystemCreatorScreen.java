package com.raishxn.gtia.mega.creator;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtia.mega.common.MegaScreen;
import com.raishxn.gtia.mega.common.client.MegaArt;
import com.raishxn.gtia.mega.common.client.MegaFx;
import com.raishxn.gtia.mega.common.ui.MegaUI;
import com.raishxn.gtia.mega.common.ui.MegaUI.Look;
import com.raishxn.gtia.mega.creator.CreatorPhysics.Stage;
import com.raishxn.gtia.mega.system.StarPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;

import java.util.Locale;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Interstellar System Creator screen, an octagonal foundry console: the forge core in a round window with the four
 * stages around it; a Hertzsprung–Russell chart to pick the star; a metallicity switch; the orrery of the seven
 * systems to select and travel to; the Dimensional era.
 */
public final class SystemCreatorScreen {

    static final String P = "gtia.system_creator";
    static final Look L = new Look("creator", 0xFFB48CFF, 0xFFEADCFF, 0xFF1A1430, 0xFF4A3A78, 0xFF9A8CC0,
            MegaUI.Shape.NOTCH);
    private static final int MANUAL_LINES = 20, PAGE_LINES = 5;

    private SystemCreatorScreen() {}

    public static ModularUI create(SystemCreatorMachine m, Player player) {
        Nav nav = new Nav();
        var root = MegaUI.window(L, m, 300, 236, 106, 9);
        root.addWidget(MegaUI.paint(90, 40, 120, 120, (g, x, y) -> core(g, m, x + 60, y + 60)));
        root.addWidget(MegaUI.paint(10, 22, 62, 58, (g, x, y) -> hrChart(g, m, x, y)));
        root.addWidget(hit(10, 22, 62, 58, c -> MegaScreen.act(m, "kind", c)).setHoverTooltips(P + ".ui.kind.tip"));
        root.addWidget(MegaUI.knob(L, 262, 48, 14, m::zIndex, CreatorPhysics.METALLICITY.length,
                () -> new String[] { "½", "1", "2", "3" }, c -> MegaScreen.act(m, "metal", c), P + ".ui.metal.tip"));
        root.addWidget(MegaUI.paint(232, 74, 60, 12, (g, x, y) -> centre(g, MegaScreen.tr(P, "metal",
                CreatorPhysics.METALLICITY[m.zIndex()]), x + 28, y, L.dim())));
        root.addWidget(MegaUI.paint(9, 127, 64, 30, (g, x, y) -> cost(g, m, x, y)));
        root.addWidget(MegaUI.paint(227, 127, 64, 30, (g, x, y) -> era(g, m, x, y)));
        root.addWidget(MegaUI.paint(8, 166, 284, 44, (g, x, y) -> orrery(g, m, x, y)));
        for (int i = -1; i < StarSystems.SLOTS; i++) {
            final int slot = i;
            root.addWidget(hit(13 + (i + 1) * 40, 167, 30, 30, c -> MegaScreen.act(m, "select_" + slot, c)));
        }
        root.addWidget(MegaUI.button(L, 8, 215, 54, 13, P + ".ui.forge", () -> m.stage() != Stage.IDLE,
                c -> MegaScreen.act(m, "forge", c)));
        root.addWidget(MegaUI.button(L, 65, 215, 54, 13, P + ".ui.cancel", () -> false,
                c -> MegaScreen.act(m, "cancel", c)));
        root.addWidget(MegaUI.button(L, 123, 215, 54, 13, P + ".ui.travel", () -> false, c -> {
            if (!c.isRemote && player instanceof ServerPlayer sp) m.travel(sp, m.selected());
        }));
        root.addWidget(MegaUI.button(L, 181, 215, 54, 13, P + ".ui.manual", () -> "manual".equals(nav.open),
                c -> nav.toggle("manual")));
        root.addWidget(MegaUI.button(L, 238, 215, 54, 13, "gtna.ui.o.power", m::isWorkingEnabled, c -> {
            if (!c.isRemote) m.setWorkingEnabled(!m.isWorkingEnabled());
        }));
        root.addWidget(MegaUI.manual(L, 8, 22, 284, 188, P, MANUAL_LINES, PAGE_LINES, nav));
        return MegaUI.ui(root, m, player);
    }

    static String kindName(Kind k) {
        return MegaScreen.tr(P, "kind." + k.name().toLowerCase(Locale.ROOT));
    }

    // ------------------------------------------------------------------ the forge core

    @OnlyIn(Dist.CLIENT)
    private static void core(GuiGraphics g, SystemCreatorMachine m, int cx, int cy) {
        Stage s = m.stage();
        double t = MegaArt.time();
        Kind k = m.kind();
        double p = m.progress();
        // Four stage arcs round the window: gather, collapse, ignite, settle.
        double[] edges = { 0, 0.4, 0.7, 0.85, 1 };
        int[] colours = { 0xFF7FA8FF, 0xFFFF7AD0, 0xFFFFD27A, 0xFFB48CFF };
        for (int a = 0; a < 4; a++) {
            int n = (int) (2 * Math.PI * 58 * (edges[a + 1] - edges[a]) * 1.6);
            for (int i = 0; i < n; i++) {
                double f = edges[a] + (edges[a + 1] - edges[a]) * i / n;
                double ang = 2 * Math.PI * f;
                int c = f <= p ? colours[a] : dim(colours[a], 0.25);
                for (int w = 0; w < 3; w++) px(g, cx + (int) Math.round(Math.sin(ang) * (58 - w)),
                        cy - (int) Math.round(Math.cos(ang) * (58 - w)), c);
            }
        }
        if (!m.isFormed()) {
            centre(g, net.minecraft.network.chat.Component.translatable("gtceu.multiblock.invalid_structure")
                    .getString(), cx, cy - 4, RED);
            return;
        }
        switch (s) {
            case IDLE -> {
                for (int i = 0; i < 40; i++) {
                    double a = i * 2.39996 + t * 0.1, r = 8 + (i * 37 % 40);
                    px(g, cx + (int) (Math.cos(a) * r), cy + (int) (Math.sin(a) * r), dim(L.dim(), 0.5));
                }
                centre(g, MegaScreen.tr(P, "idle"), cx, cy - 4, L.dim());
            }
            case GATHER -> {
                // Gas spirals in from the edge.
                double fill = m.gathered() / (double) CreatorPhysics.hydrogen(k);
                for (int i = 0; i < 260; i++) {
                    double a = i * 2.39996 + t * (0.6 + (i % 7) * 0.05), r = 50 * (1 - ((i * 0.618 + t * 0.15) % 1));
                    int c = i % 3 == 0 ? 0xFFFF7AD0 : 0xFF7FA8FF;
                    if (i < 60 + 200 * fill) px(g, cx + (int) (Math.cos(a) * r), cy + (int) (Math.sin(a) * r * 0.8),
                            dim(c, 0.35 + 0.65 * (1 - r / 50)));
                }
            }
            case COLLAPSE -> {
                int r = (int) Math.round(28 - 16 * (p - 0.4) / 0.3);
                for (int i = 0; i < 300; i++) {
                    double rr = r + 4 + (i % 30), a = i * 0.21 + t * 2.0 * Math.pow(r / rr, 1.5);
                    px(g, cx + (int) (Math.cos(a) * rr), cy + (int) (Math.sin(a) * rr * 0.25), 0xFFFFB070);
                }
                MegaArt.star(g, cx, cy, Math.max(4, r / 2), 1800 + 1700 * (p - 0.4) / 0.3);
                MegaArt.jetVertical(g, cx, cy - 6, -1, 44, 1, 4, new float[] { 0.8f, 0.9f, 1 },
                        new float[] { 0.5f, 0.3f, 1 });
                MegaArt.jetVertical(g, cx, cy + 6, 1, 44, 1, 4, new float[] { 0.8f, 0.9f, 1 },
                        new float[] { 0.5f, 0.3f, 1 });
            }
            case IGNITE, STABILIZE -> {
                if (k == Kind.BLACK_HOLE && s == Stage.STABILIZE) MegaArt.blackHole(g, cx, cy, 10, 9000);
                else {
                    int r = k == Kind.NEUTRON_STAR ? 5 : (int) Math.round(18 * StarPhysics.drawnScale(k, k.mass));
                    MegaArt.star(g, cx, cy, Math.max(4, r), StarPhysics.temperature(k, k.mass));
                }
                if (s == Stage.IGNITE) MegaArt.beam(g, cx, cy + 54, cx, cy + 20, 0xFFFFF0B0);
            }
        }
        if (s != Stage.IDLE) centre(g, MegaScreen.tr(P, "stage." + s.name().toLowerCase(Locale.ROOT)) +
                String.format(" %.0f%%", p * 100), cx, cy + 40, L.glow());
        String msg = m.message();
        if (!msg.isEmpty()) centre(g, MegaScreen.tr(P, "msg." + msg), cx, cy + 28, msg.equals("made") ? GREEN :
                YELLOW);
    }

    /** Hertzsprung–Russell chart: hot on the left, bright at the top; each kind a dot in its colour. */
    @OnlyIn(Dist.CLIENT)
    private static void hrChart(GuiGraphics g, SystemCreatorMachine m, int x, int y) {
        g.fill(x + 6, y + 50, x + 60, y + 51, L.line());
        g.fill(x + 6, y + 4, x + 7, y + 51, L.line());
        for (Kind k : CreatorPhysics.KINDS) {
            double temp = Math.max(2000, Math.min(40000, StarPhysics.temperature(k, k.mass)));
            double lum = Math.max(1e-3, StarPhysics.luminosity(k, k.mass));
            int px = x + 58 - (int) Math.round((Math.log10(temp) - 3.3) / 1.3 * 50);
            int py = y + 48 - (int) Math.round((Math.log10(lum) + 3) / 8 * 42);
            if (k == Kind.BLACK_HOLE) {
                px = x + 12;
                py = y + 46;
            }
            boolean on = k == m.kind();
            int c = k == Kind.BLACK_HOLE ? 0xFF000000 : MegaArt.rgb(MegaFx.blackbody(temp), 1);
            disc(g, px, py, on ? 3 : 2, c);
            if (k == Kind.BLACK_HOLE) ellipse(g, px, py, 3, 3, 0xFFFFC080, 0);
            if (on) ellipse(g, px, py, 5, 5, L.glow(), 0);
            boolean locked = CreatorPhysics.phase(k) > Math.max(1, m.phases());
            if (locked) px(g, px + 3, py - 3, RED);
        }
        text(g, kindName(m.kind()), x + 2, y + 52, L.glow(), false);
    }

    @OnlyIn(Dist.CLIENT)
    private static void cost(GuiGraphics g, SystemCreatorMachine m, int x, int y) {
        Kind k = m.kind();
        text(g, String.format("H %.1fM mB", CreatorPhysics.hydrogen(k) / 1e6), x + 3, y + 2, 0xFF7FA8FF, false);
        text(g, "Ψ " + sci(CreatorPhysics.psi(k, m.metallicity())), x + 3, y + 11, L.glow(), false);
        text(g, "Fe " + CreatorPhysics.dust(k, m.metallicity()), x + 3, y + 20, 0xFFC0A090, false);
    }

    @OnlyIn(Dist.CLIENT)
    private static void era(GuiGraphics g, SystemCreatorMachine m, int x, int y) {
        int made = (int) m.systems()[21];
        int lvl = CreatorPhysics.era(made);
        centre(g, MegaScreen.tr(P, "era"), x + 31, y + 2, L.dim());
        centre(g, lvl == 0 ? "-" : "I".repeat(lvl), x + 31, y + 12, lvl > 0 ? L.accent() : L.dim());
        centre(g, MegaScreen.tr(P, "made", made), x + 31, y + 21, L.dim());
    }

    /** The seven systems as small suns; the selected one with its facts. */
    @OnlyIn(Dist.CLIENT)
    private static void orrery(GuiGraphics g, SystemCreatorMachine m, int x, int y) {
        double[] v = m.systems();
        for (int i = -1; i < StarSystems.SLOTS; i++) {
            int e = (i + 1) * 3;
            int cx = x + 20 + (i + 1) * 40, cy = y + 15;
            boolean sel = i == m.selected();
            int kind = (int) v[e];
            if (kind < 0) {
                ellipse(g, cx, cy, 6, 6, L.line(), 3);
            } else {
                Kind k = Kind.values()[kind];
                if (k == Kind.BLACK_HOLE) MegaArt.blackHole(g, cx, cy, 4, 8000);
                else MegaArt.star(g, cx, cy, k == Kind.NEUTRON_STAR ? 2 : Math.max(3,
                        (int) Math.round(7 * StarPhysics.drawnScale(k, v[e + 1]))), StarPhysics.temperature(k, v[e + 1]));
            }
            if (sel) ellipse(g, cx, cy, 13, 13, L.glow(), 0);
            if (i == m.targetSlot() && m.stage() != Stage.IDLE) ellipse(g, cx, cy, 11, 11, 0xFFFFD27A, 2);
            centre(g, i < 0 ? "N" : String.valueOf(i + 1), cx, cy + 15, sel ? L.glow() : L.dim());
        }
        int s = m.selected(), e = (s + 1) * 3;
        String info = v[e] < 0 ? MegaScreen.tr(P, "empty") : kindName(Kind.values()[(int) v[e]]) +
                String.format(" · %.2f M☉ · Z %.1f", v[e + 1], v[e + 2]);
        right(g, info, x + 280, y + 34, L.dim());
    }
}
