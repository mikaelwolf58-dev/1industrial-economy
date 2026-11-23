package com.industrialeconomy.network;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }
    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(IndustrialEconomy.MODID, "messages")).networkProtocolVersion(() -> "1.0").clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true).simpleChannel();
        INSTANCE = net;
        net.messageBuilder(SyncFinanceS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT).decoder(SyncFinanceS2CPacket::new).encoder(SyncFinanceS2CPacket::toBytes).consumerMainThread(SyncFinanceS2CPacket::handle).add();
        net.messageBuilder(BankTransactionC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(BankTransactionC2SPacket::new).encoder(BankTransactionC2SPacket::toBytes).consumerMainThread(BankTransactionC2SPacket::handle).add();
        net.messageBuilder(BuyItemC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(BuyItemC2SPacket::new).encoder(BuyItemC2SPacket::toBytes).consumerMainThread(BuyItemC2SPacket::handle).add();
    }
    public static <MSG> void sendToServer(MSG msg) { INSTANCE.sendToServer(msg); }
    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer p) { INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), msg); }
}
