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
                    ((com.raishxn.gtia.mixin.client.CreateWorldScreenAccessor) screen).gtia$onCreate();
                    step = 6;
                }
                case 6 -> {
                    var server = mc.getSingleplayerServer();
                    if (mc.player == null || server == null || mc.screen != null) return;
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayers().get(0);
                        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                        var level = player.serverLevel();
                        var pos = player.blockPosition().relative(player.getDirection(), 2);
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
                        station.onClose();
                    }
                    var server = mc.getSingleplayerServer();
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayers().get(0);
                        player.getInventory().clearContent();
                        player.getInventory().add(
                                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_LOG));
                    });
                    step = 71;
                    wait = 20;
                }
                case 71 -> {
                    try {
                        Class<?> packet = Class.forName("com.sabbs.fabricate.network.PlannerCraftPacket");
                        Object msg = packet
                                .getConstructor(net.minecraft.world.item.Item.class, int.class, boolean.class)
                                .newInstance(net.minecraft.world.item.Items.CRAFTING_TABLE, 1, false);
                        Class.forName("com.sabbs.fabricate.network.NetworkHandler").getMethod("sendToServer", packet)
                                .invoke(null, msg);
                    } catch (ReflectiveOperationException e) {
                        GTIAMod.LOGGER.error("GTIA rules self-test: Fabricate not reachable", e);
                    }
                    step = 72;
                    wait = 40;
                }
                case 72 -> {
                    // Read the server's inventory: in creative the client copy is not updated by the server craft.
                    var serverInventory = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0).getInventory();
                    boolean has = serverInventory.countItem(net.minecraft.world.item.Items.CRAFTING_TABLE) > 0;
                    fabricateTries++;
                    if (!has && fabricateTries < 3) {
                        // Fabricate builds its craft graph on the first request of a session.
                        step = 71;
                        return;
                    }
                    GTIAMod.LOGGER.info(
                            "GTIA rules self-test: Fabricate log -> crafting table: {} (attempt {}, inventory {})",
                            has ? "PASS" : "FAIL", fabricateTries,
                            serverInventory.items.stream().filter(st -> !st.isEmpty()).toList());
                    step = 80;
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
                    step = 82;
                }
                case 82 -> questBook(mc, "gtia_selftest_quests_" + mc.options.languageCode + ".png", 8);
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
                    if (fpsSamples < 15) {
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
                    GTIAMod.LOGGER.info(
                            "GTIA rules self-test: average FPS in the world over 14 s: {} (render distance {}, {})",
                            fpsTotal / 14, mc.options.renderDistance().get(),
                            com.mojang.blaze3d.platform.GlUtil.getRenderer());
                    mc.options.languageCode = "en_us";
                    mc.getLanguageManager().setSelected("en_us");
                    step = 12;
                    MARKER.toFile().delete();
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

    private static boolean previewOpened;
    private static int emiWaits;
    private static int fabricateTries;
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

    private static void shot(Minecraft mc, String name) {
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> {});
    }

    private static GTRecipe firstGTRecipe(CreateWorldScreen screen) {
        return screen.getUiState().getSettings().dataPackResources().getRecipeManager().getRecipes().stream()
                .filter(r -> r instanceof GTRecipe gt && gt.duration >= 40 &&
                        gt.getId().getNamespace().equals("gtceu") && gt.getId().getPath().startsWith("macerator/"))
                .map(r -> (GTRecipe) r).findFirst().orElseThrow();
    }
}
