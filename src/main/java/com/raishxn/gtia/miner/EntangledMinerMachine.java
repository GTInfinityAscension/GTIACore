package com.raishxn.gtia.miner;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.core.GlobalPos;

/** Entangled Miner (GTIA-D-310): a single block that mines the vein on its Vein Card, from LV to IV. */
public class EntangledMinerMachine extends SimpleTieredMachine implements IMachineLife {

    public EntangledMinerMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction);
    }

    public EntangledMinerLogic.Check check() {
        return EntangledMinerLogic.check(this);
    }

    /** Every operation draws new ores, so the next one is built again instead of repeating the last. */
    @Override
    public void afterWorking() {
        super.afterWorking();
        getRecipeLogic().markLastRecipeDirty();
    }

    /** Keeps looking while it waits, so a miner takes a vein slot as soon as one frees. */
    @Override
    public boolean keepSubscribing() {
        return true;
    }

    @Override
    public void onMachineRemoved() {
        if (getLevel() != null) MinerClaims.release(GlobalPos.of(getLevel().dimension(), getPos()));
    }
}
