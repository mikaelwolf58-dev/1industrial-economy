package com.industrialeconomy.network.packet;
import com.industrialeconomy.client.ClientFinanceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.*;
import java.util.function.Supplier;
public class SyncFinanceS2CPacket {
    private final long cash; private final long bank; private final int patentMask;
    private final Map<String, ClientFinanceData.PricePair> prices;
    private final List<ClientFinanceData.HubLine> hubs;
    public SyncFinanceS2CPacket(long c, long b, int pMask, Map<String, ClientFinanceData.PricePair> prices, List<ClientFinanceData.HubLine> hubs) {
        this.cash = c; this.bank = b; this.patentMask = pMask; this.prices = prices; this.hubs = hubs;
    }
    public SyncFinanceS2CPacket(FriendlyByteBuf buf) {
        this.cash = buf.readLong(); this.bank = buf.readLong(); this.patentMask = buf.readInt();
        int psz = buf.readInt(); this.prices = new HashMap<>(psz);
        for (int i = 0; i != psz; i++) prices.put(buf.readUtf(), new ClientFinanceData.PricePair(buf.readLong(), buf.readLong()));
        int hsz = buf.readInt(); this.hubs = new ArrayList<>(hsz);
        for (int i = 0; i != hsz; i++) hubs.add(new ClientFinanceData.HubLine(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt()));
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(cash); buf.writeLong(bank); buf.writeInt(patentMask);
        buf.writeInt(prices.size()); prices.forEach((k, v) -> { buf.writeUtf(k); buf.writeLong(v.buy()); buf.writeLong(v.sell()); });
        buf.writeInt(hubs.size()); for (var h : hubs) { buf.writeUtf(h.type()); buf.writeInt(h.x()); buf.writeInt(h.y()); buf.writeInt(h.z()); }
    }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ClientFinanceData.update(cash, bank, patentMask);
            ClientFinanceData.PRICES.clear(); ClientFinanceData.PRICES.putAll(prices);
            ClientFinanceData.HUBS.clear(); ClientFinanceData.HUBS.addAll(hubs);
        });
        return true;
    }
}
