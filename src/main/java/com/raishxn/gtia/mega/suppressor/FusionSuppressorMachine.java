package com.raishxn.gtia.mega.suppressor;

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
import net.minecraft.world.item.Item;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.system.StarPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashSet;
import java.util.List;

/**
 * Controller of the Fusion Suppressor (GTIA-D-320): damps the fusion of the star of its system and reads its
 * neutrinos for research (GTNA's knowledge points, area gtia:astrophysics). A star of 8 M☉ or more, held suppressed
 * until its core gives way, ends as the player chooses: a supernova (r-process dust, a neutron star left) or a
 * collapse into a black hole. Lighter stars only flare.
 */
public class FusionSuppressorMachine extends MegaMachine {

    public static final String SHAPE = "fusion_suppressor";
    /** r-process elements a supernova spreads. */
    static final Material[] R_PROCESS = { GTMaterials.Gold, GTMaterials.Platinum, GTMaterials.Osmium,
            GTMaterials.Iridium, GTMaterials.Uranium238, GTMaterials.Thorium, GTMaterials.Europium };

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FusionSuppressorMachine.class, MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int suppressionPct = 30;
    @Persisted
    @DescSynced
    private int deficit;
    @Persisted
    @DescSynced
    private int integrity = SuppressorPhysics.INTEGRITY;
    /** Last event: 0 none, 1 supernova, 2 collapse, 3 helium flash; seconds since it began. */
    @Persisted
    @DescSynced
    private int event;
    @Persisted
    @DescSynced
    private int eventSeconds = SuppressorPhysics.EVENT_SECONDS;
    @Persisted
    @DescSynced
    private long research;
    /** r-process dust still to come out after a supernova, per element. */
    @Persisted
    private long[] owed = new long[R_PROCESS.length];
    /**
     * What the screen shows: [0] kind ordinal, [1] mass, [2] light, [3] temperature (damped), [4] research/s,
     * [5] EU/t, [6] running, [7] star drawn scale, [8] metallicity.
     */
    @DescSynced
    private double[] view = new double[9];

    public FusionSuppressorMachine(IMachineBlockEntity holder, Object... args) {
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

    public int suppressionPct() {
        return suppressionPct;
    }

    public int deficit() {
        return deficit;
    }

    public int integrity() {
        return integrity;
    }

    public int event() {
        return event;
    }

    public int eventSeconds() {
        return eventSeconds;
    }

    public long research() {
        return research;
    }

    public double[] view() {
        return view;
    }

    public Kind starKind() {
        int k = (int) view[0];
        return k >= 0 && k < Kind.values().length ? Kind.values()[k] : Kind.NONE;
    }

    /** The core has given way in a star heavy enough: the screen offers the two endings. */
    public boolean ready() {
        return deficit >= SuppressorPhysics.DEFICIT_FULL && view[1] >= SuppressorPhysics.COLLAPSE_MASS &&
                starKind().mainSequence() && phases >= 4;
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        Kind k = starKind();
        boolean on = view[6] > 0;
        float since = eventSeconds;
        return switch (effect) {
            case "star" -> k.mainSequence() || k == Kind.WHITE_DWARF ? 1 : 0;
            case "remnant" -> k == Kind.NEUTRON_STAR ? 1 : 0;
            case "hole", "hole_disk" -> k == Kind.BLACK_HOLE ? 1 : 0;
            case "damp_field" -> on && suppressionPct > 0 ? 0.4f + suppressionPct / 100f : 0;
            case "shock" -> event == 1 && since < SuppressorPhysics.EVENT_SECONDS ? 1 - since / 60f : 0;
            case "flash" -> (event == 1 || event == 3) && since < 8 ? 1.5f * (1 - since / 8f) : 0;
            default -> {
                if (effect.startsWith("damp_")) yield on && suppressionPct > 0 ? 0.3f + suppressionPct / 80f : 0;
                if (effect.startsWith("neutrino")) yield on ? 1.2f - suppressionPct / 100f : 0;
                if (effect.startsWith("probe")) yield on && deficit > 0 ? 0.4f + deficit / 1000f : 0;
                yield 1;
            }
        };
    }

    @Override
    public float fxTemperature(String effect) {
        return effect.equals("star") && view[3] > 0 ? (float) view[3] : 0;
    }

    @Override
    public float fxScale(String effect) {
        if (effect.equals("shock")) return 1 + eventSeconds / 10f;
        if (effect.equals("star") && view[7] > 0) return (float) view[7];
        return 1;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        if (eventSeconds < SuppressorPhysics.EVENT_SECONDS) eventSeconds++;
        var star = star();
        if (star == null) {
            view = new double[9];
            return false;
        }
        emitOwed();
        double light = star.luminosity();
        boolean on = isWorkingEnabled() && integrity > 0 && star.kind.mainSequence() && suppressionPct > 0;
        double eut = SuppressorPhysics.eut(suppressionPct, light);
        if (on && !pay(level, eut * 20)) on = false;
        long points = 0;
        if (on) {
            points = SuppressorPhysics.research(suppressionPct, light, phases);
            addResearch(level, points);
            deficit = Math.max(0, Math.min(SuppressorPhysics.DEFICIT_FULL,
                    deficit + SuppressorPhysics.deficit(suppressionPct)));
            if (deficit >= SuppressorPhysics.DEFICIT_FULL && star.mass < SuppressorPhysics.COLLAPSE_MASS) {
                // A star too light to collapse flares instead; the pylons take it.
                integrity = Math.max(0, integrity - SuppressorPhysics.FLASH_WEAR);
                deficit = SuppressorPhysics.DEFICIT_FULL / 2;
                event = 3;
                eventSeconds = 0;
            }
        } else {
            deficit = Math.max(0, deficit - 5);
            repair();
        }
        view = new double[] { star.kind.ordinal(), star.mass, light,
                SuppressorPhysics.dampedTemperature(star.temperature(), on ? suppressionPct : 0), points,
                on ? eut : 0, on ? 1 : 0, star.drawnScale(), star.metallicity };
        return on || eventSeconds < SuppressorPhysics.EVENT_SECONDS;
    }

    private void addResearch(ServerLevel level, long points) {
        if (points <= 0 || networkOwner == null) return;
        research += points;
        try {
            com.raishxn.gtna.research.KnowledgeService.addPoints(level.getServer(), networkOwner,
                    GTIAMod.id("astrophysics"), points);
        } catch (RuntimeException e) {
            GTIAMod.LOGGER.warn("GTNA knowledge points not added", e);
        }
    }

    /** Ends the star as chosen; true if it happened. */
    public boolean end(ServerLevel level, boolean supernova) {
        var star = star();
        if (star == null || !ready()) return false;
        if (supernova) {
            for (int i = 0; i < R_PROCESS.length; i++)
                owed[i] += SuppressorPhysics.supernovaDust(star.mass, star.metallicity, phases >= 5);
            star.kind = Kind.NEUTRON_STAR;
            star.mass = 1.4;
            event = 1;
            addResearch(level, 2000);
        } else {
            star.kind = Kind.BLACK_HOLE;
            star.mass = SuppressorPhysics.holeMass(star.mass);
            event = 2;
            addResearch(level, 5000);
        }
        star.fuel = 0;
        eventSeconds = 0;
        deficit = 0;
        StarSystems.get(level.getServer()).changed();
        view = new double[] { star.kind.ordinal(), star.mass, star.luminosity(), star.temperature(), 0, 0, 0,
                star.drawnScale(), star.metallicity };
        return true;
    }

    /** The supernova's dust comes out over time, a stack per element per second. */
    private void emitOwed() {
        for (int i = 0; i < owed.length; i++) {
            if (owed[i] <= 0) continue;
            long n = Math.min(64, owed[i]);
            long left = output(ChemicalHelper.get(TagPrefix.dust, R_PROCESS[i], 1), n);
            owed[i] -= n - left;
        }
    }

    public long owed() {
        long total = 0;
        for (long o : owed) total += o;
        return total;
    }

    private void repair() {
        if (integrity >= SuppressorPhysics.INTEGRITY) return;
        var blocks = new HashSet<Item>();
        for (var part : shape().parts.values())
            if ("pylons".equals(part.ring())) blocks.add(MegaCapture.block(part).asItem());
        if (take(stack -> blocks.contains(stack.getItem()), 1) > 0)
            integrity = Math.min(SuppressorPhysics.INTEGRITY, integrity + SuppressorPhysics.REPAIR);
    }

    @VisibleForTesting
    public void setDeficit(int perMille) {
        deficit = perMille;
    }

    public void setSuppression(int pct) {
        suppressionPct = Math.max(0, Math.min(SuppressorPhysics.MAX, pct));
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return FusionSuppressorScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "more" -> setSuppression(suppressionPct + SuppressorPhysics.STEP);
            case "less" -> setSuppression(suppressionPct - SuppressorPhysics.STEP);
            case "supernova", "collapse" -> {
                if (getLevel() instanceof ServerLevel level) end(level, data.equals("supernova"));
            }
            default -> {
                if (data.startsWith("dev_") && !net.minecraftforge.fml.loading.FMLEnvironment.production) dev(data);
                else super.handleDisplayClick(data, click);
            }
        }
    }

    /** Dev aid for screenshots: dev_heavy makes the star a ready blue giant, dev_reset a yellow dwarf again. */
    private void dev(String data) {
        var star = star();
        if (star == null) return;
        if (data.equals("dev_heavy")) {
            star.kind = Kind.BLUE_GIANT;
            star.mass = 15;
            deficit = SuppressorPhysics.DEFICIT_FULL;
        } else if (data.equals("dev_reset")) {
            star.kind = Kind.YELLOW_DWARF;
            star.mass = 0.99;
            star.fuel = 0.5;
            deficit = 0;
        }
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        text.add(Component.translatable("gtia.fusion_suppressor.state", suppressionPct, deficit / 10)
                .withStyle(ChatFormatting.AQUA));
    }

    static String kindKey(Kind k) {
        return "gtia.system_creator.ui.kind." + k.name().toLowerCase(java.util.Locale.ROOT);
    }

    static double kelvin(Kind k, double mass) {
        return StarPhysics.temperature(k, mass);
    }
}
