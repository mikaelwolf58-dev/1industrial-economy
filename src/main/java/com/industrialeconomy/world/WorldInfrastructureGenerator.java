package com.industrialeconomy.world;
import com.industrialeconomy.plot.PlotTier;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import java.util.Random;
public class WorldInfrastructureGenerator {
    private static boolean isTooClose(WorldHubSavedData data, BlockPos pos) {
        for (var h : data.getHubs()) {
            if (Math.hypot(h.x() - pos.getX(), h.z() - pos.getZ()) < 95.0) return true;
        }
        return false;
    }
    public static void tickWorldGen(ServerLevel level) {
        WorldHubSavedData data = WorldHubSavedData.get(level);
        for (ServerPlayer sp : level.players()) {
            int rx = Math.floorDiv(sp.blockPosition().getX(), 112);
            int rz = Math.floorDiv(sp.blockPosition().getZ(), 112);
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
        Rotation[] rots = Rotation.values();
        for (int attempt = 0; attempt != 60; attempt++) {
            int cx = rx * 112 + 8 + rnd.nextInt(90);
            int cz = rz * 112 + 8 + rnd.nextInt(90);
            int groundY = StructureBuilderHelper.findGroundY(level, cx, cz);
            if (groundY == -1) continue;
            BlockPos o = new BlockPos(cx, groundY, cz);
            if (isTooClose(data, o)) continue;
            Rotation rot = rots[rnd.nextInt(rots.length)];
            if (roll <= 7) {
                PlotTier[] tiers = {PlotTier.MICRO, PlotTier.SMALL, PlotTier.MEDIUM, PlotTier.LARGE};
                SpecializedStructures.buildIndustrialZone(level, data, o, tiers[rnd.nextInt(tiers.length)]);
            } else if (roll <= 9) {
                if (!CustomStructurePlacer.placeCustom(level, o, "gas_station", rot, null)) SpecializedStructures.buildGasStation(level, data, o);
                else data.addHub("GAS_STATION", o.offset(8, 0, 8), 8);
            } else if (roll == 10) { placeShop(level, data, o, rot, "shop_metals", "METALS_DEPOT", Blocks.BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.DARK_OAK_STAIRS, ModBlocks.SHOP_METALS.get(), ModBlocks.HOPPER_METALS.get()); }
            else if (roll == 11) { placeShop(level, data, o, rot, "shop_building", "BUILDING_DEPOT", Blocks.STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.SPRUCE_STAIRS, ModBlocks.SHOP_BUILDING.get(), ModBlocks.HOPPER_BUILDING.get()); }
            else if (roll == 12) { placeShop(level, data, o, rot, "shop_wood", "WOOD_MILL", Blocks.SPRUCE_PLANKS, Blocks.STRIPPED_SPRUCE_LOG, Blocks.OAK_STAIRS, ModBlocks.SHOP_WOOD.get(), ModBlocks.HOPPER_WOOD.get()); }
            else if (roll == 13) { placeShop(level, data, o, rot, "shop_agriculture", "AGRO_MARKET", Blocks.TERRACOTTA, Blocks.BIRCH_PLANKS, Blocks.STONE_STAIRS, ModBlocks.SHOP_AGRICULTURE.get(), ModBlocks.HOPPER_AGRICULTURE.get()); }
            else if (roll == 14) { placeShop(level, data, o, rot, "shop_food", "FOOD_STORE", Blocks.WHITE_CONCRETE, Blocks.SMOOTH_QUARTZ, Blocks.STONE_BRICK_STAIRS, ModBlocks.SHOP_FOOD.get(), ModBlocks.HOPPER_FOOD.get()); }
            else if (roll == 15) { placeShop(level, data, o, rot, "shop_equipment", "EQUIPMENT_CENTER", Blocks.BLUE_TERRACOTTA, Blocks.POLISHED_ANDESITE, Blocks.DARK_OAK_STAIRS, ModBlocks.SHOP_EQUIPMENT.get(), ModBlocks.HOPPER_EQUIPMENT.get()); }
            else if (roll == 16) { placeShop(level, data, o, rot, "shop_rare", "RARE_EXCHANGE", Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_BLACKSTONE, Blocks.STONE_BRICK_STAIRS, ModBlocks.SHOP_RARE.get(), ModBlocks.HOPPER_RARE.get()); }
            else { placeShop(level, data, o, rot, "shop_boutique", "LUXURY_BOUTIQUE", Blocks.QUARTZ_BLOCK, Blocks.PURPUR_PILLAR, Blocks.QUARTZ_STAIRS, ModBlocks.SHOP_BOUTIQUE.get(), ModBlocks.HOPPER_RARE.get()); }
            return;
        }
    }
    private static void placeShop(ServerLevel l, WorldHubSavedData d, BlockPos o, Rotation rot, String nbt, String hub, Block w, Block p, Block s, Block shop, Block hop) {
        if (!CustomStructurePlacer.placeCustom(l, o, nbt, rot, null)) StructureBuilderHelper.buildShopWithParking(l, o, w, p, s, shop, hop);
        d.addHub(hub, o.offset(8, 0, 8), 8);
    }
}
