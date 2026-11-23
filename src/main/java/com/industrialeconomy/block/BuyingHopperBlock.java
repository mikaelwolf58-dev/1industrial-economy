package com.industrialeconomy.block;
import com.industrialeconomy.block.entity.BuyingHopperBlockEntity;
import com.industrialeconomy.economy.MarketCategory;
import com.industrialeconomy.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
public class BuyingHopperBlock extends BaseEntityBlock {
    private final MarketCategory category;
    public BuyingHopperBlock(Properties p, MarketCategory category) { super(p); this.category = category; }
    public MarketCategory getCategory() { return category; }
    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BuyingHopperBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.BUYING_HOPPER_BE.get(), BuyingHopperBlockEntity::tick);
    }
}
