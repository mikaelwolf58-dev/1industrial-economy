package com.industrialeconomy;
import com.industrialeconomy.economy.ItemCatalogManager;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.registry.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod(IndustrialEconomy.MODID)
public class IndustrialEconomy {
    public static final String MODID = "industrial_economy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public IndustrialEconomy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(bus); ModItems.register(bus); ModBlockEntities.register(bus); ModMenuTypes.register(bus); ModCreativeTabs.register(bus);
        bus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> { ModMessages.register(); ItemCatalogManager.loadOrCreateDefault(); });
    }
}
