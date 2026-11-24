package com.industrialeconomy.client.screen;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.economy.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
public class NewspaperScreen extends Screen {
    private int tab = 0; private int page = 0; private List<MarketItemEntry> allItems = List.of();
    public NewspaperScreen() { super(Component.literal("The Industrial Times")); }
    @Override protected void init() { super.init(); this.allItems = new ArrayList<>(ItemCatalogManager.getAll()); rebuildButtons(); }
    private int maxPage() { return tab == 1 ? Math.max(0, (allItems.size() - 1) / 6) : Math.max(0, (ClientFinanceData.HUBS.size() - 1) / 6); }
    @Override public boolean mouseScrolled(double mx, double my, double delta) {
        if (delta > 0 && page > 0) { page--; return true; }
        if (delta < 0 && page != maxPage()) { page++; return true; }
        return super.mouseScrolled(mx, my, delta);
    }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - 300) / 2; int y = (this.height - 210) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Top Market"), b -> { tab = 0; page = 0; rebuildButtons(); }).bounds(x + 10, y + 26, 90, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Price Catalog"), b -> { tab = 1; page = 0; rebuildButtons(); }).bounds(x + 105, y + 26, 90, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("World Map"), b -> { tab = 2; page = 0; rebuildButtons(); }).bounds(x + 200, y + 26, 90, 18).build());
        if (tab > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("Prev"), b -> { if (page > 0) page--; }).bounds(x + 12, y + 184, 55, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("Next"), b -> { if (page != maxPage()) page++; }).bounds(x + 233, y + 184, 55, 18).build());
        }
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int x = (this.width - 300) / 2; int y = (this.height - 210) / 2;
        g.fill(x, y, x + 300, y + 210, 0xF0221E1A); g.renderOutline(x, y, 300, 210, 0xFFD4AF37);
        g.drawString(this.font, "THE INDUSTRIAL TIMES | Bank: " + String.format("%,d$", ClientFinanceData.getBankBalance()), x + 12, y + 10, 0xFFFF55);
        super.render(g, mx, my, pt);
        ItemStack hovered = ItemStack.EMPTY;
        if (tab == 0) {
            List<MarketItemEntry> sorted = new ArrayList<>(allItems);
            sorted.sort((a, b) -> Double.compare((double) ClientFinanceData.getSell(b.itemId, b.baseSellPrice) / b.baseSellPrice, (double) ClientFinanceData.getSell(a.itemId, a.baseSellPrice) / a.baseSellPrice));
            g.drawString(this.font, "HIGH DEMAND (Best to Sell):", x + 12, y + 52, 0x55FF55);
            for (int i = 0; i != Math.min(5, sorted.size()); i++) {
                MarketItemEntry e = sorted.get(i); int ry = y + 66 + i * 20;
                Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.itemId));
                if (it != null) g.renderItem(new ItemStack(it), x + 12, ry - 2);
                long s = ClientFinanceData.getSell(e.itemId, e.baseSellPrice);
                g.drawString(this.font, e.itemId.substring(e.itemId.indexOf(":") + 1) + " -> " + s + "$ (base " + e.baseSellPrice + "$)", x + 32, ry + 2, 0xFFFFFF);
            }
        } else if (tab == 1) {
            g.drawString(this.font, "Item                    Buy / Sell", x + 32, y + 50, 0xAAAAAA);
            int st = page * 6;
            for (int i = 0; i != 6 && (st + i) != allItems.size(); i++) {
                MarketItemEntry e = allItems.get(st + i); int ry = y + 64 + i * 19;
                Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.itemId));
                if (it != null) { ItemStack stk = new ItemStack(it); g.renderItem(stk, x + 12, ry - 2); if (mx >= x + 10 && mx <= x + 280 && my >= ry && my <= ry + 18) hovered = stk; }
                long buy = ClientFinanceData.getBuy(e.itemId, e.baseBuyPrice);
                long sell = ClientFinanceData.getSell(e.itemId, e.baseSellPrice);
                String name = e.itemId.substring(e.itemId.indexOf(":") + 1);
                if (name.length() > 18) name = name.substring(0, 18);
                String patTag = e.patentId > 0 ? " [P" + e.patentId + "]" : "";
                g.drawString(this.font, name + patTag, x + 32, ry + 2, ClientFinanceData.hasPatent(e.patentId) ? 0xFFFFFF : 0xFF7777);
                g.drawString(this.font, buy + "$ / " + sell + "$", x + 195, ry + 2, 0x55FFFF);
            }
            g.drawString(this.font, "Page " + (page + 1) + "/" + (maxPage() + 1), x + 125, y + 189, 0xAAAAAA);
        } else {
            var pl = Minecraft.getInstance().player;
            var hubs = new ArrayList<>(ClientFinanceData.HUBS);
            if (pl != null) hubs.sort(Comparator.comparingDouble(h -> Math.hypot(h.x() - pl.getX(), h.z() - pl.getZ())));
            int st = page * 6;
            for (int i = 0; i != 6 && (st + i) != hubs.size(); i++) {
                var h = hubs.get(st + i); int ry = y + 56 + i * 20;
                int dist = pl == null ? 0 : (int) Math.hypot(h.x() - pl.getX(), h.z() - pl.getZ());
                g.drawString(this.font, "[" + h.type() + "] X:" + h.x() + " Y:" + h.y() + " Z:" + h.z() + " (" + dist + "m)", x + 14, ry, 0xFFFF55);
            }
            g.drawString(this.font, "Page " + (page + 1) + "/" + (maxPage() + 1), x + 125, y + 189, 0xAAAAAA);
        }
        if (!hovered.isEmpty()) g.renderTooltip(this.font, hovered, mx, my);
    }
}
