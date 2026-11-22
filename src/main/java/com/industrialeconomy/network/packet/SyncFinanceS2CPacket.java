package com.industrialeconomy.network.packet;
import com.industrialeconomy.client.ClientFinanceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class SyncFinanceS2CPacket {
    private final long cash; private final long bankBalance;
    public SyncFinanceS2CPacket(long cash, long bankBalance) { this.cash = cash; this.bankBalance = bankBalance; }
    public SyncFinanceS2CPacket(FriendlyByteBuf buf) { this.cash = buf.readLong(); this.bankBalance = buf.readLong(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeLong(cash); buf.writeLong(bankBalance); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) { sup.get().enqueueWork(() -> ClientFinanceData.set(cash, bankBalance)); return true; }
}
