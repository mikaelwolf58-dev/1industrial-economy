package com.industrialeconomy.client;
import java.util.*;
public class ClientFinanceData {
    private static long cash = 0L, bankBalance = 0L, taxDebt = 0L, weeklyTax = 0L, weeklyIncome = 0L, dayTime = 0L;
    private static int patentMask = 0, corpLevel = 0;
    public record PricePair(long buy, long sell, long baseBuy, long baseSell) {}
    public record HubLine(String type, int x, int y, int z) {}
    public static final Map<String, PricePair> PRICES = new HashMap<>();
    public static final List<HubLine> HUBS = new ArrayList<>();
    public static String trackedHub = ""; public static int trackX = 0, trackZ = 0;
    public static void update(long c, long b, int pm, int corp, long td, long wt, long wi, long dt) {
        cash = c; bankBalance = b; patentMask = pm; corpLevel = corp; taxDebt = td; weeklyTax = wt; weeklyIncome = wi; dayTime = dt;
    }
    public static long getCash() { return cash; }
    public static long getBankBalance() { return bankBalance; }
    public static int getCorpLevel() { return corpLevel; }
    public static long getTaxDebt() { return taxDebt; }
    public static long getWeeklyTax() { return weeklyTax; }
    public static long getWeeklyIncome() { return weeklyIncome; }
    public static long getDayTime() { return dayTime; }
    public static boolean hasPatent(int id) { return id <= 0 || (patentMask & (1 << id)) != 0; }
    public static long getBuy(String id, long def) { PricePair p = PRICES.get(id); return p != null ? p.buy() : def; }
    public static long getSell(String id, long def) { PricePair p = PRICES.get(id); return p != null ? p.sell() : def; }
}
