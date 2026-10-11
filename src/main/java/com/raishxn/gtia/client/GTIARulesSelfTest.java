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
                    step = 8;
                    wait = 20;
                }
                case 8 -> {
                    step = 9;
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
