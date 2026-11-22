package com.industrialeconomy.registry;
import com.industrialeconomy.IndustrialEconomy;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialEconomy.MODID);
    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
