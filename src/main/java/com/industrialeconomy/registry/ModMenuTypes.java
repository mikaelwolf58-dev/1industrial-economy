package com.industrialeconomy.registry;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.menu.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, IndustrialEconomy.MODID);
    public static final RegistryObject<MenuType<BankTerminalMenu>> BANK_TERMINAL_MENU = MENUS.register("bank_terminal_menu", () -> IForgeMenuType.create(BankTerminalMenu::new));
    public static final RegistryObject<MenuType<ShopTerminalMenu>> SHOP_TERMINAL_MENU = MENUS.register("shop_terminal_menu", () -> IForgeMenuType.create(ShopTerminalMenu::new));
    public static void register(IEventBus bus) { MENUS.register(bus); }
}
