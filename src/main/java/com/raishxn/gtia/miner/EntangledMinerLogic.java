package com.raishxn.gtia.miner;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.WeightedMaterial;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtia.data.GTIARecipeTypes;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entangled Miner recipe (GTIA-D-310), built for each operation from the Vein Card in the input slot: {@code purity}
 * raw ores drawn by the vein's weights. 8 s at 30 EU/t; GTCEu's overclock does the rest (IV: 0.5 s at 7680 EU/t).
 */
public enum EntangledMinerLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    public static final int BASE_DURATION = 160;
    /** From this tier the miner reaches veins in other dimensions; below it, only its own dimension. */
    public static final int CROSS_DIMENSION_TIER = GTValues.EV;

    /** Why a miner is not running, for the GameTests and the machine's display. */
    public enum Block {
        NONE,
        NO_CARD,
        NO_VEIN,
        OTHER_DIMENSION,
        VEIN_FULL
    }

    @Nullable
    public static ItemStack card(IRecipeCapabilityHolder holder) {
        for (var list : holder.getCapabilitiesForIO(IO.IN)) {
            for (var handler : list.getCapability(ItemRecipeCapability.CAP)) {
                for (var content : handler.getContents()) {
                    if (content instanceof ItemStack stack && VeinCardItem.read(stack) != null) return stack;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        if (!(holder instanceof EntangledMinerMachine miner)) return null;
        var check = miner.check();
        if (check.block() != Block.NONE) return null;
        return build(check.definition(), check.purity(), miner.getLevel().getRandom());
    }

    /** The vein a miner would mine now, or why it cannot. */
    public record Check(Block block, @Nullable BedrockOreDefinition definition, VeinPurity purity) {

        static Check fail(Block block) {
            return new Check(block, null, VeinPurity.IMPURE);
        }
    }

    static Check check(EntangledMinerMachine miner) {
        if (!(miner.getLevel() instanceof ServerLevel level)) return Check.fail(Block.NO_CARD);
        ItemStack stack = card(miner);
        var card = stack == null ? null : VeinCardItem.read(stack);
        if (card == null) return Check.fail(Block.NO_CARD);
        if (!card.dimension().equals(level.dimension().location()) && miner.getTier() < CROSS_DIMENSION_TIER)
            return Check.fail(Block.OTHER_DIMENSION);
        ServerLevel veinLevel = card.level(level);
        var vein = veinLevel == null ? null : BedrockVeins.veinAt(veinLevel, card.originX(), card.originZ());
        if (vein == null) return Check.fail(Block.NO_VEIN);
        if (!MinerClaims.claim(card.key(), GlobalPos.of(level.dimension(), miner.getPos()), level.getGameTime()))
            return Check.fail(Block.VEIN_FULL);
        return new Check(Block.NONE, vein.definition(), vein.purity(veinLevel.getSeed()));
    }

    public static GTRecipe build(BedrockOreDefinition vein, VeinPurity purity, RandomSource random) {
        Map<Material, Integer> drawn = new LinkedHashMap<>();
        int total = vein.materials().stream().mapToInt(WeightedMaterial::weight).sum();
        for (int i = 0; i < purity.ores; i++) {
            int roll = random.nextInt(total);
            for (var m : vein.materials()) {
                roll -= m.weight();
                if (roll < 0) {
                    drawn.merge(m.material(), 1, Integer::sum);
                    break;
                }
            }
        }
        // EN: 8 s at 30 EU/t in LV, normal overclock per tier: light by design, a passive trickle and not a mine.
        // PT: 8 s a 30 EU/t no LV, overclock normal por tier: leve de propósito, um fluxo passivo e não uma mina.
        // ZH: LV 为 8 秒、30 EU/t，每级正常超频：刻意保持轻量，是被动的细流而不是矿场。
        var builder = GTIARecipeTypes.ENTANGLED_MINER.recipeBuilder("entangled_" + Long.toHexString(random.nextLong()))
                .duration(BASE_DURATION).EUt(GTValues.VA[GTValues.LV]);
        for (var e : drawn.entrySet()) builder.outputItems(raw(e.getKey(), e.getValue()));
        return builder.buildRawRecipe();
    }

    /** Raw ore of a material; gem or dust for materials without a raw ore form. */
    public static ItemStack raw(Material material, int count) {
        for (TagPrefix prefix : new TagPrefix[] { TagPrefix.rawOre, TagPrefix.gem, TagPrefix.dust }) {
            ItemStack stack = ChemicalHelper.get(prefix, material, count);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }
}
