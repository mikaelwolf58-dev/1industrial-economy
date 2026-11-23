package com.industrialeconomy.economy;
public class MarketItemEntry {
    public String itemId;
    public String category;
    public long baseBuyPrice;
    public long baseSellPrice;
    public double volatility;
    public MarketItemEntry(String id, MarketCategory cat, long buy, long sell, double vol) {
        this.itemId = id; this.category = cat.name(); this.baseBuyPrice = buy; this.baseSellPrice = sell; this.volatility = vol;
    }
}
