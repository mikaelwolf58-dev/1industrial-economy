package com.industrialeconomy.item;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.world.WorldHubSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.*;
public class BusinessNewspaperItem extends Item {
    public BusinessNewspaperItem(Properties p) { super(p); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            EconomySavedData eco = EconomySavedData.get(sl);
            sp.sendSystemMessage(Component.literal("=== THE INDUSTRIAL TIMES ===").withStyle(ChatFormatting.GOLD));
            List<MarketItemEntry> all = new ArrayList<>(ItemCatalogManager.getAll());
            all.sort((a, b) -> Double.compare((double) eco.getDynamicSellPrice(b.itemId) / b.baseSellPrice, (double) eco.getDynamicSellPrice(a.itemId) / a.baseSellPrice));
            sp.sendSystemMessage(Component.literal("Top Sell Prices:").withStyle(ChatFormatting.GREEN));
            for (int i = 0; i != Math.min(3, all.size()); i++) {
                MarketItemEntry e = all.get(i);
                String name = e.itemId.substring(e.itemId.indexOf(":") + 1);
                sp.sendSystemMessage(Component.literal(" - " + name + ": " + eco.getDynamicSellPrice(e.itemId) + "$ (base " + e.baseSellPrice + "$)").withStyle(ChatFormatting.WHITE));
            }
            sp.sendSystemMessage(Component.literal("Nearby Infrastructure:").withStyle(ChatFormatting.AQUA));
            var hubs = WorldHubSavedData.get(sl).getHubs();
            if (hubs.isEmpty()) sp.sendSystemMessage(Component.literal(" - Explore new chunks to discover Gas Stations & Trade Hubs!").withStyle(ChatFormatting.GRAY));
            else {
                hubs.stream().sorted(Comparator.comparingDouble(h -> Math.hypot(h.x() - sp.getX(), h.z() - sp.getZ()))).limit(4).forEach(h -> {
                    int dist = (int) Math.hypot(h.x() - sp.getX(), h.z() - sp.getZ());
                    sp.sendSystemMessage(Component.literal(" - [" + h.type() + "] X:" + h.x() + " Y:" + h.y() + " Z:" + h.z() + " (" + dist + "m)").withStyle(ChatFormatting.YELLOW));
                });
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
