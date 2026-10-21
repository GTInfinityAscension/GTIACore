package com.raishxn.gtia.loot;

import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.raishxn.gtia.GTIAMod;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * EN: Fortune on GregTech ores (GTIA-D-348). GTCEu 7.5.3 leaves the ore bonus out of its ore loot tables; this puts
 * the vanilla ore formula back on the ore's own drop (raw ore, or gem/dust for ores without raw ore).
 * PT: Fortuna nos minérios do GregTech (GTIA-D-348). O GTCEu 7.5.3 deixa o bônus de minério fora das tabelas; isto
 * devolve a fórmula de minério do vanilla na queda do próprio minério (minério bruto, ou gema/pó quando não há bruto).
 * ZH: GregTech 矿石的时运（GTIA-D-348）。GTCEu 7.5.3 的矿石战利品表去掉了时运加成；这里按原版矿石公式恢复，作用于
 * 矿石本身的掉落（粗矿，没有粗矿时为宝石或粉）。
 */
public final class GTOreFortune extends LootModifier {

    public static final Codec<GTOreFortune> CODEC = RecordCodecBuilder
            .create(inst -> codecStart(inst).apply(inst, GTOreFortune::new));

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GTIAMod.MOD_ID);

    static {
        SERIALIZERS.register("gt_ore_fortune", () -> CODEC);
    }

    public GTOreFortune(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        var state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (state == null || tool == null || !(state.getBlock() instanceof OreBlock ore)) return loot;
        int fortune = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        if (fortune <= 0 || EnchantmentHelper.hasSilkTouch(tool)) return loot;
        ItemStack primary = ChemicalHelper.get(TagPrefix.rawOre, ore.material);
        if (primary.isEmpty()) primary = ChemicalHelper.get(TagPrefix.gem, ore.material);
        if (primary.isEmpty()) primary = ChemicalHelper.get(TagPrefix.dust, ore.material);
        for (ItemStack stack : loot) {
            if (!ItemStack.isSameItem(stack, primary)) continue;
            // Vanilla ApplyBonusCount.addOreBonusCount: multiply by 1 + max(0, rand(fortune + 2) - 1).
            int bonus = Math.max(0, context.getRandom().nextInt(fortune + 2) - 1);
            stack.setCount(stack.getCount() * (bonus + 1));
        }
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
