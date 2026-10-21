package com.raishxn.gtia.mega.common;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtna.common.machine.multiblock.originals.OriginalMachine;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Base of the megastructures (GTIA-D-311, D-316): a pattern in five phases from {@link MegaShape}, where phases 2 to
 * 5 may be missing (their places accept air) and a phase counts once all its blocks and those before it are in
 * place; moving pieces leave the world while it runs ({@link MegaCapture}); built on GTNA's originals (incremental
 * structure checks, Nexus network power).
 */
public abstract class MegaMachine extends OriginalMachine implements IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MegaMachine.class,
            OriginalMachine.MANAGED_FIELD_HOLDER);

    public static final ResourceKey<Level> NEXUS_SYSTEM = ResourceKey.create(Registries.DIMENSION,
            GTIAMod.id("nexus_system"));
    /** Seconds between phase counts while it runs (a count reads every position of the structure). */
    static final int RECOUNT_SECONDS = 30;

    /** GameTests run in the overworld: lets them stand in for the Nexus System. */
    @VisibleForTesting
    public static boolean anyDimensionForTests;

    @Persisted
    @DescSynced
    protected int phases;
    @Persisted
    @DescSynced
    private boolean captured;
    /** Phases whose moving pieces are in the controller. */
    @Persisted
    @DescSynced
    private int capturedPhases;
    /** Running this second: what the renderer animates. */
    @DescSynced
    protected boolean running;
    /** onStructureFormed ran in this session (the tick is subscribed). */
    private boolean started;
    private boolean recount = true;
    private int sinceCount;

    protected MegaMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** Name of the data folder under {@code gtia_mega/}. */
    public abstract String shapeName();

    public MegaShape shape() {
        return MegaShape.get(shapeName());
    }

    public int phases() {
        return phases;
    }

    public boolean isRunning() {
        return running && isFormed();
    }

    /** Moving pieces are in the controller and drawn by the renderer. */
    public boolean isCaptured() {
        return captured && isFormed();
    }

    public int capturedPhases() {
        return isCaptured() ? capturedPhases : 0;
    }

    /** Where it works: the Nexus System unless a megastructure says otherwise. */
    public boolean inPlace() {
        return anyDimensionForTests || getLevel() != null && getLevel().dimension() == NEXUS_SYSTEM;
    }

    /**
     * The star of the system this machine stands in (GameTests: the Nexus System's), or null outside the star
     * systems. Server side only.
     */
    @org.jetbrains.annotations.Nullable
    public com.raishxn.gtia.mega.system.StarSystems.Star star() {
        if (!(getLevel() instanceof ServerLevel level)) return null;
        var dim = level.dimension();
        if (anyDimensionForTests && !com.raishxn.gtia.mega.system.StarSystems.isSystem(dim))
            dim = com.raishxn.gtia.mega.system.StarSystems.NEXUS;
        return com.raishxn.gtia.mega.system.StarSystems.get(level.getServer()).star(dim);
    }

    // ------------------------------------------------------------------ lifecycle

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        started = true;
        recount = true;
    }

    /**
     * A megastructure saved as formed comes back formed but without its tick: GTNA's StructureWatch skips formed
     * machines. The first poll after loading forms it again once.
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
        stopped();
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
            stopped();
        }
    }

    @Override
    protected void idleSecond() {
        running = false;
        stopped();
    }

    @Override
    protected final void secondTick(ServerLevel level) {
        if (recount || ++sinceCount >= RECOUNT_SECONDS) countPhases(level);
        if (!inPlace() || phases < 1 || shape() == null) {
            release();
            running = false;
            stopped();
            return;
        }
        running = megaSecond(level);
        // A finished phase brings its pieces in on the next capture.
        if (running && captured && phases > capturedPhases) release();
        if (running) capture();
        else release();
    }

    /** One second of work with the structure formed and in place; returns whether it runs (moving pieces turn). */
    protected abstract boolean megaSecond(ServerLevel level);

    /** Clears what the screen shows when it stops. */
    protected void stopped() {}

    // ------------------------------------------------------------------ power and hatches

    /** Takes {@code eu} from the owner's Nexus network; false (and nothing taken) if it cannot. */
    public boolean pay(ServerLevel level, double eu) {
        return consume(level, eu);
    }

    /** Gives {@code eu} to the owner's Nexus network. */
    public void payOut(ServerLevel level, double eu) {
        deliver(level, eu);
    }

    /** Puts a fluid into the output hatches; returns what did not fit. */
    protected long fillFluid(net.minecraftforge.fluids.FluidStack stack) {
        return fill(stack, com.gregtechceu.gtceu.api.capability.recipe.IO.OUT);
    }

    /** Dev aid ({@code /gtia_dev_fill}): puts a fluid into the input hatches; returns what did not fit. */
    @VisibleForTesting
    public long fillInputForTests(net.minecraftforge.fluids.FluidStack stack) {
        return fill(stack, com.gregtechceu.gtceu.api.capability.recipe.IO.IN);
    }

    private long fill(net.minecraftforge.fluids.FluidStack stack, com.gregtechceu.gtceu.api.capability.recipe.IO io) {
        if (stack == null || stack.isEmpty()) return 0;
        long left = stack.getAmount();
        for (var part : getParts()) {
            for (var handlers : part.getRecipeHandlers()) {
                if (!handlers.isValid(io)) continue;
                for (var handler : handlers.getCapability(
                        com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP)) {
                    if (!(handler instanceof com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank tank) ||
                            tank.getHandlerIO() != io || left <= 0)
                        continue;
                    var put = stack.copy();
                    put.setAmount((int) Math.min(Integer.MAX_VALUE, left));
                    left -= tank.fillInternal(put,
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
        return left;
    }

    /** Takes up to {@code max} mB of fluids matching {@code filter} from the input hatches. */
    protected long drainFluid(java.util.function.Predicate<net.minecraft.world.level.material.Fluid> filter,
                              long max) {
        return drain(filter, max);
    }

    // ------------------------------------------------------------------ effects (read by the client renderer)

    /** Strength of a render effect of this machine's {@code fx.json}; 0 hides it. */
    public float fxLevel(String effect) {
        return 1;
    }

    /** Temperature (K) of a star effect, or 0 for the one in {@code fx.json}. */
    public float fxTemperature(String effect) {
        return 0;
    }

    /** Size factor of an effect. */
    public float fxScale(String effect) {
        return 1;
    }

    /** Dev aid: makes {@code owner} the owner whose Nexus network and eras this machine uses. */
    @VisibleForTesting
    public void setOwnerForTests(java.util.UUID owner) {
        networkOwner = owner;
    }

    /** One second of work for GameTests, as the owner {@code owner}. */
    @VisibleForTesting
    public void testSecond(java.util.UUID owner) {
        networkOwner = owner;
        secondTick((ServerLevel) getLevel());
    }

    // ------------------------------------------------------------------ phases and moving pieces

    /** Counts the phases whose blocks are all in place, in order. */
    @VisibleForTesting
    public void countPhases(Level level) {
        recount = false;
        sinceCount = 0;
        var shape = shape();
        if (shape == null) return;
        var layout = structureWatch().layout();
        int[] missing = new int[MegaShape.PHASES + 1];
        for (int i = 0; i < layout.size(); i++) {
            var part = shape.part(layout.matcher(i).letter());
            // Phase 1 is checked by the pattern itself, except its moving pieces, which accept air.
            if (part == null || part.phase() < 2 && !part.moving()) continue;
            // Captured pieces are in the controller, so their air counts as built.
            if (captured && part.moving() && part.phase() <= capturedPhases) continue;
            if (level.getBlockState(BlockPos.of(layout.position(i))).isAir()) missing[part.phase()]++;
        }
        if (missing[1] > 0) {
            phases = 0;
            return;
        }
        int built = 1;
        while (built < MegaShape.PHASES && missing[built + 1] == 0) built++;
        phases = built;
    }

    private void capture() {
        if (captured || getLevel() == null) return;
        MegaCapture.capture(this, phases);
        capturedPhases = phases;
        captured = true;
    }

    protected void release() {
        if (!captured || getLevel() == null || getLevel().isClientSide) return;
        MegaCapture.release(this, capturedPhases);
        captured = false;
        capturedPhases = 0;
    }
}
