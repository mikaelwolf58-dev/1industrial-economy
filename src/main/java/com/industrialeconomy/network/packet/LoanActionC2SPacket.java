package com.industrialeconomy.network.packet;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.plot.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class LoanActionC2SPacket {
    private final boolean isTake; private final long amount;
    public LoanActionC2SPacket(boolean isTake, long amount) { this.isTake = isTake; this.amount = amount; }
    public LoanActionC2SPacket(FriendlyByteBuf buf) { this.isTake = buf.readBoolean(); this.amount = buf.readLong(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeBoolean(isTake); buf.writeLong(amount); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer sp = sup.get().getSender(); if (sp == null || amount <= 0L) return;
            ServerLevel sl = sp.serverLevel(); PlotSavedData data = PlotSavedData.get(sl);
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (isTake) {
                    if (f.getTaxDebt() > 0L || f.getLoanDebt() > 0L) {
                        sp.sendSystemMessage(Component.literal("Pay off existing tax or loan debt first!").withStyle(ChatFormatting.RED)); return;
                    }
                    long collateral = 0L;
                    for (IndustrialPlot p : data.getAll()) if (sp.getUUID().equals(p.ownerId)) collateral += p.tier.price;
                    long maxLoan = collateral / 2L;
                    if (maxLoan <= 0L) { sp.sendSystemMessage(Component.literal("You need owned plots as collateral!").withStyle(ChatFormatting.RED)); return; }
                    long take = Math.min(amount, maxLoan);
                    long totalRepay = Math.round(take * 1.10);
                    f.addBank(take); f.setLoanDebt(totalRepay); f.setLoanDaysLeft(14);
                    FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Loan approved: +" + String.format("%,d$", take) + " (Repay " + String.format("%,d$", totalRepay) + " in 14 days)").withStyle(ChatFormatting.GREEN));
                } else {
                    long debt = f.getLoanDebt(); if (debt <= 0L) return;
                    long pay = Math.min(Math.min(amount, debt), f.getBankBalance());
                    if (pay <= 0L) { sp.sendSystemMessage(Component.literal("Deposit money to your bank account first!").withStyle(ChatFormatting.RED)); return; }
                    f.spendBank(pay); f.setLoanDebt(debt - pay);
                    FinanceEvents.syncToClient(sp, f);
                    if (f.getLoanDebt() <= 0L) sp.sendSystemMessage(Component.literal("Bank loan fully repaid!").withStyle(ChatFormatting.GREEN));
                    else sp.sendSystemMessage(Component.literal("Loan payment: -" + String.format("%,d$", pay) + ". Remaining: " + String.format("%,d$", f.getLoanDebt())).withStyle(ChatFormatting.YELLOW));
                }
            });
        });
        return true;
    }
}
