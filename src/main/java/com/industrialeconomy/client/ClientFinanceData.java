package com.industrialeconomy.client;
public class ClientFinanceData {
    private static long playerCash = 0L; private static long playerBank = 0L;
    public static void set(long cash, long bank) { playerCash = cash; playerBank = bank; }
    public static long getCash() { return playerCash; }
    public static long getBank() { return playerBank; }
}
