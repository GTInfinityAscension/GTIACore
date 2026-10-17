package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.network.chat.Component;

import com.raishxn.gtia.GTIAMod;

import java.util.Locale;

/** Bee machines (GTIA-D-259): bees start at HV, kept as items inside the machine. */
public final class GTIABeeMachines {

    // EN: HV is the first tier (D-259); bees must not exist before the chemistry their combs need.
    // PT: HV é o primeiro tier (D-259); abelhas não existem antes da química que os combs pedem.
    // ZH: HV 为最低等级（D-259）；在蜂巢所需化工出现之前不提供蜜蜂。
    public static final int FIRST_TIER = GTValues.HV;

    public static final MachineDefinition[] INDUSTRIAL_APIARY = registerApiaries();

    private GTIABeeMachines() {}

    private static MachineDefinition[] registerApiaries() {
        MachineDefinition[] machines = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : GTValues.tiersBetween(FIRST_TIER, GTValues.UV)) {
            String id = GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_industrial_apiary";
            machines[tier] = GTIAMod.REGISTRATE
                    .machine(id, holder -> new SimpleTieredMachine(holder, tier,
                            GTMachineUtils.defaultTankSizeFunction))
                    .tier(tier)
                    .langValue("%s Industrial Apiary %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTIAMod.id("industrial_apiary"),
                            GTIARecipeTypes.INDUSTRIAL_APIARY))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTIARecipeTypes.INDUSTRIAL_APIARY)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullModel(GTCEu.id("block/machines/brewery"))
                    .tooltips(Component.translatable("gtia.machine.industrial_apiary.tooltip.purpose"),
                            Component.translatable("gtia.machine.industrial_apiary.tooltip.bees"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64L,
                            GTIARecipeTypes.INDUSTRIAL_APIARY, GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier),
                            true))
                    .register();
        }
        return machines;
    }

    public static void init() {}
}
