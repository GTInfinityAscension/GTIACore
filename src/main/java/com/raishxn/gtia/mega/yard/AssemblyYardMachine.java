package com.raishxn.gtia.mega.yard;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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

import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaRecipeEngine;
import com.raishxn.gtia.mega.system.StarPhysics;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.List;

/**
 * Controller of the Hyperstructural Assembly Yard (GTIA-D-326): runs Assembly Line and Assembler recipes on as many
 * slipways as the player opens, at a rate set by the star's light.
 */
public class AssemblyYardMachine extends MegaMachine {

    public static final String SHAPE = "assembly_yard";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AssemblyYardMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int active = YardPhysics.SLIPWAYS_PER_PHASE;
    @Persisted
    @DescSynced
    private boolean rush;
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
     * What the screen shows: [0] parallel, [1] speed, [2] active slipways, [3] slipways built, [4] crafts this
     * second, [5] EU this second, [6] luminosity (L☉), [7] star temperature, [8] working (1/0), [9] drawn scale.
     */
    @DescSynced
    private double[] view = new double[10];
    /** Dev aid: seconds of shown work, for screenshots. */
    private int devShow;
    private final MegaRecipeEngine.Progress progress = new MegaRecipeEngine.Progress();

    public AssemblyYardMachine(IMachineBlockEntity holder, Object... args) {
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
        return anyDimensionForTests || getLevel() != null && StarSystems.isSystem(getLevel().dimension());
    }

    public int active() {
        return active;
    }

    public boolean rush() {
        return rush;
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
        return List.of(GTRecipeTypes.ASSEMBLY_LINE_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES);
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        boolean on = view[8] > 0;
        if (effect.startsWith("weld") || effect.startsWith("spark")) return on ? (rush ? 1.4f : 1) : 0;
        if (effect.equals("traffic")) return on ? 1 : 0.3f;
        return 1;
    }

    @Override
    public float fxTemperature(String effect) {
        return effect.equals("star") && view[7] > 0 ? (float) view[7] : 0;
    }

    @Override
    public float fxScale(String effect) {
        return effect.equals("star") && view[9] > 0 ? (float) view[9] : 1;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        double light = star == null ? 0 : StarPhysics.luminosity(star.kind, star.mass);
        int built = YardPhysics.slipways(phases);
        int open = Math.min(active, built);
        int parallel = YardPhysics.parallel(open);
        double speed = YardPhysics.speed(light, rush);
        MegaRecipeEngine.Result result = new MegaRecipeEngine.Result(0, 0, "", open == 0 ? "slipways" : "");
        if (isWorkingEnabled() && open > 0) {
            progress.ticks = progressTicks;
            result = MegaRecipeEngine.second(this, level, types(), new MegaRecipeEngine.Limits(parallel, speed,
                    YardPhysics.euFactor(rush), r -> true), progress);
            progressTicks = progress.ticks;
            crafted += result.crafts();
        }
        recipe = result.recipe();
        blocked = result.blocked();
        boolean working = result.crafts() > 0;
        if (devShow > 0) {
            devShow--;
            working = true;
            result = new MegaRecipeEngine.Result(parallel * 2L, 1.6e12, "gtceu:assembly_line/uv_fusion_reactor", "");
            recipe = result.recipe();
            blocked = "";
        }
        double temp = star == null ? 0 : StarPhysics.temperature(star.kind, star.mass);
        float scale = star == null ? 1 : StarPhysics.drawnScale(star.kind, star.mass);
        view = new double[] { parallel, speed, open, built, result.crafts(), result.eu(), light, temp,
                working ? 1 : 0, scale };
        return working || isWorkingEnabled();
    }

    public void setActive(int slipways) {
        active = Math.max(0, Math.min(YardPhysics.slipways(5), slipways));
    }

    public void setRush(boolean on) {
        rush = on;
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return AssemblyYardScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "open" -> setActive(active + YardPhysics.STEP);
            case "close" -> setActive(active - YardPhysics.STEP);
            case "rush" -> setRush(!rush);
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
        text.add(Component.translatable("gtia.assembly_yard.state", (int) view[2], (int) view[3],
                String.format("%.1f", view[1])).withStyle(ChatFormatting.AQUA));
    }
}
