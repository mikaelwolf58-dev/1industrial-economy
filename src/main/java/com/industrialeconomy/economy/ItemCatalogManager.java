package com.industrialeconomy.economy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
public class ItemCatalogManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, MarketItemEntry> ITEMS = new LinkedHashMap<>();
    public static void loadOrCreateDefault() {
        try {
            Path dir = FMLPaths.CONFIGDIR.get().resolve("industrial_economy");
            Files.createDirectories(dir);
            Path file = dir.resolve("items.json");
            if (!Files.exists(file)) {
                List<MarketItemEntry> def = buildDefaultCatalog();
                try (FileWriter w = new FileWriter(file.toFile())) { GSON.toJson(def, w); }
            }
            try (FileReader r = new FileReader(file.toFile())) {
                List<MarketItemEntry> list = GSON.fromJson(r, new TypeToken<List<MarketItemEntry>>(){}.getType());
                ITEMS.clear();
                if (list != null) for (MarketItemEntry e : list) if (e.itemId != null) ITEMS.put(e.itemId, e);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    public static MarketItemEntry getItem(String id) { return ITEMS.get(id); }
    public static Collection<MarketItemEntry> getAll() { return ITEMS.values(); }
    private static List<MarketItemEntry> buildDefaultCatalog() {
        List<MarketItemEntry> l = new ArrayList<>();
        l.add(new MarketItemEntry("minecraft:coal", MarketCategory.FUEL, 25, 14, 0.15));
        l.add(new MarketItemEntry("minecraft:charcoal", MarketCategory.FUEL, 12, 6, 0.20));
        l.add(new MarketItemEntry("minecraft:iron_ingot", MarketCategory.METALS, 60, 35, 0.15));
        l.add(new MarketItemEntry("minecraft:copper_ingot", MarketCategory.METALS, 40, 22, 0.15));
        l.add(new MarketItemEntry("minecraft:gold_ingot", MarketCategory.METALS, 140, 85, 0.15));
        l.add(new MarketItemEntry("create:zinc_ingot", MarketCategory.METALS, 65, 38, 0.15));
        l.add(new MarketItemEntry("create:brass_ingot", MarketCategory.METALS, 180, 110, 0.15));
        l.add(new MarketItemEntry("create:andesite_alloy", MarketCategory.COMPONENTS, 30, 16, 0.15));
        l.add(new MarketItemEntry("create:mechanical_press", MarketCategory.EQUIPMENT, 1200, 500, 0.05));
        l.add(new MarketItemEntry("create:mechanical_mixer", MarketCategory.EQUIPMENT, 1400, 600, 0.05));
        l.add(new MarketItemEntry("create:millstone", MarketCategory.EQUIPMENT, 800, 350, 0.05));
        l.add(new MarketItemEntry("create:crushing_wheel", MarketCategory.EQUIPMENT, 2800, 1100, 0.05));
        l.add(new MarketItemEntry("create:mechanical_saw", MarketCategory.EQUIPMENT, 1100, 450, 0.05));
        l.add(new MarketItemEntry("create:mechanical_drill", MarketCategory.EQUIPMENT, 1100, 450, 0.05));
        l.add(new MarketItemEntry("create:mechanical_crafter", MarketCategory.EQUIPMENT, 1600, 700, 0.05));
        l.add(new MarketItemEntry("create:encased_fan", MarketCategory.EQUIPMENT, 950, 400, 0.05));
        l.add(new MarketItemEntry("create:spout", MarketCategory.EQUIPMENT, 1300, 550, 0.05));
        l.add(new MarketItemEntry("create:deployer", MarketCategory.EQUIPMENT, 1750, 750, 0.05));
        l.add(new MarketItemEntry("create:steam_engine", MarketCategory.EQUIPMENT, 4500, 1800, 0.05));
        l.add(new MarketItemEntry("minecraft:stone_bricks", MarketCategory.BUILDING, 10, 4, 0.10));
        l.add(new MarketItemEntry("minecraft:glass", MarketCategory.BUILDING, 14, 6, 0.10));
        l.add(new MarketItemEntry("minecraft:oak_log", MarketCategory.WOOD, 16, 8, 0.15));
        l.add(new MarketItemEntry("minecraft:wheat", MarketCategory.AGRICULTURE, 12, 6, 0.20));
        l.add(new MarketItemEntry("minecraft:bread", MarketCategory.FOOD, 24, 12, 0.15));
        l.add(new MarketItemEntry("minecraft:diamond", MarketCategory.RARE, 900, 550, 0.10));
        return l;
    }
}
