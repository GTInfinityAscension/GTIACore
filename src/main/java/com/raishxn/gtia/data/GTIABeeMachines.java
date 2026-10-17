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

/** Farm machines: bees from HV (GTIA-D-259) and the Agronomica Seed Analyzer from LV (GTIA-D-271). */
public final class GTIABeeMachines {

    // EN: HV is the first tier (D-259); bees must not exist before the chemistry their combs need.
    // PT: HV é o primeiro tier (D-259); abelhas não existem antes da química que os combs pedem.
    // ZH: HV 为最低等级（D-259）；在蜂巢所需化工出现之前不提供蜜蜂。
    public static final int FIRST_TIER = GTValues.HV;

    public static final MachineDefinition[] INDUSTRIAL_APIARY = registerApiaries();
    public static final MachineDefinition[] SEED_ANALYZER = registerSeedAnalyzers();
    public static final MachineDefinition[] MUTATION_CHAMBER = registerMutationChambers();

    private static MachineDefinition[] registerMutationChambers() {
        MachineDefinition[] machines = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : GTValues.tiersBetween(FIRST_TIER, GTValues.UV)) {
            String id = GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_mutation_chamber";
            machines[tier] = GTIAMod.REGISTRATE
                    .machine(id, holder -> new SimpleTieredMachine(holder, tier,
                            GTMachineUtils.defaultTankSizeFunction))
                    .tier(tier)
                    .langValue("%s Mutation Chamber %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTIAMod.id("mutation_chamber"),
                            GTIARecipeTypes.MUTATION_CHAMBER))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTIARecipeTypes.MUTATION_CHAMBER)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullModel(GTCEu.id("block/machines/chemical_bath"))
                    .tooltips(Component.translatable("gtia.machine.mutation_chamber.tooltip"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64L,
                            GTIARecipeTypes.MUTATION_CHAMBER, GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier),
                            true))
                    .register();
        }
        return machines;
    }

    private static MachineDefinition[] registerSeedAnalyzers() {
        GTIARecipeTypes.SEED_ANALYZER.addCustomRecipeLogic(com.raishxn.gtia.machine.logic.SeedAnalyzerLogic.INSTANCE);
        MachineDefinition[] machines = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : GTValues.tiersBetween(GTValues.LV, GTValues.HV)) {
            String id = GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_seed_analyzer";
            machines[tier] = GTIAMod.REGISTRATE
                    .machine(id, holder -> new SimpleTieredMachine(holder, tier,
                            GTMachineUtils.defaultTankSizeFunction))
                    .tier(tier)
                    .langValue("%s Seed Analyzer %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTIAMod.id("seed_analyzer"),
                            GTIARecipeTypes.SEED_ANALYZER))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTIARecipeTypes.SEED_ANALYZER)
                    .workableTieredHullModel(GTCEu.id("block/machines/scanner"))
                    .tooltips(Component.translatable("gtia.machine.seed_analyzer.tooltip"))
                    .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64L,
                            GTIARecipeTypes.SEED_ANALYZER, GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier),
                            true))
                    .register();
        }
        return machines;
    }

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
