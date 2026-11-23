package com.industrialeconomy.registry;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.block.entity.BuyingHopperBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IndustrialEconomy.MODID);
    public static final RegistryObject<BlockEntityType<BuyingHopperBlockEntity>> BUYING_HOPPER_BE = BLOCK_ENTITIES.register("buying_hopper_be", () -> BlockEntityType.Builder.of(BuyingHopperBlockEntity::new,
        ModBlocks.HOPPER_FUEL.get(), ModBlocks.HOPPER_METALS.get(), ModBlocks.HOPPER_BUILDING.get(), ModBlocks.HOPPER_WOOD.get(), ModBlocks.HOPPER_AGRICULTURE.get(), ModBlocks.HOPPER_FOOD.get()).build(null));
    public static void register(IEventBus bus) { BLOCK_ENTITIES.register(bus); }
}
