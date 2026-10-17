package com.raishxn.gtia.organ;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.WorldRules;

import java.util.function.Consumer;

/** Organ recipes, the GTO 0.6 ones with GTIA materials for the wings (GTIA-D-279). */
public final class OrganRecipes {

    private OrganRecipes() {}

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, GTIAMod.id("organ_modifier"), GTIAOrgans.ORGAN_MODIFIER.asStack(),
                " B ", "CDC", "EFF",
                'B', GTItems.VOLTAGE_COIL_MV.asStack(), 'C', GTItems.ROBOT_ARM_MV.asStack(),
                'D', new ItemStack(Items.CRAFTING_TABLE), 'E', new ItemStack(Items.SLIME_BALL),
                'F', CustomTags.MV_CIRCUITS);
        // EN: GTO makes the cheap wings from Botania materials; GTIA has no Botania, so they use phantom membranes.
        // PT: O GTO faz as asas baratas com materiais do Botania; o GTIA não tem Botania, então usa membrana de
        // phantom.
        // ZH: GTO 用植物魔法材料制作廉价翅膀；GTIA 没有植物魔法，因此改用幻翼膜。
        VanillaRecipeHelper.addShapedRecipe(provider, GTIAMod.id("steel_wing"), GTIAOrgans.STEEL_WING.asStack(),
                "ABA", "CDC", "ABA",
                'A', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Steel),
                'B', new MaterialEntry(TagPrefix.foil, GTMaterials.Aluminium),
                'C', GTItems.FIELD_GENERATOR_LV.asStack(), 'D', new ItemStack(Items.PHANTOM_MEMBRANE));
        VanillaRecipeHelper.addShapedRecipe(provider, GTIAMod.id("fairy_wing"), GTIAOrgans.FAIRY_WING.asStack(),
                "ABA", "ACA", "DDD",
                'A', new MaterialEntry(TagPrefix.foil, GTMaterials.Polyethylene),
                'B', new MaterialEntry(TagPrefix.plate, GTMaterials.StainlessSteel),
                'C', GTItems.FIELD_GENERATOR_LV.asStack(), 'D', new ItemStack(Items.PHANTOM_MEMBRANE));
        VanillaRecipeHelper.addShapedRecipe(provider, GTIAMod.id("mechanical_wing"),
                GTIAOrgans.MECHANICAL_WING.asStack(),
                "ABA", "CBC", "DED",
                'A', GTItems.BATTERY_EV_VANADIUM.asStack(),
                'B', new MaterialEntry(TagPrefix.foil, GTMaterials.Titanium),
                'C', new MaterialEntry(TagPrefix.plate, GTMaterials.Titanium),
                'D', GTItems.FIELD_GENERATOR_EV.asStack(), 'E',
                new MaterialEntry(TagPrefix.ingot, GTMaterials.Titanium));
        for (int organTier = 1; organTier <= 4; organTier++) tier(provider, organTier);
    }

    /** Organ tier 1 to 4 uses MV, EV, LuV and UV parts; field generators are one tier lower (GTO). */
    private static void tier(Consumer<FinishedRecipe> provider, int organTier) {
        int voltage = organTier * 2;
        // EN: The organ cost rule multiplies every part (GTO: ×2 on Normal, ×1 on Easy); tiers 2+ cost twice that.
        // PT: A regra de custo dos órgãos multiplica cada peça (GTO: ×2 no Normal, ×1 no Easy); tier 2+ custa o dobro.
        // ZH: 器官成本规则乘以每个部件（GTO：普通 ×2，简单 ×1）；2 级以上再翻倍。
        int scale = (int) WorldRules.get(GTIARules.ORGAN_COST) * (organTier == 1 ? 1 : 2);
        Item motor = pick(voltage, GTItems.ELECTRIC_MOTOR_MV, GTItems.ELECTRIC_MOTOR_EV, GTItems.ELECTRIC_MOTOR_LuV,
                GTItems.ELECTRIC_MOTOR_UV);
        Item conveyor = pick(voltage, GTItems.CONVEYOR_MODULE_MV, GTItems.CONVEYOR_MODULE_EV,
                GTItems.CONVEYOR_MODULE_LuV, GTItems.CONVEYOR_MODULE_UV);
        Item pump = pick(voltage, GTItems.ELECTRIC_PUMP_MV, GTItems.ELECTRIC_PUMP_EV, GTItems.ELECTRIC_PUMP_LuV,
                GTItems.ELECTRIC_PUMP_UV);
        Item piston = pick(voltage, GTItems.ELECTRIC_PISTON_MV, GTItems.ELECTRIC_PISTON_EV,
                GTItems.ELECTRIC_PISTON_LuV, GTItems.ELECTRIC_PISTON_UV);
        Item arm = pick(voltage, GTItems.ROBOT_ARM_MV, GTItems.ROBOT_ARM_EV, GTItems.ROBOT_ARM_LuV,
                GTItems.ROBOT_ARM_UV);
        Item emitter = pick(voltage, GTItems.EMITTER_MV, GTItems.EMITTER_EV, GTItems.EMITTER_LuV, GTItems.EMITTER_UV);
        Item sensor = pick(voltage, GTItems.SENSOR_MV, GTItems.SENSOR_EV, GTItems.SENSOR_LuV, GTItems.SENSOR_UV);
        Item field = pick(voltage, GTItems.FIELD_GENERATOR_LV, GTItems.FIELD_GENERATOR_HV,
                GTItems.FIELD_GENERATOR_IV, GTItems.FIELD_GENERATOR_ZPM);
        TagKey<Item> circuit = switch (organTier) {
            case 1 -> CustomTags.MV_CIRCUITS;
            case 2 -> CustomTags.EV_CIRCUITS;
            case 3 -> CustomTags.LuV_CIRCUITS;
            default -> CustomTags.UV_CIRCUITS;
        };
        Object[][] parts = {
                { OrganType.RIGHT_ARM, motor, 2, arm, 4, sensor, 2, field, 1, piston, 1 },
                { OrganType.LEFT_ARM, motor, 2, arm, 4, sensor, 2, field, 1, piston, 1 },
                { OrganType.RIGHT_LEG, motor, 4, conveyor, 2, arm, 2, sensor, 2, field, 1, piston, 1 },
                { OrganType.LEFT_LEG, motor, 4, conveyor, 2, arm, 2, sensor, 2, field, 1, piston, 1 },
                { OrganType.HEART, motor, 2, emitter, 2, sensor, 2, field, 1, piston, 2, pump, 2 },
                { OrganType.EYE, motor, 1, emitter, 1, sensor, 2 },
                { OrganType.LUNG, motor, 2, arm, 4, emitter, 1, sensor, 1, piston, 2, pump, 2, field, 1 },
                { OrganType.LIVER, emitter, 1, sensor, 2, field, 2, pump, 2 },
                { OrganType.SPINE, arm, 4, emitter, 1 },
        };
        for (int i = 0; i < parts.length; i++) {
            OrganType type = (OrganType) parts[i][0];
            var builder = GTRecipeTypes.ASSEMBLER_RECIPES
                    .recipeBuilder(GTIAMod.id("organ/" + type.key() + "_tier_" + organTier));
            for (int p = 1; p < parts[i].length; p += 2) {
                builder.inputItems(new ItemStack((ItemLike) parts[i][p], (int) parts[i][p + 1] * scale));
            }
            // EN: The spine needs one circuit instead of four, as in GTO.
            // PT: A coluna pede um circuito em vez de quatro, como no GTO.
            // ZH: 脊椎只需要一个电路而不是四个，与 GTO 相同。
            builder.inputItems(circuit, (type == OrganType.SPINE ? 1 : 4) * scale)
                    .circuitMeta(i + 1)
                    .outputItems(GTIAOrgans.TIERED.get(type).get(organTier).asStack())
                    .EUt(GTValues.VA[voltage]).duration(1200).save(provider);
        }
    }

    @SafeVarargs
    private static Item pick(int voltage, com.tterrag.registrate.util.entry.ItemEntry<? extends Item>... byTier) {
        return byTier[voltage / 2 - 1].get();
    }
}
