package com.industrialeconomy.client;
import java.util.*;
public class ClientFinanceData {
    private static long cash = 0L; private static long bankBalance = 0L; private static int patentMask = 0; private static int corpLevel = 0;
    private static long taxDebt = 0L; private static long loanDebt = 0L; private static int loanDays = 0;
    public record PricePair(long buy, long sell) {}
    public record HubLine(String type, int x, int y, int z) {}
    public static final Map<String, PricePair> PRICES = new HashMap<>();
    public static final List<HubLine> HUBS = new ArrayList<>();
    public static void update(long c, long b, int pMask, int corp, long tDebt, long lDebt, int lDays) {
        cash = c; bankBalance = b; patentMask = pMask; corpLevel = corp; taxDebt = tDebt; loanDebt = lDebt; loanDays = lDays;
    }
    public static long getCash() { return cash; }
    public static long getBankBalance() { return bankBalance; }
    public static int getCorpLevel() { return corpLevel; }
    public static long getTaxDebt() { return taxDebt; }
    public static long getLoanDebt() { return loanDebt; }
    public static int getLoanDays() { return loanDays; }
    public static boolean hasPatent(int id) { return id <= 0 || (patentMask & (1 << id)) != 0; }
    public static long getBuy(String id, long def) { PricePair p = PRICES.get(id); return p != null ? p.buy() : def; }
    public static long getSell(String id, long def) { PricePair p = PRICES.get(id); return p != null ? p.sell() : def; }
}
