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
public class PlotActionC2SPacket {
    private final String plotId; private final boolean isBuy;
    public PlotActionC2SPacket(String plotId, boolean isBuy) { this.plotId = plotId; this.isBuy = isBuy; }
    public PlotActionC2SPacket(FriendlyByteBuf buf) { this.plotId = buf.readUtf(); this.isBuy = buf.readBoolean(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(plotId); buf.writeBoolean(isBuy); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer p = sup.get().getSender(); if (p == null) return;
            ServerLevel sl = p.serverLevel(); PlotSavedData data = PlotSavedData.get(sl);
            IndustrialPlot plot = data.getPlotById(plotId); if (plot == null) return;
            p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (isBuy && plot.ownerId == null) {
                    long owned = data.getAll().stream().filter(pl -> p.getUUID().equals(pl.ownerId)).count();
                    if (owned >= f.getCorpTier().maxPlots) {
                        p.sendSystemMessage(Component.literal("Plot limit reached (" + f.getCorpTier().maxPlots + ")! Upgrade Corporation at ATM.").withStyle(ChatFormatting.RED)); return;
                    }
                    long cost = plot.tier.price;
                    if (!f.spendBank(cost) && !f.spendCash(cost)) {
                        p.sendSystemMessage(Component.literal("Not enough funds!").withStyle(ChatFormatting.RED)); return;
                    }
                    plot.ownerId = p.getUUID(); plot.ownerName = p.getGameProfile().getName(); data.setDirty();
                    FinanceEvents.syncToClient(p, f);
                    p.sendSystemMessage(Component.literal("Purchased Plot #" + plot.id + "!").withStyle(ChatFormatting.GREEN));
                } else if (!isBuy && p.getUUID().equals(plot.ownerId)) {
                    long refund = Math.round(plot.tier.price * 0.70);
                    plot.ownerId = null; plot.ownerName = null; data.setDirty();
                    f.addBank(refund); FinanceEvents.syncToClient(p, f);
                    p.sendSystemMessage(Component.literal("Sold Plot #" + plot.id + " for " + String.format("%,d$", refund)).withStyle(ChatFormatting.YELLOW));
                }
            });
        });
        return true;
    }
}
