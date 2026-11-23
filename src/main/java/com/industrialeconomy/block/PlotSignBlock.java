package com.industrialeconomy.block;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public class PlotSignBlock extends Block {
    public PlotSignBlock(Properties p) { super(p); }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos pos, Player p, InteractionHand h, BlockHitResult hit) {
        if (!l.isClientSide && p instanceof ServerPlayer sp && l instanceof ServerLevel sl) {
            PlotSavedData data = PlotSavedData.get(sl);
            IndustrialPlot plot = data.getPlotAt(pos);
            if (plot == null) { sp.sendSystemMessage(Component.literal("No plot linked to this sign.").withStyle(ChatFormatting.GRAY)); return InteractionResult.SUCCESS; }
            if (plot.ownerId != null) {
                sp.sendSystemMessage(Component.literal("Plot [" + plot.tier.name() + " " + plot.tier.size + "x" + plot.tier.size + "] | Owner: " + plot.ownerName).withStyle(ChatFormatting.AQUA));
                return InteractionResult.SUCCESS;
            }
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!f.spendCash(plot.tier.price)) {
                    sp.sendSystemMessage(Component.literal("Plot FOR SALE (" + plot.tier.size + "x" + plot.tier.size + ") | Price: " + String.format("%,d$", plot.tier.price) + " (Not enough cash!)").withStyle(ChatFormatting.YELLOW));
                } else {
                    plot.ownerId = sp.getUUID(); plot.ownerName = sp.getName().getString();
                    data.setDirty(); FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Congratulations! You bought plot " + plot.tier.size + "x" + plot.tier.size + " for " + String.format("%,d$", plot.tier.price)).withStyle(ChatFormatting.GREEN));
                }
            });
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
