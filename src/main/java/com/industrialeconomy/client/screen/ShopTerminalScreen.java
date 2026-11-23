package com.industrialeconomy.client.screen;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.economy.ItemCatalogManager;
import com.industrialeconomy.economy.MarketItemEntry;
import com.industrialeconomy.menu.ShopTerminalMenu;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.BuyItemC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;
import java.util.stream.Collectors;
public class ShopTerminalScreen extends AbstractContainerScreen<ShopTerminalMenu> {
    private List<MarketItemEntry> items; private int page = 0;
    public ShopTerminalScreen(ShopTerminalMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 285; this.imageHeight = 195; }
    @Override protected void init() {
        super.init();
        this.items = ItemCatalogManager.getAll().stream().filter(e -> e.category.equals(menu.category.name())).collect(Collectors.toList());
        rebuildButtons();
    }
    private int maxPage() { return Math.max(0, (items.size() - 1) / 5); }
    @Override public boolean mouseScrolled(double mx, double my, double delta) {
        if (delta > 0 && page > 0) { page--; rebuildButtons(); return true; }
        if (delta < 0 && page != maxPage()) { page++; rebuildButtons(); return true; }
        return super.mouseScrolled(mx, my, delta);
    }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        int start = page * 5;
        for (int i = 0; i != 5 && (start + i) != items.size(); i++) {
            MarketItemEntry entry = items.get(start + i);
            int rowY = y + 36 + i * 24;
            this.addRenderableWidget(Button.builder(Component.literal("x1"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(menu.pos, entry.itemId, 1))).bounds(x + 178, rowY, 26, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("x16"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(menu.pos, entry.itemId, 16))).bounds(x + 207, rowY, 32, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("x64"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(menu.pos, entry.itemId, 64))).bounds(x + 242, rowY, 32, 18).build());
        }
        if (page > 0) this.addRenderableWidget(Button.builder(Component.literal("Prev"), b -> { page--; rebuildButtons(); }).bounds(x + 12, y + 166, 55, 18).build());
        if (page != maxPage()) this.addRenderableWidget(Button.builder(Component.literal("Next"), b -> { page++; rebuildButtons(); }).bounds(x + 218, y + 166, 55, 18).build());
    }
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xEE1A1D24); g.renderOutline(x, y, imageWidth, imageHeight, 0xFF2196F3);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); super.render(g, mx, my, pt);
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.drawString(this.font, "SHOP: " + menu.category.name() + " | Cash: " + String.format("%,d$", ClientFinanceData.getCash()), x + 12, y + 12, 0x55FFFF);
        int start = page * 5;
        for (int i = 0; i != 5 && (start + i) != items.size(); i++) {
            MarketItemEntry entry = items.get(start + i);
            int rowY = y + 36 + i * 24;
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.itemId));
            if (item != null) g.renderItem(new ItemStack(item), x + 12, rowY + 1);
            String shortName = entry.itemId.substring(entry.itemId.indexOf(":") + 1);
            if (shortName.length() > 16) shortName = shortName.substring(0, 16);
            g.drawString(this.font, shortName + " (" + entry.baseBuyPrice + "$)", x + 32, rowY + 5, 0xFFFFFF);
        }
        g.drawString(this.font, "Page " + (page + 1) + "/" + (maxPage() + 1), x + 115, y + 171, 0xAAAAAA);
        this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
