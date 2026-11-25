package com.industrialeconomy.network.packet;
import com.industrialeconomy.client.ClientFinanceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.*;
import java.util.function.Supplier;
public class SyncFinanceS2CPacket {
    private final long cash, bank, taxDebt, weeklyTax, weeklyIncome, dayTime; private final int patentMask, corpLevel;
    private final Map<String, ClientFinanceData.PricePair> prices; private final List<ClientFinanceData.HubLine> hubs;
    public SyncFinanceS2CPacket(long c, long b, int pm, int corp, long td, long wt, long wi, long dt, Map<String, ClientFinanceData.PricePair> pr, List<ClientFinanceData.HubLine> hb) {
        this.cash = c; this.bank = b; this.patentMask = pm; this.corpLevel = corp; this.taxDebt = td; this.weeklyTax = wt; this.weeklyIncome = wi; this.dayTime = dt; this.prices = pr; this.hubs = hb;
    }
    public SyncFinanceS2CPacket(FriendlyByteBuf buf) {
        this.cash = buf.readLong(); this.bank = buf.readLong(); this.patentMask = buf.readInt(); this.corpLevel = buf.readInt();
        this.taxDebt = buf.readLong(); this.weeklyTax = buf.readLong(); this.weeklyIncome = buf.readLong(); this.dayTime = buf.readLong();
        int psz = buf.readInt(); this.prices = new HashMap<>(psz);
        for (int i = 0; i != psz; i++) prices.put(buf.readUtf(), new ClientFinanceData.PricePair(buf.readLong(), buf.readLong(), buf.readLong(), buf.readLong()));
        int hsz = buf.readInt(); this.hubs = new ArrayList<>(hsz);
        for (int i = 0; i != hsz; i++) hubs.add(new ClientFinanceData.HubLine(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt()));
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(cash); buf.writeLong(bank); buf.writeInt(patentMask); buf.writeInt(corpLevel);
        buf.writeLong(taxDebt); buf.writeLong(weeklyTax); buf.writeLong(weeklyIncome); buf.writeLong(dayTime);
        buf.writeInt(prices.size()); prices.forEach((k, v) -> { buf.writeUtf(k); buf.writeLong(v.buy()); buf.writeLong(v.sell()); buf.writeLong(v.baseBuy()); buf.writeLong(v.baseSell()); });
        buf.writeInt(hubs.size()); for (var h : hubs) { buf.writeUtf(h.type()); buf.writeInt(h.x()); buf.writeInt(h.y()); buf.writeInt(h.z()); }
    }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ClientFinanceData.update(cash, bank, patentMask, corpLevel, taxDebt, weeklyTax, weeklyIncome, dayTime);
            ClientFinanceData.PRICES.clear(); ClientFinanceData.PRICES.putAll(prices);
            ClientFinanceData.HUBS.clear(); ClientFinanceData.HUBS.addAll(hubs);
        });
        return true;
    }
}
