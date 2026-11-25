package com.industrialeconomy.network.packet;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
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
                        if (!f.spendCash(lot.price()) && !f.spendBank(lot.price())) {
                            sp.sendSystemMessage(Component.literal("Not enough funds (" + String.format("%,d$", lot.price()) + ")!").withStyle(ChatFormatting.RED)); return;
                        }
                        ItemStack give = lot.stack().copy();
                        if (!sp.getInventory().add(give)) sp.drop(give, false);
                        FinanceEvents.syncToClient(sp, f);
                        sp.sendSystemMessage(Component.literal("Purchased Limited Lot: " + lot.title()).withStyle(ChatFormatting.LIGHT_PURPLE));
                    });
                    return;
                }
            }
            MarketItemEntry entry = ItemCatalogManager.getItem(itemId); if (entry == null || entry.baseBuyPrice <= 0) return;
            EconomySavedData eco = EconomySavedData.get(sl);
            long unit = eco.getDynamicBuyPrice(itemId); long total = unit * count;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (!f.spendCash(total) && !f.spendBank(total)) {
                    sp.sendSystemMessage(Component.literal("Not enough funds (" + String.format("%,d$", total) + ")!").withStyle(ChatFormatting.RED)); return;
                }
                Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if (it != null) {
                    ItemStack st = new ItemStack(it, count);
                    if ("BOUTIQUE".equals(entry.category)) applyVanillaMaxEnchants(st);
                    if (!sp.getInventory().add(st)) sp.drop(st, false);
                }
                FinanceEvents.syncToClient(sp, f);
            });
        });
        return true;
    }
    private static void applyVanillaMaxEnchants(ItemStack st) {
        if (st.getItem() instanceof ArmorItem) { st.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4); st.enchant(Enchantments.UNBREAKING, 3); st.enchant(Enchantments.MENDING, 1); }
        else if (st.getItem() instanceof SwordItem) { st.enchant(Enchantments.SHARPNESS, 5); st.enchant(Enchantments.MOB_LOOTING, 3); st.enchant(Enchantments.UNBREAKING, 3); st.enchant(Enchantments.MENDING, 1); }
        else if (st.getItem() instanceof PickaxeItem || st.getItem() instanceof AxeItem || st.getItem() instanceof ShovelItem) { st.enchant(Enchantments.BLOCK_EFFICIENCY, 5); st.enchant(Enchantments.BLOCK_FORTUNE, 3); st.enchant(Enchantments.UNBREAKING, 3); st.enchant(Enchantments.MENDING, 1); }
        else if (st.getItem() instanceof BowItem) { st.enchant(Enchantments.POWER_ARROWS, 5); st.enchant(Enchantments.INFINITY_ARROWS, 1); st.enchant(Enchantments.UNBREAKING, 3); }
        else if (st.getItem() instanceof TridentItem) { st.enchant(Enchantments.IMPALING, 5); st.enchant(Enchantments.LOYALTY, 3); st.enchant(Enchantments.MENDING, 1); }
    }
}
