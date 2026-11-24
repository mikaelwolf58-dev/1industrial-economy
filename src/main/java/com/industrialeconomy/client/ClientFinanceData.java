package com.industrialeconomy.client;
import java.util.*;
public class ClientFinanceData {
    private static long cash = 0L; private static long bankBalance = 0L; private static int patentMask = 0;
    public record PricePair(long buy, long sell) {}
    public record HubLine(String type, int x, int y, int z) {}
    public static final Map<String, PricePair> PRICES = new HashMap<>();
    public static final List<HubLine> HUBS = new ArrayList<>();
    public static void update(long c, long b, int pMask) { cash = c; bankBalance = b; patentMask = pMask; }
    public static long getCash() { return cash; }
    public static long getBankBalance() { return bankBalance; }
    public static boolean hasPatent(int id) { return id <= 0 || (patentMask & (1 << id)) != 0; }
    public static long getBuy(String id, long def) { PricePair p = PRICES.get(id); return p != null ? p.buy() : def; }
    public static long getSell(String id, long def) { PricePair p = PRICES.get(id); return p != null ? p.sell() : def; }
}
