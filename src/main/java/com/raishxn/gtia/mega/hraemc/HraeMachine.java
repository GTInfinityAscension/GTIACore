package com.raishxn.gtia.mega.hraemc;

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

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.List;

/**
 * Controller of the HRAE-MC (GTIA-D-322): a power plant around the black hole of its system. Matter fed from the
 * hatches feeds the accretion disk (η by the hole's spin, capped by Eddington) and spins the hole up; the Penrose
 * ring takes the spin's energy and slows it; the polar bells catch the jets; the antennas turn it all into research.
 */
public class HraeMachine extends MegaMachine {

    public static final String SHAPE = "hraemc";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HraeMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    /** Share of the Penrose ring's full draw, percent. */
    @Persisted
    @DescSynced
    private int penrosePct = 30;
    @Persisted
    @DescSynced
    private long research;
    /** Dev aid: seconds of simulated feed left. */
    private int devFeed;
    /**
     * What the screen shows: [0] hole mass M☉, [1] spin, [2] η, [3] r_isco, [4] accretion EU/t, [5] Penrose EU/t,
     * [6] jet EU/t, [7] Eddington EU/t, [8] matter units/s, [9] research/s.
     */
    @DescSynced
    private double[] view = new double[10];

    public HraeMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Only around a black hole: a system whose star collapsed or was made as one. */
    @Override
    public boolean inPlace() {
        if (anyDimensionForTests) return true;
        if (getLevel() == null || !StarSystems.isSystem(getLevel().dimension())) return false;
        // The client cannot read the star: it trusts the synced mass, set only around a hole.
        if (getLevel().isClientSide) return view[0] > 0;
        var star = star();
        return star != null && star.kind == Kind.BLACK_HOLE;
    }

    public int penrosePct() {
        return penrosePct;
    }

    public long research() {
        return research;
    }

    public double[] view() {
        return view;
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        if (effect.startsWith("inject")) return view[8] > 0 ? 1 : 0;
        if (effect.startsWith("bz_jet")) return view[6] > 0 ? (float) (0.5 + view[1]) : 0;
        if (effect.equals("ergo")) return view[5] > 0 ? 1 : 0.4f;
        if (effect.equals("disk")) return (float) (0.4 + Math.min(1.2, view[4] / Math.max(1, view[7])));
        return 1;
    }

    @Override
    public float fxScale(String effect) {
        // A heavier hole draws a bigger shadow (cube root, so a 100 M☉ hole still fits the frame).
        if (effect.equals("hole")) return (float) Math.max(0.6, Math.min(1.6, Math.cbrt(Math.max(1, view[0]) / 10)));
        return 1;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        if (star == null || star.kind != Kind.BLACK_HOLE) {
            view = new double[10];
            return false;
        }
        double a = Math.max(0, Math.min(HraePhysics.MAX_SPIN, star.spin)), mass = star.mass;
        double units = 0;
        if (phases >= 2 && isWorkingEnabled()) {
            units = take(stack -> true, 1024) + drain(f -> true, 256_000) / 1000.0;
        }
        if (devFeed > 0) {
            devFeed--;
            units += 2000;
        }
        double accretion = HraePhysics.accretionEut(units, a, mass);
        double penrose = phases >= 3 && isWorkingEnabled() ? HraePhysics.penroseEut(penrosePct, a, mass) : 0;
        double jets = phases >= 4 ? HraePhysics.jetEut(accretion, a) : 0;
        double total = accretion + penrose + jets;
        if (total > 0) payOut(level, total * 20);
        star.spin = Math.max(0, Math.min(HraePhysics.MAX_SPIN, a + HraePhysics.spinChange(units, penrose, a, mass)));
        // Matter swallowed adds to the hole (rest mass, M☉ per unit is negligible but kept exact).
        star.mass += units * HraePhysics.EU_PER_UNIT / HraePhysics.EU_PER_SOLAR_MASS;
        StarSystems.get(level.getServer()).changed();
        long points = phases >= 5 ? HraePhysics.research(total) : 0;
        if (points > 0 && networkOwner != null) {
            research += points;
            try {
                com.raishxn.gtna.research.KnowledgeService.addPoints(level.getServer(), networkOwner,
                        GTIAMod.id("astrophysics"), points);
            } catch (RuntimeException e) {
                GTIAMod.LOGGER.warn("GTNA knowledge points not added", e);
            }
        }
        view = new double[] { mass, star.spin, HraePhysics.efficiency(star.spin), HraePhysics.isco(star.spin),
                accretion, penrose, jets, HraePhysics.eddingtonEut(mass), units, points };
        return true;
    }

    public void setPenrose(int pct) {
        penrosePct = Math.max(0, Math.min(100, pct));
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return HraeScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "more" -> setPenrose(penrosePct + HraePhysics.SHARE_STEP);
            case "less" -> setPenrose(penrosePct - HraePhysics.SHARE_STEP);
            default -> {
                // Dev aid for screenshots: a minute of matter fed without hatches.
                if (data.equals("dev_feed") && !net.minecraftforge.fml.loading.FMLEnvironment.production) devFeed = 60;
                else super.handleDisplayClick(data, click);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        if (!inPlace()) {
            text.add(Component.translatable("gtia.hraemc.needs_hole").withStyle(ChatFormatting.RED));
            return;
        }
        text.add(Component.translatable("gtia.hraemc.spin", String.format("%.3f", view[1]))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
