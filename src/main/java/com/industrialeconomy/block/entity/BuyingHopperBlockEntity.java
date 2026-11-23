package com.industrialeconomy.block.entity;
import com.industrialeconomy.block.BuyingHopperBlock;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;
public class BuyingHopperBlockEntity extends BlockEntity {
    public BuyingHopperBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.BUYING_HOPPER_BE.get(), pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, BuyingHopperBlockEntity be) {
        if (!(level instanceof ServerLevel sl) || sl.getGameTime() % 10 != 0) return;
        if (!(state.getBlock() instanceof BuyingHopperBlock hopperBlock)) return;
        MarketCategory cat = hopperBlock.getCategory();
        AABB box = new AABB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1.0, pos.getY() + 2.0, pos.getZ() + 1.0);
        List<ItemEntity> items = sl.getEntitiesOfClass(ItemEntity.class, box);
        if (items.isEmpty()) return;
        ServerPlayer nearest = (ServerPlayer) sl.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 24.0, false);
        if (nearest == null) return;
        EconomySavedData eco = EconomySavedData.get(sl);
        long totalEarned = 0L;
        for (ItemEntity entity : items) {
            if (!entity.isAlive()) continue;
            ItemStack stack = entity.getItem();
            String id = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            MarketItemEntry entry = ItemCatalogManager.getItem(id);
            if (entry != null && entry.category.equals(cat.name())) {
                int count = stack.getCount();
                totalEarned += eco.getDynamicSellPrice(id) * count;
                eco.recordItemSold(id, count);
                entity.discard();
            }
        }
        if (totalEarned > 0) {
            final long reward = totalEarned;
            nearest.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                f.addCash(reward);
                FinanceEvents.syncToClient(nearest, f);
                nearest.displayClientMessage(Component.literal("Sold cargo: +" + String.format("%,d$", reward)).withStyle(ChatFormatting.GREEN), true);
            });
        }
    }
}
