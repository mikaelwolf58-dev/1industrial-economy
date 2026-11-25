package com.industrialeconomy.registry;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.block.*;
import com.industrialeconomy.economy.MarketCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import java.util.function.Supplier;
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustrialEconomy.MODID);
    private static BlockBehaviour.Properties metal() { return BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL); }
    public static final RegistryObject<Block> ATM_TERMINAL = registerBlock("bank_terminal", () -> new BankTerminalBlock(metal()));
    public static final RegistryObject<Block> PLOT_SIGN = registerBlock("plot_sign", () -> new PlotSignBlock(metal()));
    public static final RegistryObject<Block> COMMERCIAL_SIGN = registerBlock("commercial_sign", () -> new CommercialSignBlock(metal()));
    public static final RegistryObject<Block> HOPPER_FUEL = registerBlock("hopper_fuel", () -> new BuyingHopperBlock(metal(), MarketCategory.FUEL));
    public static final RegistryObject<Block> HOPPER_METALS = registerBlock("hopper_metals", () -> new BuyingHopperBlock(metal(), MarketCategory.METALS));
    public static final RegistryObject<Block> HOPPER_BUILDING = registerBlock("hopper_building", () -> new BuyingHopperBlock(metal(), MarketCategory.BUILDING));
    public static final RegistryObject<Block> HOPPER_WOOD = registerBlock("hopper_wood", () -> new BuyingHopperBlock(metal(), MarketCategory.WOOD));
    public static final RegistryObject<Block> HOPPER_AGRICULTURE = registerBlock("hopper_agriculture", () -> new BuyingHopperBlock(metal(), MarketCategory.AGRICULTURE));
    public static final RegistryObject<Block> HOPPER_FOOD = registerBlock("hopper_food", () -> new BuyingHopperBlock(metal(), MarketCategory.FOOD));
    public static final RegistryObject<Block> HOPPER_EQUIPMENT = registerBlock("hopper_equipment", () -> new BuyingHopperBlock(metal(), MarketCategory.EQUIPMENT));
    public static final RegistryObject<Block> HOPPER_RARE = registerBlock("hopper_rare", () -> new BuyingHopperBlock(metal(), MarketCategory.RARE));
    public static final RegistryObject<Block> SHOP_EQUIPMENT = registerBlock("shop_equipment", () -> new ShopTerminalBlock(metal(), MarketCategory.EQUIPMENT));
    public static final RegistryObject<Block> SHOP_FUEL = registerBlock("shop_fuel", () -> new ShopTerminalBlock(metal(), MarketCategory.FUEL));
    public static final RegistryObject<Block> SHOP_METALS = registerBlock("shop_metals", () -> new ShopTerminalBlock(metal(), MarketCategory.METALS));
    public static final RegistryObject<Block> SHOP_BUILDING = registerBlock("shop_building", () -> new ShopTerminalBlock(metal(), MarketCategory.BUILDING));
    public static final RegistryObject<Block> SHOP_WOOD = registerBlock("shop_wood", () -> new ShopTerminalBlock(metal(), MarketCategory.WOOD));
    public static final RegistryObject<Block> SHOP_AGRICULTURE = registerBlock("shop_agriculture", () -> new ShopTerminalBlock(metal(), MarketCategory.AGRICULTURE));
    public static final RegistryObject<Block> SHOP_FOOD = registerBlock("shop_food", () -> new ShopTerminalBlock(metal(), MarketCategory.FOOD));
    public static final RegistryObject<Block> SHOP_RARE = registerBlock("shop_rare", () -> new ShopTerminalBlock(metal(), MarketCategory.RARE));
    public static final RegistryObject<Block> SHOP_BOUTIQUE = registerBlock("shop_boutique", () -> new ShopTerminalBlock(metal(), MarketCategory.BOUTIQUE));
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) { RegistryObject<T> ret = BLOCKS.register(name, block); ModItems.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties())); return ret; }
    public static void register(IEventBus bus) { BLOCKS.register(bus); }
}
