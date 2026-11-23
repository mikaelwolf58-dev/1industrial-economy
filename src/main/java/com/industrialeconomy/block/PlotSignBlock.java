package com.industrialeconomy.block;
import com.industrialeconomy.menu.PlotMenu;
import com.industrialeconomy.plot.*;
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
import net.minecraftforge.network.NetworkHooks;
public class PlotSignBlock extends Block {
    public PlotSignBlock(Properties p) { super(p); }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos pos, Player p, InteractionHand h, BlockHitResult hit) {
        if (!l.isClientSide && p instanceof ServerPlayer sp && l instanceof ServerLevel sl) {
            IndustrialPlot plot = PlotSavedData.get(sl).getPlotAt(pos);
            if (plot == null) { sp.sendSystemMessage(Component.literal("No plot linked to this sign.").withStyle(ChatFormatting.GRAY)); return InteractionResult.SUCCESS; }
            boolean hasOwner = plot.ownerId != null;
            boolean isMine = hasOwner && plot.ownerId.equals(sp.getUUID());
            String ownerName = hasOwner ? plot.ownerName : "None";
            NetworkHooks.openScreen(sp, new SimpleMenuProvider((id, inv, pl) -> new PlotMenu(id, inv, plot.id, plot.tier, hasOwner, isMine, ownerName), Component.literal("Plot #" + plot.id)), buf -> {
                buf.writeUtf(plot.id); buf.writeUtf(plot.tier.name()); buf.writeBoolean(hasOwner); buf.writeBoolean(isMine); buf.writeUtf(ownerName);
            });
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
