package com.industrialeconomy.finance;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
public class PlayerFinanceData {
    private long cash = 0L; private long bankBalance = 0L;
    private final Set<Integer> patents = new HashSet<>();
    public long getCash() { return cash; }
    public void setCash(long v) { this.cash = Math.max(0L, v); }
    public void addCash(long v) { if (v > 0) this.cash += v; }
    public boolean spendCash(long v) { if (v <= 0) return true; if (cash < v) return false; cash -= v; return true; }
    public long getBankBalance() { return bankBalance; }
    public void setBankBalance(long v) { this.bankBalance = v; }
    public void addBank(long v) { if (v > 0) this.bankBalance += v; }
    public boolean spendBank(long v) { if (v <= 0) return true; if (bankBalance < v) return false; bankBalance -= v; return true; }
    public boolean depositToBank(long v) { if (v <= 0 || cash < v) return false; cash -= v; bankBalance += v; return true; }
    public boolean withdrawFromBank(long v) { if (v <= 0 || bankBalance < v) return false; bankBalance -= v; cash += v; return true; }
    public boolean hasPatent(int id) { return id <= 0 || patents.contains(id); }
    public void unlockPatent(int id) { if (id > 0) patents.add(id); }
    public Set<Integer> getPatents() { return patents; }
    public int getPatentMask() { int m = 0; for (int id : patents) m |= (1  BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) { return l.isClientSide ? null : (lvl, pos, st, be) -> { if (be instanceof BuyingHopperBlockEntity b) BuyingHopperBlockEntity.tick(lvl, pos, st, b); }; }
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
                long totalEarned = eco.sellItem(id.toString(), count);
                f.addCash(totalEarned);
                FinanceEvents.syncToClient(nearest, f);
                nearest.displayClientMessage(Component.literal("Sold x" + count + " -> +" + String.format("%,d$", totalEarned)).withStyle(ChatFormatting.GREEN), true);
                itemEntity.discard();
            });
        }
        super.stepOn(level, pos, state, entity);
    }
}
