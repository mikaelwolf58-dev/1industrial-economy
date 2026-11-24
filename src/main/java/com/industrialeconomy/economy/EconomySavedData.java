package com.industrialeconomy.economy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
public class EconomySavedData extends SavedData {
    private final Map<String, Integer> stockOffset = new HashMap<>();
    public static EconomySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(EconomySavedData::load, EconomySavedData::new, "industrial_economy_market");
    }
    public static EconomySavedData load(CompoundTag tag) {
        EconomySavedData data = new EconomySavedData();
        CompoundTag offsets = tag.getCompound("Offsets");
        for (String key : offsets.getAllKeys()) data.stockOffset.put(key, offsets.getInt(key));
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        CompoundTag offsets = new CompoundTag();
        stockOffset.forEach(offsets::putInt);
        tag.put("Offsets", offsets);
        return tag;
    }
    public long getDynamicBuyPrice(String itemId) {
        MarketItemEntry e = ItemCatalogManager.getItem(itemId);
        if (e == null) return 0L;
        int off = stockOffset.getOrDefault(itemId, 0);
        double mult = Math.max(0.65, Math.min(1.65, 1.0 + (off / 256.0) * e.volatility));
        return Math.max(1L, Math.round(e.baseBuyPrice * mult));
    }
    public long getDynamicSellPrice(String itemId) {
        MarketItemEntry e = ItemCatalogManager.getItem(itemId);
        if (e == null) return 0L;
        int off = stockOffset.getOrDefault(itemId, 0);
        double mult = Math.max(0.65, Math.min(1.65, 1.0 + (off / 256.0) * e.volatility));
        return Math.max(1L, Math.round(e.baseSellPrice * mult));
    }
    public long registerSaleAndGetPayout(String itemId, int count) {
        long total = 0L;
        for (int i = 0; i < count; i++) {
            total += getDynamicSellPrice(itemId);
            stockOffset.put(itemId, stockOffset.getOrDefault(itemId, 0) - 1);
        }
        setDirty(); return total;
    }
    public long registerPurchaseAndGetCost(String itemId, int count) {
        long total = 0L;
        for (int i = 0; i < count; i++) {
            total += getDynamicBuyPrice(itemId);
            stockOffset.put(itemId, stockOffset.getOrDefault(itemId, 0) + 1);
        }
        setDirty(); return total;
    }
    public void tickMarketRecovery() {
        if (stockOffset.isEmpty()) return;
        Iterator<Map.Entry<String, Integer>> it = stockOffset.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            int v = entry.getValue();
            if (v > 0) entry.setValue(Math.max(0, v - 12));
            else if (v < 0) entry.setValue(Math.min(0, v + 12));
            if (entry.getValue() == 0) it.remove();
        }
        setDirty();
    }
}
