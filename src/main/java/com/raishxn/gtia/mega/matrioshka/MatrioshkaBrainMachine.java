package com.raishxn.gtia.mega.matrioshka;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.PsiStore;
import com.raishxn.gtna.common.machine.multiblock.originals.OriginalMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collection;
import java.util.List;

/**
 * Controller of the Matrioshka Brain (GTIA-D-311): the first megastructure. Built in five phases in the Nexus
 * System, around its star; each finished phase adds a shell of computation. Sends CWU through optical data
 * transmission hatches and turns the CWU nobody asks for into Ψ for its owner.
 *
 * <p>
 * Phases 2 to 5 may be missing (their places accept air); a phase counts once all its blocks and those of the
 * phases before it are in place. While it runs, the turning rings leave the world and the
 * controller draws them turning ({@link MatrioshkaCapture}).
 */
public class MatrioshkaBrainMachine extends OriginalMachine implements IOpticalComputationProvider, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MatrioshkaBrainMachine.class, OriginalMachine.MANAGED_FIELD_HOLDER);

    public static final ResourceKey<Level> NEXUS_SYSTEM = ResourceKey.create(Registries.DIMENSION,
            GTIAMod.id("nexus_system"));
    /** Seconds between phase counts while it runs (a count reads every position of the structure). */
    static final int RECOUNT_SECONDS = 30;

    /** GameTests run in the overworld: lets them stand in for the Nexus System. */
    @VisibleForTesting
    public static boolean anyDimensionForTests;

    @Persisted
    @DescSynced
    private int phases;
    @Persisted
    @DescSynced
    private boolean captured;
    /** Phases whose rings are in the controller. */
    @Persisted
    @DescSynced
    private int capturedPhases;
    @DescSynced
    private boolean running;
    @DescSynced
    private int maxCwut;
    @DescSynced
    private int usedCwut;
    @DescSynced
    private long psiView;
    /** Clock in percent, set from the screen. */
    @Persisted
    @DescSynced
    private int clockPct = 100;
    /** Coolant supply of the last second, 0..1, and which coolant it was. */
    @DescSynced
    private double coolant;
    @DescSynced
    private int coolantTier;
    /** Shell temperatures in kelvin (they change slowly) and integrity in per mille. */
    @Persisted
    private double[] temps = new double[MatrioshkaShells.PHASES];
    @Persisted
    private int[] integrity = { 1000, 1000, 1000, 1000, 1000 };
    /**
     * What the screen shows: [0..4] shell temperatures (K, 0 if not built), [5] overheated and [6] fractured shells as
     * bit masks, [7] Ψ per second, [8] coolant demand mB/s, [9..13] integrity per mille, [14] limit K.
     */
    @DescSynced
    private double[] view = new double[15];
    /** onStructureFormed ran in this session (the tick is subscribed). */
    private boolean started;
    private boolean recount = true;
    private int sinceCount;
    private long tickStamp = Long.MIN_VALUE;
    private int usedThisTick;
    private long usedThisSecond;
    private long psiRemainder;

    public MatrioshkaBrainMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public int phases() {
        return phases;
    }

    public boolean isRunning() {
        return running && isFormed();
    }

    /** Moving rings are in the controller and drawn by the renderer. */
    public boolean isCaptured() {
        return captured && isFormed();
    }

    public int capturedPhases() {
        return isCaptured() ? capturedPhases : 0;
    }

    public int clockPct() {
        return clockPct;
    }

    public double coolant() {
        return coolant;
    }

    public MatrioshkaShells.Coolant coolantTier() {
        return MatrioshkaShells.Coolant.values()[Math.max(0, Math.min(3, coolantTier))];
    }

    public int maxCwut() {
        return maxCwut;
    }

    public int usedCwut() {
        return usedCwut;
    }

    public long psi() {
        return psiView;
    }

    public double[] view() {
        return view;
    }

    public boolean inNexusSystem() {
        return anyDimensionForTests || getLevel() != null && getLevel().dimension() == NEXUS_SYSTEM;
    }

    // ------------------------------------------------------------------ structure

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        started = true;
        recount = true;
    }

    /**
     * A brain saved as formed comes back formed but without its tick: GTNA's StructureWatch skips formed machines.
     * The first poll after loading forms it again once.
     */
    @Override
    public void asyncCheckPattern(long periodID) {
        if (isFormed() && !started && getLevel() instanceof ServerLevel level) {
            started = true;
            level.getServer().execute(() -> {
                if (isInValid()) return;
                if (checkPatternWithLock()) onStructureFormed();
                else onStructureInvalid();
            });
            return;
        }
        super.asyncCheckPattern(periodID);
    }

    @Override
    public void onStructureInvalid() {
        started = false;
        release();
        running = false;
        maxCwut = 0;
        super.onStructureInvalid();
    }

    @Override
    public void onMachineRemoved() {
        release();
    }

    @Override
    public void setWorkingEnabled(boolean enabled) {
        super.setWorkingEnabled(enabled);
        if (!enabled) {
            release();
            running = false;
        }
    }

    @Override
    protected void idleSecond() {
        running = false;
        maxCwut = 0;
    }

    @Override
    protected void secondTick(ServerLevel level) {
        if (recount || ++sinceCount >= RECOUNT_SECONDS) countPhases(level);
        if (!inNexusSystem() || phases < 1) {
            release();
            running = false;
            maxCwut = 0;
            return;
        }
        var shape = MatrioshkaShape.get();
        if (shape == null) return;
        // The best coolant in the hatches is used; it is drained in proportion to the demand.
        var used = MatrioshkaShells.Coolant.NONE;
        double supply = 0;
        long demand = 0;
        for (int t = MatrioshkaShells.Coolant.values().length - 1; t > 0 && used == MatrioshkaShells.Coolant.NONE;
                t--) {
            var tier = MatrioshkaShells.Coolant.values()[t];
            long want = MatrioshkaShells.coolantDemand(tier, phases, clockPct);
            long got = want <= 0 ? 0 : drain(f -> MatrioshkaThermal.is(tier, f), want);
            if (got > 0) {
                used = tier;
                supply = (double) got / want;
                demand = want;
            }
        }
        coolant = supply;
        coolantTier = used.ordinal();
        repair();
        var heat = MatrioshkaThermal.second(shape.radius, phases, clockPct, used, supply, temps, integrity);
        maxCwut = (int) Math.min(Integer.MAX_VALUE, heat.cwut());
        running = maxCwut > 0;
        usedCwut = (int) Math.min(Integer.MAX_VALUE, usedThisSecond / 20);
        double[] v = new double[15];
        for (int p = 0; p < MatrioshkaShells.PHASES; p++) {
            v[p] = temps[p];
            v[9 + p] = integrity[p];
        }
        v[5] = heat.overheated();
        v[6] = heat.fractured();
        v[8] = demand;
        v[14] = MatrioshkaShells.limit(phases);
        if (networkOwner != null) {
            // EN: CWU nobody asked for this second becomes Ψ: 2^20 idle CWU-ticks per thought cycle.
            // PT: O CWU que ninguém pediu neste segundo vira Ψ: 2^20 CWU-tick ociosos por ciclo de pensamento.
            // ZH: 本秒无人请求的 CWU 转为 Ψ：每 2^20 个空闲 CWU-tick 产生一个思维周期。
            long idle = Math.max(0, (long) maxCwut * 20 - usedThisSecond) + psiRemainder;
            PsiStore.get(level.getServer()).add(networkOwner, idle / MatrioshkaShells.CWU_PER_PSI);
            v[7] = idle / MatrioshkaShells.CWU_PER_PSI;
            psiRemainder = idle % MatrioshkaShells.CWU_PER_PSI;
            psiView = PsiStore.get(level.getServer()).balance(networkOwner);
        }
        view = v;
        usedThisSecond = 0;
        // A finished phase brings its rings in on the next capture.
        if (running && captured && phases > capturedPhases) release();
        if (running) capture();
    }

    /**
     * Worn shells take their own blocks from the input buses: each block restores {@link MatrioshkaShells#REPAIR}
     * per mille, at most one per shell a second, and only while the shell is not overheated.
     */
    private void repair() {
        var shape = MatrioshkaShape.get();
        double limit = MatrioshkaShells.limit(phases);
        for (int p = 1; p <= phases; p++) {
            int i = p - 1;
            if (integrity[i] >= MatrioshkaShells.INTEGRITY || temps[i] > limit) continue;
            var blocks = new java.util.HashSet<net.minecraft.world.item.Item>();
            for (var part : shape.parts.values())
                if (part.phase() == p) blocks.add(MatrioshkaCapture.block(part).asItem());
            if (take(stack -> blocks.contains(stack.getItem()), 1) > 0)
                integrity[i] = Math.min(MatrioshkaShells.INTEGRITY, integrity[i] + MatrioshkaShells.REPAIR);
        }
    }

    public int integrity(int phase) {
        return integrity[phase - 1];
    }

    @VisibleForTesting
    public void setIntegrity(int phase, int perMille) {
        integrity[phase - 1] = perMille;
    }

    @VisibleForTesting
    public double temperature(int phase) {
        return temps[phase - 1];
    }

    // ------------------------------------------------------------------ screen

    @Override
    public com.lowdragmc.lowdraglib.gui.modular.ModularUI createUI(net.minecraft.world.entity.player.Player player) {
        return MatrioshkaScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, com.lowdragmc.lowdraglib.gui.util.ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "clock_down" -> setClock(clockPct - MatrioshkaShells.CLOCK_STEP);
            case "clock_up" -> setClock(clockPct + MatrioshkaShells.CLOCK_STEP);
            default -> super.handleDisplayClick(data, click);
        }
    }

    public void setClock(int pct) {
        clockPct = Math.max(MatrioshkaShells.CLOCK_MIN, Math.min(MatrioshkaShells.CLOCK_MAX, pct));
    }

    /** One second of work for GameTests, as the owner {@code owner}. */
    @VisibleForTesting
    public void testSecond(java.util.UUID owner) {
        networkOwner = owner;
        secondTick((ServerLevel) getLevel());
    }

    /** Counts the phases whose blocks are all in place, in order. */
    @VisibleForTesting
    public void countPhases(Level level) {
        recount = false;
        sinceCount = 0;
        var layout = structureWatch().layout();
        int[] missing = new int[MatrioshkaShells.PHASES + 1];
        for (int i = 0; i < layout.size(); i++) {
            var part = MatrioshkaShape.Part.of(layout.matcher(i).letter());
            if (part == null || part.phase() < 2) continue;
            // Captured rings are in the controller, so their air counts as built.
            if (captured && part.moving() && part.phase() <= capturedPhases) continue;
            if (level.getBlockState(BlockPos.of(layout.position(i))).isAir()) missing[part.phase()]++;
        }
        int built = 1;
        while (built < MatrioshkaShells.PHASES && missing[built + 1] == 0) built++;
        phases = built;
    }

    private void capture() {
        if (captured || getLevel() == null) return;
        MatrioshkaCapture.capture(this, phases);
        capturedPhases = phases;
        captured = true;
    }

    private void release() {
        if (!captured || getLevel() == null || getLevel().isClientSide) return;
        MatrioshkaCapture.release(this, capturedPhases);
        captured = false;
        capturedPhases = 0;
    }

    // ------------------------------------------------------------------ computation

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (!isRunning()) return 0;
        long now = getOffsetTimer();
        if (now != tickStamp) {
            tickStamp = now;
            usedThisTick = 0;
        }
        int given = Math.max(0, Math.min(cwut, maxCwut - usedThisTick));
        if (!simulate) {
            usedThisTick += given;
            usedThisSecond += given;
        }
        return given;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isRunning() ? maxCwut : 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }

    // ------------------------------------------------------------------ display

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        if (!inNexusSystem()) {
            text.add(Component.translatable("gtia.matrioshka.needs_star").withStyle(ChatFormatting.RED));
            return;
        }
        text.add(Component.translatable("gtia.matrioshka.phases", phases, MatrioshkaShells.PHASES)
                .withStyle(ChatFormatting.AQUA));
        var shape = MatrioshkaShape.get();
        if (shape != null) for (int p = 1; p <= phases; p++) {
            double r = shape.radius[p - 1];
            text.add(Component.translatable("gtia.matrioshka.shell", p,
                    String.format("%.0f", temps[p - 1]),
                    String.format("%,d", MatrioshkaShells.shellCwut(p, temps[p - 1], clockPct)))
                    .withStyle(ChatFormatting.GRAY));
        }
        text.add(Component.translatable("gtia.matrioshka.cwut", String.format("%,d", usedCwut),
                String.format("%,d", maxCwut)).withStyle(ChatFormatting.GREEN));
        text.add(Component.translatable("gtia.matrioshka.psi", String.format("%,d", psiView))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
