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
            boolean needWrite = !Files.exists(file);
            if (!needWrite) {
                try (FileReader r = new FileReader(file.toFile())) {
                    List<MarketItemEntry> old = GSON.fromJson(r, new TypeToken<List<MarketItemEntry>>(){}.getType());
                    if (old == null || old.stream().noneMatch(e -> "create:metal_girder".equals(e.itemId))) needWrite = true;
                }
            }
            if (needWrite) {
                List<MarketItemEntry> def = new ArrayList<>();
                ItemCatalogPart1.fill(def);
                ItemCatalogPart2.fill(def);
                String[] dyes = {"white_dye","red_dye","yellow_dye","blue_dye","green_dye","orange_dye","black_dye","gray_dye"};
                for (String d : dyes) def.add(new MarketItemEntry("minecraft:" + d, MarketCategory.AGRICULTURE, 14, 6, 0.22, 0));
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
}
