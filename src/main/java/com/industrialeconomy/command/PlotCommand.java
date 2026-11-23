package com.industrialeconomy.command;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.world.*;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.Random;
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
        SpecializedStructures.buildIndustrialZone(sl, WorldHubSavedData.get(sl), start, new Random());
        src.sendSuccess(() -> Component.literal("Created & leveled " + tier.name() + " plot!").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
