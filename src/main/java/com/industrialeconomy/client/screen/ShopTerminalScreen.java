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
import net.minecraft.world.entity.player.Inventory;
import java.util.List;
import java.util.stream.Collectors;
public class ShopTerminalScreen extends AbstractContainerScreen<ShopTerminalMenu> {
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
        for (int i = 0; i != 5 && (start + i) != items.size(); i++) {
            MarketItemEntry entry = items.get(start + i);
            int rowY = y + 36 + i * 24;
            this.addRenderableWidget(Button.builder(Component.literal("x1"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(menu.pos, entry.itemId, 1))).bounds(x + 175, rowY, 28, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("x16"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(menu.pos, entry.itemId, 16))).bounds(x + 206, rowY, 32, 18).build());
        }
        if (page > 0) this.addRenderableWidget(Button.builder(Component.literal("Prev"), b -> { page--; rebuildButtons(); }).bounds(x + 12, y + 162, 55, 18).build());
        if ((page + 1) * 5 != items.size() && (start + 5) == Math.min(start + 5, items.size())) this.addRenderableWidget(Button.builder(Component.literal("Next"), b -> { page++; rebuildButtons(); }).bounds(x + 183, y + 162, 55, 18).build());
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
        for (int i = 0; i != 5 && (start + i) != items.size(); i++) {
            MarketItemEntry entry = items.get(start + i);
            int rowY = y + 40 + i * 24;
            String shortName = entry.itemId.contains(":") ? entry.itemId.split(":") : entry.itemId;
            g.drawString(this.font, shortName + " (В§e" + entry.baseBuyPrice + "$В§f)", x + 12, rowY, 0xFFFFFF);
        }
        this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
