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
                    if (old == null || old.stream().noneMatch(e -> "BOUTIQUE".equals(e.category) && e.baseBuyPrice >= 450000L)) needWrite = true;
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
                def.add(new MarketItemEntry("minecraft:netherite_helmet", MarketCategory.BOUTIQUE, 195000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_chestplate", MarketCategory.BOUTIQUE, 312000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_leggings", MarketCategory.BOUTIQUE, 273000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_boots", MarketCategory.BOUTIQUE, 195000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_sword", MarketCategory.BOUTIQUE, 234000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_pickaxe", MarketCategory.BOUTIQUE, 234000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_axe", MarketCategory.BOUTIQUE, 221000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:netherite_shovel", MarketCategory.BOUTIQUE, 156000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:bow", MarketCategory.BOUTIQUE, 169000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:trident", MarketCategory.BOUTIQUE, 260000, 0, 0.0, 0));
                def.add(new MarketItemEntry("minecraft:elytra", MarketCategory.BOUTIQUE, 455000, 0, 0.0, 0));
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
