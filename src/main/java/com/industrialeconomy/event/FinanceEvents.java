package com.industrialeconomy.event;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.finance.PlayerFinanceData;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.SyncFinanceS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID)
public class FinanceEvents {
    @SubscribeEvent public static void onAttach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player && !event.getObject().getCapability(PlayerFinanceProvider.PLAYER_FINANCE).isPresent()) {
            event.addCapability(new ResourceLocation(IndustrialEconomy.MODID, "finance"), new PlayerFinanceProvider());
        }
    }
    @SubscribeEvent public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                long lost = f.getCash();
                if (lost > 0) { f.setCash(0L); player.sendSystemMessage(Component.translatable("msg.industrial_economy.lost_cash", String.format("%,d$", lost))); syncToClient(player, f); }
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
