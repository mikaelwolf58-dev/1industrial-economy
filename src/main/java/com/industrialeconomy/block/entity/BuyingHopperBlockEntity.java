package com.industrialeconomy.block.entity;
import com.industrialeconomy.block.BuyingHopperBlock;
import com.industrialeconomy.commercial.*;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.plot.*;
import com.industrialeconomy.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
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
    public static double getDistanceBonus(ServerLevel sl, UUID ownerId, BlockPos hopperPos) {
        double minDist = Double.MAX_VALUE;
        for (IndustrialPlot p : PlotSavedData.get(sl).getAll()) {
            if (ownerId.equals(p.ownerId)) {
                double cx = (p.minX + p.maxX) * 0.5; double cz = (p.minZ + p.maxZ) * 0.5;
                double d = Math.hypot(cx - hopperPos.getX(), cz - hopperPos.getZ());
                if (d < minDist) minDist = d;
            }
        }
        if (minDist == Double.MAX_VALUE || minDist <= 100.0) return 0.0;
        return Math.min(0.30, Math.floor((minDist - 100.0) / 50.0) * 0.01);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, BuyingHopperBlockEntity be) {
        if (level.isClientSide || !(level instanceof ServerLevel sl) || sl.getGameTime() % 10 != 0) return;
        if (!(state.getBlock() instanceof BuyingHopperBlock hb)) return;
        MarketCategory cat = hb.getCategory();
        List<ItemEntity> list = sl.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(0.2, 0.8, 0.2));
        if (list.isEmpty()) return;
        ServerPlayer nearest = sl.players().stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))).orElse(null);
        if (nearest == null || nearest.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 256) return;
        CommercialPoint cp = CommercialSavedData.get(sl).getAt(pos);
        boolean isWarehouse = cp != null && cp.level >= 3 && nearest.getUUID().equals(cp.ownerId);
        double distBonus = getDistanceBonus(sl, nearest.getUUID(), pos);
        nearest.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
            EconomySavedData eco = EconomySavedData.get(sl);
            long earned = 0L;
            for (ItemEntity ie : list) {
                ItemStack st = ie.getItem();
                if (st.isEmpty() || st.isDamaged() || st.isEnchanted()) continue;
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(st.getItem());
                if (id == null) continue;
                MarketItemEntry entry = ItemCatalogManager.getItem(id.toString());
                if (entry == null || !f.hasPatent(entry.patentId) || !entry.category.equals(cat.name())) continue;
                int effCount = isWarehouse ? Math.max(1, st.getCount() / 2) : st.getCount();
                long basePay = eco.registerSaleAndGetPayout(id.toString(), effCount);
                if (isWarehouse && st.getCount() > effCount) basePay = Math.round(basePay * ((double) st.getCount() / effCount));
                double bonus = 1.0 + f.getCorpTier().sellBonus + distBonus + ((cp != null && nearest.getUUID().equals(cp.ownerId)) ? cp.getSellCashback() : 0.0);
                earned += Math.round(basePay * bonus);
                ie.discard();
            }
            if (earned > 0) { f.addCash(earned); FinanceEvents.syncToClient(nearest, f); }
        });
    }
}
