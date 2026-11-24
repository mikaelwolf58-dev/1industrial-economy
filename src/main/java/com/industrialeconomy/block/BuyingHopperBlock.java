package com.industrialeconomy.block;
import com.industrialeconomy.block.entity.BuyingHopperBlockEntity;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Comparator;
public class BuyingHopperBlock extends BaseEntityBlock {
    public final MarketCategory category;
    public BuyingHopperBlock(Properties props, MarketCategory category) { super(props); this.category = category; }
    public MarketCategory getCategory() { return category; }
    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new BuyingHopperBlockEntity(p, s); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        if (l.isClientSide) return null;
        return (lvl, pos, st, be) -> { if (be instanceof BuyingHopperBlockEntity b) BuyingHopperBlockEntity.tick(lvl, pos, st, b); };
    }
    @Override public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof ItemEntity itemEntity && level instanceof ServerLevel sl) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty() || stack.isDamaged() || stack.isEnchanted()) return;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id == null) return;
            MarketItemEntry entry = ItemCatalogManager.getItem(id.toString());
            if (entry == null) return;
            boolean ok = entry.category.equals(category.name()) || (category == MarketCategory.RARE && entry.category.equals(MarketCategory.EQUIPMENT.name()) && entry.patentId > 0);
            if (!ok) return;
            ServerPlayer nearest = sl.players().stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))).orElse(null);
            if (nearest == null || nearest.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 256) return;
            nearest.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!f.hasPatent(entry.patentId)) {
                    if (sl.getGameTime() % 40 == 0) nearest.displayClientMessage(Component.literal("Requires Patent: " + IndustryPatent.byId(entry.patentId).title + " (Buy at ATM)").withStyle(ChatFormatting.RED), true);
                    return;
                }
                long totalEarned = EconomySavedData.get(sl).registerSaleAndGetPayout(id.toString(), stack.getCount());
                f.addCash(totalEarned); FinanceEvents.syncToClient(nearest, f);
                nearest.displayClientMessage(Component.literal("Sold x" + stack.getCount() + " -> +" + String.format("%,d$", totalEarned)).withStyle(ChatFormatting.GREEN), true);
                itemEntity.discard();
            });
        }
        super.stepOn(level, pos, state, entity);
    }
}
