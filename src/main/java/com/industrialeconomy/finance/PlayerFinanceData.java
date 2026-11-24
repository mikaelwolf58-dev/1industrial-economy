package com.industrialeconomy.finance;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
public class PlayerFinanceData {
    private long cash = 0L; private long bankBalance = 0L; private int corpLevel = 0;
    private long taxDebt = 0L; private int debtStage = 0; private int debtDaysLeft = 0;
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
    public long getTaxDebt() { return taxDebt; }
    public void setTaxDebt(long v) { this.taxDebt = Math.max(0L, v); if (this.taxDebt == 0L) { this.debtStage = 0; this.debtDaysLeft = 0; } }
    public int getDebtStage() { return debtStage; }
    public void setDebtStage(int v) { this.debtStage = v; }
    public int getDebtDaysLeft() { return debtDaysLeft; }
    public void setDebtDaysLeft(int v) { this.debtDaysLeft = Math.max(0, v); }
    public boolean hasPatent(int id) { return id <= 0 || patents.contains(id); }
    public void unlockPatent(int id) { if (id > 0) patents.add(id); }
    public int getPatentMask() { int m = 0; for (int i = 1; i <= 5; i++) if (patents.contains(i)) m += (1 << i); return m; }
    public void setPatentMask(int mask) { patents.clear(); for (int i = 1; i <= 5; i++) if ((mask & (1 << i)) != 0) patents.add(i); }
    public void copyFrom(PlayerFinanceData o) {
        this.cash = o.cash; this.bankBalance = o.bankBalance; this.corpLevel = o.corpLevel;
        this.taxDebt = o.taxDebt; this.debtStage = o.debtStage; this.debtDaysLeft = o.debtDaysLeft;
        this.patents.clear(); this.patents.addAll(o.patents);
    }
    public void saveNBTData(CompoundTag t) {
        t.putLong("Cash", cash); t.putLong("Bank", bankBalance); t.putInt("Corp", corpLevel);
        t.putLong("TaxDebt", taxDebt); t.putInt("DebtStage", debtStage); t.putInt("DebtDays", debtDaysLeft); t.putInt("Patents", getPatentMask());
    }
    public void loadNBTData(CompoundTag t) {
        cash = t.getLong("Cash"); bankBalance = t.getLong("Bank"); corpLevel = t.getInt("Corp");
        taxDebt = t.getLong("TaxDebt"); debtStage = t.getInt("DebtStage"); debtDaysLeft = t.getInt("DebtDays"); setPatentMask(t.getInt("Patents"));
    }
}
