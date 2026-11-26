package com.industrialeconomy.network.packet;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Supplier;
public class BuyItemC2SPacket {
    private final String itemId; private final int count;
    public BuyItemC2SPacket(String itemId, int count) { this.itemId = itemId; this.count = count; }
    public BuyItemC2SPacket(FriendlyByteBuf buf) { this.itemId = buf.readUtf(); this.count = buf.readInt(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(itemId); buf.writeInt(count); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer sp = sup.get().getSender(); if (sp == null || count <= 0) return;
            ServerLevel sl = sp.serverLevel();
            for (var lot : BoutiqueRotationManager.getCurrentLots(sl.getDayTime())) {
                if (lot.id().equals(itemId)) {
                    sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                        if (!f.spendCash(lot.price()) && !f.spendBank(lot.price())) { sp.sendSystemMessage(Component.literal("Not enough funds (" + String.format("%,d$", lot.price()) + ")!").withStyle(ChatFormatting.RED)); return; }
                        deliverToChestOrPlayer(sl, sp, lot.stack().copy());
                        FinanceEvents.syncToClient(sp, f); sp.sendSystemMessage(Component.literal("Purchased Limited Lot: " + lot.title()).withStyle(ChatFormatting.LIGHT_PURPLE));
                    }); return;
                }
            }
            MarketItemEntry entry = ItemCatalogManager.getItem(itemId); if (entry == null || entry.baseBuyPrice <= 0) return;
            EconomySavedData eco = EconomySavedData.get(sl); long unit = eco.getDynamicBuyPrice(itemId); long total = unit * count;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!f.spendCash(total) && !f.spendBank(total)) { sp.sendSystemMessage(Component.literal("Not enough funds (" + String.format("%,d$", total) + ")!").withStyle(ChatFormatting.RED)); return; }
                Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if (it != null) {
                    int rem = count;
                    while (rem > 0) {
                        int batch = Math.min(rem, it.getMaxStackSize());
                        ItemStack st = new ItemStack(it, batch);
                        if ("BOUTIQUE".equals(entry.category)) BoutiqueRotationManager.applyStandardBoutiqueEnchants(st);
                        deliverToChestOrPlayer(sl, sp, st);
                        rem -= batch;
                    }
                }
                eco.registerPurchase(itemId, count);
                FinanceEvents.syncToClient(sp, f);
            });
        }); return true;
    }
    private static void deliverToChestOrPlayer(ServerLevel sl, ServerPlayer sp, ItemStack stack) {
        BlockPos center = sp.blockPosition();
        for (int dx = -10; dx <= 10; dx++) for (int dy = -4; dy <= 5; dy++) for (int dz = -10; dz <= 10; dz++) {
            if (stack.isEmpty()) return;
            if (sl.getBlockEntity(center.offset(dx, dy, dz)) instanceof Container cont) {
                for (int slot = 0; slot < cont.getContainerSize(); slot++) {
                    if (stack.isEmpty()) break;
                    ItemStack ex = cont.getItem(slot);
                    if (ex.isEmpty()) { cont.setItem(slot, stack.copy()); stack.setCount(0); cont.setChanged(); }
                    else if (ItemStack.isSameItemSameTags(ex, stack) && ex.getCount() < ex.getMaxStackSize()) {
                        int add = Math.min(stack.getCount(), ex.getMaxStackSize() - ex.getCount());
                        ex.grow(add); stack.shrink(add); cont.setChanged();
                    }
                }
            }
        }
        if (!stack.isEmpty() && !sp.getInventory().add(stack)) sp.drop(stack, false);
    }
}
