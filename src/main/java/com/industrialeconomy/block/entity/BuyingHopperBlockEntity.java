package com.industrialeconomy.block.entity;
import com.industrialeconomy.block.BuyingHopperBlock;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
public class BuyingHopperBlockEntity extends BlockEntity {
    public BuyingHopperBlockEntity(BlockPos p, BlockState s) { super(ModBlockEntities.BUYING_HOPPER_BE.get(), p, s); }
    public static void tick(Level level, BlockPos pos, BlockState state, BuyingHopperBlockEntity be) {
        if (level.isClientSide || !(level instanceof ServerLevel sl) || sl.getGameTime() % 10 != 0) return;
        if (!(state.getBlock() instanceof BuyingHopperBlock hb)) return;
        MarketCategory cat = hb.getCategory();
        List<ItemEntity> list = sl.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(0.2, 0.8, 0.2));
        if (list.isEmpty()) return;
        ServerPlayer nearest = sl.players().stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))).orElse(null);
        if (nearest == null || nearest.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 256) return;
        nearest.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
            EconomySavedData eco = EconomySavedData.get(sl);
            long baseSum = 0L;
            for (ItemEntity ie : list) {
                ItemStack st = ie.getItem();
                if (st.isEmpty() || st.isDamaged() || st.isEnchanted()) continue;
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(st.getItem());
                if (id == null) continue;
                MarketItemEntry entry = ItemCatalogManager.getItem(id.toString());
                if (entry == null || !f.hasPatent(entry.patentId)) continue;
                boolean ok = entry.category.equals(cat.name()) || (cat == MarketCategory.RARE && entry.category.equals(MarketCategory.EQUIPMENT.name()) && entry.patentId > 0);
                if (!ok) continue;
                baseSum += eco.registerSaleAndGetPayout(id.toString(), st.getCount());
                ie.discard();
            }
            if (baseSum > 0) {
                long earned = Math.round(baseSum * (1.0 + f.getCorpTier().sellBonus));
                f.addCash(earned); FinanceEvents.syncToClient(nearest, f);
            }
        });
    }
}
