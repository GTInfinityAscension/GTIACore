package com.raishxn.gtia.mega.suprashell;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

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
import com.raishxn.gtia.mega.common.MegaModules;
import com.raishxn.gtia.mega.common.MegaRecipeEngine;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.List;

/**
 * Controller of the Black Hole Suprashell (GTIA-D-329): hosts GTCEu multiblock controllers placed in its input buses
 * as modules and runs their recipes, 64 parallels a module, at the time dilation of the shell's radius.
 */
public class SuprashellMachine extends MegaMachine {

    public static final String SHAPE = "suprashell";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SuprashellMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private double radius = 6;
    @Persisted
    private double progressTicks;
    @Persisted
    @DescSynced
    private long crafted;
    @DescSynced
    private String recipe = "";
    @DescSynced
    private String blocked = "";
    /** Hosted modules as "name=count" lines, for the screen. */
    @DescSynced
    private String modules = "";
    /**
     * What the screen shows: [0] modules hosted, [1] capacity, [2] parallel, [3] tau, [4] radius, [5] crafts this
     * second, [6] EU this second, [7] working (1/0), [8] recipe types.
     */
    @DescSynced
    private double[] view = new double[9];
    /** Dev aid: seconds of shown work, for screenshots. */
    private int devShow;
    private final MegaRecipeEngine.Progress progress = new MegaRecipeEngine.Progress();

    public SuprashellMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Only round a black hole. */
    @Override
    public boolean inPlace() {
        if (getLevel() != null && getLevel().isClientSide) return view[1] > 0;
        var star = star();
        return star != null && star.kind == Kind.BLACK_HOLE && (anyDimensionForTests ||
                StarSystems.isSystem(getLevel().dimension()));
    }

    public double radius() {
        return radius;
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

    public String modules() {
        return modules;
    }

    public double[] view() {
        return view;
    }

    @Override
    public float fxLevel(String effect) {
        boolean on = view[7] > 0;
        if (effect.equals("lowering")) return on ? 1 : 0;
        if (effect.equals("shell_field")) return on ? 1 : 0.4f;
        return 1;
    }

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var hosted = MegaModules.hosted(this);
        int count = MegaModules.count(hosted);
        int capacity = ShellPhysics.capacity(radius, phases);
        int used = Math.min(count, capacity);
        double tau = ShellPhysics.tau(radius);
        var types = MegaModules.types(hosted);
        MegaRecipeEngine.Result result = new MegaRecipeEngine.Result(0, 0, "", used == 0 ? "modules" : "");
        if (isWorkingEnabled() && used > 0 && !types.isEmpty()) {
            progress.ticks = progressTicks;
            result = MegaRecipeEngine.second(this, level, types, new MegaRecipeEngine.Limits(
                    ShellPhysics.parallel(used), tau, tau, r -> true), progress);
            progressTicks = progress.ticks;
            crafted += result.crafts();
        }
        recipe = result.recipe();
        blocked = result.blocked();
        boolean working = result.crafts() > 0;
        if (devShow > 0) {
            devShow--;
            working = true;
            result = new MegaRecipeEngine.Result(ShellPhysics.parallel(Math.max(used, 8)), 8.4e11,
                    "gtceu:large_chemical_reactor/polytetrafluoroethylene", "");
            recipe = result.recipe();
            blocked = "";
        }
        modules = MegaModules.lines(hosted);
        view = new double[] { count, capacity, ShellPhysics.parallel(used), tau, radius, result.crafts(), result.eu(),
                working ? 1 : 0, types.size() };
        return working || isWorkingEnabled();
    }

    public void setRadius(double r) {
        radius = Math.max(ShellPhysics.R_MIN, Math.min(ShellPhysics.R_MAX, Math.round(r / ShellPhysics.R_STEP) *
                ShellPhysics.R_STEP));
    }

    @Override
    public ModularUI createUI(Player player) {
        return SuprashellScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "closer" -> setRadius(radius - ShellPhysics.R_STEP);
            case "farther" -> setRadius(radius + ShellPhysics.R_STEP);
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
        text.add(Component.translatable("gtia.suprashell.state", (int) view[0], (int) view[1],
                String.format("%.1f", radius), String.format("%.3f", view[3])).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
