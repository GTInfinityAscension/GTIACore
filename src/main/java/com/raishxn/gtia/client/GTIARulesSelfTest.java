package com.raishxn.gtia.client;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.rules.RulePreset;
import com.raishxn.gtia.rules.WorldRules;

/**
 * Development self-test of the create-world rules flow, run with {@code -Dgtia.rulesSelfTest=true}: opens the create
 * world screen (also with a {@code gtia_rules_selftest} file in the game folder), switches the preset to Journey the
 * same way the Done button does, checks that a GTCEu recipe got the
 * Journey duration, shows the rules screen for a moment and quits. Never active in normal play.
 */
public final class GTIARulesSelfTest {

    private static final java.nio.file.Path MARKER = java.nio.file.Path.of("gtia_rules_selftest");
    public static final boolean ENABLED = Boolean.getBoolean("gtia.rulesSelfTest") || MARKER.toFile().exists();
    /**
     * Also re-runs the checks of earlier releases (Night Vision after /pspace tpx, quest book and multiblock previews
     * in zh_cn, average FPS): with {@code -Dgtia.rulesSelfTest.full=true} or "full" in the marker file.
     */
    private static final boolean FULL = Boolean.getBoolean("gtia.rulesSelfTest.full") || markerSays("full");
    private static final boolean CHAPTERS_ONLY = markerSays("chapters");
    private static int step;
    private static int wait;
    private static ResourceLocation recipeId;
    private static int normalDuration;

    private GTIARulesSelfTest() {}

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (wait > 0) {
            wait--;
            return;
        }
        try {
            switch (step) {
                case 0 -> {
                    // Any main menu (the pack replaces the vanilla title screen) once loading is done.
                    if (mc.screen != null && mc.level == null && mc.getOverlay() == null &&
                            !(mc.screen instanceof CreateWorldScreen)) {
                        CreateWorldScreen.openFresh(mc, mc.screen);
                        step = 1;
                        wait = 20;
                    }
                }
                case 1 -> {
                    if (!(mc.screen instanceof CreateWorldScreen screen)) return;
                    GTRecipe recipe = firstGTRecipe(screen);
                    recipeId = recipe.getId();
                    normalDuration = recipe.duration;
                    GTRecipe euRecipe = recipes(screen).filter(r -> euOf(r) >= 30 && euOf(r) <= 32).findFirst()
                            .orElseThrow();
                    euRecipeId = euRecipe.getId();
                    normalEU = euOf(euRecipe);
                    normalCasings = casings(screen);
                    GTIAMod.LOGGER.info("GTIA rules self-test: {} takes {} ticks with {}", recipeId, normalDuration,
                            WorldRules.pendingOrDefault().preset().id());
                    WorldRules.setPending(WorldRules.Snapshot.of(RulePreset.JOURNEY));
                    GTIARulesScreen.reloadCreateWorldData(screen);
                    step = 2;
                    wait = 40;
                }
                case 2 -> {
                    if (!(mc.screen instanceof CreateWorldScreen screen)) return;
                    var recipe = screen.getUiState().getSettings().dataPackResources().getRecipeManager()
                            .byKey(recipeId).orElseThrow();
                    int journey = ((GTRecipe) recipe).duration;
                    int expected = Math.max(1, (int) Math.round(normalDuration * 0.75));
                    boolean pass = Math.abs(journey - expected) <= 1;
                    GTIAMod.LOGGER.info("GTIA rules self-test: after Journey {} takes {} ticks (expected about {}): {}",
                            recipeId, journey, expected, pass ? "PASS" : "FAIL");
                    long journeyEU = euOf((GTRecipe) screen.getUiState().getSettings().dataPackResources()
                            .getRecipeManager().byKey(euRecipeId).orElseThrow());
                    long expectedEU = Math.max(1, Math.round(normalEU * 0.75));
                    GTIAMod.LOGGER.info("GTIA rules self-test: after Journey {} uses {} EU/t (was {}, expected {}): {}",
                            euRecipeId, journeyEU, normalEU, expectedEU, journeyEU == expectedEU ? "PASS" : "FAIL");
                    int journeyCasings = casings(screen);
                    GTIAMod.LOGGER.info("GTIA rules self-test: casings per craft {} -> {} (expected 3): {}",
                            normalCasings, journeyCasings, journeyCasings == 3 ? "PASS" : "FAIL");
                    shot(mc, "gtia_selftest_create_world.png");
                    GTIARulesScreen.openForNewWorld(screen);
                    step = 3;
                    wait = 20;
                }
                case 3 -> {
                    GTIAMod.LOGGER.info("GTIA rules self-test: rules screen open: {}",
                            mc.screen instanceof GTIARulesScreen ? "PASS" : "FAIL (" + mc.screen + ")");
                    shot(mc, "gtia_selftest_rules_screen.png");
                    if (mc.screen instanceof GTIARulesScreen rules) rules.onClose();
                    step = 5;
                    wait = 10;
                }
                case 5 -> {
                    if (!(mc.screen instanceof CreateWorldScreen screen)) return;
                    // Journey needs other restart rules: Create asks for a restart first, as GTO does.
                    ((com.raishxn.gtia.mixin.client.CreateWorldScreenAccessor) screen).gtia$onCreate();
                    GTIAMod.LOGGER.info("GTIA rules self-test: Journey world asks for a restart before creating: {}",
                            mc.screen instanceof GTIARestartScreen ? "PASS" : "FAIL (" + mc.screen + ")");
                    createScreen = screen;
                    step = 51;
                    wait = 20;
                }
                case 51 -> {
                    shot(mc, "gtia_selftest_restart_screen.png");
                    mc.setScreen(createScreen);
                    GTIARestartScreen.bypassOnce = true;
                    ((com.raishxn.gtia.mixin.client.CreateWorldScreenAccessor) createScreen).gtia$onCreate();
                    step = 6;
                }
                case 6 -> {
                    var server = mc.getSingleplayerServer();
                    if (mc.player == null || server == null || mc.screen != null) return;
                    if (CHAPTERS_ONLY) {
                        // "chapters" in the marker: only the quest chapter screenshots.
                        step = 86;
                        wait = 100;
                        return;
                    }
                    // Journey turns maintenance off, a rule read at startup: the world asks for a restart.
                    var startup = java.nio.file.Path.of("config", "gtia", "startup_rules.json");
                    boolean saved = java.nio.file.Files.exists(startup) &&
                            java.nio.file.Files.readString(startup).contains("\"maintenance\": 0.0");
                    GTIAMod.LOGGER.info("GTIA rules self-test: Journey restart rules saved for the next start: {}",
                            saved ? "PASS" : "FAIL");
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayers().get(0);
                        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                        var level = player.serverLevel();
                        var pos = player.blockPosition().relative(player.getDirection(), 2);
                        stationPos = pos;
                        level.setBlockAndUpdate(pos,
                                com.raishxn.gtia.station.GTIAStation.BLOCK.get().defaultBlockState());
                        level.setBlockAndUpdate(pos.east(),
                                net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState());
                        level.setBlockAndUpdate(pos.below(),
                                net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState());
                        if (level.getBlockEntity(
                                pos.east()) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
                            chest.setItem(0, new net.minecraft.world.item.ItemStack(
                                    net.minecraft.world.item.Items.OAK_PLANKS, 32));
                            chest.setItem(5, new net.minecraft.world.item.ItemStack(
                                    net.minecraft.world.item.Items.IRON_INGOT, 16));
                        }
                        level.getBlockState(pos).use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                                new net.minecraft.world.phys.BlockHitResult(
                                        net.minecraft.world.phys.Vec3.atCenterOf(pos),
                                        net.minecraft.core.Direction.UP, pos, false));
                    });
                    step = 7;
                    wait = 40;
                }
                case 7 -> {
                    boolean open = mc.screen instanceof com.raishxn.gtia.station.CraftingStationScreen;
                    GTIAMod.LOGGER.info("GTIA rules self-test: crafting station open: {}",
                            open ? "PASS" : "FAIL (" + mc.screen + ")");
                    shot(mc, "gtia_selftest_crafting_station.png");
                    if (open) {
                        // The pack changes some vanilla recipes, so take whichever crafting recipe EMI shows for
                        // sticks.
                        var screen = (com.raishxn.gtia.station.CraftingStationScreen) mc.screen;
                        var handler = new com.raishxn.gtia.integration.emi.GTIAEmiPlugin.StationHandler();
                        var ctx = new dev.emi.emi.api.recipe.handler.EmiCraftContext<>(screen,
                                handler.getInventory(screen),
                                dev.emi.emi.api.recipe.handler.EmiCraftContext.Type.FILL_BUTTON);
                        var recipe = dev.emi.emi.api.EmiApi.getRecipeManager()
                                .getRecipesByOutput(
                                        dev.emi.emi.api.stack.EmiStack.of(net.minecraft.world.item.Items.STICK))
                                .stream().filter(r -> handler.supportsRecipe(r) && handler.canCraft(r, ctx))
                                .findFirst().orElse(null);
                        var all = dev.emi.emi.api.EmiApi.getRecipeManager().getRecipesByOutput(
                                dev.emi.emi.api.stack.EmiStack.of(net.minecraft.world.item.Items.STICK));
                        if (all.isEmpty() && emiWaits++ < 60) {
                            // EMI is still loading the pack's recipes after the world opened.
                            wait = 40;
                            return;
                        }
                        for (var r : all) {
                            GTIAMod.LOGGER.info("GTIA rules self-test: stick recipe {} {} supported {} canCraft {}",
                                    r.getId(), r.getCategory().getId(), handler.supportsRecipe(r),
                                    handler.canCraft(r, ctx));
                        }
                        GTIAMod.LOGGER.info("GTIA rules self-test: {} sources, planks visible to EMI: {}",
                                handler.getInputSources(screen.getMenu()).size(),
                                handler.getInputSources(screen.getMenu()).stream()
                                        .filter(sl -> sl.getItem().is(net.minecraft.world.item.Items.OAK_PLANKS))
                                        .mapToInt(sl -> sl.getItem().getCount()).sum());
                        GTIAMod.LOGGER.info("GTIA rules self-test: EMI stick recipe from the side chest: {}",
                                recipe == null ? null : recipe.getId());
                        if (recipe != null) handler.craft(recipe, ctx);
                    }
                    step = 70;
                    wait = 40;
                }
                case 70 -> {
                    if (mc.screen instanceof com.raishxn.gtia.station.CraftingStationScreen station) {
                        var result = station.getMenu().getSlot(0).getItem();
                        int planksInGrid = 0;
                        for (int i = 1; i < 10; i++) planksInGrid += station.getMenu().getSlot(i).getItem().getCount();
                        boolean pass = result.is(net.minecraft.world.item.Items.STICK) && planksInGrid > 0;
                        GTIAMod.LOGGER.info(
                                "GTIA rules self-test: EMI fill from the side chest: {} (result {}, {} planks in grid)",
                                pass ? "PASS" : "FAIL", result, planksInGrid);
                        shot(mc, "gtia_selftest_crafting_station_filled.png");
                    }
                    // Crafting Chain: the station stays open, so the chest beside it is a source. Barrel <- Slabs <-
                    // Planks <- Logs is a three-level chain (the pack's hopper needs a GregTech wrench); the enchanted
                    // log must stay where it is.
                    var server = mc.getSingleplayerServer();
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayers().get(0);
                        player.getInventory().clearContent();
                        if (player.level().getBlockEntity(stationPos
                                .east()) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
                            chest.clearContent();
                            chest.setItem(0, new net.minecraft.world.item.ItemStack(
                                    net.minecraft.world.item.Items.OAK_LOG, 3));
                            var enchanted = new net.minecraft.world.item.ItemStack(
                                    net.minecraft.world.item.Items.OAK_LOG);
                            enchanted.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING, 1);
                            chest.setItem(2, enchanted);
                        }
                    });
                    step = 71;
                    wait = 20;
                }
                case 71 -> {
                    chainRequest(net.minecraft.world.item.Items.BARREL);
                    step = 72;
                    wait = 40;
                }
                case 72 -> {
                    // Block entities can only be read on the server thread.
                    var server = mc.getSingleplayerServer();
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayers().get(0);
                        boolean barrel = player.getInventory().countItem(net.minecraft.world.item.Items.BARREL) == 1;
                        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) player.level()
                                .getBlockEntity(stationPos.east());
                        boolean fromChest = chest != null && chest.getItem(0).getCount() < 3;
                        boolean keptEnchanted = chest != null && chest.getItem(2).isEnchanted();
                        chainCheck = String.format(
                                "%s (barrel %s, logs used from the chest %s, enchanted log kept %s; %s)",
                                barrel && fromChest && keptEnchanted ? "PASS" : "FAIL", barrel, fromChest,
                                keptEnchanted, chainResult());
                    });
                    step = 720;
                    wait = 10;
                }
                case 720 -> {
                    GTIAMod.LOGGER.info("GTIA rules self-test: Crafting Chain barrel from the side chest: {}",
                            chainCheck);
                    shot(mc, "gtia_selftest_chain_" + mc.options.languageCode + ".png");
                    chainRequest(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
                    step = 73;
                    wait = 20;
                }
                case 73 -> {
                    String result = chainResult();
                    GTIAMod.LOGGER.info("GTIA rules self-test: Crafting Chain names what is missing: {} ({})",
                            result.contains(net.minecraft.world.item.Items.DIAMOND.getDescription().getString()) ?
                                    "PASS" : "FAIL",
                            result);
                    shot(mc, "gtia_selftest_chain_missing_" + mc.options.languageCode + ".png");
                    if (mc.screen != null) mc.screen.onClose();
                    if (FULL) {
                        step = 80;
                    } else {
                        mc.setScreen(new net.minecraft.client.gui.screens.PauseScreen(true));
                        step = 83;
                        wait = 20;
                        return;
                    }
                    wait = 10;
                }
                case 80 -> {
                    var server = mc.getSingleplayerServer();
                    server.execute(() -> {
                        var source = server.createCommandSourceStack().withSuppressedOutput();
                        String name = mc.player.getGameProfile().getName();
                        server.getCommands().performPrefixedCommand(source,
                                "effect give " + name + " minecraft:night_vision infinite");
                        server.getCommands().performPrefixedCommand(source,
                                "pspace tpx " + name + " minecraft:the_nether 0 100 0");
                    });
                    step = 81;
                    wait = 100;
                }
                case 81 -> {
                    boolean nether = mc.level.dimension() == net.minecraft.world.level.Level.NETHER;
                    boolean kept = mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
                    GTIAMod.LOGGER.info("GTIA rules self-test: Night Vision after /pspace tpx to {}: {}",
                            mc.level.dimension().location(), nether && kept ? "PASS" : "FAIL");
                    mc.setScreen(new net.minecraft.client.gui.screens.PauseScreen(true));
                    step = 83;
                    wait = 20;
                }
                case 83 -> {
                    // The pause menu has no GTIA Rules button any more; the rules are behind /gtia rules.
                    boolean button = mc.screen != null && mc.screen.children().stream()
                            .anyMatch(w -> w instanceof net.minecraft.client.gui.components.AbstractWidget aw &&
                                    aw.getMessage().getString().contains("GTIA"));
                    GTIAMod.LOGGER.info("GTIA rules self-test: pause menu without the GTIA Rules button: {}",
                            mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen && !button ? "PASS" :
                                    "FAIL (" + mc.screen + ")");
                    shot(mc, "gtia_selftest_pause_" + mc.options.languageCode + ".png");
                    mc.setScreen(null);
                    net.minecraftforge.client.ClientCommandHandler.runCommand("gtia rules");
                    step = 84;
                    wait = 10;
                }
                case 84 -> {
                    GTIAMod.LOGGER.info("GTIA rules self-test: /gtia rules opens the rules screen: {}",
                            mc.screen instanceof GTIARulesScreen ? "PASS" : "FAIL (" + mc.screen + ")");
                    shot(mc, "gtia_selftest_rules_command_" + mc.options.languageCode + ".png");
                    if (mc.screen != null) mc.screen.onClose();
                    step = FULL ? 82 : 11;
                    wait = 10;
                }
                case 82 -> questBook(mc, "gtia_selftest_quests_" + mc.options.languageCode + ".png", 86);
                case 86 -> {
                    // The chapters redone in D-291, one screenshot each.
                    if (chapterShot < CHAPTERS.length) {
                        if (chapterShot == 0 && !questsOpened) bigFrame(mc, true);
                        if (!questsOpened) {
                            if (mc.screen != null) mc.screen.onClose();
                            openChapter(CHAPTERS[chapterShot]);
                            questsOpened = true;
                            zoomed = false;
                            wait = 100;
                            return;
                        }
                        if (!zoomed) {
                            fitChapter(mc);
                            zoomed = true;
                            wait = 20;
                            return;
                        }
                        shot(mc, "gtia_selftest_chapter_" + CHAPTERS[chapterShot] + ".png");
                        questsOpened = false;
                        chapterShot++;
                        wait = 10;
                        return;
                    }
                    if (mc.screen != null) mc.screen.onClose();
                    bigFrame(mc, false);
                    if (CHAPTERS_ONLY) {
                        MARKER.toFile().delete();
                        java.nio.file.Path.of("config", "gtia", "startup_rules.json").toFile().delete();
                        mc.stop();
                        return;
                    }
                    step = 8;
                    wait = 40;
                }
                case 8 -> {
                    if (mc.screen != null) mc.screen.onClose();
                    mc.options.languageCode = "zh_cn";
                    mc.getLanguageManager().setSelected("zh_cn");
                    mc.reloadResourcePacks();
                    step = 9;
                    wait = 400;
                }
                case 9 -> questBook(mc, "gtia_selftest_quests_zh_cn.png", 90);
                case 90 -> preview(mc, "gtna:eye_of_harmony", "gtia_selftest_eoh_zh.png", 10);
                case 10 -> preview(mc, "gtna:forge_of_gods", "gtia_selftest_fog_zh.png", 11);
                case 11 -> {
                    if (FULL && fpsSamples < 15) {
                        // One sample per second, in the world with no screen open, at the player's settings.
                        if (fpsSamples == 0) {
                            if (mc.screen != null) mc.screen.onClose();
                            // A hidden test window with vsync on is throttled by the compositor; measure without it.
                            mc.getWindow().updateVsync(false);
                        }
                        if (fpsSamples > 0) fpsTotal += mc.getFps();
                        fpsSamples++;
                        wait = 20;
                        return;
                    }
                    if (FULL) GTIAMod.LOGGER.info(
                            "GTIA rules self-test: average FPS in the world over 14 s: {} (render distance {}, {})",
                            fpsTotal / 14, mc.options.renderDistance().get(),
                            com.mojang.blaze3d.platform.GlUtil.getRenderer());
                    // Xaero's Minimap in the top right corner, and the inventory with the quest book and EMI. The
                    // test window has no focus, so the game would pause itself; toasts would cover the corner.
                    if (!hudReady) {
                        mc.options.pauseOnLostFocus = false;
                        mc.setScreen(null);
                        mc.getToasts().clear();
                        hudReady = true;
                        wait = 60;
                        return;
                    }
                    shot(mc, "gtia_selftest_hud_minimap.png");
                    mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
                    step = 12;
                    wait = 40;
                }
                case 12 -> {
                    shot(mc, "gtia_selftest_inventory_minimap.png");
                    if (mc.screen != null) mc.screen.onClose();
                    mc.options.languageCode = "en_us";
                    mc.getLanguageManager().setSelected("en_us");
                    step = 13;
                    MARKER.toFile().delete();
                    // The test world's restart rules must not carry over to the player's next start.
                    java.nio.file.Path.of("config", "gtia", "startup_rules.json").toFile().delete();
                    mc.stop();
                }
                default -> {}
            }
        } catch (Exception e) {
            GTIAMod.LOGGER.error("GTIA rules self-test: FAIL", e);
            step = 4;
            mc.stop();
        }
    }

    private static CreateWorldScreen createScreen;
    private static boolean previewOpened;
    private static int emiWaits;
    private static net.minecraft.core.BlockPos stationPos;
    private static volatile String chainCheck = "not run";
    private static boolean hudReady;

    /** Asks Crafting Chain (optional, reached by reflection) to craft an item, the same request its key sends. */
    private static void chainRequest(net.minecraft.world.item.Item item) {
        try {
            Class.forName("com.raishxn.craftingchain.client.ChainClient")
                    .getMethod("requestItem", net.minecraft.world.item.Item.class).invoke(null, item);
        } catch (ReflectiveOperationException e) {
            GTIAMod.LOGGER.error("GTIA rules self-test: Crafting Chain not reachable", e);
        }
    }

    private static String chainResult() {
        try {
            return String.valueOf(Class.forName("com.raishxn.craftingchain.server.ChainCrafter")
                    .getField("lastResult").get(null));
        } catch (ReflectiveOperationException e) {
            return "Crafting Chain not reachable";
        }
    }

    private static int fpsSamples;
    private static int fpsTotal;

    /** Opens the EMI recipes of a multiblock controller, waits for the preview to build, then screenshots it. */
    private static void preview(Minecraft mc, String item, String file, int next) {
        if (!previewOpened) {
            if (mc.screen != null) mc.screen.onClose();
            var stack = new net.minecraft.world.item.ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getValue(new ResourceLocation(item)));
            dev.emi.emi.api.EmiApi.displayRecipes(dev.emi.emi.api.stack.EmiStack.of(stack));
            previewOpened = true;
            wait = 600;
            return;
        }
        GTIAMod.LOGGER.info("GTIA rules self-test: preview {}: screen {}", item, mc.screen);
        shot(mc, file);
        if (mc.screen != null) mc.screen.onClose();
        previewOpened = false;
        step = next;
        wait = 20;
    }

    private static boolean questsOpened;
    private static final String[] CHAPTERS = { "welcome", "start", "steam", "lv", "mv", "hv", "agriculture", "bees",
            "energy", "wireless", "multiblocks" };
    private static boolean zoomed;
    private static int savedWidth, savedHeight;

    /**
     * The test window is tiled to half a 1080p screen; the chapter screenshots render into a 3840 x 2160 frame
     * instead, at GUI scale 2, so a whole chapter is readable. {@code false} restores the window's own size.
     */
    private static void bigFrame(Minecraft mc, boolean big) {
        var window = mc.getWindow();
        var access = (com.raishxn.gtia.mixin.client.WindowAccessor) (Object) window;
        if (big) {
            savedWidth = window.getWidth();
            savedHeight = window.getHeight();
        }
        access.gtia$setFramebufferWidth(big ? 3840 : savedWidth);
        access.gtia$setFramebufferHeight(big ? 2160 : savedHeight);
        mc.resizeDisplay();
    }

    /** Centres the open chapter in the quest book at its normal zoom. */
    private static void fitChapter(Minecraft mc) {
        try {
            Object gui = mc.screen.getClass().getMethod("getGui").invoke(mc.screen);
            Object panel = gui.getClass().getField("questPanel").get(gui);
            panel.getClass().getMethod("resetScroll").invoke(panel);
        } catch (ReflectiveOperationException e) {
            GTIAMod.LOGGER.error("GTIA rules self-test: could not zoom the quest book", e);
        }
    }

    private static int chapterShot;

    /** Opens the FTB Quests book on the chapter saved as {@code <filename>.snbt} (FTB Quests reached by reflection). */
    private static void openChapter(String filename) {
        try {
            Class<?> file = Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile");
            Object instance = file.getField("INSTANCE").get(null);
            for (Object chapter : (java.util.List<?>) file.getMethod("getAllChapters").invoke(instance)) {
                if (filename.equals(chapter.getClass().getMethod("getFilename").invoke(chapter))) {
                    long id = (long) chapter.getClass().getMethod("getId").invoke(chapter);
                    file.getMethod("openBookToQuestObject", long.class).invoke(null, id);
                    return;
                }
            }
            GTIAMod.LOGGER.error("GTIA rules self-test: no quest chapter {}", filename);
        } catch (ReflectiveOperationException e) {
            GTIAMod.LOGGER.error("GTIA rules self-test: FTB Quests not reachable", e);
        }
    }

    /** Opens the FTB Quests book, waits for it to draw, then screenshots it in the current language. */
    private static void questBook(Minecraft mc, String file, int next) {
        if (!questsOpened) {
            if (mc.screen != null) mc.screen.onClose();
            try {
                Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile").getMethod("openGui").invoke(null);
            } catch (ReflectiveOperationException e) {
                GTIAMod.LOGGER.error("GTIA rules self-test: FTB Quests not reachable", e);
            }
            questsOpened = true;
            wait = 100;
            return;
        }
        GTIAMod.LOGGER.info("GTIA rules self-test: quest book in {}: screen {}", mc.options.languageCode, mc.screen);
        shot(mc, file);
        if (mc.screen != null) mc.screen.onClose();
        questsOpened = false;
        step = next;
        wait = 20;
    }

    private static boolean markerSays(String word) {
        try {
            return MARKER.toFile().exists() && java.nio.file.Files.readString(MARKER).contains(word);
        } catch (java.io.IOException e) {
            return false;
        }
    }

    private static void shot(Minecraft mc, String name) {
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> {});
    }

    private static ResourceLocation euRecipeId;
    private static long normalEU;
    private static int normalCasings;

    private static java.util.stream.Stream<GTRecipe> recipes(CreateWorldScreen screen) {
        return screen.getUiState().getSettings().dataPackResources().getRecipeManager().getRecipes().stream()
                .filter(r -> r instanceof GTRecipe gt && gt.getId().getNamespace().equals("gtceu"))
                .map(r -> (GTRecipe) r);
    }

    private static long euOf(GTRecipe recipe) {
        var eu = recipe.tickInputs.get(com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability.CAP);
        if (eu == null || eu.isEmpty()) return 0;
        return ((com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack) eu.get(0).content).voltage();
    }

    /** Output count of the crafting recipe of the Solid Steel Machine Casing. */
    private static int casings(CreateWorldScreen screen) {
        var resources = screen.getUiState().getSettings().dataPackResources();
        return resources.getRecipeManager().byKey(new ResourceLocation("gtceu", "shaped/casing_steel_solid"))
                .map(r -> r.getResultItem(Minecraft.getInstance().level == null ?
                        net.minecraft.core.RegistryAccess.EMPTY : Minecraft.getInstance().level.registryAccess())
                        .getCount())
                .orElse(-1);
    }

    private static GTRecipe firstGTRecipe(CreateWorldScreen screen) {
        return screen.getUiState().getSettings().dataPackResources().getRecipeManager().getRecipes().stream()
                .filter(r -> r instanceof GTRecipe gt && gt.duration >= 40 &&
                        gt.getId().getNamespace().equals("gtceu") && gt.getId().getPath().startsWith("macerator/"))
                .map(r -> (GTRecipe) r).findFirst().orElseThrow();
    }
}
