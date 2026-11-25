package com.industrialeconomy.block.entity;
import com.industrialeconomy.block.BuyingHopperBlock;
import com.industrialeconomy.commercial.*;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    public BuyingHopperBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.BUYING_HOPPER_BE.get(), pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, BuyingHopperBlockEntity be) {
        if (level.isClientSide || level.getGameTime() % 10 != 0 || !(level instanceof ServerLevel sl)) return;
        if (!(state.getBlock() instanceof BuyingHopperBlock hb)) return;
        AABB box = new AABB(pos).inflate(0.25, 1.0, 0.25);
        List<ItemEntity> list = sl.getEntitiesOfClass(ItemEntity.class, box);
        if (list.isEmpty()) return;
        EconomySavedData eco = EconomySavedData.get(sl);
        CommercialPoint cp = CommercialSavedData.get(sl).getAt(pos);
        for (ItemEntity ie : list) {
            if (!ie.isAlive()) continue;
            ItemStack st = ie.getItem();
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(st.getItem());
            if (rl == null) continue;
            MarketItemEntry entry = ItemCatalogManager.getItem(rl.toString());
            if (entry == null || entry.category != hb.category || entry.baseSellPrice <= 0) continue;
            ServerPlayer sp = sl.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12.0, false);
            if (sp == null) continue;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!f.hasPatent(entry.patentId)) {
                    if (sl.getGameTime() % 40 == 0) sp.sendSystemMessage(Component.literal("Requires Patent #" + entry.patentId + " to sell this item!").withStyle(ChatFormatting.RED));
                    return;
                }
                int count = st.getCount();
                long unit = eco.getDynamicSellPrice(entry.itemId);
                double bonus = 1.0 + f.getCorpTier().sellBonus + ((cp != null && sp.getUUID().equals(cp.ownerId)) ? cp.getSellCashback() : 0.0);
                long total = Math.max(1L, Math.round(unit * count * bonus));
                eco.recordSale(entry.itemId, count);
                f.addCash(total); FinanceEvents.syncToClient(sp, f);
                sp.displayClientMessage(Component.literal("Sold " + count + "x -> +" + String.format("%,d$", total)).withStyle(ChatFormatting.GREEN), true);
                ie.discard();
            });
        }
    }
}
