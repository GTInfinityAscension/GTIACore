package com.raishxn.gtia.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.data.GTIAItems;
import com.raishxn.gtia.miner.BedrockVeins;
import com.raishxn.gtia.miner.EntangledMinerLogic;
import com.raishxn.gtia.miner.EntangledMinerMachine;
import com.raishxn.gtia.miner.GTIAMiners;
import com.raishxn.gtia.miner.MinerClaims;
import com.raishxn.gtia.miner.VeinCardItem;
import com.raishxn.gtia.miner.VeinPurity;

/**
 * Veins with purity and the Entangled Miner (GTIA-D-310): purity odds, the card, the table rates, the 4-per-vein
 * limit, no card and the dimension rule.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("gtia")
public final class EntangledMinerGameTests {

    private static final String TEMPLATE = "qa_empty_5x5";
    private static final BlockPos MACHINE_POS = new BlockPos(2, 1, 2);

    private EntangledMinerGameTests() {}

    @GameTest(template = TEMPLATE, batch = "entangled_miner", timeoutTicks = 20)
    public static void veinsAreRegisteredWithPurityOdds(GameTestHelper h) {
        long registered = GTRegistries.BEDROCK_ORE_DEFINITIONS.keys().stream()
                .filter(id -> id.getNamespace().equals(GTIAMod.MOD_ID)).count();
        h.assertTrue(registered == 14, "14 GTIA bedrock veins, got " + registered);
        int[] counts = new int[3];
        for (int x = -50; x < 50; x++)
            for (int z = -50; z < 50; z++) counts[VeinPurity.of(12345L, "minecraft:overworld", x * 16, z * 16).ordinal()]++;
        h.assertTrue(Math.abs(counts[0] - 4000) < 300 && Math.abs(counts[1] - 4000) < 300 &&
                Math.abs(counts[2] - 2000) < 300, "purity 40/40/20, got " + java.util.Arrays.toString(counts));
        h.assertTrue(VeinPurity.of(1, "a", 16, 32) == VeinPurity.of(1, "a", 16, 32), "purity never changes");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "entangled_miner", timeoutTicks = 20)
    public static void cardRecordsTheVeinAndItsPurity(GameTestHelper h) {
        var vein = findVein(h.getLevel());
        h.assertTrue(vein != null, "an overworld vein near spawn");
        ItemStack card = recorded(h.getLevel(), vein);
        var record = VeinCardItem.read(card);
        h.assertTrue(record != null && record.originX() == vein.originX() && record.originZ() == vein.originZ() &&
                record.vein().equals(vein.id()) && record.purity() == vein.purity(h.getLevel().getSeed()),
                "the card keeps origin, vein and purity");
        h.assertTrue(BedrockVeins.at(h.getLevel(), vein.originX() + 1, vein.originZ() - 1) != null,
                "the vein covers 3 x 3 chunks");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "entangled_miner", timeoutTicks = 20)
    public static void minerMinesAtTheTableRate(GameTestHelper h) {
        MinerClaims.clear();
        var vein = findVein(h.getLevel());
        var purity = vein.purity(h.getLevel().getSeed());
        var lv = place(h, GTValues.LV);
        lv.importItems.setStackInSlot(0, recorded(h.getLevel(), vein));
        lv.recipeLogic.findAndHandleRecipe();
        h.assertTrue(lv.recipeLogic.getDuration() == 160, "LV: one operation every 8 s, got " +
                lv.recipeLogic.getDuration());
        lv.recipeLogic.onRecipeFinish();
        int ores = 0;
        for (int i = 0; i < lv.exportItems.getSlots(); i++) ores += lv.exportItems.getStackInSlot(i).getCount();
        h.assertTrue(ores == purity.ores, "one operation gives " + purity.ores + " raw ores, got " + ores);
        h.assertTrue(!lv.importItems.getStackInSlot(0).isEmpty(), "the card is not spent");
        MinerClaims.clear();
        var iv = place(h, GTValues.IV);
        iv.importItems.setStackInSlot(0, recorded(h.getLevel(), vein));
        iv.recipeLogic.findAndHandleRecipe();
        h.assertTrue(iv.recipeLogic.getDuration() == 10, "IV: 0.5 s per operation, got " +
                iv.recipeLogic.getDuration());
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "entangled_miner", timeoutTicks = 20)
    public static void fifthMinerOnAVeinWaits(GameTestHelper h) {
        MinerClaims.clear();
        for (int i = 0; i < MinerClaims.MAX_PER_VEIN; i++)
            h.assertTrue(MinerClaims.claim("v", miner(i), 0), "miner " + i + " gets a slot");
        h.assertFalse(MinerClaims.claim("v", miner(9), 0), "the fifth miner waits");
        MinerClaims.release(miner(0));
        h.assertTrue(MinerClaims.claim("v", miner(9), 1), "a freed slot goes to the waiting miner");
        h.assertTrue(MinerClaims.claim("v", miner(1), MinerClaims.EXPIRY_TICKS * 3) &&
                MinerClaims.count("v") == 1, "claims not renewed expire");
        MinerClaims.clear();
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "entangled_miner", timeoutTicks = 20)
    public static void minerWithoutCardDoesNothing(GameTestHelper h) {
        MinerClaims.clear();
        var lv = place(h, GTValues.LV);
        lv.importItems.setStackInSlot(0, GTIAItems.VEIN_CARD.asStack());
        lv.recipeLogic.findAndHandleRecipe();
        h.assertFalse(lv.recipeLogic.isWorking(), "a blank card mines nothing");
        h.assertTrue(lv.check().block() == EntangledMinerLogic.Block.NO_CARD, "reason: no recorded card");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "entangled_miner", timeoutTicks = 20)
    public static void otherDimensionsOnlyFromEV(GameTestHelper h) {
        MinerClaims.clear();
        var vein = findVein(h.getLevel());
        var card = recorded(h.getLevel(), vein);
        card.getTag().getCompound(VeinCardItem.TAG).putString("dim", Level.NETHER.location().toString());
        var hv = place(h, GTValues.HV);
        hv.importItems.setStackInSlot(0, card.copy());
        h.assertTrue(hv.check().block() == EntangledMinerLogic.Block.OTHER_DIMENSION, "HV stays in its dimension");
        var ev = place(h, GTValues.EV);
        ev.importItems.setStackInSlot(0, card.copy());
        h.assertTrue(ev.check().block() != EntangledMinerLogic.Block.OTHER_DIMENSION, "EV reaches other dimensions");
        MinerClaims.clear();
        h.succeed();
    }

    // ------------------------------------------------------------------ helpers

    private static GlobalPos miner(int i) {
        return GlobalPos.of(Level.OVERWORLD, new BlockPos(i, 0, 0));
    }

    private static EntangledMinerMachine place(GameTestHelper h, int tier) {
        h.setBlock(MACHINE_POS, GTIAMiners.ENTANGLED_MINER[tier].getBlock());
        var machine = (EntangledMinerMachine) ((MetaMachineBlockEntity) h.getBlockEntity(MACHINE_POS))
                .getMetaMachine();
        machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
        return machine;
    }

    private static ItemStack recorded(ServerLevel level, BedrockVeins.Vein vein) {
        ItemStack card = GTIAItems.VEIN_CARD.asStack();
        VeinCardItem.write(card, vein, vein.purity(level.getSeed()));
        return card;
    }

    /** The first vein origin found in a spiral of 32 origins around spawn. */
    private static BedrockVeins.Vein findVein(ServerLevel level) {
        for (int r = 0; r < 4; r++)
            for (int x = -r; x <= r; x++)
                for (int z = -r; z <= r; z++) {
                    var vein = BedrockVeins.veinAt(level, x * 16, z * 16);
                    if (vein != null) return vein;
                }
        return null;
    }
}
