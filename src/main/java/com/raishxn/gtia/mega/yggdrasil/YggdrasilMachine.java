package com.raishxn.gtia.mega.yggdrasil;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterials;

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
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.List;

/**
 * Controller of the Yggdrasil Orchid (GTIA-D-327): runs Greenhouse recipes in huge batches in gardens floating at
 * the depth the player picks in a gas giant's atmosphere; every harvest also gives biomass.
 */
public class YggdrasilMachine extends MegaMachine {

    public static final String SHAPE = "yggdrasil";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(YggdrasilMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private double depth = 2;
    @Persisted
    private double progressTicks;
    @Persisted
    @DescSynced
    private long crafted;
    @Persisted
    @DescSynced
    private long biomass;
    @DescSynced
    private String recipe = "";
    @DescSynced
    private String blocked = "";
    /**
     * What the screen shows: [0] parallel, [1] speed, [2] growth, [3] luminosity, [4] best depth, [5] crafts this
     * second, [6] EU this second, [7] working (1/0).
     */
    @DescSynced
    private double[] view = new double[8];
    /** Dev aid: seconds of shown work, for screenshots. */
    private int devShow;
    private final MegaRecipeEngine.Progress progress = new MegaRecipeEngine.Progress();

    public YggdrasilMachine(IMachineBlockEntity holder, Object... args) {
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

    public double depth() {
        return depth;
    }

    public long crafted() {
        return crafted;
    }

    public long biomass() {
        return biomass;
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
        return List.of(GTNARecipeType.GREENHOUSE_RECIPES);
    }

    @Override
    public float fxLevel(String effect) {
        boolean on = view[7] > 0;
        if (effect.equals("gardens")) return on ? (float) (0.4 + view[2]) : 0;
        if (effect.equals("canopy")) return on ? 1 : 0.3f;
        return 1;
    }

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        double light = star == null ? 0 : StarPhysics.luminosity(star.kind, star.mass);
        double g = YggdrasilPhysics.growth(light, depth);
        double speed = YggdrasilPhysics.speed(g);
        int parallel = YggdrasilPhysics.parallel(phases);
        MegaRecipeEngine.Result result = new MegaRecipeEngine.Result(0, 0, "", "");
        if (isWorkingEnabled()) {
            progress.ticks = progressTicks;
            result = MegaRecipeEngine.second(this, level, types(), new MegaRecipeEngine.Limits(parallel, speed, 1,
                    r -> true), progress);
            progressTicks = progress.ticks;
            crafted += result.crafts();
            if (result.crafts() > 0) {
                int mb = (int) Math.min(Integer.MAX_VALUE, result.crafts() * YggdrasilPhysics.BIOMASS_PER_CRAFT);
                // fillFluid returns what did not fit.
                biomass += mb - fillFluid(GTMaterials.Biomass.getFluid(mb));
            }
        }
        recipe = result.recipe();
        blocked = result.blocked();
        boolean working = result.busy();
        if (devShow > 0) {
            devShow--;
            working = true;
            result = new MegaRecipeEngine.Result(parallel, 2.4e9, "gtna:greenhouse/wheat", "");
            recipe = result.recipe();
            blocked = "";
        }
        view = new double[] { parallel, speed, g, light, YggdrasilPhysics.bestDepth(light), result.crafts(),
                result.eu(), working ? 1 : 0 };
        return working || isWorkingEnabled();
    }

    public void setDepth(double z) {
        depth = Math.max(0, Math.min(YggdrasilPhysics.DEPTH_MAX, Math.round(z / YggdrasilPhysics.DEPTH_STEP) *
                YggdrasilPhysics.DEPTH_STEP));
    }

    @Override
    public ModularUI createUI(Player player) {
        return YggdrasilScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "deeper" -> setDepth(depth + YggdrasilPhysics.DEPTH_STEP);
            case "higher" -> setDepth(depth - YggdrasilPhysics.DEPTH_STEP);
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
        text.add(Component.translatable("gtia.yggdrasil.state", String.format("%.1f", depth),
                String.format("%.0f%%", view[2] * 100)).withStyle(ChatFormatting.GREEN));
    }
}
