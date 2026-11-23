package com.industrialeconomy.world;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.Random;
public class WorldInfrastructureGenerator {
    public static void tickWorldGen(ServerLevel level) {
        WorldHubSavedData data = WorldHubSavedData.get(level);
        for (ServerPlayer sp : level.players()) {
            int rx = Math.floorDiv(sp.blockPosition().getX(), 256);
            int rz = Math.floorDiv(sp.blockPosition().getZ(), 256);
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
            int cx = rx * 256 + 48 + rnd.nextInt(160);
            int cz = rz * 256 + 48 + rnd.nextInt(160);
            int cy = level.getHeight(Heightmap.Types.WORLD_SURFACE, cx, cz);
            if (cy < 60 || cy > 130) continue;
            BlockPos pos = new BlockPos(cx, cy - 1, cz);
            while (pos.getY() > 58 && !level.getBlockState(pos).blocksMotion() && level.getFluidState(pos).isEmpty()) pos = pos.below();
            if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) continue;
            if (!level.getBlockState(pos).blocksMotion()) continue;
            BlockPos buildOrigin = pos.above();
            int roll = Math.floorMod(rx * 3 + rz * 5, 4);
            if (roll == 0) buildGasStation(level, data, buildOrigin);
            else if (roll == 1) buildTradeMarket(level, data, buildOrigin);
            else if (roll == 2) buildEquipmentPlaza(level, data, buildOrigin);
            else buildIndustrialZone(level, data, buildOrigin, rnd);
            return;
        }
    }
    private static void clearAndPad(ServerLevel l, BlockPos o, int w, int d) {
        for (int x = 0; x != w; x++) for (int z = 0; z != d; z++) {
            l.setBlock(o.offset(x, -1, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            for (int y = 0; y != 6; y++) l.setBlock(o.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
    }
    private static void buildGasStation(ServerLevel l, WorldHubSavedData data, BlockPos o) {
        clearAndPad(l, o, 9, 9);
        for (int x = 0; x != 9; x++) for (int z = 0; z != 9; z++) l.setBlock(o.offset(x, 4, z), Blocks.RED_CONCRETE.defaultBlockState(), 3);
        for (int x : new int[]{0, 8}) for (int z : new int[]{0, 8}) for (int y = 0; y != 4; y++) l.setBlock(o.offset(x, y, z), Blocks.IRON_BARS.defaultBlockState(), 3);
        l.setBlock(o.offset(2, 0, 2), ModBlocks.SHOP_FUEL.get().defaultBlockState(), 3);
        l.setBlock(o.offset(4, 0, 2), Blocks.CHEST.defaultBlockState(), 3);
        l.setBlock(o.offset(6, 0, 2), ModBlocks.HOPPER_FUEL.get().defaultBlockState(), 3);
        l.setBlock(o.offset(1, 0, 7), ModBlocks.ATM_TERMINAL.get().defaultBlockState(), 3);
        data.addHub("GAS_STATION", o.offset(4, 0, 4), 6);
    }
    private static void buildTradeMarket(ServerLevel l, WorldHubSavedData data, BlockPos o) {
        clearAndPad(l, o, 13, 9);
        for (int x = 0; x != 13; x++) for (int z = 0; z != 9; z++) l.setBlock(o.offset(x, 4, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
        Block[] shops = {ModBlocks.SHOP_METALS.get(), ModBlocks.SHOP_BUILDING.get(), ModBlocks.SHOP_WOOD.get(), ModBlocks.SHOP_AGRICULTURE.get(), ModBlocks.SHOP_FOOD.get()};
        Block[] hops = {ModBlocks.HOPPER_METALS.get(), ModBlocks.HOPPER_BUILDING.get(), ModBlocks.HOPPER_WOOD.get(), ModBlocks.HOPPER_AGRICULTURE.get(), ModBlocks.HOPPER_FOOD.get()};
        for (int i = 0; i != 5; i++) {
            l.setBlock(o.offset(2 + i * 2, 0, 1), shops[i].defaultBlockState(), 3);
            l.setBlock(o.offset(2 + i * 2, 0, 4), Blocks.CHEST.defaultBlockState(), 3);
            l.setBlock(o.offset(2 + i * 2, 0, 7), hops[i].defaultBlockState(), 3);
        }
        l.setBlock(o.offset(0, 0, 7), ModBlocks.ATM_TERMINAL.get().defaultBlockState(), 3);
        data.addHub("TRADE_MARKET", o.offset(6, 0, 4), 8);
    }
    private static void buildEquipmentPlaza(ServerLevel l, WorldHubSavedData data, BlockPos o) {
        clearAndPad(l, o, 11, 9);
        for (int x = 0; x != 11; x++) for (int z = 0; z != 9; z++) l.setBlock(o.offset(x, 4, z), Blocks.BLUE_CONCRETE.defaultBlockState(), 3);
        l.setBlock(o.offset(2, 0, 1), ModBlocks.SHOP_EQUIPMENT.get().defaultBlockState(), 3);
        l.setBlock(o.offset(5, 0, 1), ModBlocks.SHOP_RARE.get().defaultBlockState(), 3);
        l.setBlock(o.offset(8, 0, 1), ModBlocks.SHOP_BOUTIQUE.get().defaultBlockState(), 3);
        l.setBlock(o.offset(3, 0, 4), Blocks.CHEST.defaultBlockState(), 3);
        l.setBlock(o.offset(7, 0, 4), Blocks.CHEST.defaultBlockState(), 3);
        l.setBlock(o.offset(5, 0, 7), ModBlocks.HOPPER_RARE.get().defaultBlockState(), 3);
        l.setBlock(o.offset(1, 0, 7), ModBlocks.ATM_TERMINAL.get().defaultBlockState(), 3);
        data.addHub("EQUIPMENT_PLAZA", o.offset(5, 0, 4), 7);
    }
    private static void buildIndustrialZone(ServerLevel l, WorldHubSavedData data, BlockPos o, Random rnd) {
        PlotTier[] tiers = {PlotTier.MICRO, PlotTier.SMALL, PlotTier.MEDIUM};
        PlotTier tier = tiers[rnd.nextInt(tiers.length)];
        PlotSavedData pData = PlotSavedData.get(l);
        String id = String.format("%03d", pData.nextNumber());
        IndustrialPlot plot = new IndustrialPlot(id, tier, o.getX(), o.getZ(), o.getY());
        pData.addPlot(plot);
        for (int i = 0; i != tier.size; i++) {
            l.setBlock(new BlockPos(plot.minX + i, plot.baseY, plot.minZ), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            l.setBlock(new BlockPos(plot.minX + i, plot.baseY, plot.maxZ), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            l.setBlock(new BlockPos(plot.minX, plot.baseY, plot.minZ + i), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            l.setBlock(new BlockPos(plot.maxX, plot.baseY, plot.minZ + i), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
        }
        l.setBlock(new BlockPos(plot.minX, plot.baseY + 1, plot.minZ), ModBlocks.PLOT_SIGN.get().defaultBlockState(), 3);
        data.addHub("PLOT_" + tier.name(), o, 2);
    }
}
