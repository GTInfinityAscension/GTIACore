package com.raishxn.gtia.mega.birch;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtia.mega.EraStore;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaModules;
import com.raishxn.gtia.mega.common.MegaRecipeEngine;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.List;

/**
 * Controller of the Birch World (GTIA-D-331): hosts multiblock controllers like the Suprashell, runs their recipes
 * 64 times as fast and books their EU as causal debt, paid back at the rate the player sets; a debt past the credit
 * is a paradox.
 */
public class BirchWorldMachine extends MegaMachine {

    public static final String SHAPE = "birch_world";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BirchWorldMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private double debt;
    @Persisted
    @DescSynced
    private double paid;
    @Persisted
    @DescSynced
    private int repay = 4;
    @Persisted
    @DescSynced
    private int paradoxes;
    @Persisted
    private int frozen;
    @Persisted
    private double progressTicks;
    @Persisted
    @DescSynced
    private long crafted;
    @DescSynced
    private String recipe = "";
    @DescSynced
    private String blocked = "";
    @DescSynced
    private String modules = "";
    /**
     * What the screen shows: [0] modules, [1] capacity, [2] parallel, [3] debt, [4] credit, [5] repaid this second,
     * [6] crafts this second, [7] borrowed this second, [8] working (1/0), [9] seconds frozen, [10] era level.
     */
    @DescSynced
    private double[] view = new double[11];
    /** True while repaying: then pay() draws real EU. */
    private boolean repaying;
    private double borrowed;
    /** Dev aid: seconds of shown work, for screenshots. */
    private int devShow;
    private final MegaRecipeEngine.Progress progress = new MegaRecipeEngine.Progress();

    public BirchWorldMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Only round a black hole: its rotating interior holds the time loops. */
    @Override
    public boolean inPlace() {
        if (getLevel() != null && getLevel().isClientSide) return view[4] > 0;
        var star = star();
        return star != null && star.kind == Kind.BLACK_HOLE && (anyDimensionForTests ||
                StarSystems.isSystem(getLevel().dimension()));
    }

    public double debt() {
        return debt;
    }

    public double paid() {
        return paid;
    }

    public int repay() {
        return repay;
    }

    public int paradoxes() {
        return paradoxes;
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

    /** Recipe EU is booked as debt, not drawn; only repayment draws real EU. */
    @Override
    public boolean pay(ServerLevel level, double eu) {
        if (repaying) return super.pay(level, eu);
        debt += eu;
        borrowed += eu;
        return true;
    }

    @Override
    public float fxLevel(String effect) {
        if (effect.equals("paradox")) return view[9] > BirchPhysics.PARADOX_SECONDS - 4 ? 1.5f : 0;
        if (effect.equals("loops")) return view[8] > 0 ? 1 : 0.2f;
        if (effect.equals("causal_field")) return (float) (0.4 + Math.min(1.2, view[3] / Math.max(1, view[4])));
        return 1;
    }

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        double mass = star == null ? 1 : star.mass;
        double credit = BirchPhysics.credit(phases, mass);
        var hosted = MegaModules.hosted(this);
        int count = MegaModules.count(hosted);
        int used = Math.min(count, BirchPhysics.capacity(phases));
        borrowed = 0;
        double repaid = 0;
        MegaRecipeEngine.Result result = new MegaRecipeEngine.Result(0, 0, "", used == 0 ? "modules" : "");
        if (frozen > 0) {
            frozen--;
            result = new MegaRecipeEngine.Result(0, 0, "", "paradox");
        } else {
            debt = BirchPhysics.grow(debt);
            double want = Math.min(debt, BirchPhysics.repayment(repay));
            if (want > 0) {
                repaying = true;
                if (pay(level, want)) {
                    debt -= want;
                    paid += want;
                    repaid = want;
                }
                repaying = false;
            }
            var types = MegaModules.types(hosted);
            if (isWorkingEnabled() && used > 0 && !types.isEmpty()) {
                progress.ticks = progressTicks;
                result = MegaRecipeEngine.second(this, level, types, new MegaRecipeEngine.Limits(
                        BirchPhysics.parallel(used), BirchPhysics.SPEED, 1, r -> true), progress);
                progressTicks = progress.ticks;
                crafted += result.crafts();
            }
            if (debt > credit) paradox();
        }
        if (networkOwner != null) {
            var era = EraStore.get(level.getServer());
            era.set(EraStore.CAUSAL_DEBT, networkOwner, debt);
            era.set(EraStore.CAUSAL_PAID, networkOwner, paid);
        }
        recipe = result.recipe();
        blocked = result.blocked();
        boolean working = result.crafts() > 0;
        if (devShow > 0) {
            devShow--;
            working = true;
            result = new MegaRecipeEngine.Result(BirchPhysics.parallel(Math.max(used, 8)), 0,
                    "gtceu:assembly_line/uv_field_generator", "");
            recipe = result.recipe();
            blocked = "";
            debt = Math.max(debt, credit * 0.62);
            borrowed = credit * 0.01;
        }
        modules = MegaModules.lines(hosted);
        view = new double[] { count, BirchPhysics.capacity(phases), BirchPhysics.parallel(used), debt, credit, repaid,
                result.crafts(), borrowed, working ? 1 : 0, frozen, BirchPhysics.eraLevel(paid) };
        return working || isWorkingEnabled();
    }

    /** History corrects itself: the debt is gone, and so is what it bought; the loops close for a while. */
    void paradox() {
        debt = 0;
        paradoxes++;
        frozen = BirchPhysics.PARADOX_SECONDS;
        for (var part : getParts())
            for (var handlers : part.getRecipeHandlers()) {
                if (!handlers.isValid(IO.OUT)) continue;
                for (var h : handlers.getCapability(ItemRecipeCapability.CAP))
                    if (h instanceof NotifiableItemStackHandler items)
                        for (int i = 0; i < items.storage.getSlots(); i++)
                            items.storage.setStackInSlot(i, ItemStack.EMPTY);
                for (var h : handlers.getCapability(FluidRecipeCapability.CAP))
                    if (h instanceof NotifiableFluidTank tank)
                        tank.drainInternal(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            }
    }

    /** Books {@code eu} of debt directly (GameTests and dev aids). */
    public void addDebt(double eu) {
        debt += Math.max(0, eu);
    }

    public void setRepay(int level) {
        repay = Math.max(0, Math.min(BirchPhysics.REPAY_MAX, level));
    }

    @Override
    public ModularUI createUI(Player player) {
        return BirchWorldScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "more" -> setRepay(repay + 1);
            case "less" -> setRepay(repay - 1);
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
        text.add(Component.translatable("gtia.birch_world.state", String.format("%.3g", view[3]),
                String.format("%.3g", view[4]), paradoxes).withStyle(ChatFormatting.BLUE));
    }
}
