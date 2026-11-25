package com.industrialeconomy.command;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.plot.TaxBailiffManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
public class EcoCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("eco").requires(s -> s.hasPermission(2))
            .then(Commands.literal("add").then(Commands.argument("target", EntityArgument.player()).then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(c -> {
                ServerPlayer p = EntityArgument.getPlayer(c, "target"); long a = LongArgumentType.getLong(c, "amount");
                p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> { f.addCash(a); FinanceEvents.syncToClient(p, f); });
                c.getSource().sendSuccess(() -> Component.literal("Added " + String.format("%,d$", a)), true); return 1;
            }))))
            .then(Commands.literal("nextday").executes(c -> {
                ServerLevel sl = c.getSource().getServer().overworld();
                sl.setDayTime(sl.getDayTime() + 24000L);
                TaxBailiffManager.tickDaily(sl);
                if ((sl.getDayTime() / 24000L) % 7L == 0L) FinanceEvents.runWeeklyCycle(sl);
                for (ServerPlayer sp : sl.players()) sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> FinanceEvents.syncToClient(sp, f));
                c.getSource().sendSuccess(() -> Component.literal("[Test] +1 Day (Day " + (sl.getDayTime() / 24000L) + ")."), true); return 1;
            }))
            .then(Commands.literal("nextweek").executes(c -> {
                ServerLevel sl = c.getSource().getServer().overworld();
                sl.setDayTime(sl.getDayTime() + 168000L);
                FinanceEvents.runWeeklyCycle(sl);
                for (ServerPlayer sp : sl.players()) sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> FinanceEvents.syncToClient(sp, f));
                c.getSource().sendSuccess(() -> Component.literal("[Test] +1 Week (Day " + (sl.getDayTime() / 24000L) + ")."), true); return 1;
            }))
        );
    }
}
