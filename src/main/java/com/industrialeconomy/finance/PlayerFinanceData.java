package com.industrialeconomy.finance;
import net.minecraft.nbt.CompoundTag;
public class PlayerFinanceData {
    private long cash = 0L;
    private long bankBalance = 0L;
    public long getCash() { return cash; }
    public void setCash(long cash) { this.cash = Math.max(0L, cash); }
    public void addCash(long amount) { if (amount > 0) this.cash += amount; }
    public boolean spendCash(long amount) { if (amount <= 0 || this.cash < amount) return false; this.cash -= amount; return true; }
    public long getBankBalance() { return bankBalance; }
    public void setBankBalance(long bankBalance) { this.bankBalance = Math.max(0L, bankBalance); }
    public boolean depositToBank(long amount) { if (amount <= 0 || this.cash < amount) return false; this.cash -= amount; this.bankBalance += amount; return true; }
    public boolean withdrawFromBank(long amount) { if (amount <= 0 || this.bankBalance < amount) return false; this.bankBalance -= amount; this.cash += amount; return true; }
    public void copyFrom(PlayerFinanceData source) { this.cash = source.cash; this.bankBalance = source.bankBalance; }
    public void saveNBTData(CompoundTag nbt) { nbt.putLong("Cash", cash); nbt.putLong("BankBalance", bankBalance); }
    public void loadNBTData(CompoundTag nbt) { this.cash = nbt.getLong("Cash"); this.bankBalance = nbt.getLong("BankBalance"); }
}
