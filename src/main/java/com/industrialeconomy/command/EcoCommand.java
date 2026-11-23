package com.industrialeconomy.command;
import com.industrialeconomy.economy.EconomySavedData;
import com.industrialeconomy.economy.ItemCatalogManager;
import com.industrialeconomy.economy.MarketItemEntry;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
public class EcoCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("eco").requires(s -> s.hasPermission(2))
            .then(Commands.literal("add").then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                long amt = LongArgumentType.getLong(c, "amount");
                p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> { f.addCash(amt); FinanceEvents.syncToClient(p, f); });
                c.getSource().sendSuccess(() -> Component.literal("Added cash: " + amt + "$"), false);
                return 1;
            })))
            .then(Commands.literal("info").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                ItemStack held = p.getMainHandItem();
                String id = ForgeRegistries.ITEMS.getKey(held.getItem()).toString();
                MarketItemEntry e = ItemCatalogManager.getItem(id);
                if (e == null) { c.getSource().sendSuccess(() -> Component.literal("Item not in catalog: " + id), false); }
                else {
                    EconomySavedData eco = EconomySavedData.get(p.serverLevel());
                    c.getSource().sendSuccess(() -> Component.literal("Item: " + id + " [" + e.category + "] | Buy: " + eco.getDynamicBuyPrice(id) + "$ | Sell: " + eco.getDynamicSellPrice(id) + "$"), false);
                }
                return 1;
            }))
        );
    }
}
