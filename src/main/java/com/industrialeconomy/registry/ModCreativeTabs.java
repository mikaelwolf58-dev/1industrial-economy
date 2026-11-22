package com.industrialeconomy.registry;
import com.industrialeconomy.IndustrialEconomy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IndustrialEconomy.MODID);
    public static final RegistryObject<CreativeModeTab> INDUSTRIAL_TAB = TABS.register("industrial_tab", () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.BANK_TERMINAL.get())).title(Component.literal("Industrial Economy")).displayItems((p, o) -> o.accept(ModBlocks.BANK_TERMINAL.get())).build());
    public static void register(IEventBus bus) { TABS.register(bus); }
}
