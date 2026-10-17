package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.ToolItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAAlloys;
import com.raishxn.gtia.data.GTIAExtraForms;
import com.raishxn.gtia.data.GTIAItems;
import com.raishxn.gtia.data.GTIAMachines;

/** Checks the pack's own alloy forms and their Steam-era entry point in a real game world. */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class AlloyGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";
    private static final BlockPos MACHINE_POS = new BlockPos(2, 1, 2);

    private AlloyGameTests() {}

    @GameTest(template = TEMPLATE, batch = "alloy", timeoutTicks = 20)
    public static void nativeFormsAndCouplerCrafting(GameTestHelper helper) {
        helper.assertTrue(!ModList.get().isLoaded("enderio"), "QA must run without EnderIO");

        var pulsating = ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY);
        var conductive = ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.CONDUCTIVE_ALLOY);
        var conductiveWire = ChemicalHelper.get(TagPrefix.wireGtSingle, GTIAAlloys.CONDUCTIVE_ALLOY);
        assertItemId(helper, pulsating, "pulsating_alloy_ingot");
        assertItemId(helper, conductive, "conductive_alloy_ingot");
        assertItemId(helper, conductiveWire, "conductive_alloy_single_wire");
        assertAlloyForms(helper, GTIAAlloys.PULSATING_ALLOY, "pulsating_alloy");
        assertAlloyForms(helper, GTIAAlloys.CONDUCTIVE_ALLOY, "conductive_alloy");

        var recipe = helper.getLevel().getRecipeManager().byKey(GTIAMod.id("shaped/phase_coupler"))
                .orElseThrow(() -> new AssertionError("Phase Coupler crafting recipe is absent"));
        helper.assertTrue(recipe instanceof CraftingRecipe, "Phase Coupler must have a crafting recipe");
        var crafting = (CraftingRecipe) recipe;
        helper.assertTrue(ItemStack.isSameItem(crafting.getResultItem(helper.getLevel().registryAccess()),
                GTIAItems.PHASE_COUPLER.asStack()), "Crafting must output the Phase Coupler");
        var ingredients = crafting.getIngredients();
        helper.assertTrue(ingredients.size() == 9, "Phase Coupler recipe must occupy a 3x3 grid");
        helper.assertTrue(ingredients.get(3).test(pulsating), "Recipe must accept GTIA Pulsating Alloy");
        helper.assertTrue(ingredients.get(1).test(ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.RedAlloy)),
                "Recipe must accept Red Alloy wire");
        helper.assertTrue(ingredients.get(7).test(ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.RedAlloy)),
                "Recipe must require a second Red Alloy wire");
        helper.assertTrue(ingredients.get(5).test(ChemicalHelper.get(TagPrefix.plate, GTMaterials.WroughtIron)),
                "Recipe must accept Wrought Iron plate");
        var grid = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {

            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 3, 3);
        grid.setItem(1, ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.RedAlloy));
        grid.setItem(3, pulsating.copy());
        grid.setItem(4, com.gregtechceu.gtceu.common.data.GTItems.VACUUM_TUBE.asStack());
        grid.setItem(5, ChemicalHelper.get(TagPrefix.plate, GTMaterials.WroughtIron));
        grid.setItem(7, ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.RedAlloy));
        helper.assertTrue(crafting.matches(grid, helper.getLevel()), "Phase Coupler must match a real crafting grid");
        helper.assertTrue(ItemStack.isSameItem(crafting.assemble(grid, helper.getLevel().registryAccess()),
                GTIAItems.PHASE_COUPLER.asStack()), "Crafting grid must assemble the Phase Coupler");

        var dehydratorRecipe = helper.getLevel().getRecipeManager()
                .byKey(GTIAMod.id("shaped/lv_catalytic_dehydrator"))
                .orElseThrow(() -> new AssertionError("Dehydrator construction recipe is absent"));
        helper.assertTrue(dehydratorRecipe instanceof CraftingRecipe,
                "Dehydrator construction must be a crafting recipe");
        helper.assertTrue(dehydratorRecipe.getIngredients().get(3).test(GTIAItems.PHASE_COUPLER.asStack()),
                "Dehydrator construction must consume a Phase Coupler");
        helper.assertTrue(ItemStack.isSameItem(dehydratorRecipe.getResultItem(helper.getLevel().registryAccess()),
                GTIAMachines.CATALYTIC_DEHYDRATOR.asStack()),
                "Construction recipe must output the GTIA Dehydrator");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "alloy", timeoutTicks = 20)
    public static void steamSmelterMakesPulsatingAlloy(GameTestHelper helper) {
        var machine = placeSteamAlloySmelter(helper);
        machine.importItems.setStackInSlot(0, ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Iron));
        machine.importItems.setStackInSlot(1, new ItemStack(Items.ENDER_PEARL));

        machine.recipeLogic.findAndHandleRecipe();

        helper.assertTrue(machine.recipeLogic.getDuration() == 600,
                "Low-pressure Steam Alloy Smelter must accept the 300-tick recipe at double duration");
        helper.assertTrue(machine.importItems.getStackInSlot(0).isEmpty() &&
                machine.importItems.getStackInSlot(1).isEmpty(), "Smelter must consume iron and ender pearl");
        machine.recipeLogic.onRecipeFinish();
        helper.assertTrue(ItemStack.isSameItem(machine.exportItems.getStackInSlot(0),
                ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY)),
                "Smelter must output a GTIA Pulsating Alloy ingot");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "alloy", timeoutTicks = 20)
    public static void steamSmelterMakesConductiveAlloy(GameTestHelper helper) {
        var machine = placeSteamAlloySmelter(helper);
        machine.importItems.setStackInSlot(0, ChemicalHelper.get(TagPrefix.ingot, GTMaterials.RedAlloy));
        machine.importItems.setStackInSlot(1, ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY));

        machine.recipeLogic.findAndHandleRecipe();

        helper.assertTrue(machine.recipeLogic.getDuration() == 600,
                "Low-pressure Steam Alloy Smelter must accept the Conductive Alloy recipe");
        machine.recipeLogic.onRecipeFinish();
        var output = machine.exportItems.getStackInSlot(0);
        helper.assertTrue(
                ItemStack.isSameItem(output, ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.CONDUCTIVE_ALLOY)) &&
                        output.getCount() == 2,
                "Smelter must output two GTIA Conductive Alloy ingots");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "alloy", timeoutTicks = 20)
    public static void compoundAlloyFormsUseOwnIds(GameTestHelper helper) {
        assertCompoundForms(helper, GTIAAlloys.PULSATING_ALLOY, "pulsating_alloy");
        assertCompoundForms(helper, GTIAAlloys.CONDUCTIVE_ALLOY, "conductive_alloy");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "alloy", timeoutTicks = 20)
    public static void compoundIngotCraftingUsesOwnForms(GameTestHelper helper) {
        var doubleRecipe = helper.getLevel().getRecipeManager()
                .byKey(GTIAMod.id("shaped/double_pulsating_alloy_ingot"))
                .orElseThrow(() -> new AssertionError("Double ingot crafting recipe is absent"));
        helper.assertTrue(doubleRecipe instanceof CraftingRecipe, "Double ingot must use crafting");
        var crafting = (CraftingRecipe) doubleRecipe;
        var grid = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {

            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 2, 2);
        grid.setItem(0, ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY));
        grid.setItem(2, ChemicalHelper.get(TagPrefix.ingot, GTIAAlloys.PULSATING_ALLOY));
        helper.assertTrue(crafting.matches(grid, helper.getLevel()), "Two ingots must craft into a double ingot");
        helper.assertTrue(ItemStack.isSameItem(crafting.assemble(grid, helper.getLevel().registryAccess()),
                ChemicalHelper.get(GTIAExtraForms.DOUBLE_INGOT, GTIAAlloys.PULSATING_ALLOY)),
                "Crafting must output a GTIA double ingot");
        var quintupleRecipe = helper.getLevel().getRecipeManager()
                .byKey(GTIAMod.id("shapeless/quintuple_pulsating_alloy_ingot"))
                .orElseThrow(() -> new AssertionError("Quintuple ingot crafting recipe is absent"));
        helper.assertTrue(quintupleRecipe instanceof CraftingRecipe &&
                quintupleRecipe.getIngredients().get(0).test(
                        ChemicalHelper.get(GTIAExtraForms.QUADRUPLE_INGOT, GTIAAlloys.PULSATING_ALLOY)),
                "Quintuple ingot must use a quadruple ingot, not itself");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "alloy", timeoutTicks = 20)
    public static void conductivePlateCraftsIntoWire(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager()
                .byKey(GTCEu.id("shaped/conductive_alloy_wire_single"))
                .orElseThrow(() -> new AssertionError("GTCEu manual Conductive Alloy wire recipe is absent"));
        helper.assertTrue(recipe instanceof CraftingRecipe, "Manual wire recipe must use crafting");
        var crafting = (CraftingRecipe) recipe;
        var grid = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {

            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 2, 2);
        grid.setItem(0, ChemicalHelper.get(TagPrefix.plate, GTIAAlloys.CONDUCTIVE_ALLOY));
        grid.setItem(1, ToolItemHelper.getToolItem(GTToolType.WIRE_CUTTER));
        helper.assertTrue(crafting.matches(grid, helper.getLevel()),
                "Conductive Alloy plate and wire cutter must match the generated recipe");
        helper.assertTrue(ItemStack.isSameItem(crafting.assemble(grid, helper.getLevel().registryAccess()),
                ChemicalHelper.get(TagPrefix.wireGtSingle, GTIAAlloys.CONDUCTIVE_ALLOY)),
                "Manual crafting must produce a GTIA Conductive Alloy wire");
        helper.succeed();
    }

    private static void assertCompoundForms(GameTestHelper helper,
                                            com.gregtechceu.gtceu.api.data.chemical.material.Material material,
                                            String name) {
        assertItemId(helper, ChemicalHelper.get(TagPrefix.plate, material), name + "_plate");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.DOUBLE_INGOT, material),
                "double_" + name + "_ingot");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.TRIPLE_INGOT, material),
                "triple_" + name + "_ingot");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.QUADRUPLE_INGOT, material),
                "quadruple_" + name + "_ingot");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.QUINTUPLE_INGOT, material),
                "quintuple_" + name + "_ingot");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.TRIPLE_PLATE, material),
                "triple_" + name + "_plate");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.QUADRUPLE_PLATE, material),
                "quadruple_" + name + "_plate");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.QUINTUPLE_PLATE, material),
                "quintuple_" + name + "_plate");
        assertItemId(helper, ChemicalHelper.get(GTIAExtraForms.SUPERDENSE_PLATE, material),
                "superdense_" + name + "_plate");
    }

    private static SimpleSteamMachine placeSteamAlloySmelter(GameTestHelper helper) {
        helper.setBlock(MACHINE_POS, GTMachines.STEAM_ALLOY_SMELTER.left().getBlock());
        var holder = (MetaMachineBlockEntity) helper.getBlockEntity(MACHINE_POS);
        var machine = (SimpleSteamMachine) holder.getMetaMachine();
        machine.steamTank.setFluidInTank(0, GTMaterials.Steam.getFluid(16000));
        return machine;
    }

    private static void assertItemId(GameTestHelper helper, ItemStack stack, String path) {
        helper.assertTrue(!stack.isEmpty(), "Missing GTIA item: " + path);
        helper.assertTrue(GTIAMod.id(path).equals(BuiltInRegistries.ITEM.getKey(stack.getItem())),
                "Unexpected item ID for " + path + ": " + BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static void assertAlloyForms(GameTestHelper helper,
                                         com.gregtechceu.gtceu.api.data.chemical.material.Material material,
                                         String name) {
        assertItemId(helper, ChemicalHelper.get(TagPrefix.dust, material), name + "_dust");
        assertItemId(helper, ChemicalHelper.get(TagPrefix.nugget, material), name + "_nugget");
        assertItemId(helper, ChemicalHelper.get(TagPrefix.block, material), name + "_block");
    }
}
