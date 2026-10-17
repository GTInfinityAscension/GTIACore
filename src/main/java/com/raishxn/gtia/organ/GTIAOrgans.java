package com.raishxn.gtia.organ;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.MinecraftForge;

import com.raishxn.gtia.GTIAMod;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.MenuEntry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Items, menu and player tick of the organ system (GTIA-D-279). */
public final class GTIAOrgans {

    private static final String[] TIER_LANG = { "Baby", "Standard", "Reinforced", "Tactical", "Prototype" };
    private static final Map<OrganType, String> TYPE_LANG = new EnumMap<>(Map.of(OrganType.EYE, "Eye",
            OrganType.SPINE, "Spine", OrganType.LUNG, "Lung", OrganType.LIVER, "Liver", OrganType.HEART, "Heart",
            OrganType.LEFT_ARM, "Left Arm", OrganType.RIGHT_ARM, "Right Arm", OrganType.LEFT_LEG, "Left Leg",
            OrganType.RIGHT_LEG, "Right Leg"));

    /** Tiered organs: {@code TIERED.get(type).get(tier)}. */
    public static final Map<OrganType, List<ItemEntry<OrganItem>>> TIERED = new EnumMap<>(OrganType.class);

    static {
        for (OrganType type : OrganType.TIERED) {
            List<ItemEntry<OrganItem>> tiers = new ArrayList<>();
            for (int tier = 0; tier < TIER_LANG.length; tier++) {
                int t = tier;
                tiers.add(GTIAMod.REGISTRATE.item("organ_" + type.key() + "_tier_" + tier,
                        p -> new OrganItem(p.stacksTo(1), type, t, 0))
                        .lang("Organ: " + TYPE_LANG.get(type) + " (" + TIER_LANG[tier] + ")")
                        .model((ctx, prov) -> prov.generated(ctx,
                                GTIAMod.id("item/organ/" + type.key() + "/tier" + t)))
                        .register());
            }
            TIERED.put(type, tiers);
        }
    }

    // EN: Durability is seconds of flight: 4 hours for the fairy wing, 15 minutes for the cheap steel wing (GTO).
    // PT: A durabilidade é em segundos de voo: 4 horas na asa de fada, 15 minutos na asa de aço barata (GTO).
    // ZH: 耐久即飞行秒数：妖精之翼 4 小时，廉价的钢之翼 15 分钟（同 GTO）。
    public static final ItemEntry<OrganItem> STEEL_WING = wing("steel_wing", "Steel Wing", 900, 0.1F);
    public static final ItemEntry<OrganItem> FAIRY_WING = wing("fairy_wing", "Fairy Wing", 4 * 3600, 0.1F);
    public static final ItemEntry<OrganItem> MECHANICAL_WING = GTIAMod.REGISTRATE
            .item("mechanical_wing", p -> new OrganItem(p.stacksTo(1), OrganType.WING, -1, 0.15F))
            .lang("Mechanical Wing")
            .model((ctx, prov) -> prov.generated(ctx, GTIAMod.id("item/organ/wing/mechanical_wing")))
            // EN: Attached here, not with GTItems.attach: touching GTItems this early loads it before the materials.
            // PT: Anexado aqui, não com GTItems.attach: tocar no GTItems tão cedo o carrega antes dos materiais.
            // ZH: 在此直接附加而不用 GTItems.attach：过早访问 GTItems 会在材料注册前加载它。
            .onRegister(item -> item.attachComponents(
                    ElectricStats.createElectricItem(GTValues.V[GTValues.EV] * 32 * 3600L, GTValues.EV)))
            .register();

    public static final ItemEntry<OrganModifierItem> ORGAN_MODIFIER = GTIAMod.REGISTRATE
            .item("organ_modifier", OrganModifierItem::new)
            .lang("Organ Modifier")
            .model((ctx, prov) -> prov.generated(ctx, GTIAMod.id("item/organ/organ_modifier")))
            .register();

    public static final MenuEntry<OrganMenu> MENU = GTIAMod.REGISTRATE
            .menu("organ_modifier", (MenuType<OrganMenu> type, int id,
                                     net.minecraft.world.entity.player.Inventory inventory,
                                     net.minecraft.network.FriendlyByteBuf buf) -> new OrganMenu(type, id, inventory),
                    () -> OrganScreen::new)
            .register();

    private GTIAOrgans() {}

    private static ItemEntry<OrganItem> wing(String id, String name, int seconds, float speed) {
        return GTIAMod.REGISTRATE.item(id, p -> new OrganItem(p.durability(seconds), OrganType.WING, -1, speed))
                .lang(name)
                .model((ctx, prov) -> prov.generated(ctx, GTIAMod.id("item/organ/wing/" + id)))
                .register();
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(OrganEffects::onPlayerTick);
    }
}
