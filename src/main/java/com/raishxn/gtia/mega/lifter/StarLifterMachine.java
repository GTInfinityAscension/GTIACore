package com.raishxn.gtia.mega.lifter;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
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
import net.minecraft.world.item.Item;

import com.raishxn.gtia.mega.StellarEraStore;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.engine.EnginePhysics;
import com.raishxn.gtia.mega.lifter.LifterPhysics.Element;
import com.raishxn.gtia.mega.lifter.LifterPhysics.Mode;
import com.raishxn.gtia.mega.system.StarPhysics;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashSet;
import java.util.List;

/**
 * Controller of the Star Lifter (GTIA-D-317): lifts matter off the star of the system it stands in. Phase 1 heats
 * (thermal wind), phase 2's ring current funnels the plasma out of the poles, phase 3's chalices catch it, phase 4's
 * spires sort it into elements, phase 5's arcs stir the core. The star loses mass: dimmer and longer-lived.
 */
public class StarLifterMachine extends MegaMachine {

    public static final String SHAPE = "star_lifter";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(StarLifterMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int ratePct = 50;
    @Persisted
    @DescSynced
    private int modeIndex;
    /** Girdle integrity and instability of the star, per mille. */
    @Persisted
    @DescSynced
    private int integrity = LifterPhysics.INTEGRITY;
    @Persisted
    @DescSynced
    private int instability;
    /** Fractions of an output carried to the next second, by element. */
    @Persisted
    private double[] carry = new double[Element.values().length];
    /**
     * What the screen shows: [0] mdot kg/s, [1] caught kg/s, [2] EU/t, [3] star mass M☉, [4] luminosity L☉,
     * [5] temperature K, [6] lifetime yr, [7] fuel 0..1, [8] powered (1/0), [9..18] output per second by element,
     * [19] drawn star scale.
     */
    @DescSynced
    private double[] view = new double[20];

    public StarLifterMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Works around the star of any star system. */
    @Override
    public boolean inPlace() {
        return anyDimensionForTests || getLevel() != null &&
                com.raishxn.gtia.mega.system.StarSystems.isSystem(getLevel().dimension());
    }

    public int ratePct() {
        return ratePct;
    }

    public Mode mode() {
        return Mode.values()[Math.max(0, Math.min(Mode.values().length - 1, modeIndex))];
    }

    public int integrity() {
        return integrity;
    }

    public int instability() {
        return instability;
    }

    public double[] view() {
        return view;
    }

    @Override
    protected void stopped() {
        double[] v = view.clone();
        v[0] = v[1] = v[2] = v[8] = 0;
        for (int i = 9; i < 19; i++) v[i] = 0;
        view = v;
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        boolean on = view[8] > 0;
        if (effect.startsWith("heater")) return on ? 0.6f + ratePct / 100f : 0;
        if (effect.startsWith("polar_jet")) return on && mode() != Mode.THERMAL ? 0.5f + ratePct / 100f : 0;
        if (effect.startsWith("separator")) return on && phases >= 4 ? 1 : 0;
        if (effect.startsWith("pinch")) return on && mode() == Mode.DEEP ? 0.6f + instability / 1000f : 0;
        if (effect.equals("current_field")) return on && mode() != Mode.THERMAL ? 1 : 0;
        return 1;
    }

    @Override
    public float fxTemperature(String effect) {
        return effect.equals("star") && view[5] > 0 ? (float) view[5] : 0;
    }

    @Override
    public float fxScale(String effect) {
        return effect.equals("star") && view[19] > 0 ? (float) view[19] : 1;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        if (star == null || !star.kind.mainSequence()) {
            view = new double[20];
            return false;
        }
        Mode m = phases >= mode().phase ? mode() : Mode.THERMAL;
        boolean lifting = isWorkingEnabled() && star.mass > LifterPhysics.MIN_MASS && integrity > 0;
        double mdot = lifting ? LifterPhysics.mdot(m, ratePct) *
                EnginePhysics.mdotBonus(StellarEraStore.farthest(level.getServer(), networkOwner)) : 0;
        double eut = LifterPhysics.eut(mdot, star.mass);
        if (mdot > 0 && !pay(level, eut * 20)) mdot = 0;
        double caught = mdot * (phases >= 3 ? LifterPhysics.CAPTURE_CHALICES : LifterPhysics.CAPTURE_OPEN);
        double[] out = new double[Element.values().length];
        if (mdot > 0) {
            double lost = LifterPhysics.massLost(mdot);
            star.mass = Math.max(LifterPhysics.MIN_MASS, star.mass - lost);
            star.fuel = Math.min(1, star.fuel + LifterPhysics.fuelAdded(m, lost, star.mass));
            for (var e : Element.values()) {
                // Without the spires only the plasma comes out; the rest is lost with the wind.
                if (!e.plasma() && phases < 4) continue;
                double kg = caught * e.fraction(m);
                double amount = kg * (e.plasma() || gas(e) ? LifterPhysics.MB_PER_KG : LifterPhysics.DUST_PER_KG) +
                        carry[e.ordinal()];
                long whole = (long) Math.floor(amount);
                carry[e.ordinal()] = amount - whole;
                out[e.ordinal()] = whole;
                if (whole > 0) emit(e, whole);
            }
            int gain = LifterPhysics.instability(m, ratePct);
            instability = Math.min(LifterPhysics.INTEGRITY, instability + gain);
            if (gain == 0) instability = Math.max(0, instability - 10);
            // A flare when the star cannot take more: the girdle takes the blow.
            if (instability >= LifterPhysics.INTEGRITY) {
                integrity = Math.max(0, integrity - 250);
                instability = LifterPhysics.INTEGRITY / 2;
            }
            var server = level.getServer();
            if (server != null) com.raishxn.gtia.mega.system.StarSystems.get(server).changed();
        } else {
            instability = Math.max(0, instability - 10);
            repair();
        }
        double[] v = new double[20];
        v[0] = mdot;
        v[1] = caught;
        v[2] = mdot > 0 ? eut : 0;
        v[3] = star.mass;
        v[4] = star.luminosity();
        v[5] = star.temperature();
        v[6] = StarPhysics.lifetimeYears(star.mass) * star.fuel;
        v[7] = star.fuel;
        v[8] = mdot > 0 ? 1 : 0;
        System.arraycopy(out, 0, v, 9, out.length);
        v[19] = star.drawnScale();
        view = v;
        return true;
    }

    private static boolean gas(Element e) {
        return e == Element.OXYGEN || e == Element.NITROGEN || e == Element.NEON;
    }

    private void emit(Element e, long amount) {
        Material mat = switch (e) {
            case HYDROGEN -> GTMaterials.Hydrogen;
            case HELIUM -> GTMaterials.Helium;
            case OXYGEN -> GTMaterials.Oxygen;
            case CARBON -> GTMaterials.Carbon;
            case IRON -> GTMaterials.Iron;
            case NEON -> GTMaterials.Neon;
            case NITROGEN -> GTMaterials.Nitrogen;
            case SILICON -> GTMaterials.Silicon;
            case MAGNESIUM -> GTMaterials.Magnesium;
            case NICKEL -> GTMaterials.Nickel;
        };
        if (e.plasma()) fillFluid(mat.getFluid(FluidStorageKeys.PLASMA, (int) Math.min(Integer.MAX_VALUE, amount)));
        else if (gas(e)) fillFluid(mat.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
        else output(ChemicalHelper.get(TagPrefix.dust, mat, 1), amount);
    }

    /** Idle, the girdle takes girdle and petal blocks from the input buses: +5 % each, one a second. */
    private void repair() {
        if (integrity >= LifterPhysics.INTEGRITY) return;
        var blocks = new HashSet<Item>();
        for (var part : shape().parts.values())
            if (part.phase() == 1 && !"station".equals(part.ring())) blocks.add(MegaCapture.block(part).asItem());
        if (take(stack -> blocks.contains(stack.getItem()), 1) > 0)
            integrity = Math.min(LifterPhysics.INTEGRITY, integrity + LifterPhysics.REPAIR);
    }

    @VisibleForTesting
    public void setIntegrity(int perMille) {
        integrity = perMille;
    }

    @VisibleForTesting
    public void setInstability(int perMille) {
        instability = perMille;
    }

    public void setRate(int pct) {
        ratePct = Math.max(LifterPhysics.RATE_MIN, Math.min(LifterPhysics.RATE_MAX, pct));
    }

    public void setMode(Mode m) {
        modeIndex = m.ordinal();
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return StarLifterScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "rate_down" -> setRate(ratePct - LifterPhysics.RATE_STEP);
            case "rate_up" -> setRate(ratePct + LifterPhysics.RATE_STEP);
            case "mode" -> {
                // Only the modes whose phase is built.
                Mode next = mode();
                do next = Mode.values()[(next.ordinal() + 1) % Mode.values().length];
                while (next.phase > Math.max(1, phases) && next != Mode.THERMAL);
                setMode(next);
            }
            default -> super.handleDisplayClick(data, click);
        }
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        if (!inPlace()) {
            text.add(Component.translatable("gtia.star_lifter.needs_star").withStyle(ChatFormatting.RED));
            return;
        }
        text.add(Component.translatable("gtia.star_lifter.mdot", String.format("%.3g", view[0]))
                .withStyle(ChatFormatting.GOLD));
        text.add(Component.translatable("gtia.star_lifter.mass", String.format("%.4f", view[3]))
                .withStyle(ChatFormatting.YELLOW));
    }
}
