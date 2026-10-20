package com.raishxn.gtia.mega.matrioshka;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtia.mega.MegaMaterials;
import com.raishxn.gtia.mega.matrioshka.MatrioshkaShells.Coolant;

/**
 * One second of the shells' heat: temperatures move towards their target, overheated shells wear and stop, fractured
 * shells (integrity zero) stay off until repaired. Works on the machine's own arrays.
 */
public final class MatrioshkaThermal {

    private MatrioshkaThermal() {}

    /** CWU/t of the shells this second, and which of them are overheated or fractured (bit masks). */
    public record Result(long cwut, int overheated, int fractured) {}

    /**
     * @param temps     shell temperatures in kelvin, updated in place (0 = not settled yet)
     * @param integrity shell integrity in per mille, updated in place
     */
    public static Result second(float[] radius, int phases, int clockPct, Coolant coolant, double supply,
                                double[] temps, int[] integrity) {
        return second(radius, phases, clockPct, coolant, supply, temps, integrity, 1);
    }

    /** @param background factor on the target temperature from the region the Stellar Engine reached */
    public static Result second(float[] radius, int phases, int clockPct, Coolant coolant, double supply,
                                double[] temps, int[] integrity, double background) {
        double limit = MatrioshkaShells.limit(phases);
        int wear = phases >= 5 ? MatrioshkaShells.WEAR / 2 : MatrioshkaShells.WEAR;
        long cwut = 0;
        int hot = 0, broken = 0;
        for (int p = 1; p <= MatrioshkaShells.PHASES; p++) {
            int i = p - 1;
            if (p > phases) {
                temps[i] = 0;
                continue;
            }
            temps[i] = MatrioshkaShells.step(temps[i],
                    MatrioshkaShells.target(radius[i], clockPct, coolant, supply) * background);
            if (temps[i] > limit) {
                hot |= 1 << i;
                integrity[i] = Math.max(0, integrity[i] - wear);
            }
            if (integrity[i] <= 0) broken |= 1 << i;
            if (((hot | broken) & (1 << i)) != 0) continue;
            cwut += MatrioshkaShells.shellCwut(p, temps[i], clockPct);
        }
        return new Result(cwut, hot, broken);
    }

    /** Whether {@code fluid} is the given coolant. */
    public static boolean is(Coolant coolant, Fluid fluid) {
        Fluid wanted = switch (coolant) {
            case LIQUID_HELIUM -> GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID);
            case SUPERFLUID_HELIUM -> MegaMaterials.SUPERFLUID_HELIUM == null ? null :
                    MegaMaterials.SUPERFLUID_HELIUM.getFluid();
            case SHIFTED_SUPERFLUID -> ForgeRegistries.FLUIDS
                    .getValue(new ResourceLocation("gtna", "dimensionally_shifted_superfluid"));
            case NONE -> null;
        };
        return wanted != null && fluid.isSame(wanted);
    }
}
