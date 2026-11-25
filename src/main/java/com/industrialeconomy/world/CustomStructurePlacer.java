package com.industrialeconomy.world;
import com.industrialeconomy.IndustrialEconomy;
import com.industrialeconomy.economy.MarketCategory;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import java.util.Optional;
public class CustomStructurePlacer {
    public static boolean placeCustom(ServerLevel sl, BlockPos o, String name, Rotation rot, MarketCategory replaceCat) {
        StructureTemplateManager mgr = sl.getStructureManager();
        Optional<StructureTemplate> opt = mgr.get(new ResourceLocation(IndustrialEconomy.MODID, name));
        if (opt.isEmpty()) return false;
        StructureBuilderHelper.terraformArea(sl, o, 17, 17, 22, Blocks.DIRT);
        BlockPos placePos = switch (rot) {
            case CLOCKWISE_90 -> o.offset(16, -1, 0);
            case CLOCKWISE_180 -> o.offset(16, -1, 16);
            case COUNTERCLOCKWISE_90 -> o.offset(0, -1, 16);
            default -> o.below();
        };
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rot).setMirror(Mirror.NONE).setIgnoreEntities(false);
        opt.get().placeInWorld(sl, placePos, placePos, settings, RandomSource.create(), 3);
        if ("shop_equipment".equals(name)) {
            replaceBlockInBox(sl, o, 17, 22, ModBlocks.HOPPER_METALS.get(), ModBlocks.HOPPER_EQUIPMENT.get());
        }
        if (replaceCat != null) {
            replaceBlockInBox(sl, o, 17, 22, ModBlocks.SHOP_METALS.get(), getShopFor(replaceCat));
            replaceBlockInBox(sl, o, 17, 22, ModBlocks.HOPPER_METALS.get(), getHopperFor(replaceCat));
        }
        return true;
    }
    private static void replaceBlockInBox(ServerLevel sl, BlockPos o, int sz, int h, Block target, Block repl) {
        if (target == repl) return;
        for (int x = 0; x < sz; x++) for (int z = 0; z < sz; z++) for (int y = -1; y <= h; y++) {
            BlockPos p = o.offset(x, y, z);
            if (sl.getBlockState(p).is(target)) sl.setBlock(p, repl.defaultBlockState(), 3);
        }
    }
    public static Block getShopFor(MarketCategory c) {
        return switch (c) {
            case FUEL -> ModBlocks.SHOP_FUEL.get(); case BUILDING -> ModBlocks.SHOP_BUILDING.get();
            case WOOD -> ModBlocks.SHOP_WOOD.get(); case AGRICULTURE -> ModBlocks.SHOP_AGRICULTURE.get();
            case FOOD -> ModBlocks.SHOP_FOOD.get(); case EQUIPMENT -> ModBlocks.SHOP_EQUIPMENT.get();
            case RARE -> ModBlocks.SHOP_RARE.get(); case BOUTIQUE -> ModBlocks.SHOP_BOUTIQUE.get();
            default -> ModBlocks.SHOP_METALS.get();
        };
    }
    public static Block getHopperFor(MarketCategory c) {
        return switch (c) {
            case FUEL -> ModBlocks.HOPPER_FUEL.get(); case BUILDING -> ModBlocks.HOPPER_BUILDING.get();
            case WOOD -> ModBlocks.HOPPER_WOOD.get(); case AGRICULTURE -> ModBlocks.HOPPER_AGRICULTURE.get();
            case FOOD -> ModBlocks.HOPPER_FOOD.get(); case EQUIPMENT -> ModBlocks.HOPPER_EQUIPMENT.get();
            case RARE, BOUTIQUE -> ModBlocks.HOPPER_RARE.get();
            default -> ModBlocks.HOPPER_METALS.get();
        };
    }
}
