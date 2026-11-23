package com.industrialeconomy.registry;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.item.BusinessNewspaperItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialEconomy.MODID);
    public static final RegistryObject<Item> NEWSPAPER = ITEMS.register("business_newspaper", () -> new BusinessNewspaperItem(new Item.Properties().stacksTo(1)));
    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
