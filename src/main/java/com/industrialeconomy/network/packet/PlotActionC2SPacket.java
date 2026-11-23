package com.industrialeconomy.network.packet;
import com.industrialeconomy.block.PlotSignBlock;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.plot.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PlotActionC2SPacket {
    private final String plotId; private final boolean isSell;
    public PlotActionC2SPacket(String plotId, boolean isSell) { this.plotId = plotId; this.isSell = isSell; }
    public PlotActionC2SPacket(FriendlyByteBuf buf) { this.plotId = buf.readUtf(); this.isSell = buf.readBoolean(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(plotId); buf.writeBoolean(isSell); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            ServerLevel sl = sp.serverLevel();
            PlotSavedData data = PlotSavedData.get(sl);
            IndustrialPlot plot = data.getPlotById(plotId);
            if (plot == null) return;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!isSell) {
                    if (plot.ownerId != null) return;
                    if (!f.spendCash(plot.tier.price)) { sp.sendSystemMessage(Component.literal("Not enough cash!").withStyle(ChatFormatting.RED)); return; }
                    plot.ownerId = sp.getUUID(); plot.ownerName = sp.getName().getString();
                    data.setDirty(); FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Purchased Plot #" + plot.id + " (" + plot.tier.size + "x" + plot.tier.size + ")!").withStyle(ChatFormatting.GREEN));
                } else {
                    if (plot.ownerId == null || !plot.ownerId.equals(sp.getUUID())) return;
                    clearPlotArea(sl, plot);
                    long refund = plot.tier.price / 2L;
                    plot.ownerId = null; plot.ownerName = null;
                    f.addCash(refund); data.setDirty(); FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Sold Plot #" + plot.id + " for +" + String.format("%,d$", refund) + ". All items inside cleared.").withStyle(ChatFormatting.YELLOW));
                }
            });
        });
        return true;
    }
    private static void clearPlotArea(ServerLevel sl, IndustrialPlot plot) {
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(plot.minX, plot.baseY + 1, plot.minZ), new BlockPos(plot.maxX, plot.baseY + 32, plot.maxZ))) {
            if (!(sl.getBlockState(p).getBlock() instanceof PlotSignBlock)) sl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
        AABB box = new AABB(plot.minX, plot.baseY, plot.minZ, plot.maxX + 1, plot.baseY + 33, plot.maxZ + 1);
        for (ItemEntity item : sl.getEntitiesOfClass(ItemEntity.class, box)) item.discard();
    }
}
