package com.industrialeconomy.network.packet;
import com.industrialeconomy.economy.IndustryPatent;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class BuyPatentC2SPacket {
    private final int patentId;
    public BuyPatentC2SPacket(int patentId) { this.patentId = patentId; }
    public BuyPatentC2SPacket(FriendlyByteBuf buf) { this.patentId = buf.readInt(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeInt(patentId); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer sp = sup.get().getSender();
            if (sp == null) return;
            IndustryPatent pat = IndustryPatent.byId(patentId);
            if (pat == IndustryPatent.NONE) return;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (f.hasPatent(pat.id)) return;
                if (!f.spendBank(pat.price) && !f.spendCash(pat.price)) {
                    sp.sendSystemMessage(Component.literal("Not enough funds to buy patent!").withStyle(ChatFormatting.RED)); return;
                }
                f.unlockPatent(pat.id); FinanceEvents.syncToClient(sp, f);
                sp.sendSystemMessage(Component.literal("Patent unlocked: " + pat.title + "!").withStyle(ChatFormatting.GREEN));
            });
        });
        return true;
    }
}
