package com.industrialeconomy.event;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.command.*;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.finance.*;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.SyncFinanceS2CPacket;
import com.industrialeconomy.plot.InspectionManager;
import com.industrialeconomy.registry.ModItems;
import com.industrialeconomy.world.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID)
public class FinanceEvents {
    @SubscribeEvent public static void onServerStarted(ServerStartedEvent e) { RecipeBlocker.filterCraftingRecipes(e.getServer()); }
    @SubscribeEvent public static void onDatapackSync(OnDatapackSyncEvent e) { if (e.getPlayer() == null) RecipeBlocker.filterCraftingRecipes(e.getPlayerList().getServer()); }
    @SubscribeEvent public static void onCommands(RegisterCommandsEvent event) { EcoCommand.register(event.getDispatcher()); PlotCommand.register(event.getDispatcher()); }
    @SubscribeEvent public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide && event.level instanceof ServerLevel sl && sl.dimension() == ServerLevel.OVERWORLD) {
            long gt = sl.getGameTime();
            if (gt % 1200 == 0) EconomySavedData.get(sl).tickMarketRecovery();
            if (gt % 400 == 0) InspectionManager.tickInspection(sl);
            if (gt % 100 == 0) WorldInfrastructureGenerator.tickWorldGen(sl);
            if (gt > 0 && gt % 168000 == 0) {
                for (ServerPlayer sp : sl.players()) sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                    if (f.getBankBalance() > 0) {
                        long interest = Math.round(f.getBankBalance() * 0.02);
                        f.addBank(interest); syncToClient(sp, f);
                        sp.sendSystemMessage(Component.literal("Weekly Bank Interest (+2%): +" + String.format("%,d$", interest)).withStyle(ChatFormatting.GOLD));
                    }
                });
            }
        }
    }
    @SubscribeEvent public static void onAttach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player && !event.getObject().getCapability(PlayerFinanceProvider.PLAYER_FINANCE).isPresent()) {
            event.addCapability(new ResourceLocation(IndustrialEconomy.MODID, "finance"), new PlayerFinanceProvider());
        }
    }
    @SubscribeEvent public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                long lost = f.getCash();
                if (lost > 0) { f.setCash(0L); player.sendSystemMessage(Component.literal("You died and lost all cash: " + String.format("%,d$", lost)).withStyle(ChatFormatting.RED)); syncToClient(player, f); }
            });
        }
    }
    @SubscribeEvent public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(o -> event.getEntity().getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(n -> { n.copyFrom(o); n.setCash(0L); }));
            event.getOriginal().invalidateCaps();
        }
    }
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> syncToClient(p, f));
            if (!p.getTags().contains("ie_got_paper")) { p.addTag("ie_got_paper"); p.getInventory().add(new ItemStack(ModItems.NEWSPAPER.get())); }
        }
    }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) { if (event.getEntity() instanceof ServerPlayer p) p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> syncToClient(p, f)); }
    @SubscribeEvent public static void onRegCap(RegisterCapabilitiesEvent event) { event.register(PlayerFinanceData.class); }
    public static void syncToClient(ServerPlayer player, PlayerFinanceData f) {
        ServerLevel sl = player.serverLevel();
        EconomySavedData eco = EconomySavedData.get(sl);
        Map<String, ClientFinanceData.PricePair> prices = new HashMap<>();
        for (MarketItemEntry e : ItemCatalogManager.getAll()) prices.put(e.itemId, new ClientFinanceData.PricePair(eco.getDynamicBuyPrice(e.itemId), eco.getDynamicSellPrice(e.itemId)));
        List<ClientFinanceData.HubLine> hubs = new ArrayList<>();
        for (var h : WorldHubSavedData.get(sl).getHubs()) hubs.add(new ClientFinanceData.HubLine(h.type(), h.x(), h.y(), h.z()));
        ModMessages.sendToPlayer(new SyncFinanceS2CPacket(f.getCash(), f.getBankBalance(), f.getPatentMask(), f.getCorpLevel(), prices, hubs), player);
    }
}
