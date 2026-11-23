package com.industrialeconomy.plot;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.block.PlotSignBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID)
public class PlotProtectionEvents {
    private static boolean canModify(ServerPlayer sp, ServerLevel sl, BlockPos pos) {
        if (sp.isCreative()) return true;
        IndustrialPlot plot = PlotSavedData.get(sl).getPlotAt(pos);
        if (plot == null) return true;
        return plot.ownerId != null && plot.ownerId.equals(sp.getUUID());
    }
    @SubscribeEvent public static void onBreak(BlockEvent.BreakEvent e) {
        if (e.getPlayer() instanceof ServerPlayer sp && e.getLevel() instanceof ServerLevel sl && !canModify(sp, sl, e.getPos())) {
            e.setCanceled(true); sp.displayClientMessage(Component.literal("Private or unpurchased plot!").withStyle(ChatFormatting.RED), true);
        }
    }
    @SubscribeEvent public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && e.getLevel() instanceof ServerLevel sl && !canModify(sp, sl, e.getPos())) {
            e.setCanceled(true); sp.displayClientMessage(Component.literal("Private or unpurchased plot!").withStyle(ChatFormatting.RED), true);
        }
    }
    @SubscribeEvent public static void onInteract(PlayerInteractEvent.RightClickBlock e) {
        if (e.getEntity() instanceof ServerPlayer sp && e.getLevel() instanceof ServerLevel sl) {
            if (sl.getBlockState(e.getPos()).getBlock() instanceof PlotSignBlock) return;
            if (!canModify(sp, sl, e.getPos())) {
                e.setCanceled(true); sp.displayClientMessage(Component.literal("Access denied: Buy plot first or owned by another player!").withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
