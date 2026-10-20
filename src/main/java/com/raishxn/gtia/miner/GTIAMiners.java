package com.raishxn.gtia.miner;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAItems;
import com.raishxn.gtia.data.GTIARecipeTypes;

import java.util.Locale;
import java.util.function.Consumer;

/** Veins with purity and the Entangled Miner (GTIA-D-310): machines, the card copy recipe and crafting recipes. */
public final class GTIAMiners {

    public static final int FIRST_TIER = GTValues.LV, LAST_TIER = GTValues.IV;

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, GTIAMod.MOD_ID);
    public static final RegistryObject<RecipeSerializer<VeinCardCopyRecipe>> CARD_COPY = SERIALIZERS
            .register("vein_card_copy", () -> new SimpleCraftingRecipeSerializer<>(VeinCardCopyRecipe::new));

    public static MachineDefinition[] ENTANGLED_MINER;

    private GTIAMiners() {}

    public static void registerSerializers(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    public static void init() {
        GTIARecipeTypes.ENTANGLED_MINER.addCustomRecipeLogic(EntangledMinerLogic.INSTANCE);
        ENTANGLED_MINER = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : GTValues.tiersBetween(FIRST_TIER, LAST_TIER)) {
            String id = GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_entangled_miner";
            ENTANGLED_MINER[tier] = GTIAMod.REGISTRATE
                    .machine(id, holder -> new EntangledMinerMachine(holder, tier))
                    .tier(tier)
                    .langValue("%s Entangled Miner %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTIAMod.id("entangled_miner"),
                            GTIARecipeTypes.ENTANGLED_MINER))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTIARecipeTypes.ENTANGLED_MINER)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullModel(GTCEu.id("block/machines/miner"))
                    .tooltips(Component.translatable("gtia.machine.entangled_miner.tooltip.0"),
                            Component.translatable("gtia.machine.entangled_miner.tooltip.1", MinerClaims.MAX_PER_VEIN),
                            Component.translatable(tier >= EntangledMinerLogic.CROSS_DIMENSION_TIER ?
                                    "gtia.machine.entangled_miner.tooltip.any_dimension" :
                                    "gtia.machine.entangled_miner.tooltip.same_dimension"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64L,
                            GTIARecipeTypes.ENTANGLED_MINER, 0, true))
                    .register();
        }
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        // EN: The card is LV electronics: a circuit, a sensor-like crystal and paper, cheap enough to make dozens.
        // PT: O cartão é eletrônica LV: circuito, cristal e papel, barato o bastante para fazer dezenas.
        // ZH: 卡片为 LV 电子元件：电路、晶体与纸，足够便宜可批量制作。
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("vein_card"), GTIAItems.VEIN_CARD.asStack(2),
                "PCP", "PQP", "PPP",
                'P', Items.PAPER,
                'C', CustomTags.LV_CIRCUITS,
                'Q', Items.QUARTZ);
        for (int tier : GTValues.tiersBetween(FIRST_TIER, LAST_TIER)) {
            VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id(ENTANGLED_MINER[tier].getName()),
                    ENTANGLED_MINER[tier].asStack(), "SES", "CMC", "WPW",
                    'S', GTCraftingComponents.SENSOR.get(tier),
                    'E', GTCraftingComponents.EMITTER.get(tier),
                    'C', GTCraftingComponents.CIRCUIT.get(tier),
                    'M', GTCraftingComponents.HULL.get(tier),
                    'W', GTCraftingComponents.CABLE.get(tier),
                    'P', GTCraftingComponents.MOTOR.get(tier));
        }
    }
}
