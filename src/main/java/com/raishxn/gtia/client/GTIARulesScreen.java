package com.raishxn.gtia.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

import com.raishxn.gtia.mixin.client.CreateWorldScreenAccessor;
import com.raishxn.gtia.rules.GTIARule;
import com.raishxn.gtia.rules.GTIARuleEffects;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.RulePreset;
import com.raishxn.gtia.rules.WorldRules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * GTIA Rules screen (GTIA-D-254): pick Journey, Normal or Custom for a new world, see every rule grouped with its
 * description and the value of each preset, and edit the values in Custom. For a world already running it is read-only.
 */
public final class GTIARulesScreen extends Screen {

    private final Screen parent;
    private final boolean editable;
    private final Consumer<WorldRules.Snapshot> onDone;
    private RulePreset preset;
    private final Map<String, Double> customValues = new LinkedHashMap<>();
    private final Map<RulePreset, Button> presetButtons = new LinkedHashMap<>();
    private EditBox search;
    private RuleList list;

    public GTIARulesScreen(Screen parent, WorldRules.Snapshot initial, boolean editable,
                           Consumer<WorldRules.Snapshot> onDone) {
        super(Component.translatable(editable ? "gtia.rules.screen.title" : "gtia.rules.screen.title.world"));
        this.parent = parent;
        this.editable = editable;
        this.onDone = onDone;
        this.preset = initial.preset();
        for (GTIARule rule : GTIARules.all().values()) customValues.put(rule.id(), initial.get(rule));
    }

    /** Label of the button in the create-world Game tab. */
    public static Component buttonLabel() {
        return Component.translatable("gtia.rules.screen.button",
                presetName(WorldRules.pendingOrDefault().preset()));
    }

    public static Component presetName(RulePreset preset) {
        return Component.translatable("gtia.rules.preset." + preset.id());
    }

    /** Opens the editor for the world being created; a change reloads the screen's data so recipes follow it. */
    public static void openForNewWorld(CreateWorldScreen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        WorldRules.Snapshot before = WorldRules.pendingOrDefault();
        minecraft.setScreen(new GTIARulesScreen(screen, before, true, chosen -> {
            if (chosen.equals(before)) {
                minecraft.setScreen(screen);
                return;
            }
            WorldRules.setPending(chosen);
            reloadCreateWorldData(screen);
        }));
    }

    /** Opens the read-only view of the rules of the world being played. */
    public static void openForCurrentWorld(Screen parent) {
        Minecraft.getInstance().setScreen(new GTIARulesScreen(parent, WorldRules.active(), false,
                ignored -> Minecraft.getInstance().setScreen(parent)));
    }

    /**
     * The create-world screen loaded its data packs (and GTCEu generated its recipes) when it opened. Rebuilding the
     * pack repository makes {@code ServerPacksSource.createPackRepository} run again, which loads the pending rules
     * before GTCEu regenerates the recipes; then the screen reloads its world-creation context like after a data pack
     * change.
     */
    static void reloadCreateWorldData(CreateWorldScreen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        var accessor = (CreateWorldScreenAccessor) screen;
        var configuration = screen.getUiState().getSettings().dataConfiguration();
        accessor.gtia$setTempDataPackRepository(null);
        var settings = accessor.gtia$getDataPackSelectionSettings(configuration);
        if (settings == null) {
            minecraft.setScreen(screen);
            return;
        }
        accessor.gtia$applyNewPackConfig(settings.getSecond(), configuration, ignored -> minecraft.setScreen(screen));
    }

    private WorldRules.Snapshot snapshot() {
        if (preset != RulePreset.CUSTOM) return WorldRules.Snapshot.of(preset);
        return new WorldRules.Snapshot(RulePreset.CUSTOM, Map.copyOf(customValues));
    }

    @Override
    protected void init() {
        int x = width / 2 - 155;
        presetButtons.clear();
        int i = 0;
        for (RulePreset value : RulePreset.values()) {
            Button button = Button.builder(presetName(value), b -> selectPreset(value))
                    .bounds(x + i * 105, 24, 100, 20).build();
            button.active = editable;
            presetButtons.put(value, addRenderableWidget(button));
            i++;
        }
        search = addRenderableWidget(
                new EditBox(font, x, 62, 310, 18, Component.translatable("gtia.rules.screen.search")));
        search.setHint(Component.translatable("gtia.rules.screen.search").withStyle(ChatFormatting.DARK_GRAY));
        search.setResponder(text -> list.refill(text));
        list = addRenderableWidget(new RuleList(minecraft, width, height, 86, height - 50, 36));
        list.refill(search.getValue());
        if (editable) {
            addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onDone.accept(snapshot()))
                    .bounds(width / 2 - 155, height - 28, 150, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> minecraft.setScreen(parent))
                    .bounds(width / 2 + 5, height - 28, 150, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onDone.accept(snapshot()))
                    .bounds(width / 2 - 75, height - 28, 150, 20).build());
        }
        updatePresetButtons();
    }

    private void selectPreset(RulePreset value) {
        if (value == RulePreset.CUSTOM && preset != RulePreset.CUSTOM) {
            // Custom starts from the values of the preset that was selected.
            for (GTIARule rule : GTIARules.all().values()) customValues.put(rule.id(), rule.valueFor(preset));
        }
        preset = value;
        updatePresetButtons();
        list.refill(search.getValue());
    }

    private void updatePresetButtons() {
        presetButtons.forEach((value, button) -> {
            var label = value == preset ?
                    presetName(value).copy().withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD) :
                    presetName(value).copy();
            // As GTO marks a difficulty that needs a restart.
            if (editable && value != RulePreset.CUSTOM &&
                    GTIARuleEffects.needsRestart(WorldRules.Snapshot.of(value)))
                label.append(Component.literal(" ⚠").withStyle(ChatFormatting.GOLD));
            button.setMessage(label);
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("gtia.rules.preset." + preset.id() + ".desc"),
                width / 2, 48, 0xAAAAAA);
        if (!editable) {
            graphics.drawCenteredString(font, Component.translatable("gtia.rules.screen.locked"), width / 2,
                    height - 44, 0xFFAA00);
        } else if (GTIARuleEffects.needsRestart(snapshot())) {
            graphics.drawCenteredString(font, Component.translatable("gtia.rules.screen.restart_needed"), width / 2,
                    height - 44, 0xFFAA00);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** A rule value as shown: ×1.5, 32 or On/Off. */
    static String format(GTIARule rule, double value) {
        return switch (rule.kind()) {
            case TOGGLE -> Component.translatable(value != 0 ? "gtia.rules.value.on" : "gtia.rules.value.off")
                    .getString();
            case NUMBER -> Long.toString(Math.round(value));
            case MULTIPLIER -> "×" + plain(value);
        };
    }

    private static String plain(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private final class RuleList extends ContainerObjectSelectionList<RuleList.Row> {

        RuleList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
            // Its dirt bands would cover the preset buttons and the search box above it.
            setRenderTopAndBottom(false);
        }

        void refill(String filter) {
            clearEntries();
            String query = filter.toLowerCase(Locale.ROOT).trim();
            Map<String, List<GTIARule>> groups = new LinkedHashMap<>();
            for (GTIARule rule : GTIARules.all().values()) {
                String text = (Component.translatable(rule.translationKey()).getString() + " " +
                        Component.translatable(rule.translationKey() + ".desc").getString() + " " + rule.id())
                        .toLowerCase(Locale.ROOT);
                if (query.isEmpty() || text.contains(query)) {
                    groups.computeIfAbsent(rule.group(), g -> new ArrayList<>()).add(rule);
                }
            }
            groups.forEach((group, rules) -> {
                addEntry(new GroupRow(Component.translatable("gtia.rules.group." + group)));
                rules.forEach(rule -> addEntry(new RuleRow(rule)));
            });
            setScrollAmount(0);
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        protected int getScrollbarPosition() {
            return width / 2 + 165;
        }

        abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {}

        final class GroupRow extends Row {

            private final Component label;

            GroupRow(Component label) {
                this.label = label;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                               int mouseY, boolean hovered, float partialTick) {
                graphics.drawString(font, label.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), left,
                        top + height - 12, 0xFFFFFF);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }
        }

        final class RuleRow extends Row {

            private final GTIARule rule;
            private final EditBox value;
            private final Button toggle;

            RuleRow(GTIARule rule) {
                this.rule = rule;
                this.toggle = Button.builder(Component.literal(format(rule, customValues.get(rule.id()))), b -> {
                    customValues.put(rule.id(), customValues.get(rule.id()) != 0 ? 0.0 : 1.0);
                    b.setMessage(Component.literal(format(rule, customValues.get(rule.id()))));
                }).bounds(0, 0, 56, 16).build();
                this.value = new EditBox(font, 0, 0, 56, 16, Component.translatable(rule.translationKey()));
                value.setValue(rule.kind() == GTIARule.Kind.NUMBER ? format(rule, customValues.get(rule.id())) :
                        plain(customValues.get(rule.id())));
                value.setFilter(text -> text.matches(rule.kind() == GTIARule.Kind.NUMBER ? "[0-9]*" :
                        "[0-9]*[.,]?[0-9]*"));
                value.setResponder(text -> {
                    try {
                        double parsed = Double.parseDouble(text.replace(',', '.'));
                        customValues.put(rule.id(), rule.clamp(parsed));
                        value.setTextColor(parsed == rule.clamp(parsed) ? 0xE0E0E0 : 0xFF5555);
                    } catch (NumberFormatException e) {
                        value.setTextColor(0xFF5555);
                    }
                });
            }

            private boolean editing() {
                return editable && preset == RulePreset.CUSTOM;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                               int mouseY, boolean hovered, float partialTick) {
                Component name = Component.translatable(rule.translationKey());
                if (rule.restart()) {
                    name = name.copy().append(" ").append(Component.translatable("gtia.rules.screen.restart")
                            .withStyle(ChatFormatting.GOLD));
                }
                graphics.drawString(font, name, left, top + 2, 0xFFFFFF);
                Component values = rule.kind() == GTIARule.Kind.TOGGLE ?
                        Component.translatable("gtia.rules.screen.values.toggle", format(rule, rule.journey()),
                                format(rule, rule.normal())) :
                        Component.translatable("gtia.rules.screen.values", format(rule, rule.journey()),
                                format(rule, rule.normal()), format(rule, rule.min()), format(rule, rule.max()));
                graphics.drawString(font, values, left, top + 12, 0x8888AA);
                graphics.drawString(font, Component.translatable(rule.translationKey() + ".desc"), left, top + 22,
                        0xAAAAAA);
                if (editing()) {
                    var widget = rule.kind() == GTIARule.Kind.TOGGLE ? toggle : value;
                    widget.setX(left + width - 58);
                    widget.setY(top + 2);
                    widget.render(graphics, mouseX, mouseY, partialTick);
                } else {
                    double current = preset == RulePreset.CUSTOM ? customValues.get(rule.id()) : rule.valueFor(preset);
                    String text = format(rule, current);
                    graphics.drawString(font, Component.literal(text).withStyle(ChatFormatting.GREEN),
                            left + width - font.width(text) - 2, top + 6, 0xFFFFFF);
                }
                if (hovered && mouseX < left + width - 60) {
                    setTooltipForNextRenderPass(font.split(
                            Component.translatable(rule.translationKey() + ".desc"), 240));
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return editing() ? List.of(widget()) : List.of();
            }

            private net.minecraft.client.gui.components.AbstractWidget widget() {
                return rule.kind() == GTIARule.Kind.TOGGLE ? toggle : value;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return editing() ? List.of(widget()) : List.of();
            }
        }
    }
}
