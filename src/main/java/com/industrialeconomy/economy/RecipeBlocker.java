package com.industrialeconomy.economy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
public class RecipeBlocker {
    public static final Set<String> RESTRICTED_MACHINES = Set.of(
        "create:mechanical_press", "create:mechanical_mixer", "create:millstone",
        "create:crushing_wheel", "create:mechanical_saw", "create:mechanical_drill",
        "create:mechanical_crafter", "create:encased_fan", "create:spout",
        "create:deployer", "create:steam_engine"
    );
    public static void filterCraftingRecipes(MinecraftServer server) {
        RecipeManager rm = server.getRecipeManager();
        List<Recipe<?>> kept = new ArrayList<>();
        for (Recipe<?> r : rm.getRecipes()) {
            if (r.getType() == RecipeType.CRAFTING) {
                ResourceLocation outId = ForgeRegistries.ITEMS.getKey(r.getResultItem(server.registryAccess()).getItem());
                if (outId != null && RESTRICTED_MACHINES.contains(outId.toString())) continue;
            }
            kept.add(r);
        }
        rm.replaceRecipes(kept);
    }
}
