package com.raishxn.gtia.mega.common;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Runs GTCEu recipes in batches for the megastructures that process (Stellar Hoop, Nidavellir, Assembly Yard,
 * Suprashell): each second the machine gets {@code 20 × speed} ticks of work; a recipe of {@code d} ticks runs every
 * {@code d} ticks of work, {@code parallel} at a time, paid in EU from the owner's Nexus network
 * ({@code EU/t × d × parallel × euFactor}). Inputs come from the input hatches, outputs go to the output hatches.
 */
public final class MegaRecipeEngine {

    /** At most this many batches per second, so a fast recipe cannot hold the server. */
    public static final int MAX_BATCHES = 64;

    /** Work carried over between seconds for recipes longer than one second of work. */
    public static final class Progress {

        public double ticks;
        public String recipe = "";
        public long crafted;
        public String last = "";
    }

    /**
     * {@code maxBatches}: at most this many batches this second (a fuel that runs out caps it); {@code recipeEu}: an
     * extra EU factor per recipe (the Assembly Yard's discount on megastructure parts).
     */
    public record Limits(int parallel, double speed, double euFactor, Predicate<GTRecipe> allowed, int maxBatches,
                         java.util.function.ToDoubleFunction<GTRecipe> recipeEu) {

        public Limits(int parallel, double speed, double euFactor, Predicate<GTRecipe> allowed, int maxBatches) {
            this(parallel, speed, euFactor, allowed, maxBatches, r -> 1);
        }

        public Limits(int parallel, double speed, double euFactor, Predicate<GTRecipe> allowed) {
            this(parallel, speed, euFactor, allowed, MAX_BATCHES, r -> 1);
        }
    }

    /** What one second did: crafts, EU spent, the recipe that ran (or the reason it did not). */
    public record Result(long crafts, double eu, String recipe, String blocked, int batches) {

        public Result(long crafts, double eu, String recipe, String blocked) {
            this(crafts, eu, recipe, blocked, 0);
        }

        /**
         * A recipe is under way: it finished batches this second, or it is part way through one and nothing stops it.
         * Effects that show the work (welding lasers, forge glow) follow this, not finished batches, or a recipe of
         * several seconds would show them only on the second it ends.
         */
        public boolean busy() {
            return crafts > 0 || !recipe.isEmpty() && blocked.isEmpty();
        }
    }

    private MegaRecipeEngine() {}

    public static Result second(MegaMachine m, ServerLevel level, List<GTRecipeType> types, Limits lim,
                                Progress progress) {
        GTRecipe found = find(m, types, lim.allowed());
        GTRecipe recipe = found == null ? null : withoutEu(found);
        if (recipe == null) {
            progress.ticks = 0;
            return new Result(0, 0, "", "no_recipe");
        }
        String id = recipe.id.toString();
        if (!id.equals(progress.recipe)) {
            progress.recipe = id;
            progress.ticks = 0;
        }
        progress.ticks += 20 * lim.speed();
        int duration = Math.max(1, recipe.duration);
        long crafts = 0;
        double eu = 0;
        String blocked = "";
        int batches = 0, done = 0;
        int cap = Math.min(MAX_BATCHES, lim.maxBatches());
        while (progress.ticks >= duration && batches < cap) {
            batches++;
            int p = ParallelLogic.getParallelAmountWithoutEU(m, recipe, Math.max(1, lim.parallel()));
            if (p <= 0) {
                blocked = "inputs";
                break;
            }
            double cost = RecipeHelper.getRealEUt(found).getTotalEU() * (double) duration * p * lim.euFactor() *
                    lim.recipeEu().applyAsDouble(found);
            if (!m.pay(level, cost)) {
                blocked = "power";
                break;
            }
            var run = p == 1 ? recipe : recipe.copy(ContentModifier.multiplier(p), false);
            var caches = new HashMap<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>,
                    it.unimi.dsi.fastutil.objects.Object2IntMap<?>>();
            if (!RecipeHelper.handleRecipeIO(m, run, IO.IN, caches).isSuccess()) {
                blocked = "inputs";
                break;
            }
            RecipeHelper.handleRecipeIO(m, run, IO.OUT, caches);
            progress.ticks -= duration;
            done++;
            crafts += p;
            eu += cost;
        }
        // Work cannot pile up while the inputs or the power run short.
        if (!blocked.isEmpty()) progress.ticks = Math.min(progress.ticks, duration);
        progress.crafted += crafts;
        progress.last = id;
        return new Result(crafts, eu, id, blocked, done);
    }

    /** The recipe without its EU: the megastructure pays it from the Nexus network instead of an energy hatch. */
    static GTRecipe withoutEu(GTRecipe r) {
        var c = r.copy();
        var eu = com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability.CAP;
        c.inputs.remove(eu);
        c.tickInputs.remove(eu);
        c.outputs.remove(eu);
        c.tickOutputs.remove(eu);
        return c;
    }

    private static GTRecipe find(MegaMachine m, List<GTRecipeType> types, Predicate<GTRecipe> allowed) {
        for (var type : types) {
            var it = type.searchRecipe(m,
                    r -> allowed.test(r) && RecipeHelper.matchContents(m, withoutEu(r)).isSuccess());
            while (it.hasNext()) {
                var r = it.next();
                if (r != null) return r;
            }
        }
        return null;
    }
}
