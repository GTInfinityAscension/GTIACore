package com.raishxn.gtia.organ;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.items.ItemStackHandler;

import com.raishxn.gtia.rules.GTIARules;
import com.raishxn.gtia.rules.WorldRules;

import java.util.Map;
import java.util.UUID;

/** Applies the effects of a player's organs once per second (GTIA-D-279, same effects as GTO 0.6). */
public final class OrganEffects {

    public static final int ARMOR_PER_TIER = 5;
    public static final double ARM_REACH = 4;
    private static final int PERIOD = 20;
    private static final float VANILLA_FLY_SPEED = 0.05F;
    private static final String FLIGHT_FLAG = "gtia_organ_flight";
    private static final String PLANET_HITS = "gtia_planet_hits";
    private static final UUID SPEED_ID = UUID.fromString("5b0d4b32-9a3e-4a8e-8f43-7f5a3c1d0a01");
    private static final UUID ARMOR_ID = UUID.fromString("5b0d4b32-9a3e-4a8e-8f43-7f5a3c1d0a02");
    private static final UUID TOUGHNESS_ID = UUID.fromString("5b0d4b32-9a3e-4a8e-8f43-7f5a3c1d0a03");
    private static final UUID REACH_ID = UUID.fromString("5b0d4b32-9a3e-4a8e-8f43-7f5a3c1d0a04");
    // EN: Ad Astra planet tiers. A planet of tier t needs an organ set of tier (t - 1) / 2 + 1, the GTO formula: the
    // Moon and Mars need tier 1, Venus and Mercury tier 2. Glacio is breathable, so it needs none.
    // PT: Tiers dos planetas do Ad Astra. Um planeta de tier t pede conjunto de órgãos tier (t - 1) / 2 + 1, a fórmula
    // do GTO: Lua e Marte pedem tier 1, Vênus e Mercúrio tier 2. Glacio é respirável e não pede nada.
    // ZH: Ad Astra 星球等级。等级 t 的星球需要 (t - 1) / 2 + 1 级器官套装（GTO 公式）：月球与火星需要 1 级，金星与水星需要
    // 2 级。Glacio 可呼吸，不需要器官。
    private static final Map<String, Integer> PLANET_TIERS = Map.of("ad_astra:moon", 1, "ad_astra:mars", 2,
            "ad_astra:venus", 3, "ad_astra:mercury", 3);
    private static final int PLANET_HITS_TO_DIE = 40;

    private OrganEffects() {}

    /** Movement speed bonus of a pair of legs: +10%, +30%, +60%, +100% (GTO adds 0.023 × each tier up to it). */
    public static double legSpeedBonus(int tier) {
        return 0.1 * tier * (tier + 1) / 2.0;
    }

    public static int requiredSetTier(String dimension) {
        Integer planet = PLANET_TIERS.get(dimension);
        return planet == null ? 0 : (planet - 1) / 2 + 1;
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % PERIOD != 0) return;
        apply(player, OrganData.load(player));
    }

    public static void apply(ServerPlayer player, ItemStackHandler organs) {
        int set = OrganData.setTier(organs);
        int legs = Math.min(OrganData.tier(organs, OrganType.LEFT_LEG), OrganData.tier(organs, OrganType.RIGHT_LEG));
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID, legSpeedBonus(legs),
                AttributeModifier.Operation.MULTIPLY_BASE);
        modifier(player, Attributes.ARMOR, ARMOR_ID, set * ARMOR_PER_TIER, AttributeModifier.Operation.ADDITION);
        modifier(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, set * ARMOR_PER_TIER,
                AttributeModifier.Operation.ADDITION);
        modifier(player, ForgeMod.BLOCK_REACH.get(), REACH_ID,
                OrganData.tier(organs, OrganType.RIGHT_ARM) >= 2 ? ARM_REACH : 0, AttributeModifier.Operation.ADDITION);
        if (set >= 3) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
            player.getFoodData().setExhaustion(0.0F);
        }
        if (OrganData.tier(organs, OrganType.LIVER) >= 2) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
        }
        if (OrganData.tier(organs, OrganType.LUNG) >= 2 && player.isInWater()) {
            player.setAirSupply(player.getMaxAirSupply());
        }
        updateFlight(player, organs, set);
        tickPlanet(player, set);
    }

    private static void updateFlight(ServerPlayer player, ItemStackHandler organs, int set) {
        if (player.isCreative() || player.isSpectator()) return;
        float speed = set >= 4 ? VANILLA_FLY_SPEED : 0;
        if (speed == 0) speed = useWing(player, organs);
        var abilities = player.getAbilities();
        boolean granted = player.getPersistentData().getBoolean(FLIGHT_FLAG);
        if (speed > 0) {
            if (!abilities.mayfly || abilities.getFlyingSpeed() != speed) {
                abilities.mayfly = true;
                abilities.setFlyingSpeed(speed);
                player.getPersistentData().putBoolean(FLIGHT_FLAG, true);
                player.onUpdateAbilities();
            }
        } else if (granted) {
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
            player.getPersistentData().remove(FLIGHT_FLAG);
            player.onUpdateAbilities();
        }
    }

    /** The first usable wing, worn by one second of flight; returns its flight speed, or 0 when none can fly. */
    private static float useWing(ServerPlayer player, ItemStackHandler organs) {
        boolean flying = player.getAbilities().flying;
        for (int slot = OrganData.WING_START; slot < OrganData.SLOTS; slot++) {
            ItemStack stack = organs.getStackInSlot(slot);
            if (!(stack.getItem() instanceof OrganItem wing)) continue;
            var electric = GTCapabilityHelper.getElectricItem(stack);
            if (electric != null) {
                long cost = GTValues.V[GTValues.EV] * PERIOD;
                if (electric.getCharge() < cost) continue;
                if (flying) {
                    electric.discharge(cost, electric.getTier(), true, false, false);
                    OrganData.save(player, organs);
                }
                return wing.flySpeed();
            }
            if (stack.isDamageableItem()) {
                if (stack.getDamageValue() >= stack.getMaxDamage()) continue;
                if (flying) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                    if (stack.getDamageValue() >= stack.getMaxDamage()) {
                        player.displayClientMessage(Component.translatable("gtia.organ.wing_broken")
                                .withStyle(ChatFormatting.RED), true);
                    }
                    OrganData.save(player, organs);
                }
                return wing.flySpeed();
            }
        }
        return 0;
    }

    private static void tickPlanet(ServerPlayer player, int set) {
        var data = player.getPersistentData();
        int required = requiredSetTier(player.level().dimension().location().toString());
        if (required == 0 || set >= required || !player.gameMode.isSurvival() ||
                WorldRules.get(GTIARules.ORGAN_PLANET_REQUIREMENT) == 0) {
            data.remove(PLANET_HITS);
            return;
        }
        int hits = data.getInt(PLANET_HITS) + 1;
        data.putInt(PLANET_HITS, hits);
        player.displayClientMessage(Component.translatable("gtia.organ.planet_warning", required)
                .withStyle(ChatFormatting.RED), true);
        player.hurt(player.damageSources().magic(), hits);
        if (hits >= PLANET_HITS_TO_DIE) {
            player.kill();
            data.remove(PLANET_HITS);
        }
    }

    private static void modifier(ServerPlayer player, Attribute attribute, UUID id, double amount,
                                 AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier current = instance.getModifier(id);
        if (current != null && current.getAmount() == amount) return;
        if (current != null) instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, "gtia_organs", amount, operation));
    }
}
