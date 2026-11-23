package com.industrialeconomy.world;
import com.industrialeconomy.plot.PlotTier;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import java.util.Random;
public class WorldInfrastructureGenerator {
    public static void tickWorldGen(ServerLevel level) {
        WorldHubSavedData data = WorldHubSavedData.get(level);
        for (ServerPlayer sp : level.players()) {
            int rx = Math.floorDiv(sp.blockPosition().getX(), 96);
            int rz = Math.floorDiv(sp.blockPosition().getZ(), 96);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                int crx = rx + dx; int crz = rz + dz;
                if (!data.markRegionChecked(crx, crz)) continue;
                generateInRegion(level, data, crx, crz);
            }
        }
    }
    private static void generateInRegion(ServerLevel level, WorldHubSavedData data, int rx, int rz) {
        Random rnd = new Random(level.getSeed() ^ (rx * 341873128712L + rz * 132897987541L));
        int roll = Math.floorMod(rx * 7 + rz * 13, 18);
        for (int attempt = 0; attempt != 60; attempt++) {
            int cx = rx * 96 + 8 + rnd.nextInt(76);
            int cz = rz * 96 + 8 + rnd.nextInt(76);
            int groundY = StructureBuilderHelper.findGroundY(level, cx, cz);
            if (groundY == -1) continue;
            BlockPos o = new BlockPos(cx, groundY, cz);
            if (roll <= 7) {
                PlotTier[] tiers = {PlotTier.MICRO, PlotTier.SMALL, PlotTier.MEDIUM, PlotTier.LARGE};
                SpecializedStructures.buildIndustrialZone(level, data, o, tiers[rnd.nextInt(tiers.length)]);
            } else if (roll <= 9) SpecializedStructures.buildGasStation(level, data, o);
            else if (roll == 10) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.DARK_OAK_STAIRS, ModBlocks.SHOP_METALS.get(), ModBlocks.HOPPER_METALS.get()); data.addHub("METALS_DEPOT", o.offset(7, 0, 8), 8); }
            else if (roll == 11) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.SPRUCE_STAIRS, ModBlocks.SHOP_BUILDING.get(), ModBlocks.HOPPER_BUILDING.get()); data.addHub("BUILDING_DEPOT", o.offset(7, 0, 8), 8); }
            else if (roll == 12) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.SPRUCE_PLANKS, Blocks.STRIPPED_SPRUCE_LOG, Blocks.OAK_STAIRS, ModBlocks.SHOP_WOOD.get(), ModBlocks.HOPPER_WOOD.get()); data.addHub("WOOD_MILL", o.offset(7, 0, 8), 8); }
            else if (roll == 13) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.TERRACOTTA, Blocks.BIRCH_PLANKS, Blocks.SMOOTH_STONE_STAIRS, ModBlocks.SHOP_AGRICULTURE.get(), ModBlocks.HOPPER_AGRICULTURE.get()); data.addHub("AGRO_MARKET", o.offset(7, 0, 8), 8); }
            else if (roll == 14) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.WHITE_CONCRETE, Blocks.SMOOTH_QUARTZ, Blocks.STONE_BRICK_STAIRS, ModBlocks.SHOP_FOOD.get(), ModBlocks.HOPPER_FOOD.get()); data.addHub("FOOD_STORE", o.offset(7, 0, 8), 8); }
            else if (roll == 15) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.BLUE_TERRACOTTA, Blocks.POLISHED_ANDESITE, Blocks.DARK_OAK_STAIRS, ModBlocks.SHOP_EQUIPMENT.get(), ModBlocks.HOPPER_RARE.get()); data.addHub("EQUIPMENT_CENTER", o.offset(7, 0, 8), 8); }
            else if (roll == 16) { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_BLACKSTONE, Blocks.STONE_BRICK_STAIRS, ModBlocks.SHOP_RARE.get(), ModBlocks.HOPPER_RARE.get()); data.addHub("RARE_EXCHANGE", o.offset(7, 0, 8), 8); }
            else { StructureBuilderHelper.buildShopWithParking(level, o, Blocks.QUARTZ_BLOCK, Blocks.PURPUR_PILLAR, Blocks.QUARTZ_STAIRS, ModBlocks.SHOP_BOUTIQUE.get(), ModBlocks.HOPPER_RARE.get()); data.addHub("LUXURY_BOUTIQUE", o.offset(7, 0, 8), 8); }
            return;
        }
    }
}
