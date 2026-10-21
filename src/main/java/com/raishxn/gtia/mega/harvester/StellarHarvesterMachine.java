package com.raishxn.gtia.mega.harvester;

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
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.List;

/**
 * Controller of the Stellar Harvester (GTIA-D-328): heats the system star's core and pays its accelerated output to
 * the Nexus network until the core hydrogen runs out; then the star ends by its mass.
 */
public class StellarHarvesterMachine extends MegaMachine {

    public static final String SHAPE = "stellar_harvester";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            StellarHarvesterMachine.class, MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private double heat;
    @Persisted
    @DescSynced
    private double harvested;
    /** 0 none, 1 white dwarf, 2 neutron star, 3 black hole: what the last exhausted star became. */
    @Persisted
    @DescSynced
    private int event;
    /**
     * What the screen shows: [0] star kind ordinal, [1] mass, [2] fuel, [3] heat, [4] boost, [5] EU/t, [6] fusion
     * exponent, [7] seconds left, [8] working (1/0), [9] drawn temperature, [10] drawn scale, [11] max heat.
     */
    @DescSynced
    private double[] view = new double[12];
    /** Dev aid: seconds of shown burning, for screenshots. */
    private int devShow;

    public StellarHarvesterMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Only round a star still burning hydrogen. */
    @Override
    public boolean inPlace() {
        if (getLevel() != null && getLevel().isClientSide) return view[0] >= 0 && view[2] > 0 || view[8] > 0;
        var star = star();
        return star != null && star.kind.mainSequence() && (anyDimensionForTests ||
                StarSystems.isSystem(getLevel().dimension()));
    }

    public double heat() {
        return heat;
    }

    public double harvested() {
        return harvested;
    }

    public int event() {
        return event;
    }

    public double[] view() {
        return view;
    }

    @Override
    public float fxLevel(String effect) {
        boolean on = view[8] > 0;
        if (effect.startsWith("heat")) return on ? (float) (0.5 + Math.min(1.5, heat)) : 0;
        if (effect.startsWith("wind") || effect.equals("ejecta"))
            return on ? (float) Math.min(1.6, 0.3 + Math.log10(Math.max(1, view[4])) / 4) : 0;
        if (effect.startsWith("mirror_field")) return on ? 1 : 0.3f;
        return 1;
    }

    @Override
    public float fxTemperature(String effect) {
        return effect.equals("star") && view[9] > 0 ? (float) view[9] : 0;
    }

    @Override
    public float fxScale(String effect) {
        return effect.equals("star") && view[10] > 0 ? (float) view[10] : 1;
    }

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        if (star == null) return false;
        double maxHeat = HarvesterPhysics.maxHeat(phases);
        double h = Math.min(heat, maxHeat);
        boolean burning = star.kind.mainSequence() && star.fuel > 0;
        double boost = burning ? HarvesterPhysics.boost(h, star.mass) : 0;
        double eut = burning ? HarvesterPhysics.power(star.luminosity(), boost) : 0;
        boolean working = false;
        if (burning && isWorkingEnabled() && eut > 0) {
            double share = Math.min(star.fuel, HarvesterPhysics.burn(eut, star.mass));
            double eu = HarvesterPhysics.budget(star.mass, share);
            payOut(level, eu);
            harvested += eu;
            star.fuel -= share;
            working = true;
            if (star.fuel <= 1e-12) exhaust(level, star);
            StarSystems.get(level.getServer()).changed();
        }
        if (devShow > 0) {
            devShow--;
            working = true;
            boost = Math.max(boost, 1e3);
            eut = Math.max(eut, HarvesterPhysics.power(star.luminosity(), boost));
        }
        view = new double[] { star.kind.ordinal(), star.mass, star.fuel, h, boost, eut,
                HarvesterPhysics.exponent(star.mass), HarvesterPhysics.secondsLeft(eut, star.mass, star.fuel),
                working ? 1 : 0, HarvesterPhysics.drawnTemperature(star.temperature(), boost), star.drawnScale(),
                maxHeat };
        return working || isWorkingEnabled();
    }

    /** The core is spent: the star collapses into the remnant its mass allows. */
    private void exhaust(ServerLevel level, StarSystems.Star star) {
        var end = HarvesterPhysics.remnant(star.mass);
        star.mass = HarvesterPhysics.remnantMass(star.mass);
        star.kind = end;
        star.fuel = 0;
        event = switch (end) {
            case WHITE_DWARF -> 1;
            case NEUTRON_STAR -> 2;
            default -> 3;
        };
        heat = 0;
    }

    public void setHeat(double h) {
        heat = Math.max(0, Math.min(HarvesterPhysics.maxHeat(5), Math.round(h / HarvesterPhysics.HEAT_STEP) *
                HarvesterPhysics.HEAT_STEP));
    }

    @Override
    public ModularUI createUI(Player player) {
        return StellarHarvesterScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "hotter" -> setHeat(heat + HarvesterPhysics.HEAT_STEP);
            case "cooler" -> setHeat(heat - HarvesterPhysics.HEAT_STEP);
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
        text.add(Component.translatable("gtia.stellar_harvester.state", String.format("%.2f", view[3]),
                String.format("%.3g", view[4]), String.format("%.1f%%", view[2] * 100))
                .withStyle(ChatFormatting.GOLD));
    }
}
