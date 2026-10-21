package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtia.mega.GTIAMegastructures;
import com.raishxn.gtia.mega.common.MegaMachine;
import com.raishxn.gtia.mega.common.MegaShape;
import com.raishxn.gtia.mega.suprashell.ShellPhysics;
import com.raishxn.gtia.mega.suprashell.SuprashellMachine;
import com.raishxn.gtia.mega.system.StarPhysics.Kind;
import com.raishxn.gtia.mega.system.StarSystems;

import java.util.UUID;

/** Bodies of {@link SuprashellGameTests}, apart so the GTNA classes load only when it is registered. */
final class SuprashellTestRig {

    private SuprashellTestRig() {}

    static void shellMath(GameTestHelper helper) {
        helper.assertTrue(Math.abs(ShellPhysics.tau(1.5) - Math.sqrt(1 / 3.0)) < 1e-9 &&
                Math.abs(ShellPhysics.tau(20) - Math.sqrt(0.95)) < 1e-9, "tau = sqrt(1 - rs/r)");
        helper.assertTrue(ShellPhysics.capacity(1.5, 5) == 2 && ShellPhysics.capacity(10, 5) == 100 &&
                ShellPhysics.capacity(20, 5) == 320 && ShellPhysics.capacity(20, 1) == 64,
                "room r^2, capped at 64 a phase");
        helper.assertTrue(Math.abs(ShellPhysics.loweringShare(4) - (1 - Math.sqrt(0.75))) < 1e-9,
                "lowering gives 1 - tau");
        helper.succeed();
    }

    /** An Assembler recipe with a few plain item inputs and an item output. */
    private static GTRecipe simpleAssembly(GameTestHelper helper) {
        GTRecipe best = null;
        for (var r : helper.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.ASSEMBLER_RECIPES)) {
            if (!RecipeHelper.getInputFluids(r).isEmpty() || !RecipeHelper.getOutputFluids(r).isEmpty()) continue;
            var in = RecipeHelper.getInputItems(r);
            if (in.isEmpty() || in.size() > 3 || RecipeHelper.getOutputItems(r).isEmpty()) continue;
            if (best == null || in.size() < RecipeHelper.getInputItems(best).size()) best = r;
        }
        if (best == null) throw new IllegalStateException("no plain Assembler recipe");
        return best;
    }

    static void hostsModules(GameTestHelper helper) {
        var star = StarSystems.get(helper.getLevel().getServer()).star(StarSystems.NEXUS);
        var kind = star.kind;
        double mass = star.mass;
        try {
            MegaMachine.anyDimensionForTests = true;
            star.kind = Kind.BLACK_HOLE;
            star.mass = 10;
            SuprashellMachine m = MegaTestRig.build(helper, SuprashellMachine.SHAPE, GTIAMegastructures.SUPRASHELL_DEF,
                    5, 26);
            var bus = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 0, GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var modules = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 1,
                    GTMachines.ITEM_IMPORT_BUS[GTValues.LuV]);
            var out = (ItemBusPartMachine) MegaTestRig.hatch(helper, m, 2, GTMachines.ITEM_EXPORT_BUS[GTValues.LuV]);
            helper.assertTrue(m.checkPatternWithLock(), "the whole shell forms: " + MegaTestRig.error(m));
            m.onStructureFormed();
            UUID owner = UUID.randomUUID();
            StellarEngineTestRig.power(helper, owner);
            m.setRadius(4);
            m.testSecond(owner);
            helper.assertTrue(m.crafted() == 0 && "modules".equals(m.blocked()), "no module: it waits");
            modules.getInventory().storage.insertItem(0, GCYMMachines.LARGE_ASSEMBLER.asStack(2), false);
            modules.getInventory().storage.insertItem(1, GTIAMegastructures.SUPRASHELL_DEF.asStack(1), false);
            var recipe = simpleAssembly(helper);
            for (var stack : RecipeHelper.getInputItems(recipe)) {
                var big = stack.copyWithCount(64);
                for (int s = 0; s < bus.getInventory().getSlots() && !big.isEmpty(); s++)
                    big = bus.getInventory().storage.insertItem(s, big, false);
            }
            for (int i = 0; i < Math.max(4, recipe.duration * 2 / 20 + 4) && m.crafted() == 0; i++) m.testSecond(owner);
            helper.assertTrue(m.view()[0] == 2, "two Large Assemblers hosted, the megastructure ignored: " +
                    m.view()[0]);
            helper.assertTrue(m.view()[2] == 128 && Math.abs(m.view()[3] - Math.sqrt(0.75)) < 1e-9,
                    "64 parallels a module, at tau of 4 rs");
            helper.assertTrue(m.crafted() > 1, "the hosted assemblers run " + recipe.id + ": " + m.crafted() + " (" +
                    m.blocked() + ")");
            boolean any = false;
            for (int s = 0; s < out.getInventory().getSlots(); s++)
                any |= !out.getInventory().storage.getStackInSlot(s).isEmpty();
            helper.assertTrue(any, "the outputs reach the output bus");
            star.kind = Kind.YELLOW_DWARF;
            helper.assertTrue(!m.inPlace(), "round a shining star there is no shell");
            MegaTestRig.capturesAndReleases(helper, m);
            helper.succeed();
        } finally {
            MegaMachine.anyDimensionForTests = false;
            star.kind = kind;
            star.mass = mass;
        }
    }

    static void noPlatesNoShell(GameTestHelper helper) {
        SuprashellMachine m = MegaTestRig.build(helper, SuprashellMachine.SHAPE, GTIAMegastructures.SUPRASHELL_DEF, 5,
                27);
        var shape = MegaShape.get(SuprashellMachine.SHAPE);
        int[] c = MegaTestRig.firstCell(shape, MegaTestRig.part(shape, "plates_1", "body"));
        helper.getLevel().setBlock(shape.world(m.getPos(), MegaTestRig.FRONT, c[0], c[1], c[2]),
                Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.assertTrue(!m.checkPatternWithLock(), "a missing plate block keeps the shell from forming");
        helper.succeed();
    }
}
