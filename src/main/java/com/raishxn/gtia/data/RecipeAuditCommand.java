package com.raishxn.gtia.data;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code /gtia audit recipes}: lists every loaded recipe (any mod) with an item input that resolves to nothing, the
 * empty EMI slot the author found by hand. Writes {@code gtia-recipe-audit.txt} in the server directory.
 */
public final class RecipeAuditCommand {

    private RecipeAuditCommand() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gtia").requires(s -> s.hasPermission(2))
                .then(Commands.literal("audit").then(Commands.literal("recipes").executes(ctx -> {
                    int broken = audit(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal("GTIA recipe audit: " + broken +
                            " recipes with empty inputs, see gtia-recipe-audit.txt"), true);
                    return broken;
                }))));
    }

    public static int audit(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        Map<String, Integer> perMod = new TreeMap<>();
        for (var recipe : server.getRecipeManager().getRecipes()) {
            List<Ingredient> inputs = new ArrayList<>();
            if (recipe instanceof GTRecipe gt) {
                for (var content : gt.getInputContents(ItemRecipeCapability.CAP))
                    inputs.add(ItemRecipeCapability.CAP.of(content.content));
            } else {
                inputs.addAll(recipe.getIngredients());
            }
            for (int i = 0; i < inputs.size(); i++) {
                var ingredient = inputs.get(i);
                // EN: Ingredient.EMPTY is a legal blank cell in shaped crafting, not a broken input.
                // PT: Ingredient.EMPTY é célula vazia legítima em receita moldada, não entrada quebrada.
                // ZH: Ingredient.EMPTY 是有序合成中合法的空格，不算损坏的输入。
                if (ingredient == Ingredient.EMPTY) continue;
                boolean empty = true;
                for (var stack : ingredient.getItems())
                    if (!stack.isEmpty() && !stack.is(Items.BARRIER)) empty = false;
                if (!empty) continue;
                lines.add(recipe.getId() + " slot " + i + " " + describe(ingredient));
                perMod.merge(recipe.getId().getNamespace(), 1, Integer::sum);
                break;
            }
        }
        List<String> out = new ArrayList<>();
        // EN: Also counts GTIA's integration recipes, so a missing optional mod shows up as 0 here.
        // PT: Também conta as receitas de integração do GTIA; um mod opcional faltando aparece como 0 aqui.
        // ZH: 同时统计 GTIA 的联动配方；缺少可选模组时此处显示为 0。
        for (String group : new String[] { "agro/", "bees/" }) {
            long n = server.getRecipeManager().getRecipes().stream()
                    .filter(r -> "gtia".equals(r.getId().getNamespace()) && r.getId().getPath().contains(group))
                    .count();
            out.add("= gtia " + group + " " + n);
        }
        perMod.forEach((mod, n) -> out.add("# " + mod + ": " + n));
        out.addAll(lines);
        try {
            Files.write(server.getServerDirectory().toPath().resolve("gtia-recipe-audit.txt"), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines.size();
    }

    private static String describe(Ingredient ingredient) {
        try {
            return ingredient.toJson().toString();
        } catch (RuntimeException e) {
            return ingredient.getClass().getSimpleName();
        }
    }
}
