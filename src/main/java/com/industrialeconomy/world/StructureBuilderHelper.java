package com.industrialeconomy.world;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
public class StructureBuilderHelper {
    public static void prepareSite(ServerLevel l, BlockPos o, int w, int d, int clearH) {
        for (int x = -1; x != w + 1; x++) for (int z = -1; z != d + 1; z++) {
            for (int y = -4; y != 0; y++) {
                BlockPos p = o.offset(x, y, z);
                if (!l.getBlockState(p).blocksMotion()) l.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
            for (int y = 0; y != clearH; y++) l.setBlock(o.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
    }
    public static void buildShopWithParking(ServerLevel l, BlockPos o, Block wall, Block roof, Block shop, Block hopper) {
        prepareSite(l, o, 13, 15, 9);
        for (int x = 0; x != 13; x++) for (int z = 0; z != 15; z++) {
            boolean isBorder = (x == 0 || x == 12 || z == 14);
            Block floor = z >= 6 ? (isBorder ? Blocks.YELLOW_CONCRETE : Blocks.GRAY_CONCRETE) : Blocks.POLISHED_ANDESITE;
            l.setBlock(o.offset(x, -1, z), floor.defaultBlockState(), 3);
        }
        for (int x = 1; x != 12; x++) for (int z = 0; z != 6; z++) for (int y = 0; y != 4; y++) {
            boolean edge = (x == 1 || x == 11 || z == 0 || z == 5);
            if (!edge) continue;
            if (z == 5 && (x == 5 || x == 6 || x == 7) && y < 3) continue;
            if (z == 5 && (x == 3 || x == 9) && (y == 1 || y == 2)) l.setBlock(o.offset(x, y, z), Blocks.GLASS_PANE.defaultBlockState(), 3);
            else l.setBlock(o.offset(x, y, z), wall.defaultBlockState(), 3);
        }
        for (int x = 0; x != 13; x++) for (int z = 0; z != 7; z++) l.setBlock(o.offset(x, 4, z), roof.defaultBlockState(), 3);
        l.setBlock(o.offset(6, 3, 2), Blocks.LANTERN.defaultBlockState(), 3);
        l.setBlock(o.offset(6, 0, 1), shop.defaultBlockState(), 3);
        l.setBlock(o.offset(2, 0, 4), ModBlocks.ATM_TERMINAL.get().defaultBlockState(), 3);
        l.setBlock(o.offset(2, 0, 7), Blocks.OAK_SLAB.defaultBlockState(), 3);
        l.setBlock(o.offset(3, 0, 7), Blocks.OAK_SLAB.defaultBlockState(), 3);
        l.setBlock(o.offset(2, 1, 7), Blocks.CHEST.defaultBlockState(), 3);
        for (int hx = 8; hx != 10; hx++) for (int hz = 9; hz != 11; hz++) {
            l.setBlock(o.offset(hx, -1, hz), hopper.defaultBlockState(), 3);
        }
        for (int y = 0; y != 4; y++) l.setBlock(o.offset(11, y, 8), Blocks.OAK_FENCE.defaultBlockState(), 3);
        l.setBlock(o.offset(11, 4, 8), Blocks.LANTERN.defaultBlockState(), 3);
    }
}
