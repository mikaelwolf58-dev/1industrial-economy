package com.industrialeconomy.command;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.registry.ModBlocks;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
public class PlotCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("plot").requires(s -> s.hasPermission(2))
            .then(Commands.literal("create")
                .then(Commands.literal("micro").executes(c -> create(c.getSource(), PlotTier.MICRO)))
                .then(Commands.literal("small").executes(c -> create(c.getSource(), PlotTier.SMALL)))
                .then(Commands.literal("medium").executes(c -> create(c.getSource(), PlotTier.MEDIUM)))
                .then(Commands.literal("large").executes(c -> create(c.getSource(), PlotTier.LARGE)))
            )
            .then(Commands.literal("info").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                IndustrialPlot plot = PlotSavedData.get(p.serverLevel()).getPlotAt(p.blockPosition());
                if (plot == null) c.getSource().sendSuccess(() -> Component.literal("Wilderness (Max 2 machines/type, 0 Steam Engines)"), false);
                else c.getSource().sendSuccess(() -> Component.literal("Plot #" + plot.id + " [" + plot.tier.name() + "] | Owner: " + (plot.ownerName == null ? "FOR SALE" : plot.ownerName)), false);
                return 1;
            }))
        );
    }
    private static int create(CommandSourceStack src, PlotTier tier) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException(); ServerLevel sl = sp.serverLevel();
        BlockPos start = sp.blockPosition().offset(2, 0, 2);
        PlotSavedData data = PlotSavedData.get(sl);
        String id = String.format("%03d", data.nextNumber());
        IndustrialPlot plot = new IndustrialPlot(id, tier, start.getX(), start.getZ(), start.getY());
        data.addPlot(plot);
        for (int i = 0; i != tier.size; i++) {
            sl.setBlock(new BlockPos(plot.minX + i, plot.baseY, plot.minZ), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            sl.setBlock(new BlockPos(plot.minX + i, plot.baseY, plot.maxZ), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            sl.setBlock(new BlockPos(plot.minX, plot.baseY, plot.minZ + i), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            sl.setBlock(new BlockPos(plot.maxX, plot.baseY, plot.minZ + i), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
        }
        sl.setBlock(new BlockPos(plot.minX, plot.baseY + 1, plot.minZ), ModBlocks.PLOT_SIGN.get().defaultBlockState(), 3);
        src.sendSuccess(() -> Component.literal("Created Plot #" + id + " (" + tier.size + "x" + tier.size + ") for " + String.format("%,d$", tier.price)).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
