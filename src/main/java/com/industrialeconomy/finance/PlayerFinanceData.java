package com.industrialeconomy.finance;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
public class PlayerFinanceData {
    private long cash = 0L; private long bankBalance = 0L; private int corpLevel = 0;
    private final Set<Integer> patents = new HashSet<>();
    public long getCash() { return cash; }
    public void setCash(long v) { this.cash = Math.max(0L, v); }
    public void addCash(long v) { if (v > 0) this.cash += v; }
    public boolean spendCash(long v) { if (v <= 0) return true; if (cash < v) return false; cash -= v; return true; }
    public long getBankBalance() { return bankBalance; }
    public void setBankBalance(long v) { this.bankBalance = v; }
    public void addBank(long v) { if (v > 0) this.bankBalance += v; }
    public boolean spendBank(long v) { if (v <= 0) return true; if (bankBalance < v) return false; bankBalance -= v; return true; }
    public boolean depositToBank(long v) { if (v <= 0 || cash < v) return false; cash -= v; bankBalance += v; return true; }
    public boolean withdrawFromBank(long v) { if (v <= 0 || bankBalance < v) return false; bankBalance -= v; cash += v; return true; }
    public int getCorpLevel() { return corpLevel; }
    public void setCorpLevel(int v) { this.corpLevel = Math.max(0, Math.min(4, v)); }
    public CorporationTier getCorpTier() { return CorporationTier.byLevel(corpLevel); }
    public boolean hasPatent(int id) { return id <= 0 || patents.contains(id); }
    public void unlockPatent(int id) { if (id > 0) patents.add(id); }
    public int getPatentMask() { int m = 0; if (patents.contains(1)) m += 2; if (patents.contains(2)) m += 4; if (patents.contains(3)) m += 8; return m; }
    public void setPatentMask(int mask) { patents.clear(); if ((mask & 2) != 0) patents.add(1); if ((mask & 4) != 0) patents.add(2); if ((mask & 8) != 0) patents.add(3); }
    public void copyFrom(PlayerFinanceData o) { this.cash = o.cash; this.bankBalance = o.bankBalance; this.corpLevel = o.corpLevel; this.patents.clear(); this.patents.addAll(o.patents); }
    public void saveNBTData(CompoundTag t) { t.putLong("Cash", cash); t.putLong("Bank", bankBalance); t.putInt("Corp", corpLevel); t.putInt("Patents", getPatentMask()); }
    public void loadNBTData(CompoundTag t) { cash = t.getLong("Cash"); bankBalance = t.getLong("Bank"); corpLevel = t.getInt("Corp"); setPatentMask(t.getInt("Patents")); }
}
