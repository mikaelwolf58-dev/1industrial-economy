package com.industrialeconomy.network.packet;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.plot.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PlotActionC2SPacket {
    private final String plotId; private final boolean isSell;
    public PlotActionC2SPacket(String plotId, boolean isSell) { this.plotId = plotId; this.isSell = isSell; }
    public PlotActionC2SPacket(FriendlyByteBuf buf) { this.plotId = buf.readUtf(); this.isSell = buf.readBoolean(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(plotId); buf.writeBoolean(isSell); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer p = sup.get().getSender(); if (p == null) return;
            ServerLevel sl = p.serverLevel(); PlotSavedData data = PlotSavedData.get(sl);
            String clean = plotId.replace("Plot #", "").replace("#", "").trim();
            IndustrialPlot plot = data.getPlotById(plotId);
            if (plot == null) plot = data.getPlotById(clean);
            if (plot == null) for (IndustrialPlot ip : data.getAll()) if (ip.id.equalsIgnoreCase(clean)) { plot = ip; break; }
            if (plot == null) return;
            final IndustrialPlot target = plot;
            p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!isSell && target.ownerId == null) {
                    long owned = data.getAll().stream().filter(pl -> p.getUUID().equals(pl.ownerId)).count();
                    if (owned >= f.getCorpTier().maxPlots) {
                        p.sendSystemMessage(Component.literal("Plot limit reached (" + f.getCorpTier().maxPlots + ")! Upgrade Corporation at ATM.").withStyle(ChatFormatting.RED)); return;
                    }
                    long cost = target.tier.price;
                    if (!f.spendBank(cost) && !f.spendCash(cost)) {
                        p.sendSystemMessage(Component.literal("Not enough funds (" + String.format("%,d$", cost) + ")!").withStyle(ChatFormatting.RED)); return;
                    }
                    target.ownerId = p.getUUID(); target.ownerName = p.getGameProfile().getName(); data.setDirty();
                    FinanceEvents.syncToClient(p, f);
                    p.sendSystemMessage(Component.literal("Purchased Plot #" + target.id + "!").withStyle(ChatFormatting.GREEN));
                } else if (isSell && p.getUUID().equals(target.ownerId)) {
                    long refund = target.tier.price / 2L;
                    for (int x = target.minX + 1; x <= target.maxX - 1; x++) for (int z = target.minZ + 1; z <= target.maxZ - 1; z++) {
                        for (int y = target.baseY + 1; y <= target.baseY + 25; y++) sl.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                        sl.setBlock(new BlockPos(x, target.baseY, z), Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), 3);
                    }
                    target.ownerId = null; target.ownerName = null; data.setDirty();
                    f.addBank(refund); FinanceEvents.syncToClient(p, f);
                    p.sendSystemMessage(Component.literal("Sold Plot #" + target.id + " for 50% (" + String.format("%,d$", refund) + ")").withStyle(ChatFormatting.YELLOW));
                }
            });
        });
        return true;
    }
}
