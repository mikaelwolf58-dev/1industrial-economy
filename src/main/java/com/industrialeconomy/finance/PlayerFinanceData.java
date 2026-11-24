package com.industrialeconomy.finance;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
public class PlayerFinanceData {
    private long cash = 0L; private long bankBalance = 0L;
    private final Set<Integer> patents = new HashSet<>();
    public long getCash() { return cash; }
    public void setCash(long v) { this.cash = Math.max(0L, v); }
    public void addCash(long v) { if (v > 0) this.cash += v; }
    public boolean spendCash(long v) { if (v <= 0) return true; if (cash < v) return false; cash -= v; return true; }
    public long getBankBalance() { return bankBalance; }
    public void setBankBalance(long v) { this.bankBalance = v; }
    public void addBank(long v) { if (v > 0) this.bankBalance += v; }
    public boolean spendBank(long v) { if (v <= 0) return true; if (bankBalance < v) return false; bankBalance -= v; return true; }
    public boolean hasPatent(int id) { return id <= 0 || patents.contains(id); }
    public void unlockPatent(int id) { if (id > 0) patents.add(id); }
    public Set<Integer> getPatents() { return patents; }
    public int getPatentMask() { int m = 0; for (int id : patents) m |= (1 << id); return m; }
    public void setPatentMask(int mask) { patents.clear(); for (int i = 1; i <= 5; i++) if ((mask & (1 << i)) != 0) patents.add(i); }
    public void copyFrom(PlayerFinanceData o) { this.cash = o.cash; this.bankBalance = o.bankBalance; this.patents.clear(); this.patents.addAll(o.patents); }
    public void saveNBTData(CompoundTag t) { t.putLong("Cash", cash); t.putLong("Bank", bankBalance); t.putInt("Patents", getPatentMask()); }
    public void loadNBTData(CompoundTag t) { cash = t.getLong("Cash"); bankBalance = t.getLong("Bank"); setPatentMask(t.getInt("Patents")); }
}
