package com.industrialeconomy.event;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.command.*;
import com.industrialeconomy.commercial.*;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.finance.*;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.SyncFinanceS2CPacket;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.registry.ModItems;
import com.industrialeconomy.world.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
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
            if (gt > 0 && gt % 24000 == 0) TaxBailiffManager.tickDaily(sl);
            if (gt > 0 && gt % 168000 == 0) runWeeklyCycle(sl);
        }
    }
    public static void runWeeklyCycle(ServerLevel sl) {
        TaxBailiffManager.assessWeeklyTaxes(sl);
        for (ServerPlayer sp : sl.players()) sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
            if (f.getBankBalance() > 0) { long i = Math.round(f.getBankBalance() * 0.02); f.addBank(i); syncToClient(sp, f); sp.sendSystemMessage(Component.literal("Weekly Bank Interest (+2%): +" + String.format("%,d$", i)).withStyle(ChatFormatting.GOLD)); }
        });
    }
    @SubscribeEvent public static void onAttach(AttachCapabilitiesEvent<Entity> e) { if (e.getObject() instanceof Player && !e.getObject().getCapability(PlayerFinanceProvider.PLAYER_FINANCE).isPresent()) e.addCapability(new ResourceLocation(IndustrialEconomy.MODID, "finance"), new PlayerFinanceProvider()); }
    @SubscribeEvent public static void onDeath(LivingDeathEvent e) { if (e.getEntity() instanceof ServerPlayer p) p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> { long l = f.getCash(); if (l > 0) { f.setCash(0L); p.sendSystemMessage(Component.literal("You died and lost all cash: " + String.format("%,d$", l)).withStyle(ChatFormatting.RED)); syncToClient(p, f); } }); }
    @SubscribeEvent public static void onClone(PlayerEvent.Clone e) { if (e.isWasDeath()) { e.getOriginal().reviveCaps(); e.getOriginal().getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(o -> e.getEntity().getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(n -> { n.copyFrom(o); n.setCash(0L); })); e.getOriginal().invalidateCaps(); } }
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) { if (e.getEntity() instanceof ServerPlayer p) { p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> syncToClient(p, f)); if (!p.getTags().contains("ie_got_paper")) { p.addTag("ie_got_paper"); p.getInventory().add(new ItemStack(ModItems.NEWSPAPER.get())); } } }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) { if (e.getEntity() instanceof ServerPlayer p) p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> syncToClient(p, f)); }
    @SubscribeEvent public static void onRegCap(RegisterCapabilitiesEvent e) { e.register(PlayerFinanceData.class); }
    public static void syncToClient(ServerPlayer player, PlayerFinanceData f) {
        ServerLevel sl = player.serverLevel(); EconomySavedData eco = EconomySavedData.get(sl);
        Map<String, ClientFinanceData.PricePair> prices = new HashMap<>();
        for (MarketItemEntry e : ItemCatalogManager.getAll()) prices.put(e.itemId, new ClientFinanceData.PricePair(eco.getDynamicBuyPrice(e.itemId), eco.getDynamicSellPrice(e.itemId), e.baseBuyPrice, e.baseSellPrice));
        List<ClientFinanceData.HubLine> hubs = new ArrayList<>();
        for (var h : WorldHubSavedData.get(sl).getHubs()) hubs.add(new ClientFinanceData.HubLine(h.type(), h.x(), h.y(), h.z()));
        long wTax = 0L; for (IndustrialPlot p : PlotSavedData.get(sl).getAll()) if (player.getUUID().equals(p.ownerId)) wTax += p.getWeeklyTax();
        long wInc = 0L; for (CommercialPoint cp : CommercialSavedData.get(sl).getAll()) if (player.getUUID().equals(cp.ownerId)) wInc += cp.getWeeklyPassiveIncome();
        ModMessages.sendToPlayer(new SyncFinanceS2CPacket(f.getCash(), f.getBankBalance(), f.getPatentMask(), f.getCorpLevel(), f.getTaxDebt(), wTax, wInc, sl.getDayTime(), prices, hubs), player);
    }
}
