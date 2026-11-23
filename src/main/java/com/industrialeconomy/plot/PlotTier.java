package com.industrialeconomy.plot;
public enum PlotTier {
    SMALL(16, 10000L, 8),
    MEDIUM(32, 35000L, 24),
    LARGE(64, 90000L, 999);
    public final int size; public final long price; public final int maxMachines;
    PlotTier(int size, long price, int maxMachines) { this.size = size; this.price = price; this.maxMachines = maxMachines; }
}
