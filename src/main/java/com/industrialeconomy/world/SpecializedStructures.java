package com.industrialeconomy.world;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import java.util.Random;
public class SpecializedStructures {
    public static void buildGasStation(ServerLevel l, WorldHubSavedData data, BlockPos o) {
        StructureBuilderHelper.prepareSite(l, o, 14, 14, 9);
        for (int x = 0; x != 14; x++) for (int z = 0; z != 14; z++) {
            boolean border = (x == 0 || x == 13 || z == 0 || z == 13);
            l.setBlock(o.offset(x, -1, z), (border ? Blocks.YELLOW_CONCRETE : Blocks.BLACK_CONCRETE).defaultBlockState(), 3);
        }
        for (int x = 1; x != 13; x++) for (int z = 1; z != 7; z++) l.setBlock(o.offset(x, 4, z), Blocks.RED_CONCRETE.defaultBlockState(), 3);
        for (int x : new int[]{2, 11}) for (int z : new int[]{2, 6}) for (int y = 0; y != 4; y++) l.setBlock(o.offset(x, y, z), Blocks.IRON_BLOCK.defaultBlockState(), 3);
        l.setBlock(o.offset(4, 0, 4), Blocks.RED_TERRACOTTA.defaultBlockState(), 3);
        l.setBlock(o.offset(4, 1, 4), ModBlocks.SHOP_FUEL.get().defaultBlockState(), 3);
        l.setBlock(o.offset(6, 0, 4), Blocks.OAK_SLAB.defaultBlockState(), 3);
        l.setBlock(o.offset(6, 1, 4), Blocks.CHEST.defaultBlockState(), 3);
        l.setBlock(o.offset(9, 0, 4), ModBlocks.ATM_TERMINAL.get().defaultBlockState(), 3);
        for (int hx = 8; hx != 10; hx++) for (int hz = 9; hz != 11; hz++) {
            l.setBlock(o.offset(hx, -1, hz), ModBlocks.HOPPER_FUEL.get().defaultBlockState(), 3);
        }
        data.addHub("GAS_STATION", o.offset(6, 0, 6), 8);
    }
    public static void buildIndustrialZone(ServerLevel l, WorldHubSavedData data, BlockPos o, Random rnd) {
        PlotTier[] tiers = {PlotTier.MICRO, PlotTier.SMALL, PlotTier.MEDIUM, PlotTier.LARGE};
        PlotTier tier = tiers[rnd.nextInt(tiers.length)];
        int sz = tier.size;
        for (int x = -1; x != sz + 1; x++) for (int z = -1; z != sz + 1; z++) {
            for (int y = -5; y != -1; y++) {
                BlockPos p = o.offset(x, y, z);
                if (!l.getBlockState(p).blocksMotion()) l.setBlock(p, Blocks.DIRT.defaultBlockState(), 3);
            }
            l.setBlock(o.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            for (int y = 0; y != 18; y++) l.setBlock(o.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
        PlotSavedData pData = PlotSavedData.get(l);
        String id = String.format("%03d", pData.nextNumber());
        IndustrialPlot plot = new IndustrialPlot(id, tier, o.getX(), o.getZ(), o.getY());
        pData.addPlot(plot);
        for (int i = 0; i != sz; i++) {
            l.setBlock(new BlockPos(plot.minX + i, plot.baseY, plot.minZ), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            l.setBlock(new BlockPos(plot.minX + i, plot.baseY, plot.maxZ), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            l.setBlock(new BlockPos(plot.minX, plot.baseY, plot.minZ + i), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            l.setBlock(new BlockPos(plot.maxX, plot.baseY, plot.minZ + i), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
        }
        l.setBlock(new BlockPos(plot.minX, plot.baseY + 1, plot.minZ), ModBlocks.PLOT_SIGN.get().defaultBlockState(), 3);
        data.addHub("PLOT_" + tier.name(), o, 2);
    }
}
