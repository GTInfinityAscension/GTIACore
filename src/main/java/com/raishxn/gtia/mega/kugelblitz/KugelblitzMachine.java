package com.raishxn.gtia.mega.kugelblitz;

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
import net.minecraft.world.item.Item;

import com.raishxn.gtia.mega.common.MegaCapture;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.system.StarSystems;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashSet;
import java.util.List;

/**
 * Controller of the Kugelblitz Containment Silo (GTIA-D-321): lasers (EU from the Nexus network) and the vault
 * (items and fluids, from phase 4) give a black hole mass; it gives energy back as Hawking radiation, caught as EU
 * by the petals of phase 5. A small hole is a strong generator but close to running away: below the critical mass
 * it flashes out and breaks the chamber.
 */
public class KugelblitzMachine extends MegaMachine {

    public static final String SHAPE = "kugelblitz";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(KugelblitzMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    /** Mass of the hole (kg); before it forms, the light gathered towards it. */
    @Persisted
    @DescSynced
    private double mass;
    @Persisted
    @DescSynced
    private boolean hole;
    @Persisted
    @DescSynced
    private int rateIndex = 1;
    @Persisted
    @DescSynced
    private int targetIndex = 3;
    @Persisted
    @DescSynced
    private int integrity = KugelblitzPhysics.INTEGRITY;
    /** Seconds since the last breach (a flash). */
    @Persisted
    @DescSynced
    private int breachSeconds = 999;
    /** What the screen shows: [0] Hawking EU/t, [1] EU/t caught, [2] charge EU/t, [3] vault kg/s. */
    @DescSynced
    private double[] view = new double[4];

    public KugelblitzMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Makes its own hole: works in any star system. */
    @Override
    public boolean inPlace() {
        return anyDimensionForTests || getLevel() != null && StarSystems.isSystem(getLevel().dimension());
    }

    public double mass() {
        return mass;
    }

    public boolean hole() {
        return hole;
    }

    public int rateIndex() {
        return rateIndex;
    }

    public int targetIndex() {
        return targetIndex;
    }

    public int integrity() {
        return integrity;
    }

    public int breachSeconds() {
        return breachSeconds;
    }

    public double[] view() {
        return view;
    }

    public double critical() {
        return KugelblitzPhysics.critical(phases);
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        if (effect.equals("hole")) return hole ? 1 : 0;
        if (effect.equals("hawking")) {
            if (breachSeconds < 6) return 2f * (1 - breachSeconds / 6f);
            return hole ? (float) Math.max(0.15, Math.min(1.6, Math.log10(1 + view[0]) / 9)) : 0;
        }
        if (effect.startsWith("laser")) return view[2] > 0 ? 1 : 0;
        if (effect.equals("vault_stream")) return view[3] > 0 ? 1 : 0;
        if (effect.startsWith("stab_field")) return hole ? 1 : 0;
        return 1;
    }

    @Override
    public float fxScale(String effect) {
        // The shadow grows with the logarithm of the mass, so a 10⁹ kg hole still fits the chamber.
        if (effect.equals("hole")) return (float) Math.max(0.5, Math.min(3, 0.6 + 0.35 * Math.log10(Math.max(1,
                mass / 1000))));
        return 1;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        if (breachSeconds < 999) breachSeconds++;
        double hawking = hole ? KugelblitzPhysics.hawkingEut(mass) : 0;
        if (hole) mass -= KugelblitzPhysics.evaporated(mass);
        double caught = 0;
        if (hawking > 0 && phases >= 5) {
            caught = hawking * KugelblitzPhysics.CATCH;
            payOut(level, caught * 20);
        }
        double target = KugelblitzPhysics.TARGETS[targetIndex];
        int maxRate = Math.min(rateIndex, KugelblitzPhysics.maxRate(phases));
        double charge = 0;
        if (isWorkingEnabled() && integrity > 0 && mass < target && maxRate > 0) {
            double eut = KugelblitzPhysics.RATES[maxRate];
            if (pay(level, eut * 20)) {
                charge = eut;
                mass += eut * 20 / KugelblitzPhysics.EU_PER_KG;
            }
        }
        double vault = 0;
        if (phases >= 4 && isWorkingEnabled() && integrity > 0) {
            // Everything but the chamber's own blocks, which are kept for repairs.
            var keep = repairBlocks();
            long items = take(stack -> !keep.contains(stack.getItem()), 256);
            long fluid = drain(f -> true, 64_000);
            vault = items * KugelblitzPhysics.KG_PER_ITEM + fluid / 1000.0 * KugelblitzPhysics.KG_PER_BUCKET;
            mass += vault;
        }
        if (!hole && mass >= KugelblitzPhysics.FORM_MASS) hole = true;
        if (hole && mass < critical()) breach();
        if (charge == 0) repair();
        view = new double[] { hawking, caught, charge, vault };
        return hole || charge > 0;
    }

    /** The hole runs away and flashes out: everything it held is gone and the chamber breaks. */
    private void breach() {
        mass = 0;
        hole = false;
        integrity = 0;
        breachSeconds = 0;
    }

    private HashSet<Item> repairBlocks() {
        var blocks = new HashSet<Item>();
        for (var part : shape().parts.values())
            if ("chamber".equals(part.ring())) blocks.add(MegaCapture.block(part).asItem());
        return blocks;
    }

    private void repair() {
        if (integrity >= KugelblitzPhysics.INTEGRITY) return;
        var blocks = repairBlocks();
        if (take(stack -> blocks.contains(stack.getItem()), 1) > 0)
            integrity = Math.min(KugelblitzPhysics.INTEGRITY, integrity + KugelblitzPhysics.REPAIR);
    }

    @VisibleForTesting
    public void setMass(double kg, boolean formed) {
        mass = kg;
        hole = formed;
    }

    public void setRate(int index) {
        rateIndex = Math.max(0, Math.min(KugelblitzPhysics.RATES.length - 1, index));
    }

    public void setTarget(int index) {
        targetIndex = Math.max(0, Math.min(KugelblitzPhysics.TARGETS.length - 1, index));
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return KugelblitzScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "rate" -> setRate(click.button == 1 ? rateIndex - 1 : (rateIndex + 1) % KugelblitzPhysics.RATES.length);
            case "target_up" -> setTarget(targetIndex + 1);
            case "target_down" -> setTarget(targetIndex - 1);
            default -> {
                // Dev aid for screenshots: a whole chamber again.
                if (data.equals("dev_repair") && !net.minecraftforge.fml.loading.FMLEnvironment.production)
                    integrity = KugelblitzPhysics.INTEGRITY;
                else super.handleDisplayClick(data, click);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        text.add(Component.translatable("gtia.kugelblitz.mass", String.format("%.3g", mass))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
