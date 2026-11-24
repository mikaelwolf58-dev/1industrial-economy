package com.industrialeconomy.world;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
public class StructureBuilderHelper {
    public static int findGroundY(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (!level.isFlat() && y < 62) return -1;
        BlockState st = level.getBlockState(new BlockPos(x, y - 1, z));
        if (!st.getFluidState().isEmpty() || st.is(Blocks.ICE) || st.is(Blocks.PACKED_ICE)) return -1;
        return y;
    }
    public static void clearAndPave(ServerLevel l, BlockPos o, int w, int d, BlockState floor) {
        for (int dx = 0; dx < w; dx++) for (int dz = 0; dz < d; dz++) {
            for (int dy = 1; dy <= 8; dy++) l.setBlock(o.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
            for (int dy = -3; dy <= 0; dy++) l.setBlock(o.offset(dx, dy, dz), floor, 3);
        }
    }
    public static void buildShopWithParking(ServerLevel l, BlockPos o, Block wall, Block trim, Block roof, Block shop, Block hopper) {
        clearAndPave(l, o, 14, 16, Blocks.GRAY_CONCRETE.defaultBlockState());
        for (int dx = 2; dx < 12; dx++) for (int dz = 1; dz < 9; dz++) {
            l.setBlock(o.offset(dx, 0, dz), Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
            for (int dy = 1; dy <= 4; dy++) {
                boolean edge = dx == 2 || dx == 11 || dz == 1 || dz == 8;
                if (dy == 4) l.setBlock(o.offset(dx, dy, dz), roof.defaultBlockState(), 3);
                else if (edge) l.setBlock(o.offset(dx, dy, dz), (dx == 6 || dx == 7) && dz == 8 && dy <= 2 ? Blocks.AIR.defaultBlockState() : wall.defaultBlockState(), 3);
            }
        }
        l.setBlock(o.offset(5, 1, 4), shop.defaultBlockState(), 3);
        l.setBlock(o.offset(8, 1, 4), hopper.defaultBlockState(), 3);
        l.setBlock(o.offset(3, 1, 7), ModBlocks.BANK_TERMINAL.get().defaultBlockState(), 3);
        l.setBlock(o.offset(6, 3, 7), Blocks.LANTERN.defaultBlockState(), 3);
    }
}
