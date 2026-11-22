package com.industrialeconomy.registry;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.block.BankTerminalBlock;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import java.util.function.Supplier;
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustrialEconomy.MODID);
    public static final RegistryObject<Block> BANK_TERMINAL = registerBlock("bank_terminal", () -> new BankTerminalBlock(BlockBehaviour.Properties.of().strength(3.5f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) { RegistryObject<T> ret = BLOCKS.register(name, block); ModItems.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties())); return ret; }
    public static void register(IEventBus bus) { BLOCKS.register(bus); }
}
