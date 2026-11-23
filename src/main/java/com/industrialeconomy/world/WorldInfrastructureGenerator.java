package com.industrialeconomy.world;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.Random;
public class WorldInfrastructureGenerator {
    public static void tickWorldGen(ServerLevel level) {
        WorldHubSavedData data = WorldHubSavedData.get(level);
        for (ServerPlayer sp : level.players()) {
            int rx = Math.floorDiv(sp.blockPosition().getX(), 192);
            int rz = Math.floorDiv(sp.blockPosition().getZ(), 192);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                int crx = rx + dx; int crz = rz + dz;
                if (!data.markRegionChecked(crx, crz)) continue;
                generateInRegion(level, data, crx, crz);
            }
        }
    }
    private static void generateInRegion(ServerLevel level, WorldHubSavedData data, int rx, int rz) {
        Random rnd = new Random(level.getSeed() ^ (rx * 341873128712L + rz * 132897987541L));
        for (int attempt = 0; attempt != 6; attempt++) {
            int cx = rx * 192 + 32 + rnd.nextInt(128);
            int cz = rz * 192 + 32 + rnd.nextInt(128);
            int cy = level.getHeight(Heightmap.Types.WORLD_SURFACE, cx, cz);
            if (cy < 60 || cy > 130) continue;
            BlockPos pos = new BlockPos(cx, cy - 1, cz);
            while (pos.getY() > 58 && !level.getBlockState(pos).blocksMotion() && level.getFluidState(pos).isEmpty()) pos = pos.below();
            if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) continue;
            if (!level.getBlockState(pos).blocksMotion()) continue;
            BlockPos o = pos.above();
            int roll = Math.floorMod(rx * 7 + rz * 11, 9);
            if (roll == 0) SpecializedStructures.buildGasStation(level, data, o);
            else if (roll == 1) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.BRICKS, Blocks.DARK_OAK_SLAB, ModBlocks.SHOP_METALS.get(), ModBlocks.HOPPER_METALS.get()); data.addHub("METALS_DEPOT", o.offset(6, 0, 7), 8); }
            else if (roll == 2) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.STONE_BRICKS, Blocks.SPRUCE_SLAB, ModBlocks.SHOP_BUILDING.get(), ModBlocks.HOPPER_BUILDING.get()); data.addHub("BUILDING_DEPOT", o.offset(6, 0, 7), 8); }
            else if (roll == 3) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.SPRUCE_PLANKS, Blocks.OAK_SLAB, ModBlocks.SHOP_WOOD.get(), ModBlocks.HOPPER_WOOD.get()); data.addHub("WOOD_MILL", o.offset(6, 0, 7), 8); }
            else if (roll == 4) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.TERRACOTTA, Blocks.BIRCH_SLAB, ModBlocks.SHOP_AGRICULTURE.get(), ModBlocks.HOPPER_AGRICULTURE.get()); data.addHub("AGRO_MARKET", o.offset(6, 0, 7), 8); }
            else if (roll == 5) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.WHITE_CONCRETE, Blocks.SMOOTH_STONE_SLAB, ModBlocks.SHOP_FOOD.get(), ModBlocks.HOPPER_FOOD.get()); data.addHub("FOOD_STORE", o.offset(6, 0, 7), 8); }
            else if (roll == 6) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.BLUE_TERRACOTTA, Blocks.STONE_BRICK_SLAB, ModBlocks.SHOP_EQUIPMENT.get(), ModBlocks.HOPPER_RARE.get()); data.addHub("EQUIPMENT_CENTER", o.offset(6, 0, 7), 8); }
            else if (roll == 7) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.QUARTZ_BLOCK, Blocks.PURPUR_SLAB, ModBlocks.SHOP_BOUTIQUE.get(), ModBlocks.HOPPER_RARE.get()); data.addHub("BOUTIQUE_PLAZA", o.offset(6, 0, 7), 8); }
            else SpecializedStructures.buildIndustrialZone(level, data, o, rnd);
            return;
        }
    }
}
