package com.raishxn.gtia.mega.nidavellir;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
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
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtia.mega.StellarEraStore;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaRecipeEngine;
import com.raishxn.gtia.mega.engine.EnginePhysics.Region;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;
import com.raishxn.gtna.common.data.GTNARecipeType;

import java.util.List;

/**
 * Controller of the Nidavellir Hyperforge (GTIA-D-323): runs Alloy Blast Smelter and Ultimate Material Forge recipes
 * in huge parallel batches, each pressed under neutronium dust from the input buses; four times as fast around a
 * neutron star.
 */
public class NidavellirMachine extends MegaMachine {

    public static final String SHAPE = "nidavellir";

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NidavellirMachine.class,
            MegaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int compression = 1;
    @Persisted
    private double progressTicks;
    @Persisted
    @DescSynced
    private long crafted;
    @Persisted
    @DescSynced
    private long neutroniumUsed;
    @DescSynced
    private String recipe = "";
    @DescSynced
    private String blocked = "";
    /**
     * What the screen shows: [0] parallel, [1] speed, [2] neutronium per batch, [3] neutronium in the buses,
     * [4] crafts this second, [5] EU this second, [6] magnetar (1/0), [7] pulsar wind (1/0), [8] working (1/0).
     */
    @DescSynced
    private double[] view = new double[9];
    /** Dev aid: seconds of shown work round a neutron star, for screenshots. */
    private int devShow;
    private final MegaRecipeEngine.Progress progress = new MegaRecipeEngine.Progress();

    public NidavellirMachine(IMachineBlockEntity holder, Object... args) {
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

    public int compression() {
        return compression;
    }

    public long crafted() {
        return crafted;
    }

    public long neutroniumUsed() {
        return neutroniumUsed;
    }

    public String recipe() {
        return recipe;
    }

    public String blocked() {
        return blocked;
    }

    public double[] view() {
        return view;
    }

    static List<GTRecipeType> types() {
        return List.of(GCYMRecipeTypes.ALLOY_BLAST_RECIPES, GTNARecipeType.ULTIMATE_MATERIAL_FORGE_RECIPES);
    }

    private static ItemStack neutroniumDust() {
        return ChemicalHelper.get(TagPrefix.dust, GTMaterials.Neutronium, 1);
    }

    // ------------------------------------------------------------------ effects

    @Override
    public float fxLevel(String effect) {
        boolean on = view[8] > 0;
        if (effect.startsWith("strike")) return on ? 0.6f + compression * 0.2f : 0;
        if (effect.equals("press_feed")) return on ? 1 : 0;
        if (effect.startsWith("pulsar") || effect.equals("neutron_star") || effect.equals("magnetosphere"))
            return view[6] > 0 ? 1 : 0.35f;
        return 1;
    }

    // ------------------------------------------------------------------ work

    @Override
    protected boolean megaSecond(ServerLevel level) {
        var star = star();
        boolean magnetar = star != null && star.kind == Kind.NEUTRON_STAR;
        boolean wind = Region.PULSAR_VICINITY.reached(StellarEraStore.farthest(level.getServer(), networkOwner));
        int c = Math.min(compression, NidavellirPhysics.maxCompression(phases));
        int perBatch = NidavellirPhysics.neutronium(c, wind);
        var dust = neutroniumDust();
        long have = count(s -> s.is(dust.getItem()));
        int parallel = NidavellirPhysics.parallel(c);
        double speed = magnetar ? NidavellirPhysics.SPEED_NEUTRON_STAR : NidavellirPhysics.SPEED;
        MegaRecipeEngine.Result result = new MegaRecipeEngine.Result(0, 0, "", "neutronium");
        if (isWorkingEnabled() && have >= perBatch) {
            progress.ticks = progressTicks;
            result = MegaRecipeEngine.second(this, level, types(), new MegaRecipeEngine.Limits(parallel, speed,
                    NidavellirPhysics.EU_FACTOR, r -> true, (int) (have / perBatch)), progress);
            progressTicks = progress.ticks;
            long used = (long) result.batches() * perBatch;
            take(s -> s.is(dust.getItem()), used);
            neutroniumUsed += used;
            crafted += result.crafts();
        }
        recipe = result.recipe();
        blocked = result.blocked();
        boolean working = result.crafts() > 0;
        if (devShow > 0) {
            devShow--;
            magnetar = working = true;
            speed = NidavellirPhysics.SPEED_NEUTRON_STAR;
            result = new MegaRecipeEngine.Result(parallel * 4L, 3.2e12, "gtceu:alloy_blast_smelter/naquadah_alloy", "");
            recipe = result.recipe();
            blocked = "";
            have = Math.max(have, 512);
        }
        view = new double[] { parallel, speed, perBatch, have, result.crafts(), result.eu(), magnetar ? 1 : 0,
                wind ? 1 : 0, working ? 1 : 0 };
        return working || isWorkingEnabled();
    }

    public void setCompression(int c) {
        compression = Math.max(0, Math.min(NidavellirPhysics.COMPRESSION_MAX, c));
    }

    // ------------------------------------------------------------------ screen

    @Override
    public ModularUI createUI(Player player) {
        return NidavellirScreen.create(this, player);
    }

    @Override
    public void handleDisplayClick(String data, ClickData click) {
        if (click.isRemote) return;
        switch (data) {
            case "press" -> setCompression(compression + 1);
            case "ease" -> setCompression(compression - 1);
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
        text.add(Component.translatable("gtia.nidavellir.state", (int) view[0], String.format("%.0f", view[1]))
                .withStyle(ChatFormatting.GOLD));
    }
}
