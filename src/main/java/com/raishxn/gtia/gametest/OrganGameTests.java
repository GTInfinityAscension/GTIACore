package com.raishxn.gtia.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemStackHandler;

import com.raishxn.gtia.GTIAMod;
import com.raishxn.gtia.organ.GTIAOrgans;
import com.raishxn.gtia.organ.OrganData;
import com.raishxn.gtia.organ.OrganEffects;
import com.raishxn.gtia.organ.OrganType;

/** Organ system (GTIA-D-279): storage, set tier, effects and the planet requirement. */
@GameTestHolder(GTIAMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OrganGameTests {

    private OrganGameTests() {}

    private static ItemStackHandler fullSet(int tier) {
        ItemStackHandler organs = new ItemStackHandler(OrganData.SLOTS);
        for (int slot = 0; slot < OrganData.WING_START; slot++) {
            organs.setStackInSlot(slot, GTIAOrgans.TIERED.get(OrganType.TIERED[slot]).get(tier).asStack());
        }
        return organs;
    }

    /** Organs survive a save and load; the set tier is the lowest organ; slots only take their type. */
    @GameTest(template = "qa_empty_5x5", batch = "organs", timeoutTicks = 20)
    public static void organsSaveAndSetTier(GameTestHelper helper) {
        var player = TestPlayers.survival(helper);
        ItemStackHandler organs = fullSet(2);
        organs.setStackInSlot(0, GTIAOrgans.TIERED.get(OrganType.EYE).get(1).asStack());
        organs.setStackInSlot(OrganData.WING_START, GTIAOrgans.FAIRY_WING.asStack());
        OrganData.save(player, organs);
        ItemStackHandler loaded = OrganData.load(player);
        helper.assertTrue(OrganData.setTier(loaded) == 1, "set tier is the lowest organ");
        helper.assertTrue(OrganData.tier(loaded, OrganType.HEART) == 2, "heart tier 2 kept");
        helper.assertTrue(loaded.getStackInSlot(OrganData.WING_START).is(GTIAOrgans.FAIRY_WING.get()), "wing kept");
        helper.assertTrue(OrganData.setTier(new ItemStackHandler(OrganData.SLOTS)) == 0, "no organs: set tier 0");
        helper.assertFalse(OrganData.fits(0, GTIAOrgans.TIERED.get(OrganType.HEART).get(1).asStack()),
                "a heart does not fit the eye slot");
        helper.assertFalse(OrganData.fits(0, GTIAOrgans.FAIRY_WING.asStack()), "a wing does not fit an organ slot");
        helper.assertTrue(OrganData.fits(OrganData.WING_START + 3, GTIAOrgans.STEEL_WING.asStack()), "wing slot");
        helper.succeed();
    }

    /** A tier 4 set gives armor, speed, saturation, cleansing and flight; removing it takes them back. */
    @GameTest(template = "qa_empty_5x5", batch = "organs", timeoutTicks = 20)
    public static void organEffectsApplyAndClear(GameTestHelper helper) {
        var player = TestPlayers.survival(helper);
        double baseArmor = player.getAttributeValue(Attributes.ARMOR);
        double baseSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        player.getFoodData().setFoodLevel(5);
        OrganEffects.apply(player, fullSet(4));
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR) == baseArmor + 20, "tier 4 set: +20 armor");
        helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) > baseSpeed * 1.99, "tier 4 legs: x2");
        helper.assertFalse(player.hasEffect(MobEffects.POISON), "liver tier 2+ removes poison");
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20, "set tier 3+ keeps the player fed");
        helper.assertTrue(player.getAbilities().mayfly, "set tier 4 flies");
        OrganEffects.apply(player, new ItemStackHandler(OrganData.SLOTS));
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR) == baseArmor, "armor removed");
        helper.assertFalse(player.getAbilities().mayfly, "flight removed");
        helper.succeed();
    }

    /** Negative: a broken wing gives no flight; the Moon needs set tier 1 and the Overworld nothing. */
    @GameTest(template = "qa_empty_5x5", batch = "organs", timeoutTicks = 20)
    public static void brokenWingAndPlanetTiers(GameTestHelper helper) {
        var player = TestPlayers.survival(helper);
        ItemStackHandler organs = new ItemStackHandler(OrganData.SLOTS);
        var wing = GTIAOrgans.STEEL_WING.asStack();
        wing.setDamageValue(wing.getMaxDamage());
        organs.setStackInSlot(OrganData.WING_START, wing);
        OrganEffects.apply(player, organs);
        helper.assertFalse(player.getAbilities().mayfly, "a broken wing does not fly");
        organs.setStackInSlot(OrganData.WING_START + 1, GTIAOrgans.STEEL_WING.asStack());
        OrganEffects.apply(player, organs);
        helper.assertTrue(player.getAbilities().mayfly, "the next wing flies");
        helper.assertTrue(OrganEffects.requiredSetTier("ad_astra:moon") == 1, "Moon: tier 1");
        helper.assertTrue(OrganEffects.requiredSetTier("ad_astra:venus") == 2, "Venus: tier 2");
        helper.assertTrue(OrganEffects.requiredSetTier("minecraft:overworld") == 0, "Overworld: none");
        helper.succeed();
    }
}
