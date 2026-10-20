package com.raishxn.gtia.mega.engine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
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
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtia.mega.StellarEraStore;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.engine.EnginePhysics.Region;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashSet;
import java.util.List;

/**
 * Controller of the Stellar Engine (GTIA-D-315): a Shkadov mirror (phase 2) and a Caplan engine (phases 3 to 5)
 * moving the Nexus System through the galaxy. The Δv gathered goes to the owner's Stellar era; regions reached give
 * bonuses to this and other megastructures. While it runs the collector ring turns and the jets burn.
 */
public class StellarEngineMachine extends MegaMachine {

    public static final String SHAPE = "stellar_engine";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            StellarEngineMachine.class, MegaMachine.MANAGED_FIELD_HOLDER);

    /** Share of the lifted mass sent out as the main jet, percent. */
    @Persisted
    @DescSynced
    private int betaPct = 60;
    @Persisted
    @DescSynced
    private boolean caplanOn = true;
    /** Statite cage integrity in per mille. */
    @Persisted
    @DescSynced
    private int integrity = EnginePhysics.INTEGRITY;
    /**
     * What the screen shows: [0] thrust N, [1] Shkadov N, [2] Caplan N, [3] Δv per second km/s, [4] owner's Δv km/s,
     * [5] farthest region, [6] Caplan powered (1/0), [7] tension 0..1, [8] lifted kg/s, [9] H plasma mB/s, [10] He
     * plasma mB/s.
     */
    @DescSynced
    private double[] view = new double[11];

    public StellarEngineMachine(IMachineBlockEntity holder, Object... args) {
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

    public int betaPct() {
        return betaPct;
    }

    public boolean caplanOn() {
        return caplanOn;
    }

    public int integrity() {
        return integrity;
    }

    public double[] view() {
        return view;
    }

    @Override
    protected void stopped() {
        double[] v = view.clone();
        v[0] = v[1] = v[2] = v[3] = v[6] = v[7] = v[8] = v[9] = v[10] = 0;
        view = v;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var server = level.getServer();
        Region farthest = StellarEraStore.farthest(server, networkOwner);
        boolean mirror = phases >= 2;
        // The engine needs its body (phase 4); the collector feeds it (phase 3); the guides (phase 5) hold the star.
        boolean engine = phases >= 4 && caplanOn && integrity > 0;
        double tension = 0;
        if (engine) {
            tension = Math.max(EnginePhysics.tension(betaPct), phases < 5 ? 0.5 : 0);
            engine = consume(level, (double) EnginePhysics.CAPLAN_EUT * 20);
        }
        double mdot = engine ? EnginePhysics.MDOT_MAX * EnginePhysics.mdotBonus(farthest) : 0;
        double shk = EnginePhysics.shkadovThrust(mirror);
        double cap = engine ? EnginePhysics.caplanThrust(mdot, betaPct) : 0;
        double thrust = shk + cap;
        double dv = EnginePhysics.dvPerSecond(thrust);
        if (engine && tension > 0)
            integrity = Math.max(0, integrity - (int) Math.ceil(EnginePhysics.WEAR * tension));
        else repair();
        double h = 0, he = 0;
        if (engine) {
            h = EnginePhysics.HYDROGEN_PLASMA * EnginePhysics.hydrogenBonus(farthest);
            he = EnginePhysics.HELIUM_PLASMA;
            fill(GTMaterials.Hydrogen.getFluid(FluidStorageKeys.PLASMA, (int) h));
            fill(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA, (int) he));
            if (Region.PULSAR_VICINITY.reached(farthest))
                output(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neutronium, 1), 1);
        }
        double total = 0;
        if (networkOwner != null) {
            var store = StellarEraStore.get(server);
            store.add(networkOwner, dv);
            total = store.dv(networkOwner);
            farthest = store.farthest(networkOwner);
        }
        view = new double[] { thrust, shk, cap, dv, total, farthest.ordinal(), engine ? 1 : 0, tension, mdot, h, he };
        return thrust > 0;
    }

    /** With no tension, the cage takes rib and hoop blocks from the input buses: +5 % each, one a second. */
    private void repair() {
        if (integrity >= EnginePhysics.INTEGRITY) return;
        var blocks = new HashSet<Item>();
        for (var part : shape().parts.values())
            if (part.phase() == 1 && !"station".equals(part.ring())) blocks.add(MegaCapture.block(part).asItem());
        if (take(stack -> blocks.contains(stack.getItem()), 1) > 0)
            integrity = Math.min(EnginePhysics.INTEGRITY, integrity + EnginePhysics.REPAIR);
    }

    /** Puts a fluid into the output hatches; what does not fit is lost (the jet carries it away). */
    private void fill(FluidStack stack) {
        if (stack == null || stack.isEmpty()) return;
        int left = stack.getAmount();
        for (var part : getParts()) {
            for (var handlers : part.getRecipeHandlers()) {
                if (!handlers.isValid(IO.OUT)) continue;
                for (var handler : handlers.getCapability(FluidRecipeCapability.CAP)) {
                    if (!(handler instanceof NotifiableFluidTank tank) || left <= 0) continue;
                    var put = stack.copy();
                    put.setAmount(left);
                    left -= tank.fillInternal(put, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    @VisibleForTesting
    public void setIntegrity(int perMille) {
        integrity = perMille;
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return StellarEngineScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "beta_down" -> setBeta(betaPct - EnginePhysics.BETA_STEP);
            case "beta_up" -> setBeta(betaPct + EnginePhysics.BETA_STEP);
            case "caplan" -> caplanOn = !caplanOn;
            default -> super.handleDisplayClick(data, click);
        }
    }

    public void setBeta(int pct) {
        betaPct = Math.max(EnginePhysics.BETA_MIN, Math.min(EnginePhysics.BETA_MAX, pct));
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        if (!inPlace()) {
            text.add(Component.translatable("gtia.stellar_engine.needs_star").withStyle(ChatFormatting.RED));
            return;
        }
        text.add(Component.translatable("gtia.stellar_engine.thrust", String.format("%.3g", view[0]))
                .withStyle(ChatFormatting.AQUA));
        text.add(Component.translatable("gtia.stellar_engine.dv", String.format("%.1f", view[4]))
                .withStyle(ChatFormatting.GREEN));
    }
}
