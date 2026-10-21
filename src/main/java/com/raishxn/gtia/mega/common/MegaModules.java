package com.raishxn.gtia.mega.common;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.raishxn.gtia.GTIAMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hosted modules (Suprashell, Birch World): GTCEu multiblock controllers put in a megastructure's input buses, whose
 * recipe types it runs. The GTIA megastructures themselves cannot be hosted.
 */
public final class MegaModules {

    private MegaModules() {}

    public static Map<MultiblockMachineDefinition, Integer> hosted(MegaMachine m) {
        Map<MultiblockMachineDefinition, Integer> out = new LinkedHashMap<>();
        for (var part : m.getParts())
            for (var handlers : part.getRecipeHandlers()) {
                if (!handlers.isValid(IO.IN)) continue;
                for (var handler : handlers.getCapability(ItemRecipeCapability.CAP)) {
                    if (!(handler instanceof NotifiableItemStackHandler items)) continue;
                    for (int i = 0; i < items.storage.getSlots(); i++) {
                        var stack = items.storage.getStackInSlot(i);
                        if (!(stack.getItem() instanceof MetaMachineItem item) ||
                                !(item.getDefinition() instanceof MultiblockMachineDefinition def) ||
                                GTIAMod.MOD_ID.equals(def.getId().getNamespace()))
                            continue;
                        out.merge(def, stack.getCount(), Integer::sum);
                    }
                }
            }
        return out;
    }

    public static int count(Map<MultiblockMachineDefinition, Integer> hosted) {
        return hosted.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static List<GTRecipeType> types(Map<MultiblockMachineDefinition, Integer> hosted) {
        List<GTRecipeType> out = new ArrayList<>();
        for (var def : hosted.keySet())
            for (var type : def.getRecipeTypes())
                if (type != null && type != GTRecipeTypes.DUMMY_RECIPES && !out.contains(type)) out.add(type);
        return out;
    }

    /** "description id=count" lines, for a screen. */
    public static String lines(Map<MultiblockMachineDefinition, Integer> hosted) {
        var out = new StringBuilder();
        hosted.forEach((def, n) -> out.append(def.getBlock().getDescriptionId()).append('=').append(n).append('\n'));
        return out.toString();
    }
}
