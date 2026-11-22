package com.industrialeconomy.network.packet;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class BankTransactionC2SPacket {
    private final boolean isDeposit; private final long amount;
    public BankTransactionC2SPacket(boolean isDeposit, long amount) { this.isDeposit = isDeposit; this.amount = amount; }
    public BankTransactionC2SPacket(FriendlyByteBuf buf) { this.isDeposit = buf.readBoolean(); this.amount = buf.readLong(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeBoolean(isDeposit); buf.writeLong(amount); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer p = ctx.getSender();
            if (p == null || amount <= 0) return;
            p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (isDeposit ? f.depositToBank(amount) : f.withdrawFromBank(amount)) FinanceEvents.syncToClient(p, f);
            });
        });
        return true;
    }
}
