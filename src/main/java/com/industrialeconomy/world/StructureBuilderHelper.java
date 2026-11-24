package com.industrialeconomy.world;
import com.industrialeconomy.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
public class StructureBuilderHelper {
    public static boolean isValidGround(BlockState st) {
        return st.is(BlockTags.DIRT) || st.is(BlockTags.SAND) || st.is(BlockTags.BASE_STONE_OVERWORLD) || st.is(Blocks.GRAVEL) || st.is(Blocks.SNOW_BLOCK);
    }
    public static int findGroundY(ServerLevel l, int cx, int cz) {
        int y = l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        if (l.isFlat()) return y;
        if (y < 62 || y > 135) return -1;
        BlockPos g = new BlockPos(cx, y - 1, cz);
        while (g.getY() > 60 && !isValidGround(l.getBlockState(g)) && l.getFluidState(g).isEmpty()) {
            if (l.getBlockState(g).is(BlockTags.LOGS)) return -1;
            g = g.below();
        }
        if (!l.getFluidState(g).isEmpty() || !l.getFluidState(g.above()).isEmpty()) return -1;
        return isValidGround(l.getBlockState(g)) ? g.getY() + 1 : -1;
    }
    public static void terraformArea(ServerLevel l, BlockPos o, int w, int d, int clearH, Block baseFill) {
        for (int x = -3; x != w + 3; x++) for (int z = -3; z != d + 3; z++) {
            boolean outer = (x < 0 || x >= w || z < 0 || z >= d);
            int depth = outer ? 4 : 10;
            for (int y = -depth; y != 0; y++) {
                BlockPos p = o.offset(x, y, z);
                if (!l.getBlockState(p).blocksMotion()) {
                    Block fill = (outer && y == -1) ? Blocks.GRASS_BLOCK : baseFill;
                    l.setBlock(p, fill.defaultBlockState(), 3);
                }
            }
            for (int y = 0; y != clearH; y++) l.setBlock(o.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
    }
    public static void buildShopWithParking(ServerLevel l, BlockPos o, Block wall, Block pillar, Block stairBlock, Block shop, Block hopper) {
        terraformArea(l, o, 15, 17, 22, Blocks.DIRT);
        for (int x = 0; x != 15; x++) for (int z = 0; z != 17; z++) {
            Block fl = Blocks.POLISHED_ANDESITE;
            if (z == 6 || z == 7) fl = Blocks.SMOOTH_STONE;
            else if (z >= 8) fl = (x == 0 || x == 14 || z == 16 || (x == 7 && z >= 10 && z <= 14)) ? Blocks.YELLOW_CONCRETE : Blocks.GRAY_CONCRETE;
            l.setBlock(o.offset(x, -1, z), fl.defaultBlockState(), 3);
        }
        for (int x = 1; x != 14; x++) for (int z = 0; z != 6; z++) for (int y = 0; y != 4; y++) {
            boolean edge = (x == 1 || x == 13 || z == 0 || z == 5);
            if (!edge) continue;
            if (z == 5 && (x == 6 || x == 7 || x == 8) && y < 3) continue;
            boolean corner = (x == 1 || x == 13) && (z == 0 || z == 5);
            if (corner) l.setBlock(o.offset(x, y, z), pillar.defaultBlockState(), 3);
            else if (z == 5 && (x >= 3 && x <= 4 || x >= 10 && x <= 11) && (y == 1 || y == 2)) l.setBlock(o.offset(x, y, z), Blocks.GLASS_PANE.defaultBlockState(), 3);
            else if ((x == 1 || x == 13) && (z == 2 || z == 3) && y == 2) l.setBlock(o.offset(x, y, z), Blocks.GLASS_PANE.defaultBlockState(), 3);
            else l.setBlock(o.offset(x, y, z), wall.defaultBlockState(), 3);
        }
        BlockState stSouth = stairBlock.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState stNorth = stairBlock.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        for (int x = 0; x != 15; x++) {
            l.setBlock(o.offset(x, 4, 6), stSouth, 3);
            l.setBlock(o.offset(x, 5, 5), stSouth, 3);
            l.setBlock(o.offset(x, 5, 0), stNorth, 3);
            for (int z = 1; z != 5; z++) l.setBlock(o.offset(x, 5, z), wall.defaultBlockState(), 3);
            l.setBlock(o.offset(x, 6, 4), stSouth, 3);
            l.setBlock(o.offset(x, 6, 1), stNorth, 3);
            l.setBlock(o.offset(x, 6, 2), pillar.defaultBlockState(), 3);
            l.setBlock(o.offset(x, 6, 3), pillar.defaultBlockState(), 3);
        }
        for (int x = 4; x != 11; x++) l.setBlock(o.offset(x, 1, 2), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
        l.setBlock(o.offset(7, 1, 2), shop.defaultBlockState(), 3);
        l.setBlock(o.offset(4, 3, 2), Blocks.LANTERN.defaultBlockState(), 3);
        l.setBlock(o.offset(10, 3, 2), Blocks.LANTERN.defaultBlockState(), 3);
        l.setBlock(o.offset(2, 0, 6), ModBlocks.ATM_TERMINAL.get().defaultBlockState(), 3);
        l.setBlock(o.offset(12, 0, 6), Blocks.COMPOSTER.defaultBlockState(), 3);
        l.setBlock(o.offset(12, 1, 6), Blocks.OAK_LEAVES.defaultBlockState(), 3);
        for (int px = 1; px != 4; px++) for (int pz = 9; pz != 11; pz++) l.setBlock(o.offset(px, 0, pz), Blocks.OAK_SLAB.defaultBlockState(), 3);
        l.setBlock(o.offset(2, 1, 9), Blocks.CHEST.defaultBlockState(), 3);
        l.setBlock(o.offset(3, 1, 9), Blocks.BARREL.defaultBlockState(), 3);
        for (int hx = 10; hx != 12; hx++) for (int hz = 11; hz != 13; hz++) l.setBlock(o.offset(hx, -1, hz), hopper.defaultBlockState(), 3);
        for (int y = 0; y != 4; y++) { l.setBlock(o.offset(1, y, 15), Blocks.SPRUCE_FENCE.defaultBlockState(), 3); l.setBlock(o.offset(13, y, 15), Blocks.SPRUCE_FENCE.defaultBlockState(), 3); }
        l.setBlock(o.offset(1, 4, 15), Blocks.LANTERN.defaultBlockState(), 3);
        l.setBlock(o.offset(13, 4, 15), Blocks.LANTERN.defaultBlockState(), 3);
    }
}
