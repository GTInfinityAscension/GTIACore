package com.raishxn.gtia.mega.creator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.PsiStore;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.creator.CreatorPhysics.Stage;
import com.raishxn.gtia.mega.system.StarPhysics;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

/**
 * Controller of the Interstellar System Creator (GTIA-D-319): gathers a hydrogen cloud, collapses it, lights it
 * with the Fusion Candle and settles it into a new star system (a dimension slot) with the chosen star and metal
 * content. The systems an owner has made set the Dimensional era. The screen also travels to the systems.
 */
public class SystemCreatorMachine extends MegaMachine {

    public static final String SHAPE = "system_creator";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SystemCreatorMachine.class, MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int kindIndex;
    @Persisted
    @DescSynced
    private int zIndex = 1;
    @Persisted
    @DescSynced
    private int stage;
    /** Seconds spent in the stage, and hydrogen gathered (mB). */
    @Persisted
    @DescSynced
    private int stageSeconds;
    @Persisted
    @DescSynced
    private long gathered;
    /** Slot the current star goes to, and the slot shown on the screen (-1 = Nexus System). */
    @Persisted
    @DescSynced
    private int targetSlot = -1;
    @Persisted
    @DescSynced
    private int selected = -1;
    @DescSynced
    private String message = "";
    /**
     * Systems as the screen shows them, 7 entries (Nexus, then slots): [3 i] kind ordinal (-1 none), [3 i + 1] mass,
     * [3 i + 2] metallicity; then [21] owner's systems, [22] owner's Ψ.
     */
    @DescSynced
    private double[] systems = new double[23];

    public SystemCreatorMachine(IMachineBlockEntity holder, Object... args) {
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

    /** Works in any star system. */
    @Override
    public boolean inPlace() {
        return anyDimensionForTests || getLevel() != null && StarSystems.isSystem(getLevel().dimension());
    }

    public Kind kind() {
        return CreatorPhysics.KINDS[Math.max(0, Math.min(CreatorPhysics.KINDS.length - 1, kindIndex))];
    }

    public double metallicity() {
        return CreatorPhysics.METALLICITY[Math.max(0, Math.min(3, zIndex))];
    }

    public int zIndex() {
        return zIndex;
    }

    public Stage stage() {
        return Stage.values()[Math.max(0, Math.min(Stage.values().length - 1, stage))];
    }

    public int stageSeconds() {
        return stageSeconds;
    }

    public long gathered() {
        return gathered;
    }

    public int targetSlot() {
        return targetSlot;
    }

    public int selected() {
        return selected;
    }

    public String message() {
        return message;
    }

    public double[] systems() {
        return systems;
    }

    /** Progress of the whole forge, 0..1. */
    public double progress() {
        Kind k = kind();
        return switch (stage()) {
            case IDLE -> 0;
            case GATHER -> 0.4 * gathered / (double) CreatorPhysics.hydrogen(k);
            case COLLAPSE -> 0.4 + 0.3 * stageSeconds / (double) CreatorPhysics.collapseSeconds(k);
            case IGNITE -> 0.7 + 0.15 * stageSeconds / (double) CreatorPhysics.IGNITE_SECONDS;
            case STABILIZE -> 0.85 + 0.15 * stageSeconds / (double) CreatorPhysics.STABILIZE_SECONDS;
        };
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        Stage s = stage();
        boolean remnant = kind() == Kind.BLACK_HOLE;
        return switch (effect) {
            case "nebula_a", "nebula_b" -> s == Stage.GATHER || s == Stage.COLLAPSE ?
                    (float) (0.3 + gathered / (double) CreatorPhysics.hydrogen(kind())) : 0;
            case "protostar" -> s == Stage.COLLAPSE || s == Stage.IGNITE || s == Stage.STABILIZE && !remnant ? 1 : 0;
            case "disk", "hh_jet_n", "hh_jet_s" -> s == Stage.COLLAPSE ? 1 : 0;
            case "candle_beam" -> s == Stage.IGNITE ? 1.2f : 0;
            case "forge_field" -> s != Stage.IDLE && phases >= 4 ? 1 : 0;
            case "hole", "hole_disk" -> s == Stage.STABILIZE && remnant ? 1 : 0;
            default -> 1;
        };
    }

    @Override
    public float fxTemperature(String effect) {
        if (!effect.equals("protostar")) return 0;
        Kind k = kind();
        double target = StarPhysics.temperature(k, k.mass);
        // A protostar glows red while it contracts and heats to the new star's surface as it is lit.
        return (float) (stage() == Stage.COLLAPSE ? 1800 + 1700 * progress() : target);
    }

    @Override
    public float fxScale(String effect) {
        if (!effect.equals("protostar")) return 1;
        Kind k = kind();
        double size = k == Kind.NEUTRON_STAR ? 0.35 : StarPhysics.drawnScale(k, k.mass);
        return stage() == Stage.COLLAPSE ? (float) (1.5 - 0.9 * (progress() - 0.4) / 0.3) : (float) size;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var server = level.getServer();
        var store = StarSystems.get(server);
        refreshSystems(store, server);
        if (!isWorkingEnabled()) return false;
        Kind k = kind();
        switch (stage()) {
            case IDLE -> {
                return false;
            }
            case GATHER -> {
                long want = Math.min(CreatorPhysics.GATHER_MB_PER_SECOND, CreatorPhysics.hydrogen(k) - gathered);
                long plasma = drain(f -> f == GTMaterials.Hydrogen.getFluid(FluidStorageKeys.PLASMA), want / 10);
                long gas = drain(f -> f == GTMaterials.Hydrogen.getFluid(), want - plasma * 10);
                gathered += plasma * 10 + gas;
                message = plasma + gas > 0 ? "" : "hydrogen";
                if (gathered >= CreatorPhysics.hydrogen(k)) next(Stage.COLLAPSE);
            }
            case COLLAPSE -> {
                if (++stageSeconds >= CreatorPhysics.collapseSeconds(k)) next(Stage.IGNITE);
            }
            case IGNITE -> {
                if (phases < 3) {
                    // Without the Candle a cloud only sparks into a red dwarf, slowly.
                    stageSeconds++;
                } else if (pay(level, CreatorPhysics.igniteEu(k) / CreatorPhysics.IGNITE_SECONDS)) {
                    stageSeconds++;
                    message = "";
                } else message = "power";
                if (stageSeconds >= CreatorPhysics.IGNITE_SECONDS) next(Stage.STABILIZE);
            }
            case STABILIZE -> {
                if (targetSlot < 0) next(Stage.IDLE);
                else if (++stageSeconds >= CreatorPhysics.STABILIZE_SECONDS) {
                    if (store.create(targetSlot, k, networkOwner, level.random.nextLong(), metallicity()))
                        message = "made";
                    else message = "slot_taken";
                    selected = targetSlot;
                    next(Stage.IDLE);
                    refreshSystems(store, server);
                }
            }
        }
        return stage() != Stage.IDLE;
    }

    private void next(Stage s) {
        stage = s.ordinal();
        stageSeconds = 0;
        if (s == Stage.IDLE) gathered = 0;
    }

    private void refreshSystems(StarSystems store, net.minecraft.server.MinecraftServer server) {
        double[] v = new double[23];
        for (int i = -1; i < StarSystems.SLOTS; i++) {
            var star = store.star(i < 0 ? StarSystems.NEXUS : StarSystems.slot(i));
            int e = (i + 1) * 3;
            v[e] = star == null || star.kind == Kind.NONE ? -1 : star.kind.ordinal();
            v[e + 1] = star == null ? 0 : star.mass;
            v[e + 2] = star == null ? 0 : star.metallicity;
        }
        if (networkOwner != null) {
            v[21] = store.madeBy(networkOwner);
            v[22] = PsiStore.get(server).balance(networkOwner);
        }
        systems = v;
    }

    /**
     * Starts a star: the kind's phase built, a free slot, the iron dust for the metals and the Ψ to design it, both
     * taken now; the hydrogen comes in while it gathers.
     */
    public void start(ServerLevel level) {
        if (stage() != Stage.IDLE) return;
        Kind k = kind();
        if (CreatorPhysics.phase(k) > Math.max(1, phases)) {
            message = "phase";
            return;
        }
        var store = StarSystems.get(level.getServer());
        int slot = store.freeSlot();
        if (slot < 0) {
            message = "no_slot";
            return;
        }
        int dust = CreatorPhysics.dust(k, metallicity());
        var iron = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Iron, 1);
        if (count(s -> s.is(iron.getItem())) < dust) {
            message = "dust";
            return;
        }
        if (networkOwner == null || !PsiStore.get(level.getServer()).spend(networkOwner,
                CreatorPhysics.psi(k, metallicity()))) {
            message = "psi";
            return;
        }
        take(s -> s.is(iron.getItem()), dust);
        targetSlot = slot;
        message = "";
        next(Stage.GATHER);
    }

    /** Takes a player to a system: the Nexus (-1) or a made slot, onto a small landing platform. */
    public void travel(ServerPlayer player, int slot) {
        var server = player.getServer();
        if (server == null) return;
        var store = StarSystems.get(server);
        var key = slot < 0 ? StarSystems.NEXUS : StarSystems.slot(slot);
        var star = store.star(key);
        ServerLevel target = server.getLevel(key);
        if (target == null || star == null || star.kind == Kind.NONE) {
            message = "no_system";
            return;
        }
        BlockPos base = new BlockPos(0, 100, 0);
        for (int x = -4; x <= 4; x++)
            for (int z = -4; z <= 4; z++)
                if (target.getBlockState(base.offset(x, 0, z)).isAir())
                    target.setBlockAndUpdate(base.offset(x, 0, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        player.teleportTo(target, 0.5, 101, 0.5, player.getYRot(), player.getXRot());
    }

    @VisibleForTesting
    public void setKind(Kind k) {
        for (int i = 0; i < CreatorPhysics.KINDS.length; i++) if (CreatorPhysics.KINDS[i] == k) kindIndex = i;
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return SystemCreatorScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "kind" -> {
                if (stage() == Stage.IDLE) kindIndex = (kindIndex + 1) % CreatorPhysics.KINDS.length;
            }
            case "metal" -> {
                if (stage() == Stage.IDLE) zIndex = (zIndex + 1) % CreatorPhysics.METALLICITY.length;
            }
            case "forge" -> {
                if (getLevel() instanceof ServerLevel level) start(level);
            }
            case "cancel" -> {
                // The cloud disperses: what was gathered and the Ψ spent are lost.
                if (stage() != Stage.IDLE) next(Stage.IDLE);
            }
            default -> {
                if (data.startsWith("select_")) selected = Integer.parseInt(data.substring(7));
                else if (data.startsWith("dev_stage_") && !net.minecraftforge.fml.loading.FMLEnvironment.production) {
                    // Dev aid for screenshots: dev_stage_<stage>_<kind index>.
                    String[] p = data.substring(10).split("_");
                    stage = Integer.parseInt(p[0]);
                    kindIndex = Integer.parseInt(p[1]);
                    stageSeconds = 3;
                    gathered = CreatorPhysics.hydrogen(kind()) / 2;
                    targetSlot = 0;
                }
                else super.handleDisplayClick(data, click);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        if (!isFormed()) return;
        text.add(Component.translatable("gtia.system_creator.stage",
                Component.translatable("gtia.system_creator.ui.stage." + stage().name().toLowerCase(java.util.Locale.ROOT)),
                String.format("%.0f", progress() * 100)).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
