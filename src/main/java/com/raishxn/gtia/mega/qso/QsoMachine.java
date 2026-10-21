package com.raishxn.gtia.mega.qso;

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

import com.raishxn.gtia.mega.EraStore;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.WorldRules;

import java.util.List;

/**
 * Controller of the Quasi-Stellar Obliterator (GTIA-D-332): charges a capacitor from its system's quasar and the
 * Nexus network, fires it, and harvests negentropy from the world's finite stock. With the stock spent and the
 * world rule on, the last shot ends the game.
 */
public class QsoMachine extends MegaMachine {

    public static final String SHAPE = "qso";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QsoMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private double charge;
    @Persisted
    @DescSynced
    private int draw = 4;
    @Persisted
    @DescSynced
    private int size;
    @Persisted
    @DescSynced
    private long shots;
    @Persisted
    @DescSynced
    private boolean ended;
    /** Seconds since the last shot, for the beam effect. */
    @DescSynced
    private int sinceShot = 99;
    /**
     * What the screen shows: [0] charge, [1] shot energy, [2] ceiling, [3] input a second, [4] negentropy of the owner,
     * [5] world stock left, [6] world stock, [7] hole mass, [8] working (1/0), [9] max size.
     */
    @DescSynced
    private double[] view = new double[10];

    public QsoMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Only round a black hole: the quasar is its accretion. */
    @Override
    public boolean inPlace() {
        if (getLevel() != null && getLevel().isClientSide) return view[7] > 0;
        var star = star();
        return star != null && star.kind == Kind.BLACK_HOLE && (anyDimensionForTests ||
                StarSystems.isSystem(getLevel().dimension()));
    }

    public double charge() {
        return charge;
    }

    public int draw() {
        return draw;
    }

    public int size() {
        return size;
    }

    public long shots() {
        return shots;
    }

    public boolean ended() {
        return ended;
    }

    public double[] view() {
        return view;
    }

    @Override
    public float fxLevel(String effect) {
        if (effect.equals("shot")) return sinceShot < 3 ? 1.6f : 0;
        if (effect.equals("charge")) return view[8] > 0 ? (float) (0.3 + Math.min(1.2, view[0] / Math.max(1,
                view[1]))) : 0;
        return 1;
    }

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        double mass = star == null ? 0 : star.mass;
        var era = EraStore.get(level.getServer());
        double stock = WorldRules.get(GTIARules.NEGENTROPY_STOCK);
        double left = Math.max(0, stock - era.get(EraStore.NEGENTROPY_TAKEN, EraStore.WORLD));
        int k = Math.min(size, QsoPhysics.maxSize(phases));
        double shot = QsoPhysics.shot(k);
        double input = 0;
        boolean working = false;
        if (sinceShot < 99) sinceShot++;
        if (!ended && isWorkingEnabled()) {
            double fromNet = QsoPhysics.DRAW_STEP * draw;
            boolean drew = fromNet <= 0 || pay(level, fromNet);
            input = QsoPhysics.input(mass, drew ? draw : 0);
            charge = QsoPhysics.charge(charge, input);
            working = true;
            if (charge >= shot) {
                charge -= shot;
                shots++;
                sinceShot = 0;
                if (left > 0) {
                    double got = Math.min(left, QsoPhysics.yield(k));
                    if (networkOwner != null) era.add(EraStore.NEGENTROPY, networkOwner, got);
                    era.add(EraStore.NEGENTROPY_TAKEN, EraStore.WORLD, got);
                    left -= got;
                } else if (WorldRules.get(GTIARules.FINAL_ENDING) != 0) {
                    end(level, era);
                }
            }
        }
        view = new double[] { charge, shot, QsoPhysics.ceiling(QsoPhysics.input(mass, draw)), input,
                networkOwner == null ? 0 : era.get(EraStore.NEGENTROPY, networkOwner), left, stock, mass,
                working ? 1 : 0, QsoPhysics.maxSize(phases) };
        return working || isWorkingEnabled();
    }

    /** The last shot: the world's order is spent; the game is over. */
    private void end(ServerLevel level, EraStore era) {
        ended = true;
        era.set(EraStore.FINAL_DONE, EraStore.WORLD, 1);
        if (networkOwner != null) era.set(EraStore.FINAL_DONE, networkOwner, 1);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("gtia.qso.final_broadcast").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        setWorkingEnabled(false);
    }

    public void setDraw(int level) {
        draw = Math.max(0, Math.min(QsoPhysics.DRAW_MAX, level));
    }

    public void setSize(int k) {
        size = Math.max(0, Math.min(QsoPhysics.SIZE_MAX, k));
    }

    @Override
    public ModularUI createUI(Player player) {
        return QsoScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "draw_up" -> setDraw(draw + 1);
            case "draw_down" -> setDraw(draw - 1);
            case "size_up" -> setSize(size + 1);
            case "size_down" -> setSize(size - 1);
            default -> {
                if (data.equals("dev_show") && !net.minecraftforge.fml.loading.FMLEnvironment.production) {
                    sinceShot = 0;
                    charge = Math.max(charge, QsoPhysics.shot(size) * 0.7);
                } else super.handleDisplayClick(data, click);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        text.add(Component.translatable("gtia.qso.state", String.format("%.0f%%", 100 * view[0] / Math.max(1,
                view[1])), String.format("%.0f", view[5])).withStyle(ChatFormatting.AQUA));
    }
}
