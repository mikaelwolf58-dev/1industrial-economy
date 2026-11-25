package com.industrialeconomy.client;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.client.screen.*;
import com.industrialeconomy.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            MenuScreens.register(ModMenus.BANK_MENU.get(), BankTerminalScreen::new);
            MenuScreens.register(ModMenus.SHOP_MENU.get(), ShopTerminalScreen::new);
            MenuScreens.register(ModMenus.PLOT_MENU.get(), PlotScreen::new);
            MenuScreens.register(ModMenus.COMMERCIAL_MENU.get(), CommercialScreen::new);
        });
    }
}
