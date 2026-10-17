package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;

import java.util.function.Consumer;

/** Recipes for GTNA items that ship without one in the pack. */
public final class GTIANexusRecipes {

    private GTIANexusRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        var linker = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtna", "nexus_linker"));
        if (linker == null || linker == Items.AIR) return;
        // EN: LV tool that binds wireless hatches to a Flux Matrix: sensor + emitter (it reads and sends), ender pearl.
        // PT: Ferramenta LV que liga escotilhas sem fio a uma Flux Matrix: sensor + emissor (lê e envia), pérola do
        // fim.
        // ZH: 将无线仓绑定到通量矩阵的 LV 工具：传感器 + 发射器（读取并发送）、末影珍珠。
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("nexus_linker"), new ItemStack(linker),
                "SPE", "CHC", "RPR",
                'S', GTCraftingComponents.SENSOR.get(GTValues.LV),
                'E', GTCraftingComponents.EMITTER.get(GTValues.LV),
                'C', GTCraftingComponents.CIRCUIT.get(GTValues.LV),
                'H', new ItemStack(Items.ENDER_PEARL),
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel),
                'R', new MaterialEntry(TagPrefix.wireGtSingle, GTMaterials.RedAlloy));
    }
}
