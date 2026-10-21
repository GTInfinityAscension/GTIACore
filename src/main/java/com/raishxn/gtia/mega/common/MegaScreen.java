package com.raishxn.gtia.mega.common;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Nav;
import com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.Theme;

import java.util.function.Supplier;

import static com.raishxn.gtna.common.machine.multiblock.originals.ui.OriginalScreen.*;

/**
 * Shared pieces of the megastructure screens on GTNA's original-multiblock window: the paged field manual (lang keys
 * {@code <prefix>.manual.<i>}, a leading "=" marks a formula, "#" a heading) and a table panel.
 */
public final class MegaScreen {

    private MegaScreen() {}

    public static String tr(String prefix, String key, Object... args) {
        return Component.translatable(prefix + ".ui." + key, args).getString();
    }

    /** Runs a screen action on the server copy of the machine only. */
    public static void act(MegaMachine m, String data, ClickData click) {
        if (!click.isRemote) m.handleDisplayClick(data, click);
    }

    /**
     * The field manual: {@code lines} lang keys {@code <prefix>.ui.manual.<i>}, {@code perPage} to a page, with
     * page arrows.
     */
    public static WidgetGroup manual(Theme t, Nav nav, String prefix, String titleKey, int lines, int perPage) {
        int pages = (lines + perPage - 1) / perPage;
        var group = panel(t, nav, "manual", titleKey, 292, 200, "slate", (g, x, y) -> {
            var font = net.minecraft.client.Minecraft.getInstance().font;
            int page = Math.min(nav.page, pages - 1);
            int ly = y + 20;
            for (int i = page * perPage; i < Math.min(lines, (page + 1) * perPage); i++) {
                String s = Component.translatable(prefix + ".ui.manual." + i).getString();
                boolean formula = s.startsWith("="), heading = s.startsWith("#");
                if (formula || heading) s = s.substring(1).trim();
                var split = font.split(Component.literal(s), 272);
                if (formula) g.fill(x + 8, ly - 2, x + 284, ly + split.size() * 10, 0xFF0A1420);
                for (var l : split) {
                    if (ly > y + 176) break;
                    g.drawString(font, l, x + 10, ly, formula ? 0xFF9FE8FF : heading ? t.accent() : 0xFFE0E0E4,
                            heading);
                    ly += 10;
                }
                ly += 3;
            }
            centre(g, "< " + (page + 1) + " / " + pages + " >", x + 146, y + 186, t.accent());
        });
        group.addWidget(hit(110, 182, 22, 14, c -> nav.page = Math.max(0, nav.page - 1)));
        group.addWidget(hit(160, 182, 22, 14, c -> nav.page = Math.min(pages - 1, nav.page + 1)));
        return group;
    }

    /** A wide panel with a table: header row, then the rows the supplier gives (first column left, others right). */
    public static WidgetGroup table(Theme t, Nav nav, String name, String titleKey, String[] header, int[] columns,
                                    Supplier<String[][]> rows, Supplier<int[]> colours) {
        return panel(t, nav, name, titleKey, 380, 170, "panel_wide", (g, x, y) -> {
            int ly = y + 20;
            for (int c = 0; c < header.length; c++) cell(g, header[c], x, ly, columns, c, t.accent());
            g.fill(x + 6, ly + 10, x + 374, ly + 11, t.line());
            ly += 15;
            String[][] r = rows.get();
            int[] col = colours.get();
            for (int i = 0; i < r.length && ly < y + 160; i++, ly += 11)
                for (int c = 0; c < r[i].length; c++)
                    cell(g, r[i][c], x, ly, columns, c, col != null && i < col.length ? col[i] : 0xFFE0E0E4);
        });
    }

    private static void cell(net.minecraft.client.gui.GuiGraphics g, String s, int x, int y, int[] columns, int c,
                             int colour) {
        if (c == 0) text(g, s, x + columns[0], y, colour, false);
        else right(g, s, x + columns[c], y, colour);
    }
}
