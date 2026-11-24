package com.industrialeconomy.network.packet;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class UpgradeCorpC2SPacket {
    public UpgradeCorpC2SPacket() {}
    public UpgradeCorpC2SPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer sp = sup.get().getSender(); if (sp == null) return;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (f.getCorpLevel() >= 4) return;
                CorporationTier next = CorporationTier.byLevel(f.getCorpLevel() + 1);
                if (!f.spendBank(next.cost) && !f.spendCash(next.cost)) {
                    sp.sendSystemMessage(Component.literal("Not enough funds for " + next.title + "!").withStyle(ChatFormatting.RED)); return;
                }
                f.setCorpLevel(next.level); FinanceEvents.syncToClient(sp, f);
                sp.sendSystemMessage(Component.literal("Corporation upgraded to: " + next.title + "!").withStyle(ChatFormatting.GOLD));
            });
        });
        return true;
    }
}
