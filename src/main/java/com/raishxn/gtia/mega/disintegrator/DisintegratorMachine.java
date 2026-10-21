package com.raishxn.gtia.mega.disintegrator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Controller of the Solar System Disintegrator (GTIA-D-330): takes a made system of the same owner apart layer by
 * layer, paying its binding energy from the Nexus network, and gives the matter out; taking the star erases the
 * system.
 */
public class DisintegratorMachine extends MegaMachine {

    public static final String SHAPE = "disintegrator";
    /** Every dust any layer gives, in one fixed order for the owed counts. */
    static final Material[] DUSTS;

    static {
        var all = new LinkedHashSet<Material>();
        for (int l = 0; l < 3; l++) all.addAll(List.of(DisintegratorPhysics.yields(l)));
        DUSTS = all.toArray(new Material[0]);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DisintegratorMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int target = -1;
    /** The made system's generation when it was picked: a remade slot starts over. */
    @Persisted
    private int targetGeneration;
    @Persisted
    @DescSynced
    private double taken;
    /** Last layer to take: 0 crust, 1 mantle, 2 core, 3 the star too. */
    @Persisted
    @DescSynced
    private int stopAt = 2;
    @Persisted
    private long[] owed = new long[DUSTS.length];
    @Persisted
    private long owedHydrogen, owedHelium;
    @Persisted
    @DescSynced
    private int erased;
    /**
     * What the screen shows: [0] target slot (-1 none), [1] share taken, [2] layer, [3] mass, [4] metallicity,
     * [5] EU/t spent, [6] stop at, [7] working (1/0), [8] dust owed, [9] star kind ordinal, [10] gas owed (mB).
     */
    @DescSynced
    private double[] view = { -1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0 };
    /** Dev aid: seconds of shown work, for screenshots. */
    private int devShow;

    public DisintegratorMachine(IMachineBlockEntity holder, Object... args) {
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

    public int target() {
        return target;
    }

    public double taken() {
        return taken;
    }

    public int stopAt() {
        return stopAt;
    }

    public int erased() {
        return erased;
    }

    public double[] view() {
        return view;
    }

    public long owedDust() {
        long t = 0;
        for (long o : owed) t += o;
        return t;
    }

    public long owedGas() {
        return owedHydrogen + owedHelium;
    }

    /** Made systems of this machine's owner, other than the one it stands in. */
    List<Integer> targets(ServerLevel level) {
        var systems = StarSystems.get(level.getServer());
        int here = StarSystems.slotOf(level.dimension());
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            var s = systems.star(StarSystems.slot(i));
            if (i == here || s == null || s.kind == Kind.NONE) continue;
            if (networkOwner != null && !networkOwner.equals(s.owner)) continue;
            out.add(i);
        }
        return out;
    }

    @Override
    public float fxLevel(String effect) {
        boolean on = view[7] > 0;
        if (effect.startsWith("inflow") || effect.startsWith("projector") || effect.equals("debris"))
            return on ? 1 : 0;
        if (effect.equals("pocket_field")) return on ? 1.2f : 0.4f;
        return 1;
    }

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var systems = StarSystems.get(level.getServer());
        var star = target < 0 ? null : systems.star(StarSystems.slot(target));
        if (star != null && (star.kind == Kind.NONE || star.generation != targetGeneration)) {
            target = -1;
            taken = 0;
            star = null;
        }
        double spent = 0;
        boolean working = false;
        if (star != null && isWorkingEnabled()) {
            double stop = stopAt >= 3 ? 1 : stopAt == 2 ? DisintegratorPhysics.CORE :
                    stopAt == 1 ? DisintegratorPhysics.MANTLE : DisintegratorPhysics.CRUST;
            if (taken < stop) {
                double u = DisintegratorPhysics.binding(star.mass);
                double need = (DisintegratorPhysics.energyShare(stop) - DisintegratorPhysics.energyShare(taken)) * u;
                double chunk = Math.min(need, DisintegratorPhysics.throughput(phases) * 20);
                if (pay(level, chunk)) {
                    double e = DisintegratorPhysics.energyShare(taken) + chunk / u;
                    double next = Math.min(stop, DisintegratorPhysics.massShare(e));
                    take(star.mass, star.metallicity, taken, next);
                    taken = next;
                    spent = chunk / 20;
                    working = true;
                    if (taken >= 1 - 1e-9) {
                        systems.clear(target);
                        com.raishxn.gtia.mega.system.SystemWipe.request(level.getServer(), target);
                        erased++;
                        target = -1;
                        taken = 0;
                    }
                    systems.changed();
                }
            }
        }
        deliver();
        if (devShow > 0) {
            devShow--;
            working = true;
            spent = Math.max(spent, DisintegratorPhysics.throughput(phases));
        }
        view = new double[] { target, taken, DisintegratorPhysics.layer(taken), star == null ? 0 : star.mass,
                star == null ? 0 : star.metallicity, spent, stopAt, working ? 1 : 0, owedDust(),
                star == null ? 0 : star.kind.ordinal(), owedGas() };
        return working || isWorkingEnabled();
    }

    /** Adds what the layers between shares {@code from} and {@code to} give. */
    private void take(double mass, double z, double from, double to) {
        double[] edges = { 0, DisintegratorPhysics.CRUST, DisintegratorPhysics.MANTLE, DisintegratorPhysics.CORE, 1 };
        for (int l = 0; l < 4; l++) {
            double df = Math.max(0, Math.min(to, edges[l + 1]) - Math.max(from, edges[l]));
            if (df <= 0) continue;
            if (l == 3) {
                long gas = DisintegratorPhysics.gas(df, mass);
                owedHydrogen += gas * 3 / 4;
                owedHelium += gas / 4;
                continue;
            }
            for (var mat : DisintegratorPhysics.yields(l))
                for (int i = 0; i < DUSTS.length; i++)
                    if (DUSTS[i] == mat) owed[i] += DisintegratorPhysics.dust(df, mass, z);
        }
    }

    /** Pours out what is owed: up to 256 of each dust and a million mB of each gas a second. */
    private void deliver() {
        for (int i = 0; i < DUSTS.length; i++) {
            if (owed[i] <= 0) continue;
            long n = Math.min(256, owed[i]);
            long left = output(ChemicalHelper.get(TagPrefix.dust, DUSTS[i], 1), n);
            owed[i] -= n - left;
        }
        owedHydrogen -= pour(GTMaterials.Hydrogen, owedHydrogen);
        owedHelium -= pour(GTMaterials.Helium, owedHelium);
    }

    private long pour(Material gas, long owedMb) {
        if (owedMb <= 0) return 0;
        int n = (int) Math.min(1_000_000, owedMb);
        return n - fillFluid(gas.getFluid(n));
    }

    public void pick(ServerLevel level, int slot) {
        var s = StarSystems.get(level.getServer()).star(StarSystems.slot(slot));
        if (s == null || s.kind == Kind.NONE) return;
        target = slot;
        targetGeneration = s.generation;
        taken = 0;
    }

    public void setStopAt(int layer) {
        stopAt = Math.max(0, Math.min(3, layer));
    }

    @Override
    public ModularUI createUI(Player player) {
        return DisintegratorScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote || !(getLevel() instanceof ServerLevel level)) return;
        switch (data) {
            case "next", "prev" -> {
                var list = targets(level);
                if (list.isEmpty()) return;
                int at = list.indexOf(target);
                int to = at < 0 ? 0 : Math.floorMod(at + (data.equals("next") ? 1 : -1), list.size());
                if (taken > 0 && target >= 0) return; // a system half taken apart is not dropped by a click
                pick(level, list.get(to));
            }
            case "stop" -> setStopAt((stopAt + 1) % 4);
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
        text.add(Component.translatable("gtia.disintegrator.state", (int) view[0] + 1,
                String.format("%.1f%%", view[1] * 100)).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
