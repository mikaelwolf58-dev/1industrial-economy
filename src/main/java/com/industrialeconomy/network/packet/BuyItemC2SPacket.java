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
            if (player == null || count <= 0) return;
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
        for (int dx = -r; dx  {
    private List<MarketItemEntry> items; private int page = 0;
    public ShopTerminalScreen(ShopTerminalMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 250; this.imageHeight = 190; }
    @Override protected void init() {
        super.init();
        this.items = ItemCatalogManager.getAll().stream().filter(e -> e.category.equals(menu.category.name())).collect(Collectors.toList());
        rebuildButtons();
    }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        int start = page * 5;
        for (int i = 0; i < 5 && (start + i) < items.size(); i++) {
            MarketItemEntry entry = items.get(start + i);
            int rowY = y + 36 + i * 24;
            this.addRenderableWidget(Button.builder(Component.literal("x1"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(menu.pos, entry.itemId, 1))).bounds(x + 175, rowY, 28, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("x16"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(menu.pos, entry.itemId, 16))).bounds(x + 206, rowY, 32, 18).build());
        }
        if (page > 0) this.addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> { page--; rebuildButtons(); }).bounds(x + 12, y + 162, 55, 18).build());
        if ((page + 1) * 5 < items.size()) this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> { page++; rebuildButtons(); }).bounds(x + 183, y + 162, 55, 18).build());
    }
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xEE1A1D24); g.renderOutline(x, y, imageWidth, imageHeight, 0xFF2196F3);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); super.render(g, mx, my, pt);
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.drawString(this.font, "В§bSHOP: " + menu.category.name() + " В§7| В§a" + String.format("%,d$", ClientFinanceData.getCash()), x + 12, y + 12, 0xFFFFFF);
        int start = page * 5;
        for (int i = 0; i < 5 && (start + i) < items.size(); i++) {
            MarketItemEntry entry = items.get(start + i);
            int rowY = y + 40 + i * 24;
            String shortName = entry.itemId.contains(":") ? entry.itemId.split(":")[1] : entry.itemId;
            g.drawString(this.font, shortName + " (В§e" + entry.baseBuyPrice + "$В§f)", x + 12, rowY, 0xFFFFFF);
        }
        this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
