package com.raishxn.gtia.mega.common.ui;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Painter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Kit of the megastructure screens from the Star Lifter on (GTIA-D-316): every machine draws its own window (a PNG
 * cut to its silhouette, made by design/estudos/mega_ui_frame.py) and lays out its own controls — levers, rotary
 * switches, ring gauges, tanks, spectra, tickers — instead of sharing one window.
 */
public final class MegaUI {

    /** Button outlines a machine can choose. */
    public enum Shape {
        HEX, CHEVRON, CAPSULE, NOTCH
    }

    /** Colours (ARGB), art folder and button outline of one machine. */
    public record Look(String dir, int accent, int glow, int base, int line, int dim, Shape button) {

        public ResourceTexture tex(String name) {
            return new ResourceTexture("gtia:textures/gui/mega/" + dir + "/" + name + ".png");
        }
    }

    private MegaUI() {}

    // ------------------------------------------------------------------ window

    /** The window: its art and the machine's name on the title plate at (tx, ty). */
    public static WidgetGroup window(Look l, MegaMachine m, int w, int h, int tx, int ty) {
        var root = new WidgetGroup(0, 0, w, h);
        root.addWidget(new ImageWidget(0, 0, w, h, l.tex("frame")));
        root.addWidget(paint(tx, ty, w - tx, 12, (g, x, y) -> OriginalScreen.text(g,
                m.getBlockState().getBlock().getName().getString(), x, y, l.glow(), true)));
        return root;
    }

    public static ModularUI ui(WidgetGroup root, MegaMachine m, Player player) {
        return new ModularUI(root.getSizeWidth(), root.getSizeHeight(), m, player).widget(root);
    }

    public static Widget paint(int x, int y, int w, int h, Painter painter) {
        return new OriginalScreen.Paint(x, y, w, h, painter);
    }

    // ------------------------------------------------------------------ controls

    /** A button in the machine's outline: label inside, lit while {@code on}. */
    public static Widget button(Look l, int x, int y, int w, int h, String labelKey, BooleanSupplier on,
                                Consumer<ClickData> click) {
        var b = new ButtonWidget(x, y, w, h, IGuiTexture.EMPTY, click) {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                var p = getPosition();
                boolean over = isMouseOverElement(mx, my), lit = on.getAsBoolean();
                int fill = lit ? OriginalScreen.dim(l.accent(), 0.55) : over ? OriginalScreen.dim(l.line(), 1.3) :
                        l.base();
                outline(g, l.button(), p.x, p.y, w, h, fill, lit || over ? l.accent() : l.line());
                OriginalScreen.centre(g, Component.translatable(labelKey).getString(), p.x + w / 2,
                        p.y + (h - 8) / 2, lit ? l.glow() : over ? l.glow() : l.dim());
            }
        };
        b.setHoverTooltips(labelKey + ".tip");
        return b;
    }

    @OnlyIn(Dist.CLIENT)
    static void outline(GuiGraphics g, Shape s, int x, int y, int w, int h, int fill, int edge) {
        for (int yy = 0; yy < h; yy++) {
            int inset = switch (s) {
                case HEX -> Math.abs(yy - h / 2) * (h / 3) / Math.max(1, h / 2);
                case CAPSULE -> (int) Math.round(h / 2.0 - Math.sqrt(Math.max(0, h * h / 4.0 -
                        (yy - h / 2.0 + 0.5) * (yy - h / 2.0 + 0.5))));
                case NOTCH -> yy < 3 ? 3 - yy : 0;
                case CHEVRON -> 0;
            };
            int shift = s == Shape.CHEVRON ? Math.abs(yy - h / 2) / 2 : 0;
            int x0 = x + inset + shift, x1 = x + w - inset + (s == Shape.CHEVRON ? -(h / 4) + shift : 0);
            boolean border = yy == 0 || yy == h - 1;
            g.fill(x0, y + yy, x1, y + yy + 1, border ? edge : fill);
            g.fill(x0, y + yy, x0 + 1, y + yy + 1, edge);
            g.fill(x1 - 1, y + yy, x1, y + yy + 1, edge);
        }
    }

    /**
     * A vertical lever of height h: the handle sits at {@code frac} (0 bottom, 1 top); a click on the upper half
     * runs {@code up}, on the lower half {@code down}.
     */
    public static Widget lever(Look l, int x, int y, int h, DoubleSupplier frac, Consumer<ClickData> up,
                               Consumer<ClickData> down, String tipKey) {
        var group = new WidgetGroup(x, y, 16, h);
        group.addWidget(paint(0, 0, 16, h, (g, px, py) -> {
            g.fill(px + 6, py + 2, px + 10, py + h - 2, OriginalScreen.dim(l.base(), 0.6));
            g.fill(px + 7, py + 3, px + 9, py + h - 3, l.line());
            for (int k = 0; k <= 10; k++) {
                int ty = py + 3 + (h - 6) * k / 10;
                g.fill(px + (k % 5 == 0 ? 1 : 3), ty, px + 5, ty + 1, l.dim());
            }
            double f = Math.max(0, Math.min(1, frac.getAsDouble()));
            int hy = py + 3 + (int) Math.round((h - 12) * (1 - f));
            g.fill(px + 2, hy, px + 14, hy + 6, l.accent());
            g.fill(px + 3, hy + 1, px + 13, hy + 2, l.glow());
            g.fill(px + 2, hy + 6, px + 14, hy + 7, OriginalScreen.dim(l.accent(), 0.4));
        }));
        group.addWidget(OriginalScreen.hit(0, 0, 16, h / 2, up).setHoverTooltips(tipKey + ".up"));
        group.addWidget(OriginalScreen.hit(0, h / 2, 16, h - h / 2, down).setHoverTooltips(tipKey + ".down"));
        return group;
    }

    /** A rotary switch of radius r: the pointer at position {@code pos} of {@code count}; a click turns it. */
    public static Widget knob(Look l, int cx, int cy, int r, IntSupplier pos, int count, Supplier<String[]> labels,
                              Consumer<ClickData> turn, String tipKey) {
        int size = r * 2 + 40;
        var group = new WidgetGroup(cx - size / 2, cy - r - 12, size, r * 2 + 24);
        group.addWidget(paint(0, 0, size, r * 2 + 24, (g, px, py) -> {
            int ox = px + size / 2, oy = py + r + 12;
            OriginalScreen.disc(g, ox, oy, r, OriginalScreen.dim(l.base(), 1.4));
            OriginalScreen.ellipse(g, ox, oy, r, r, l.line(), 0);
            String[] names = labels.get();
            for (int k = 0; k < count; k++) {
                double a = Math.toRadians(-135 + 270.0 * k / Math.max(1, count - 1));
                int lx = ox + (int) Math.round(Math.sin(a) * (r + 4)), ly = oy - (int) Math.round(Math.cos(a) *
                        (r + 4));
                boolean here = k == pos.getAsInt();
                OriginalScreen.disc(g, lx, ly, 1, here ? l.glow() : l.dim());
                if (names != null && k < names.length)
                    OriginalScreen.centre(g, names[k], ox + (int) Math.round(Math.sin(a) * (r + 15)),
                            oy - (int) Math.round(Math.cos(a) * (r + 11)) - 4, here ? l.glow() : l.dim());
            }
            double a = Math.toRadians(-135 + 270.0 * pos.getAsInt() / Math.max(1, count - 1));
            OriginalScreen.line(g, ox, oy, ox + (int) Math.round(Math.sin(a) * (r - 2)),
                    oy - (int) Math.round(Math.cos(a) * (r - 2)), l.accent());
            OriginalScreen.disc(g, ox, oy, 2, l.accent());
        }));
        group.addWidget(OriginalScreen.hit(size / 2 - r, 12, r * 2, r * 2, turn).setHoverTooltips(tipKey));
        return group;
    }

    // ------------------------------------------------------------------ painters

    /** A ring gauge: track, filled arc for {@code frac} (from the top, clockwise), label and value inside. */
    @OnlyIn(Dist.CLIENT)
    public static void ring(GuiGraphics g, Look l, int cx, int cy, int r, double frac, int colour, String label,
                            String value) {
        int n = (int) (2 * Math.PI * r * 1.6);
        for (int k = 0; k < n; k++) {
            double f = k / (double) n, a = 2 * Math.PI * f;
            int c = f <= frac ? colour : OriginalScreen.dim(l.line(), 0.8);
            for (int t = 0; t < 3; t++)
                OriginalScreen.px(g, cx + (int) Math.round(Math.sin(a) * (r - t)),
                        cy - (int) Math.round(Math.cos(a) * (r - t)), c);
        }
        OriginalScreen.centre(g, value, cx, cy - 4, l.glow());
        OriginalScreen.centre(g, label, cx, cy + r + 3, l.dim());
    }

    /** A tank of fluid at {@code frac}, with a slow wave on top. */
    @OnlyIn(Dist.CLIENT)
    public static void tank(GuiGraphics g, Look l, int x, int y, int w, int h, double frac, int colour) {
        g.fill(x, y, x + w, y + h, OriginalScreen.dim(l.base(), 0.5));
        int top = y + h - (int) Math.round(h * Math.max(0, Math.min(1, frac)));
        double t = com.raishxn.gtia.mega.common.client.MegaArt.time();
        for (int xx = x + 1; xx < x + w - 1; xx++) {
            int wave = (int) Math.round(Math.sin(xx * 0.6 + t * 3) * 1.2);
            int yy = Math.max(top + wave, y + 1);
            if (yy < y + h - 1) g.fill(xx, yy, xx + 1, y + h - 1, OriginalScreen.dim(colour, 0.75));
            if (yy < y + h - 1) g.fill(xx, yy, xx + 1, yy + 1, OriginalScreen.lighter(colour));
        }
        for (int k = 1; k < 4; k++) g.fill(x + w - 3, y + h * k / 4, x + w, y + h * k / 4 + 1, l.dim());
        OriginalScreen.ellipse(g, x + w / 2, y, w / 2.0, 1, l.line(), 0);
    }

    /** Vertical spectrum bars (log scale), one per value, labels under them. */
    @OnlyIn(Dist.CLIENT)
    public static void spectrum(GuiGraphics g, Look l, int x, int y, int w, int h, double[] values, int[] colours,
                                String[] labels) {
        int n = values.length, bw = Math.max(2, w / n - 2);
        double max = 1;
        for (double v : values) max = Math.max(max, v);
        for (int i = 0; i < n; i++) {
            int bx = x + i * (w / n) + 1;
            double f = values[i] <= 0 ? 0 : Math.log10(1 + values[i]) / Math.log10(1 + max);
            int bh = (int) Math.round(h * f);
            for (int k = 0; k < bh; k += 2) g.fill(bx, y + h - k - 1, bx + bw, y + h - k, colours[i % colours.length]);
            g.fill(bx, y + h, bx + bw, y + h + 1, l.line());
            if (labels != null && i < labels.length)
                OriginalScreen.centre(g, labels[i], bx + bw / 2, y + h + 3, l.dim());
        }
    }

    /** A line of text that scrolls when it does not fit. */
    @OnlyIn(Dist.CLIENT)
    public static void ticker(GuiGraphics g, Look l, int x, int y, int w, String s, int colour) {
        var font = Minecraft.getInstance().font;
        int tw = font.width(s);
        g.enableScissor(x, y - 1, x + w, y + 9);
        int off = tw <= w ? 0 : (int) ((com.raishxn.gtia.mega.common.client.MegaArt.time() * 25) % (tw + 30));
        g.drawString(font, s, x - off, y, colour, false);
        if (tw > w) g.drawString(font, s, x - off + tw + 30, y, colour, false);
        g.disableScissor();
    }

    /** Big seven-segment digits (and . - e +), 6 px wide each. */
    @OnlyIn(Dist.CLIENT)
    public static void digits(GuiGraphics g, int x, int y, String s, int colour) {
        // Segments a b c d e f g as bits 0..6.
        int[] map = new int[128];
        String[] defs = { "0abcdef", "1bc", "2abdeg", "3abcdg", "4bcfg", "5acdfg", "6acdefg", "7abc", "8abcdefg",
                "9abcdfg", "-g", "eadefg", "Eadefg" };
        for (String d : defs) {
            int bits = 0;
            for (char c : d.substring(1).toCharArray()) bits |= 1 << (c - 'a');
            map[d.charAt(0)] = bits;
        }
        int cx = x;
        for (char ch : s.toCharArray()) {
            if (ch == '.' || ch == ',') {
                g.fill(cx, y + 8, cx + 1, y + 9, colour);
                cx += 3;
                continue;
            }
            if (ch == '+' || ch == ' ') {
                cx += 4;
                continue;
            }
            int b = ch < 128 ? map[ch] : 0;
            int off = OriginalScreen.dim(colour, 0.18);
            seg(g, cx + 1, y, cx + 4, y + 1, (b & 1) != 0 ? colour : off);
            seg(g, cx + 4, y + 1, cx + 5, y + 4, (b & 2) != 0 ? colour : off);
            seg(g, cx + 4, y + 5, cx + 5, y + 8, (b & 4) != 0 ? colour : off);
            seg(g, cx + 1, y + 8, cx + 4, y + 9, (b & 8) != 0 ? colour : off);
            seg(g, cx, y + 5, cx + 1, y + 8, (b & 16) != 0 ? colour : off);
            seg(g, cx, y + 1, cx + 1, y + 4, (b & 32) != 0 ? colour : off);
            seg(g, cx + 1, y + 4, cx + 4, y + 5, (b & 64) != 0 ? colour : off);
            cx += 7;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void seg(GuiGraphics g, int x0, int y0, int x1, int y1, int c) {
        g.fill(x0, y0, x1, y1, c);
    }

    // ------------------------------------------------------------------ overlay manual

    /**
     * The field manual as an overlay drawn in the machine's colours: {@code lines} keys {@code <prefix>.ui.manual.<i>}
     * ("=" formula, "#" heading), {@code perPage} to a page; shown while {@code nav.open} is "manual" (so GTNA's dev
     * commands {@code /gtna_dev_panel} and {@code /gtna_dev_page} drive it too).
     */
    public static WidgetGroup manual(Look l, int x, int y, int w, int h, String prefix, int lines, int perPage,
                                     OriginalScreen.Nav nav) {
        return overlay(x, y, w, h, () -> "manual".equals(nav.open), manualPainter(l, w, h, prefix, lines, perPage,
                nav), group -> {
                    int pages = (lines + perPage - 1) / perPage;
                    group.addWidget(OriginalScreen.hit(w / 2 - 40, h - 18, 26, 14,
                            c -> nav.page = Math.max(0, nav.page - 1)));
                    group.addWidget(OriginalScreen.hit(w / 2 + 14, h - 18, 26, 14,
                            c -> nav.page = Math.min(pages - 1, nav.page + 1)));
                    group.addWidget(OriginalScreen.hit(w - 16, 1, 14, 12, c -> nav.open = null));
                });
    }

    /** A group shown only while {@code open}, painted by {@code painter}, with extra widgets. */
    public static WidgetGroup overlay(int x, int y, int w, int h, BooleanSupplier open, Painter painter,
                                      Consumer<WidgetGroup> extra) {
        var group = new WidgetGroup(x, y, w, h) {

            @Override
            public boolean isVisible() {
                return open.getAsBoolean();
            }

            @Override
            public boolean isActive() {
                return open.getAsBoolean();
            }
        };
        group.addWidget(paint(0, 0, w, h, painter));
        extra.accept(group);
        return group;
    }

    private static Painter manualPainter(Look l, int w, int h, String prefix, int lines, int perPage,
                                         OriginalScreen.Nav nav) {
        int pages = (lines + perPage - 1) / perPage;
        return (g, px, py) -> {
            g.fill(px, py, px + w, py + h, OriginalScreen.dim(l.base(), 0.85) | 0xF0000000);
            outline(g, l.button(), px, py, w, 14, l.base(), l.accent());
            OriginalScreen.text(g, Component.translatable(prefix + ".ui.manual_title").getString(), px + 8,
                    py + 3, l.glow(), true);
            g.fill(px, py + h - 1, px + w, py + h, l.accent());
            var font = Minecraft.getInstance().font;
            int pg = Math.min(nav.page, pages - 1), ly = py + 20;
            for (int i = pg * perPage; i < Math.min(lines, (pg + 1) * perPage); i++) {
                String s = Component.translatable(prefix + ".ui.manual." + i).getString();
                boolean formula = s.startsWith("="), heading = s.startsWith("#");
                if (formula || heading) s = s.substring(1).trim();
                var split = font.split(Component.literal(s), w - 20);
                if (formula) g.fill(px + 8, ly - 2, px + w - 8, ly + split.size() * 10, OriginalScreen.dim(l.base(),
                        0.4) | 0xFF000000);
                for (var line : split) {
                    if (ly > py + h - 24) break;
                    g.drawString(font, line, px + 10, ly, formula ? l.glow() : heading ? l.accent() : 0xFFE0E0E4,
                            heading);
                    ly += 10;
                }
                ly += 3;
            }
            OriginalScreen.centre(g, "< " + (pg + 1) + " / " + pages + " >", px + w / 2, py + h - 14, l.accent());
        };
    }
}
