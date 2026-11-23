package com.industrialeconomy.network.packet;
import com.industrialeconomy.economy.EconomySavedData;
import com.industrialeconomy.economy.ItemCatalogManager;
import com.industrialeconomy.economy.MarketItemEntry;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Supplier;
public class BuyItemC2SPacket {
    private final BlockPos terminalPos; private final String itemId; private final int count;
    public BuyItemC2SPacket(BlockPos pos, String itemId, int count) { this.terminalPos = pos; this.itemId = itemId; this.count = count; }
    public BuyItemC2SPacket(FriendlyByteBuf buf) { this.terminalPos = buf.readBlockPos(); this.itemId = buf.readUtf(); this.count = buf.readInt(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeBlockPos(terminalPos); buf.writeUtf(itemId); buf.writeInt(count); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || count == 0) return;
            MarketItemEntry entry = ItemCatalogManager.getItem(itemId);
            if (entry == null) return;
            ServerLevel level = player.serverLevel();
            Container targetChest = findNearbyChest(level, terminalPos, 10);
            if (targetChest == null) { player.sendSystemMessage(Component.literal("В§cNo delivery chest found near terminal (radius 10)!")); return; }
            EconomySavedData eco = EconomySavedData.get(level);
            long totalCost = eco.getDynamicBuyPrice(itemId) * count;
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) return;
            player.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!f.spendCash(totalCost)) { player.sendSystemMessage(Component.literal("В§cNot enough cash! Need: " + totalCost + "$")); return; }
                ItemStack toInsert = new ItemStack(item, count);
                ItemStack rem = insertIntoContainer(targetChest, toInsert);
                if (!rem.isEmpty()) player.getInventory().placeItemBackInInventory(rem);
                FinanceEvents.syncToClient(player, f);
                player.sendSystemMessage(Component.literal("В§aPurchased " + count + "x " + itemId + " -> delivered to pallet chest!"));
            });
        });
        return true;
    }
    private static Container findNearbyChest(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -3, -r), center.offset(r, 3, r))) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof Container c && hasFreeSpace(c)) return c;
        }
        return null;
    }
    private static boolean hasFreeSpace(Container c) { for (int i = 0; i != c.getContainerSize(); i++) if (c.getItem(i).isEmpty()) return true; return false; }
    private static ItemStack insertIntoContainer(Container c, ItemStack stack) {
        for (int i = 0; i != c.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack slot = c.getItem(i);
            if (slot.isEmpty()) { c.setItem(i, stack.copy()); stack.setCount(0); }
            else if (ItemStack.isSameItemSameTags(slot, stack) && slot.getCount() != slot.getMaxStackSize()) {
                int add = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(add); stack.shrink(add);
            }
        }
        c.setChanged(); return stack;
    }
}
