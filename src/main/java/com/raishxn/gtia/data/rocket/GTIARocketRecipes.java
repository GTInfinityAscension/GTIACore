package com.raishxn.gtia.data.rocket;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIARecipeTypes;

import java.util.function.Consumer;

/**
 * The tier 1 rocket through GT (GTIA-D-272, D-274): Ad Astra's parts in the Assembler, the rocket in the Rocket
 * Assembler. {@link AdAstraRocketRecipeFilter} drops Ad Astra's own recipes for the same items.
 */
public final class GTIARocketRecipes {

    private GTIARocketRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        // EN: The casing is a stainless steel casing in a new skin (two per craft, like GT's own casings).
        // PT: A casing é uma casing de aço inox com outra aparência (duas por receita, como as do GT).
        // ZH: 该外壳是换了外观的不锈钢外壳（与 GT 外壳一样每次产出两个）。
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("rocket_assembly_casing"),
                GTIARocketBlocks.ROCKET_ASSEMBLY_CASING.asStack(2), "PhP", "PFP", "PwP",
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.StainlessSteel),
                'F', new MaterialEntry(TagPrefix.frameGt, GTMaterials.StainlessSteel));
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("rocket/rocket_assembly_casing"))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 6)
                .inputItems(TagPrefix.frameGt, GTMaterials.StainlessSteel)
                .circuitMeta(6).outputItems(GTIARocketBlocks.ROCKET_ASSEMBLY_CASING.asStack(2))
                .duration(50).EUt(16).save(provider);

        // EN: HV controller: hull, circuits, robot arms and conveyors, the tools of an assembly line.
        // PT: Controlador HV: carcaça, circuitos, braços robóticos e esteiras, as ferramentas de uma linha de montagem.
        // ZH: HV 控制器：机壳、电路、机械臂和传送带，即装配线所需的工具。
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTIAMod.id("rocket_assembler"),
                GTIARocketAssembler.ROCKET_ASSEMBLER.asStack(), "ACA", "VHV", "PCP",
                'A', GTCraftingComponents.ROBOT_ARM.get(GTValues.HV),
                'C', GTCraftingComponents.CIRCUIT.get(GTValues.HV),
                'V', GTCraftingComponents.CONVEYOR.get(GTValues.HV),
                'H', GTCraftingComponents.HULL.get(GTValues.HV),
                'P', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.StainlessSteel));

        if (!ModList.get().isLoaded("ad_astra")) return;
        Item nose = item("rocket_nose_cone"), fin = item("rocket_fin"), tank = item("steel_tank"),
                engine = item("steel_engine"), rocket = item("tier_1_rocket"), gasTank = item("gas_tank"),
                frame = item("engine_frame"), fan = item("fan");
        if (nose == null || fin == null || tank == null || engine == null || rocket == null || gasTank == null ||
                frame == null || fan == null)
            return;

        // EN: MV Assembler (120 EU/t): the parts need stainless steel and an MV pump, so they belong to the end of MV.
        // PT: Assembler MV (120 EU/t): as peças pedem aço inox e bomba MV, então ficam no fim do MV.
        // ZH: MV 组装机（120 EU/t）：部件需要不锈钢和 MV 泵，因此属于 MV 末期。
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("rocket/nose_cone"))
                .inputItems(new ItemStack(Items.LIGHTNING_ROD))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .outputItems(new ItemStack(nose)).duration(400).EUt(120).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("rocket/fins"))
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
                .inputItems(TagPrefix.rod, GTMaterials.StainlessSteel, 2)
                .outputItems(new ItemStack(fin, 2)).duration(300).EUt(120).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("rocket/steel_tank"))
                .inputItems(new ItemStack(gasTank)).inputItems(TagPrefix.plateDouble, GTMaterials.Steel, 4)
                .inputItems(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel)
                .outputItems(new ItemStack(tank)).duration(400).EUt(120).save(provider);
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTIAMod.id("rocket/steel_engine"))
                .inputItems(new ItemStack(frame)).inputItems(new ItemStack(fan))
                .inputItems(GTItems.ELECTRIC_PUMP_MV.asStack(2))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .outputItems(new ItemStack(engine)).duration(600).EUt(120).save(provider);

        // EN: 480 EU/t for 2 minutes: the first rocket is an HV project (D-274), fuelled with GT rocket fuel.
        // PT: 480 EU/t por 2 minutos: o primeiro foguete é um projeto do HV (D-274), com combustível de foguete do GT.
        // ZH: 480 EU/t 持续 2 分钟：第一枚火箭是 HV 阶段的工程（D-274），使用 GT 火箭燃料。
        GTIARecipeTypes.ROCKET_ASSEMBLY.recipeBuilder(GTIAMod.id("rocket/tier_1_rocket"))
                .inputItems(new ItemStack(nose)).inputItems(new ItemStack(fin, 4)).inputItems(new ItemStack(tank, 2))
                .inputItems(new ItemStack(engine)).inputItems(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 8)
                .inputItems(CustomTags.HV_CIRCUITS, 2).inputItems(GTItems.ELECTRIC_MOTOR_HV.asStack(4))
                .inputFluids(GTMaterials.RocketFuel.getFluid(1000))
                .outputItems(new ItemStack(rocket)).duration(2400).EUt(480).save(provider);
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ad_astra", path));
        return item == null || item == Items.AIR ? null : item;
    }
}
