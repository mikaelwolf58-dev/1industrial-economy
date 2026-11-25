package com.industrialeconomy.client;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.client.screen.CommercialScreen;
import com.industrialeconomy.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> MenuScreens.register(ModMenuTypes.COMMERCIAL_MENU.get(), CommercialScreen::new));
    }
}
