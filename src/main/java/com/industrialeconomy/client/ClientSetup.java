package com.industrialeconomy.client;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.client.screen.*;
import com.industrialeconomy.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent public static void onClientSetup(FMLClientSetupEvent event) { event.enqueueWork(() -> {
        MenuScreens.register(ModMenuTypes.BANK_TERMINAL_MENU.get(), BankTerminalScreen::new);
        MenuScreens.register(ModMenuTypes.SHOP_TERMINAL_MENU.get(), ShopTerminalScreen::new);
    }); }
}
