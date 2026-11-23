package com.industrialeconomy.event;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.command.EcoCommand;
import com.industrialeconomy.economy.EconomySavedData;
import com.industrialeconomy.economy.RecipeBlocker;
import com.industrialeconomy.finance.PlayerFinanceData;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.SyncFinanceS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID)
public class FinanceEvents {
    @SubscribeEvent public static void onServerStarted(ServerStartedEvent e) { RecipeBlocker.filterCraftingRecipes(e.getServer()); }
    @SubscribeEvent public static void onDatapackSync(OnDatapackSyncEvent e) { if (e.getPlayer() == null) RecipeBlocker.filterCraftingRecipes(e.getPlayerList().getServer()); }
    @SubscribeEvent public static void onCommands(RegisterCommandsEvent event) { EcoCommand.register(event.getDispatcher()); }
    @SubscribeEvent public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide && event.level instanceof ServerLevel sl && sl.dimension() == ServerLevel.OVERWORLD) {
            if (sl.getGameTime() % 1200 == 0) EconomySavedData.get(sl).tickMarketRecovery();
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
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer p) p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> syncToClient(p, f)); }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) { if (event.getEntity() instanceof ServerPlayer p) p.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> syncToClient(p, f)); }
    @SubscribeEvent public static void onRegCap(RegisterCapabilitiesEvent event) { event.register(PlayerFinanceData.class); }
    public static void syncToClient(ServerPlayer player, PlayerFinanceData f) { ModMessages.sendToPlayer(new SyncFinanceS2CPacket(f.getCash(), f.getBankBalance()), player); }
}
