package com.raishxn.gtia.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.raishxn.gtia.rules.GTIARule;
import com.raishxn.gtia.rules.GTIARuleEffects;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.WorldRules;
import org.jetbrains.annotations.Nullable;

/**
 * Shown when a world needs other values for the rules GTCEu reads at startup than the ones this game started with. As
 * in GTO, "Save and Quit" writes them and closes the game; the player starts it again and the world gets its rules.
 */
public final class GTIARestartScreen extends Screen {

    /** Lets the next create-world or load request through once (the rules self-test continues into the world). */
    static boolean bypassOnce;

    private final Screen parent;
    private final WorldRules.Snapshot rules;
    private final boolean newWorld;
    @Nullable
    private final Runnable playAnyway;
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    private GTIARestartScreen(Screen parent, WorldRules.Snapshot rules, boolean newWorld,
                              @Nullable Runnable playAnyway) {
        super(Component.translatable("gtia.rules.restart.title"));
        this.parent = parent;
        this.rules = rules;
        this.newWorld = newWorld;
        this.playAnyway = playAnyway;
    }

    /** True (and the screen is shown) when creating a world with {@code rules} needs a restart first. */
    public static boolean interceptNewWorld(Screen parent, WorldRules.Snapshot rules) {
        if (takeBypass() || !GTIARuleEffects.needsRestart(rules)) return false;
        Minecraft.getInstance().setScreen(new GTIARestartScreen(parent, rules, true, null));
        return true;
    }

    /** True (and the screen is shown) when loading a world with {@code rules} needs a restart first. */
    public static boolean interceptExistingWorld(Screen parent, @Nullable WorldRules.Snapshot rules,
                                                 Runnable playAnyway) {
        if (rules == null || takeBypass() || !GTIARuleEffects.needsRestart(rules)) return false;
        Minecraft.getInstance().setScreen(new GTIARestartScreen(parent, rules, false, playAnyway));
        return true;
    }

    private static boolean takeBypass() {
        boolean bypass = bypassOnce;
        bypassOnce = false;
        return bypass;
    }

    @Override
    protected void init() {
        MutableComponent text = Component.translatable(newWorld ? "gtia.rules.restart.new_world" :
                "gtia.rules.restart.existing_world", GTIARulesScreen.presetName(rules.preset()));
        text.append("\n");
        for (GTIARule rule : GTIARules.all().values()) {
            if (!GTIARuleEffects.differsFromSession(rules, rule)) continue;
            text.append("\n").append(Component.literal("• ").append(Component.translatable(rule.translationKey()))
                    .withStyle(ChatFormatting.YELLOW));
        }
        text.append("\n\n").append(Component.translatable(newWorld ? "gtia.rules.restart.new_world.after" :
                "gtia.rules.restart.existing_world.after").withStyle(ChatFormatting.GRAY));
        message = MultiLineLabel.create(font, text, width - 80);
        int y = Math.min(height - 30, 70 + message.getLineCount() * 10 + 12);
        Button quit = Button.builder(Component.translatable("gtia.rules.restart.save_and_quit"), b -> saveAndQuit())
                .bounds(width / 2 - 155, y, 150, 20).build();
        addRenderableWidget(quit);
        if (playAnyway != null) {
            addRenderableWidget(Button.builder(Component.translatable("gtia.rules.restart.play_anyway"), b -> {
                bypassOnce = true;
                playAnyway.run();
            }).bounds(width / 2 + 5, y, 150, 20).build());
        } else {
            addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                    .bounds(width / 2 + 5, y, 150, 20).build());
        }
    }

    private void saveAndQuit() {
        if (newWorld) WorldRules.keepChoiceForRestart(rules);
        else GTIARuleEffects.writeStartupRules(rules);
        Minecraft.getInstance().stop();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 40, 0xFFFFFF);
        message.renderCentered(graphics, width / 2, 70);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
