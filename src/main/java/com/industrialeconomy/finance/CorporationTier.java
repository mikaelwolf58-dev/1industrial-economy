package com.industrialeconomy.finance;
public enum CorporationTier {
    CRAFTSMAN(0, "Private Master", 0L, 1, 0.00),
    SMALL_BIZ(1, "Small Enterprise", 80000L, 2, 0.08),
    HOLDING(2, "Industrial Holding", 350000L, 4, 0.18),
    TYCOON(3, "Industrial Tycoon", 1500000L, 8, 0.30),
    MONOPOLIST(4, "Monopolist", 5000000L, 999, 0.45);
    public final int level; public final String title; public final long cost; public final int maxPlots; public final double sellBonus;
    CorporationTier(int lvl, String title, long cost, int plots, double bonus) {
        this.level = lvl; this.title = title; this.cost = cost; this.maxPlots = plots; this.sellBonus = bonus;
    }
    public static CorporationTier byLevel(int lvl) {
        for (CorporationTier t : values()) if (t.level == lvl) return t;
        return CRAFTSMAN;
    }
}
