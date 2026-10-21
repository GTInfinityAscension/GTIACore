package com.raishxn.gtia.mega.hoop;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaRecipeEngine;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaShells.Coolant;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaThermal;
import com.raishxn.gtna.common.data.GTNARecipeType;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashSet;
import java.util.List;

/**
 * Controller of the Stellar Hoop (GTIA-D-318): a hoop through the star's corona that runs fusion, Electric Blast
 * Furnace and Plasma Forge recipes with the corona's heat, in parallel batches. The dive height trades parallel
 * against shield heat; coolant from the input hatches carries the heat away.
 */
public class StellarHoopMachine extends MegaMachine {

    public static final String SHAPE = "stellar_hoop";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(StellarHoopMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int heightPct = 10;
    @Persisted
    @DescSynced
    private int integrity = HoopPhysics.INTEGRITY;
    @Persisted
    private double progressTicks;
    @Persisted
    @DescSynced
    private long crafted;
    @DescSynced
    private String recipe = "";
    @DescSynced
    private String blocked = "";
    /**
     * What the screen shows: [0] parallel, [1] corona K, [2] load units/s, [3] coolant share 0..1, [4] coolant tier,
     * [5] crafts this second, [6] EU this second, [7] running (1/0), [8] star K, [9] star drawn scale.
     */
    @DescSynced
    private double[] view = new double[10];
    private final MegaRecipeEngine.Progress progress = new MegaRecipeEngine.Progress();
    /** Dev aid ({@code /gtia_dev_click <pos> dev_show}): seconds left showing the forge lit for screenshots. */
    private int devShow;

    public StellarHoopMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public String shapeName() {
        return SHAPE;
    }

    @Override
    public boolean inPlace() {
        return anyDimensionForTests || getLevel() != null &&
                com.raishxn.gtia.mega.system.StarSystems.isSystem(getLevel().dimension());
    }

    public int heightPct() {
        return heightPct;
    }

    public int integrity() {
        return integrity;
    }

    public long crafted() {
        return crafted;
    }

    public String recipe() {
        return recipe;
    }

    public String blocked() {
        return blocked;
    }

    public double[] view() {
        return view;
    }

    static List<GTRecipeType> types() {
        return List.of(GTRecipeTypes.FUSION_RECIPES, GTRecipeTypes.BLAST_RECIPES, GTNARecipeType.PLASMA_FORGE_RECIPES);
    }

    @Override
    protected void stopped() {
        double[] v = view.clone();
        v[3] = v[5] = v[6] = v[7] = 0;
        view = v;
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        boolean on = view[7] > 0;
        if (effect.equals("dive_glow")) return on ? 0.6f + (HoopPhysics.HEIGHT_MAX - heightPct) / 40f : 0.25f;
        if (effect.startsWith("intake")) return on ? 0.5f + (HoopPhysics.HEIGHT_MAX - heightPct) / 30f : 0;
        if (effect.startsWith("radiator")) return on ? (float) Math.min(1.2, 0.3 + view[2] / 300) : 0;
        if (effect.equals("hoop_field")) return on ? 1 : 0;
        return 1;
    }

    @Override
    public float fxTemperature(String effect) {
        return effect.equals("star") && view[8] > 0 ? (float) view[8] : 0;
    }

    @Override
    public float fxScale(String effect) {
        return effect.equals("star") && view[9] > 0 ? (float) view[9] : 1;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        if (star == null || !star.kind.shines()) {
            view = new double[10];
            return false;
        }
        double kelvin = HoopPhysics.coronaKelvin(heightPct);
        int parallel = HoopPhysics.parallel(phases, heightPct);
        double load = HoopPhysics.load(phases, heightPct);
        // The best coolant in the hatches takes the heat; what it cannot take wears the shields.
        double cooled = 0;
        int tier = 0;
        for (int t = 3; t >= 1 && cooled <= 0; t--) {
            final Coolant c = Coolant.values()[t];
            long want = HoopPhysics.coolantDemand(load, t);
            long got = want <= 0 ? 0 : drain(f -> MatrioshkaThermal.is(c, f), want);
            if (got > 0) {
                cooled = Math.min(1, (double) got / want);
                tier = t;
            }
        }
        integrity = Math.max(0, integrity - HoopPhysics.wear(1 - cooled));
        MegaRecipeEngine.Result result = new MegaRecipeEngine.Result(0, 0, "", "shields");
        if (integrity > 0 && isWorkingEnabled()) {
            progress.ticks = progressTicks;
            result = MegaRecipeEngine.second(this, level, types(),
                    new MegaRecipeEngine.Limits(parallel, HoopPhysics.SPEED, HoopPhysics.EU_FACTOR,
                            r -> heatOk(r, kelvin)),
                    progress);
            progressTicks = progress.ticks;
            crafted += result.crafts();
        }
        if (result.crafts() == 0) repair();
        recipe = result.recipe();
        blocked = result.blocked();
        // The hoop turns whenever it is on and whole; the forge glow shows only while recipes run.
        boolean on = integrity > 0 && isWorkingEnabled();
        boolean forging = on && (result.busy() || "inputs".equals(blocked));
        if (devShow > 0) {
            devShow--;
            forging = on;
        }
        view = new double[] { parallel, kelvin, load, cooled, tier, result.crafts(), result.eu(), forging ? 1 : 0,
                star.temperature(), star.drawnScale() };
        return on;
    }

    /** A recipe runs if its heat (Electric Blast Furnace and Plasma Forge store it as ebf_temp) is in reach. */
    static boolean heatOk(GTRecipe recipe, double kelvin) {
        return recipe.data == null || !recipe.data.contains("ebf_temp") || recipe.data.getInt("ebf_temp") <= kelvin;
    }

    /** Idle, the hoop takes shield and hull blocks from the input buses: +5 % each, one a second. */
    private void repair() {
        if (integrity >= HoopPhysics.INTEGRITY) return;
        var blocks = new HashSet<Item>();
        for (var part : shape().parts.values())
            if ("hoop".equals(part.ring())) blocks.add(MegaCapture.block(part).asItem());
        if (take(stack -> blocks.contains(stack.getItem()), 1) > 0)
            integrity = Math.min(HoopPhysics.INTEGRITY, integrity + HoopPhysics.REPAIR);
    }

    @VisibleForTesting
    public void setIntegrity(int perMille) {
        integrity = perMille;
    }

    public void setHeight(int pct) {
        heightPct = Math.max(HoopPhysics.HEIGHT_MIN, Math.min(HoopPhysics.HEIGHT_MAX, pct));
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return StellarHoopScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "dive" -> setHeight(heightPct - HoopPhysics.HEIGHT_STEP);
            case "rise" -> setHeight(heightPct + HoopPhysics.HEIGHT_STEP);
            default -> {
                if (data.equals("dev_show") && !net.minecraftforge.fml.loading.FMLEnvironment.production) devShow = 600;
                else super.handleDisplayClick(data, click);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        if (!inPlace()) {
            text.add(Component.translatable("gtia.stellar_hoop.needs_star").withStyle(ChatFormatting.RED));
            return;
        }
        text.add(Component.translatable("gtia.stellar_hoop.parallel", (int) view[0]).withStyle(ChatFormatting.GOLD));
        text.add(Component.translatable("gtia.stellar_hoop.corona", String.format("%.2f", view[1] / 1e6))
                .withStyle(ChatFormatting.YELLOW));
    }
}
