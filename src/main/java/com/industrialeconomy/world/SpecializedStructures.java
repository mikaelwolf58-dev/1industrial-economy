package com.industrialeconomy.world;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
public class SpecializedStructures {
    public static void buildGasStation(ServerLevel l, WorldHubSavedData data, BlockPos o) {
        StructureBuilderHelper.terraformArea(l, o, 16, 16, 22, Blocks.DIRT);
        for (int x = 0; x != 16; x++) for (int z = 0; z != 16; z++) {
            boolean border = (x == 0 || x == 15 || z == 0 || z == 15);
            boolean lane = (x == 7 || x == 8) && z > 1 && z < 14;
            var fl = border ? Blocks.YELLOW_CONCRETE : (lane ? Blocks.WHITE_CONCRETE : Blocks.BLACK_CONCRETE);
            l.setBlock(o.offset(x, -1, z), fl.defaultBlockState(), 3);
        }
        for (int x = 1; x != 15; x++) for (int z = 1; z != 8; z++) {
            boolean rim = (x == 1 || x == 14 || z == 1 || z == 7);
            l.setBlock(o.offset(x, 4, z), (rim ? Blocks.WHITE_CONCRETE : Blocks.RED_CONCRETE).defaultBlockState(), 3);
        }
        l.setBlock(o.offset(4, 3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        l.setBlock(o.offset(11, 3, 4), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        for (int x : new int[]{2, 13}) for (int z : new int[]{2, 6}) for (int y = 0; y != 4; y++) l.setBlock(o.offset(x, y, z), Blocks.QUARTZ_PILLAR.defaultBlockState(), 3);
        l.setBlock(o.offset(4, 0, 4), Blocks.RED_CONCRETE.defaultBlockState(), 3);
        l.setBlock(o.offset(4, 1, 4), ModBlocks.SHOP_FUEL.get().defaultBlockState(), 3);
        l.setBlock(o.offset(11, 0, 4), Blocks.RED_CONCRETE.defaultBlockState(), 3);
        l.setBlock(o.offset(11, 1, 4), Blocks.IRON_BLOCK.defaultBlockState(), 3);
        l.setBlock(o.offset(7, 0, 3), Blocks.OAK_SLAB.defaultBlockState(), 3);
        l.setBlock(o.offset(8, 0, 3), Blocks.OAK_SLAB.defaultBlockState(), 3);
        l.setBlock(o.offset(7, 1, 3), Blocks.CHEST.defaultBlockState(), 3);
        l.setBlock(o.offset(2, 0, 13), ModBlocks.ATM_TERMINAL.get().defaultBlockState(), 3);
        for (int hx = 10; hx != 12; hx++) for (int hz = 10; hz != 12; hz++) l.setBlock(o.offset(hx, -1, hz), ModBlocks.HOPPER_FUEL.get().defaultBlockState(), 3);
        data.addHub("GAS_STATION", o.offset(7, 0, 7), 9);
    }
    public static void buildIndustrialZone(ServerLevel l, WorldHubSavedData data, BlockPos o, PlotTier tier) {
        int sz = tier.size;
        StructureBuilderHelper.terraformArea(l, o, sz, sz, 28, Blocks.DIRT);
        for (int x = 0; x != sz; x++) for (int z = 0; z != sz; z++) {
            l.setBlock(o.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
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
        for (int cx : new int[]{plot.minX, plot.maxX}) for (int cz : new int[]{plot.minZ, plot.maxZ}) {
            l.setBlock(new BlockPos(cx, plot.baseY, cz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            l.setBlock(new BlockPos(cx, plot.baseY + 1, cz), Blocks.IRON_BARS.defaultBlockState(), 3);
            l.setBlock(new BlockPos(cx, plot.baseY + 2, cz), Blocks.LANTERN.defaultBlockState(), 3);
        }
        l.setBlock(new BlockPos(plot.minX + 1, plot.baseY, plot.minZ), Blocks.AIR.defaultBlockState(), 3);
        l.setBlock(new BlockPos(plot.minX + 2, plot.baseY, plot.minZ), Blocks.AIR.defaultBlockState(), 3);
        l.setBlock(new BlockPos(plot.minX, plot.baseY + 1, plot.minZ), ModBlocks.PLOT_SIGN.get().defaultBlockState(), 3);
        data.addHub("PLOT_" + tier.name(), o, 2);
    }
}
