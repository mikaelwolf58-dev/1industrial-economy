package com.industrialeconomy.client.screen;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.economy.IndustryPatent;
import com.industrialeconomy.finance.CorporationTier;
import com.industrialeconomy.menu.BankTerminalMenu;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
public class BankTerminalScreen extends AbstractContainerScreen<BankTerminalMenu> {
    private int tab = 0;
    public BankTerminalScreen(BankTerminalMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 285; this.imageHeight = 205; }
    @Override protected void init() { super.init(); rebuildButtons(); }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Account"), b -> { tab = 0; rebuildButtons(); }).bounds(x + 10, y + 8, 84, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Patents"), b -> { tab = 1; rebuildButtons(); }).bounds(x + 100, y + 8, 84, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Corporation"), b -> { tab = 2; rebuildButtons(); }).bounds(x + 190, y + 8, 84, 18).build());
        if (tab == 0) {
            long[] amts = {100L, 1000L, 10000L};
            for (int i = 0; i != 3; i++) {
                long a = amts[i]; int bx = x + 16 + i * 86;
                this.addRenderableWidget(Button.builder(Component.literal("Dep " + a + "$"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(true, a))).bounds(bx, y + 95, 82, 20).build());
                this.addRenderableWidget(Button.builder(Component.literal("Wit " + a + "$"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(false, a))).bounds(bx, y + 122, 82, 20).build());
            }
            this.addRenderableWidget(Button.builder(Component.literal("Deposit ALL"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(true, ClientFinanceData.getCash()))).bounds(x + 16, y + 152, 120, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Withdraw ALL"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(false, Math.max(0L, ClientFinanceData.getBankBalance())))).bounds(x + 144, y + 152, 120, 20).build());
        } else if (tab == 1) {
            IndustryPatent[] list = {IndustryPatent.FOOD_PROCESSING, IndustryPatent.HEAVY_MATERIALS, IndustryPatent.PRECISION_TECH};
            for (int i = 0; i != 3; i++) {
                IndustryPatent pat = list[i]; int rowY = y + 36 + i * 52;
                if (!ClientFinanceData.hasPatent(pat.id)) {
                    this.addRenderableWidget(Button.builder(Component.literal("Buy (" + String.format("%,d$", pat.price) + ")"), b -> ModMessages.sendToServer(new BuyPatentC2SPacket(pat.id))).bounds(x + 175, rowY + 2, 98, 18).build());
                }
            }
        } else {
            int lvl = ClientFinanceData.getCorpLevel();
            if (lvl < 4) {
                CorporationTier next = CorporationTier.byLevel(lvl + 1);
                this.addRenderableWidget(Button.builder(Component.literal("Upgrade (" + String.format("%,d$", next.cost) + ")"), b -> ModMessages.sendToServer(new UpgradeCorpC2SPacket())).bounds(x + 16, y + 158, 252, 22).build());
            }
        }
    }
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xEE1A1D24); g.renderOutline(x, y, imageWidth, imageHeight, 0xFF4CAF50);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); super.render(g, mx, my, pt);
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        ItemStack hovered = ItemStack.EMPTY;
        if (tab == 0) {
            g.drawString(this.font, "Cash (Pocket): " + String.format("%,d$", ClientFinanceData.getCash()), x + 16, y + 40, 0x55FF55);
            g.drawString(this.font, "Bank Balance: " + String.format("%,d$", ClientFinanceData.getBankBalance()), x + 16, y + 58, 0x55FFFF);
            g.drawString(this.font, "Weekly Deposit Interest: +2.0% (every 7 days)", x + 16, y + 76, 0xFFAA00);
        } else if (tab == 1) {
            IndustryPatent[] list = {IndustryPatent.FOOD_PROCESSING, IndustryPatent.HEAVY_MATERIALS, IndustryPatent.PRECISION_TECH};
            for (int i = 0; i != 3; i++) {
                IndustryPatent pat = list[i]; int rowY = y + 36 + i * 52;
                g.drawString(this.font, pat.title, x + 12, rowY + 4, 0xFFFF55);
                if (ClientFinanceData.hasPatent(pat.id)) g.drawString(this.font, "[UNLOCKED]", x + 200, rowY + 4, 0x55FF55);
                for (int k = 0; k != Math.min(11, pat.previewItems.size()); k++) {
                    Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(pat.previewItems.get(k)));
                    if (it != null) {
                        ItemStack st = new ItemStack(it); int ix = x + 12 + k * 18; int iy = rowY + 22;
                        g.renderItem(st, ix, iy);
                        if (mx >= ix && mx <= ix + 16 && my >= iy && my <= iy + 16) hovered = st;
                    }
                }
            }
        } else {
            CorporationTier cur = CorporationTier.byLevel(ClientFinanceData.getCorpLevel());
            g.drawString(this.font, "Current Status: " + cur.title + " (Lvl " + (cur.level + 1) + "/5)", x + 16, y + 40, 0xFFFF55);
            g.drawString(this.font, "Max Industrial Plots: " + cur.maxPlots, x + 16, y + 58, 0xFFFFFF);
            g.drawString(this.font, "Sell Revenue Bonus: +" + (int)(cur.sellBonus * 100) + "%", x + 16, y + 76, 0x55FF55);
            if (cur.level < 4) {
                CorporationTier next = CorporationTier.byLevel(cur.level + 1);
                g.drawString(this.font, "Next Tier: " + next.title, x + 16, y + 106, 0x55FFFF);
                g.drawString(this.font, "Grants: " + next.maxPlots + " plots & +" + (int)(next.sellBonus * 100) + "% revenue", x + 16, y + 122, 0xAAAAAA);
            } else {
                g.drawString(this.font, "Maximum Monopolist Status Reached!", x + 16, y + 115, 0xFFD700);
            }
        }
        if (!hovered.isEmpty()) g.renderTooltip(this.font, hovered, mx, my); else this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
