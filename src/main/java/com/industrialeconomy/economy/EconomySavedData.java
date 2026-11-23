package com.industrialeconomy.economy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
public class EconomySavedData extends SavedData {
    private final Map<String, Double> supplyPressure = new HashMap<>();
    public static EconomySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(EconomySavedData::load, EconomySavedData::new, "industrial_economy_market");
    }
    public static EconomySavedData load(CompoundTag tag) {
        EconomySavedData data = new EconomySavedData();
        CompoundTag mapTag = tag.getCompound("Supply");
        for (String key : mapTag.getAllKeys()) data.supplyPressure.put(key, mapTag.getDouble(key));
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        CompoundTag mapTag = new CompoundTag();
        supplyPressure.forEach(mapTag::putDouble);
        tag.put("Supply", mapTag);
        return tag;
    }
    public void recordItemSold(String itemId, int count) {
        double cur = supplyPressure.getOrDefault(itemId, 0.0);
        supplyPressure.put(itemId, Math.min(0.60, cur + (count * 0.0005)));
        setDirty();
    }
    public void tickMarketRecovery() {
        supplyPressure.replaceAll((k, v) -> Math.max(-0.40, v - 0.01));
        setDirty();
    }
    public long getDynamicSellPrice(String itemId) {
        MarketItemEntry e = ItemCatalogManager.getItem(itemId);
        if (e == null) return 0L;
        double p = supplyPressure.getOrDefault(itemId, 0.0);
        return Math.max(1L, Math.round(e.baseSellPrice * (1.0 - p)));
    }
    public long getDynamicBuyPrice(String itemId) {
        MarketItemEntry e = ItemCatalogManager.getItem(itemId);
        if (e == null) return 0L;
        double p = supplyPressure.getOrDefault(itemId, 0.0);
        return Math.max(2L, Math.round(e.baseBuyPrice * (1.0 - (p * 0.5))));
    }
}
