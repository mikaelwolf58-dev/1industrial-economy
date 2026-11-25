package com.industrialeconomy.client.screen;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.finance.CorporationTier;
import net.minecraft.ChatFormatting;
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
    private int tab = 0, page = 0, catIdx = 0;
    private static final String[] CATS = {"ALL", "METALS", "BUILDING", "WOOD", "AGRICULTURE", "FOOD", "FUEL", "EQUIPMENT", "RARE"};
    public NewspaperScreen() { super(Component.literal("The Industrial Times")); }
    @Override protected void init() { super.init(); rebuildButtons(); }
    private List<MarketItemEntry> filtered() {
        List<MarketItemEntry> out = new ArrayList<>(); String sel = CATS[catIdx];
        for (MarketItemEntry e : ItemCatalogManager.getAll()) if ("ALL".equals(sel) || e.category.equals(sel)) out.add(e);
        return out;
    }
    @Override public boolean mouseScrolled(double mx, double my, double delta) {
        if (tab == 0 || tab == 1) {
            int sz = tab == 0 ? filtered().size() : ClientFinanceData.HUBS.size();
            int maxPage = Math.max(0, (sz - 1) / 7);
            if (delta < 0 && page < maxPage) { page++; rebuildButtons(); return true; }
            if (delta > 0 && page > 0) { page--; rebuildButtons(); return true; }
        }
        return super.mouseScrolled(mx, my, delta);
    }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - 310) / 2; int y = (this.height - 225) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Market Quotes"), b -> { tab = 0; page = 0; rebuildButtons(); }).bounds(x + 8, y + 8, 96, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Directory & GPS"), b -> { tab = 1; page = 0; rebuildButtons(); }).bounds(x + 108, y + 8, 96, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("My Empire"), b -> { tab = 2; page = 0; rebuildButtons(); }).bounds(x + 208, y + 8, 94, 18).build());
        if (tab == 0) {
            this.addRenderableWidget(Button.builder(Component.literal("Filter: " + CATS[catIdx]), b -> { catIdx = (catIdx + 1) % CATS.length; page = 0; rebuildButtons(); }).bounds(x + 12, y + 30, 140, 18).build());
            int total = filtered().size();
            if (page > 0) this.addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildButtons(); }).bounds(x + 12, y + 200, 24, 18).build());
            if ((page + 1) * 7 < total) this.addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildButtons(); }).bounds(x + 274, y + 200, 24, 18).build());
        } else if (tab == 1) {
            int start = page * 7;
            for (int i = 0; i < 7 && start + i < ClientFinanceData.HUBS.size(); i++) {
                var h = ClientFinanceData.HUBS.get(start + i); int ry = y + 34 + i * 22;
                this.addRenderableWidget(Button.builder(Component.literal("GPS"), b -> {
                    ClientFinanceData.trackedHub = h.type(); ClientFinanceData.trackX = h.x(); ClientFinanceData.trackZ = h.z();
                    if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.sendSystemMessage(Component.literal("[GPS] Target: " + h.type() + " at X=" + h.x() + ", Z=" + h.z()).withStyle(ChatFormatting.AQUA));
                }).bounds(x + 260, ry, 38, 18).build());
            }
            if (page > 0) this.addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildButtons(); }).bounds(x + 12, y + 200, 24, 18).build());
            if ((page + 1) * 7 < ClientFinanceData.HUBS.size()) this.addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildButtons(); }).bounds(x + 274, y + 200, 24, 18).build());
        }
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); int x = (this.width - 310) / 2; int y = (this.height - 225) / 2;
        g.fill(x, y, x + 310, y + 225, 0xF01C1B18); g.renderOutline(x, y, 310, 225, 0xFFD4AF37);
        super.render(g, mx, my, pt); ItemStack hov = ItemStack.EMPTY;
        if (tab == 0) {
            var list = filtered(); int start = page * 7; g.drawString(this.font, "Buy / Sell / Trend", x + 165, y + 35, 0xAAAAAA);
            for (int i = 0; i < 7 && start + i < list.size(); i++) {
                MarketItemEntry e = list.get(start + i); int ry = y + 52 + i * 20;
                Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.itemId));
                if (it != null) { ItemStack st = new ItemStack(it); g.renderItem(st, x + 12, ry - 2); if (mx >= x + 12 && mx <= x + 28 && my >= ry - 2 && my <= ry + 14) hov = st; g.drawString(this.font, st.getHoverName().getString(), x + 32, ry + 2, 0xFFFFFF); }
                var pp = ClientFinanceData.PRICES.get(e.itemId); long buy = pp != null ? pp.buy() : e.baseBuyPrice; long sell = pp != null ? pp.sell() : e.baseSellPrice;
                int pct = (e.baseSellPrice > 0 && pp != null) ? (int) Math.round(((double)(sell - e.baseSellPrice) / e.baseSellPrice) * 100.0) : 0;
                g.drawString(this.font, (buy > 0 ? buy + "$" : "-") + " / " + (sell > 0 ? sell + "$" : "-"), x + 165, ry + 2, 0xFFFF55);
                g.drawString(this.font, pct > 0 ? ("+" + pct + "%") : (pct < 0 ? (pct + "%") : "0%"), x + 266, ry + 2, pct > 0 ? 0x55FF55 : (pct < 0 ? 0xFF5555 : 0xAAAAAA));
            }
        } else if (tab == 1) {
            var pl = Minecraft.getInstance().player; int start = page * 7;
            for (int i = 0; i < 7 && start + i < ClientFinanceData.HUBS.size(); i++) {
                var h = ClientFinanceData.HUBS.get(start + i); int ry = y + 38 + i * 22;
                int dist = pl != null ? (int) Math.hypot(pl.getX() - h.x(), pl.getZ() - h.z()) : 0;
                g.drawString(this.font, h.type() + " [" + h.x() + ", " + h.z() + "] (" + dist + "m)", x + 14, ry + 2, 0xFFFFFF);
            }
            if (!ClientFinanceData.trackedHub.isEmpty() && pl != null) {
                int d = (int) Math.hypot(pl.getX() - ClientFinanceData.trackX, pl.getZ() - ClientFinanceData.trackZ);
                g.drawString(this.font, "Active GPS: " + ClientFinanceData.trackedHub + " (" + d + " blocks away)", x + 44, y + 204, 0x55FFFF);
            }
        } else {
            CorporationTier cur = CorporationTier.byLevel(ClientFinanceData.getCorpLevel());
            g.drawString(this.font, "=== CORPORATE FINANCIAL SUMMARY ===", x + 16, y + 38, 0xFFD700);
            g.drawString(this.font, "Status: " + cur.title + " (Sell Bonus +" + (int)(cur.sellBonus * 100) + "%)", x + 16, y + 58, 0xFFFF55);
            g.drawString(this.font, "Cash: " + String.format("%,d$", ClientFinanceData.getCash()) + " | Bank: " + String.format("%,d$", ClientFinanceData.getBankBalance()), x + 16, y + 76, 0x55FF55);
            g.drawString(this.font, "Weekly Commercial Income: +" + String.format("%,d$/week", ClientFinanceData.getWeeklyIncome()), x + 16, y + 98, 0x55FFFF);
            g.drawString(this.font, "Weekly Land Tax: -" + String.format("%,d$/week", ClientFinanceData.getWeeklyTax()), x + 16, y + 116, 0xFFAA00);
            g.drawString(this.font, "Logistics Distance Bonus: up to +30% (1% per 50m > 100m)", x + 16, y + 142, 0xAAAAAA);
            if (ClientFinanceData.getTaxDebt() > 0L) g.drawString(this.font, "OVERDUE DEBT: " + String.format("%,d$", ClientFinanceData.getTaxDebt()), x + 16, y + 164, 0xFF5555);
        }
        if (!hov.isEmpty()) g.renderTooltip(this.font, hov, mx, my);
    }
    @Override public boolean isPauseScreen() { return false; }
}
