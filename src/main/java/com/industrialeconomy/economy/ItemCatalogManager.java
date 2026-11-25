package com.industrialeconomy.economy;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public class ItemCatalogManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, MarketItemEntry> ITEMS = new LinkedHashMap<>();
    public static void loadOrCreateDefault() {
        try {
            Path dir = FMLPaths.CONFIGDIR.get().resolve("industrial_economy");
            Files.createDirectories(dir);
            Path file = dir.resolve("items.json");
            boolean needWrite = !Files.exists(file);
            if (!needWrite) {
                try (FileReader r = new FileReader(file.toFile())) {
                    List<MarketItemEntry> old = GSON.fromJson(r, new TypeToken<List<MarketItemEntry>>(){}.getType());
                    if (old == null || old.stream().noneMatch(e -> "minecraft:diamond_chestplate".equals(e.itemId) && "BOUTIQUE".equals(e.category))) needWrite = true;
                }
            }
            if (needWrite) {
                List<MarketItemEntry> def = new ArrayList<>();
                ItemCatalogPart1.fill(def); ItemCatalogPart2.fill(def);
                for (MarketItemEntry m : def) {
                    if ("minecraft:calcite".equals(m.itemId) || "minecraft:dripstone_block".equals(m.itemId)) { m.baseBuyPrice = 3L; m.baseSellPrice = 1L; }
                    if ("create:crimsite".equals(m.itemId)) { m.baseBuyPrice = 8L; m.baseSellPrice = 4L; }
                    if ("create:ochrum".equals(m.itemId) || "create:veridium".equals(m.itemId)) { m.baseBuyPrice = 10L; m.baseSellPrice = 5L; }
                    if ("create:crushing_wheel".equals(m.itemId)) { m.baseBuyPrice = 9000L; m.baseSellPrice = 650L; }
                    if ("EQUIPMENT".equals(m.category) && m.patentId == 0 && m.baseBuyPrice >= 800L) m.patentId = 4;
                }
                String[] dyes = {"white_dye","red_dye","yellow_dye","blue_dye","green_dye","orange_dye","black_dye","gray_dye"};
                for (String d : dyes) def.add(new MarketItemEntry("minecraft:" + d, MarketCategory.AGRICULTURE, 14, 6, 0.22, 0));
                def.add(new MarketItemEntry("minecraft:iron_helmet", MarketCategory.BOUTIQUE, 3500, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:iron_chestplate", MarketCategory.BOUTIQUE, 6500, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:iron_leggings", MarketCategory.BOUTIQUE, 5000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:iron_boots", MarketCategory.BOUTIQUE, 3500, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:iron_sword", MarketCategory.BOUTIQUE, 4000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:iron_pickaxe", MarketCategory.BOUTIQUE, 4000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:iron_axe", MarketCategory.BOUTIQUE, 4000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:iron_shovel", MarketCategory.BOUTIQUE, 2500, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_helmet", MarketCategory.BOUTIQUE, 28000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_chestplate", MarketCategory.BOUTIQUE, 52000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_leggings", MarketCategory.BOUTIQUE, 42000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_boots", MarketCategory.BOUTIQUE, 28000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_sword", MarketCategory.BOUTIQUE, 35000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_pickaxe", MarketCategory.BOUTIQUE, 35000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_axe", MarketCategory.BOUTIQUE, 35000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:diamond_shovel", MarketCategory.BOUTIQUE, 22000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_helmet", MarketCategory.BOUTIQUE, 135000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_chestplate", MarketCategory.BOUTIQUE, 220000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_leggings", MarketCategory.BOUTIQUE, 190000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_boots", MarketCategory.BOUTIQUE, 135000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_sword", MarketCategory.BOUTIQUE, 165000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_pickaxe", MarketCategory.BOUTIQUE, 165000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_axe", MarketCategory.BOUTIQUE, 155000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_shovel", MarketCategory.BOUTIQUE, 110000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:bow", MarketCategory.BOUTIQUE, 120000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:trident", MarketCategory.BOUTIQUE, 180000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:elytra", MarketCategory.BOUTIQUE, 320000, 0, 0.0, 0));
                try (FileWriter w = new FileWriter(file.toFile())) { GSON.toJson(def, w); }
            }
            try (FileReader r = new FileReader(file.toFile())) {
                List<MarketItemEntry> list = GSON.fromJson(r, new TypeToken<List<MarketItemEntry>>(){}.getType());
                ITEMS.clear();
                if (list != null) for (MarketItemEntry e : list) ITEMS.put(e.itemId, e);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    public static MarketItemEntry getItem(String id) { return ITEMS.get(id); }
    public static Collection<MarketItemEntry> getAll() { return ITEMS.values(); }
}
