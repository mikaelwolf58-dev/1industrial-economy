package com.industrialeconomy.client.screen;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.menu.ShopTerminalMenu;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.BuyItemC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
public class ShopTerminalScreen extends AbstractContainerScreen<ShopTerminalMenu> {
    private int page = 0; private final List<MarketItemEntry> items = new ArrayList<>();
    public ShopTerminalScreen(ShopTerminalMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 300; this.imageHeight = 230; }
    @Override protected void init() {
        super.init(); items.clear();
        for (MarketItemEntry e : ItemCatalogManager.getAll()) if (e.category.equals(menu.category.name()) && e.baseBuyPrice > 0) items.add(e);
        rebuildButtons();
    }
    private boolean isBoutique() { return menu.category == MarketCategory.BOUTIQUE; }
    private int perPage() { return isBoutique() ? 4 : 7; }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2; int startY = y + 28;
        if (isBoutique()) {
            var lots = BoutiqueRotationManager.getCurrentLots(ClientFinanceData.getDayTime());
            for (int i = 0; i < lots.size(); i++) {
                var lot = lots.get(i); int ry = startY + i * 22;
                this.addRenderableWidget(Button.builder(Component.literal("Buy"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(lot.id(), 1))).bounds(x + 246, ry, 42, 18).build());
            }
            startY += 72;
        }
        int pp = perPage(); int start = page * pp;
        for (int i = 0; i < pp && start + i < items.size(); i++) {
            MarketItemEntry e = items.get(start + i); int ry = startY + i * 22;
            this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(e.itemId, 1))).bounds(x + 224, ry, 30, 18).build());
            if (!isBoutique()) this.addRenderableWidget(Button.builder(Component.literal("+64"), b -> ModMessages.sendToServer(new BuyItemC2SPacket(e.itemId, 64))).bounds(x + 256, ry, 34, 18).build());
        }
        if (page > 0) this.addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildButtons(); }).bounds(x + 12, y + 204, 24, 18).build());
        if ((page + 1) * pp < items.size()) this.addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildButtons(); }).bounds(x + 264, y + 204, 24, 18).build());
    }
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xEE181C22); g.renderOutline(x, y, imageWidth, imageHeight, 0xFF2196F3);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); super.render(g, mx, my, pt);
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.drawString(this.font, menu.category.name() + " | Cash: " + String.format("%,d$", ClientFinanceData.getCash()), x + 12, y + 10, 0x55FF55);
        ItemStack hov = ItemStack.EMPTY; int startY = y + 28;
        if (isBoutique()) {
            var lots = BoutiqueRotationManager.getCurrentLots(ClientFinanceData.getDayTime());
            for (int i = 0; i < lots.size(); i++) {
                var lot = lots.get(i); int ry = startY + i * 22;
                g.fill(x + 10, ry - 1, x + 242, ry + 19, 0x44FFD700);
                g.renderItem(lot.stack(), x + 12, ry + 1);
                if (mx >= x + 12 && mx <= x + 28 && my >= ry + 1 && my <= ry + 17) hov = lot.stack();
                g.drawString(this.font, lot.title(), x + 32, ry + 5, 0xFFD700);
                g.drawString(this.font, String.format("%,d$", lot.price()), x + 185, ry + 5, 0xFF55FF);
            }
            startY += 72;
        }
        int pp = perPage(); int start = page * pp;
        for (int i = 0; i < pp && start + i < items.size(); i++) {
            MarketItemEntry e = items.get(start + i); int ry = startY + i * 22;
            Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.itemId));
            if (it != null) { ItemStack st = new ItemStack(it); g.renderItem(st, x + 12, ry + 1); if (mx >= x + 12 && mx <= x + 28 && my >= ry + 1 && my <= ry + 17) hov = st; g.drawString(this.font, st.getHoverName().getString(), x + 32, ry + 5, 0xFFFFFF); }
            long price = ClientFinanceData.getBuy(e.itemId, e.baseBuyPrice);
            g.drawString(this.font, String.format("%,d$", price), x + 170, ry + 5, 0xFFFF55);
        }
        if (!hov.isEmpty()) g.renderTooltip(this.font, hov, mx, my); else this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
