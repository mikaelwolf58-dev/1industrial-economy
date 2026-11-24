package com.industrialeconomy.block;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Comparator;
public class BuyingHopperBlock extends Block {
    public final MarketCategory category;
    public BuyingHopperBlock(Properties props, MarketCategory category) { super(props); this.category = category; }
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
                EconomySavedData eco = EconomySavedData.get(sl);
                int count = stack.getCount();
                long totalEarned = eco.registerSaleAndGetPayout(id.toString(), count);
                f.addCash(totalEarned);
                FinanceEvents.syncToClient(nearest, f);
                nearest.displayClientMessage(Component.literal("Sold x" + count + " -> +" + String.format("%,d$", totalEarned)).withStyle(ChatFormatting.GREEN), true);
                itemEntity.discard();
            });
        }
        super.stepOn(level, pos, state, entity);
    }
}
