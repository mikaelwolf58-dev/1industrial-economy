package com.industrialeconomy.network.packet;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PayTaxC2SPacket {
    public PayTaxC2SPacket() {}
    public PayTaxC2SPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer sp = sup.get().getSender(); if (sp == null) return;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                long debt = f.getTaxDebt(); if (debt <= 0L) return;
                long pay = Math.min(f.getBankBalance(), debt);
                if (pay <= 0L) { sp.sendSystemMessage(Component.literal("Deposit money to your bank account first!").withStyle(ChatFormatting.RED)); return; }
                f.spendBank(pay); f.setTaxDebt(debt - pay);
                FinanceEvents.syncToClient(sp, f);
                if (f.getTaxDebt() <= 0L) sp.sendSystemMessage(Component.literal("Tax debt fully paid (" + String.format("%,d$", pay) + ")!").withStyle(ChatFormatting.GREEN));
                else sp.sendSystemMessage(Component.literal("Partial tax paid: -" + String.format("%,d$", pay) + ". Remaining: " + String.format("%,d$", f.getTaxDebt())).withStyle(ChatFormatting.YELLOW));
            });
        });
        return true;
    }
}
