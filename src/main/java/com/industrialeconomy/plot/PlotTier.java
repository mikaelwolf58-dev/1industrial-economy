package com.industrialeconomy.plot;
public enum PlotTier {
    MICRO(10, 10000L, 4, 1),
    SMALL(16, 25600L, 8, 1),
    MEDIUM(32, 102400L, 32, 4),
    LARGE(64, 409600L, 999, 999);
    public final int size; public final long price; public final int maxMachinesPerType; public final int maxSteamEngines;
    PlotTier(int size, long price, int maxPerType, int maxSteam) {
        this.size = size; this.price = price; this.maxMachinesPerType = maxPerType; this.maxSteamEngines = maxSteam;
    }
}
