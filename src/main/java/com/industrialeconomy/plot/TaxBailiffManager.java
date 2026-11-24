package com.industrialeconomy.plot;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;
public class TaxBailiffManager {
    public static void assessWeeklyTaxes(ServerLevel sl) {
        PlotSavedData data = PlotSavedData.get(sl);
        for (ServerPlayer sp : sl.players()) {
            long tax = 0L;
            for (IndustrialPlot p : data.getAll()) if (sp.getUUID().equals(p.ownerId)) tax += p.getWeeklyTax();
            if (tax <= 0L) continue;
            final long totalTax = tax;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (f.getDebtStage() == 0 && f.getBankBalance() >= totalTax) {
                    f.spendBank(totalTax); FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Weekly Property Tax paid: -" + String.format("%,d$", totalTax)).withStyle(ChatFormatting.YELLOW));
                } else {
                    f.setTaxDebt(f.getTaxDebt() + totalTax);
                    if (f.getDebtStage() == 0) { f.setDebtStage(1); f.setDebtDaysLeft(7); }
                    FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Insufficient bank funds for property tax (" + String.format("%,d$", f.getTaxDebt()) + ")! Pay at ATM within 7 days.").withStyle(ChatFormatting.GOLD));
                }
            });
        }
    }
    public static void tickDaily(ServerLevel sl) {
        PlotSavedData data = PlotSavedData.get(sl);
        for (ServerPlayer sp : sl.players()) {
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (f.getTaxDebt() <= 0L) return;
                if (f.getDebtStage() == 1) {
                    int left = f.getDebtDaysLeft() - 1;
                    if (left > 0) {
                        f.setDebtDaysLeft(left); FinanceEvents.syncToClient(sp, f);
                        sp.sendSystemMessage(Component.literal("Tax reminder: " + String.format("%,d$", f.getTaxDebt()) + " due in " + left + " day(s).").withStyle(ChatFormatting.GOLD));
                    } else {
                        long seized = Math.min(f.getBankBalance(), f.getTaxDebt());
                        if (seized > 0L) { f.spendBank(seized); f.setTaxDebt(f.getTaxDebt() - seized); }
                        if (f.getTaxDebt() <= 0L) {
                            FinanceEvents.syncToClient(sp, f);
                            sp.sendSystemMessage(Component.literal("Tax debt forcibly settled from bank balance!").withStyle(ChatFormatting.YELLOW));
                        } else {
                            f.setDebtStage(2); f.setDebtDaysLeft(3); FinanceEvents.syncToClient(sp, f);
                            sp.sendSystemMessage(Component.literal("FINAL NOTICE: All bank funds (" + String.format("%,d$", seized) + ") forcibly debited! Remaining debt: " + String.format("%,d$", f.getTaxDebt()) + ". Pay within 3 days or your plot will be seized!").withStyle(ChatFormatting.RED));
                        }
                    }
                } else if (f.getDebtStage() == 2) {
                    int left = f.getDebtDaysLeft() - 1;
                    if (left > 0) {
                        f.setDebtDaysLeft(left); FinanceEvents.syncToClient(sp, f);
                        sp.sendSystemMessage(Component.literal("URGENT: Tax debt " + String.format("%,d$", f.getTaxDebt()) + "! Bailiffs arrive in " + left + " day(s).").withStyle(ChatFormatting.RED));
                    } else executeBailiffSeizure(sl, sp, data, f);
                }
            });
        }
    }
    private static void executeBailiffSeizure(ServerLevel sl, ServerPlayer sp, PlotSavedData data, PlayerFinanceData f) {
        List<IndustrialPlot> owned = new ArrayList<>();
        for (IndustrialPlot p : data.getAll()) if (sp.getUUID().equals(p.ownerId)) owned.add(p);
        owned.sort(Comparator.comparingInt(p -> p.tier.size));
        if (owned.size() > 1) {
            IndustrialPlot sm = owned.get(0);
            long credit = Math.round(sm.tier.price * 0.30);
            clearPlotStructures(sl, sm);
            sm.ownerId = null; sm.ownerName = null; data.setDirty();
            f.setTaxDebt(Math.max(0L, f.getTaxDebt() - credit));
            if (f.getTaxDebt() > 0L) {
                f.setDebtDaysLeft(3);
                sp.sendSystemMessage(Component.literal("BAILIFFS: Plot #" + sm.id + " (" + sm.tier.size + "x" + sm.tier.size + ") seized for debt! Remaining debt: " + String.format("%,d$", f.getTaxDebt()) + ". Pay within 3 days or next plot/tier will be seized!").withStyle(ChatFormatting.RED));
            } else sp.sendSystemMessage(Component.literal("BAILIFFS: Plot #" + sm.id + " (" + sm.tier.size + "x" + sm.tier.size + ") seized! Debt settled.").withStyle(ChatFormatting.RED));
        } else if (f.getCorpLevel() > 0) {
            f.setCorpLevel(f.getCorpLevel() - 1); f.setTaxDebt(0L);
            sp.sendSystemMessage(Component.literal("BAILIFFS: Corporation level reduced by 1 tier to settle overdue tax debt!").withStyle(ChatFormatting.RED));
        } else {
            if (!owned.isEmpty()) { IndustrialPlot last = owned.get(0); clearPlotStructures(sl, last); last.ownerId = null; last.ownerName = null; data.setDirty(); }
            f.setTaxDebt(0L);
            sp.sendSystemMessage(Component.literal("BAILIFFS: Your last plot was seized to settle overdue tax debt!").withStyle(ChatFormatting.RED));
        }
        FinanceEvents.syncToClient(sp, f);
    }
    private static void clearPlotStructures(ServerLevel sl, IndustrialPlot p) {
        for (int x = p.minX + 1; x <= p.maxX - 1; x++) for (int z = p.minZ + 1; z <= p.maxZ - 1; z++) {
            for (int y = p.baseY + 1; y <= p.baseY + 25; y++) sl.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            sl.setBlock(new BlockPos(x, p.baseY, z), Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), 3);
        }
    }
}
