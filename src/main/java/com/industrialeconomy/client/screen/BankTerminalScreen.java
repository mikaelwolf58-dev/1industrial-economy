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
    public BankTerminalScreen(BankTerminalMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 290; this.imageHeight = 230; }
    @Override protected void init() { super.init(); rebuildButtons(); }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Account"), b -> { tab = 0; rebuildButtons(); }).bounds(x + 8, y + 8, 66, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Patents"), b -> { tab = 1; rebuildButtons(); }).bounds(x + 78, y + 8, 66, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Corp"), b -> { tab = 2; rebuildButtons(); }).bounds(x + 148, y + 8, 66, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Loans"), b -> { tab = 3; rebuildButtons(); }).bounds(x + 218, y + 8, 64, 18).build());
        if (tab == 0) {
            long[] amts = {100L, 1000L, 10000L};
            for (int i = 0; i != 3; i++) {
                long a = amts[i]; int bx = x + 16 + i * 88;
                this.addRenderableWidget(Button.builder(Component.literal("Dep " + a + "$"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(true, a))).bounds(bx, y + 98, 84, 18).build());
                this.addRenderableWidget(Button.builder(Component.literal("Wit " + a + "$"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(false, a))).bounds(bx, y + 120, 84, 18).build());
            }
            this.addRenderableWidget(Button.builder(Component.literal("Deposit ALL"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(true, ClientFinanceData.getCash()))).bounds(x + 16, y + 144, 122, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("Withdraw ALL"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(false, Math.max(0L, ClientFinanceData.getBankBalance())))).bounds(x + 148, y + 144, 122, 18).build());
            if (ClientFinanceData.getTaxDebt() > 0L) {
                this.addRenderableWidget(Button.builder(Component.literal("PAY TAX DEBT (" + String.format("%,d$", ClientFinanceData.getTaxDebt()) + ")"), b -> ModMessages.sendToServer(new PayTaxC2SPacket())).bounds(x + 16, y + 170, 254, 20).build());
            }
        } else if (tab == 1) {
            IndustryPatent[] list = {IndustryPatent.FOOD_PROCESSING, IndustryPatent.HEAVY_MATERIALS, IndustryPatent.PRECISION_TECH, IndustryPatent.MACHINERY_TECH};
            for (int i = 0; i != 4; i++) {
                IndustryPatent pat = list[i]; int rowY = y + 32 + i * 46;
                if (!ClientFinanceData.hasPatent(pat.id)) {
                    this.addRenderableWidget(Button.builder(Component.literal("Buy (" + String.format("%,d$", pat.price) + ")"), b -> ModMessages.sendToServer(new BuyPatentC2SPacket(pat.id))).bounds(x + 178, rowY + 2, 102, 18).build());
                }
            }
        } else if (tab == 2) {
            int lvl = ClientFinanceData.getCorpLevel();
            if (lvl < 4) {
                CorporationTier next = CorporationTier.byLevel(lvl + 1);
                this.addRenderableWidget(Button.builder(Component.literal("Upgrade (" + String.format("%,d$", next.cost) + ")"), b -> ModMessages.sendToServer(new UpgradeCorpC2SPacket())).bounds(x + 16, y + 170, 258, 22).build());
            }
        } else {
            if (ClientFinanceData.getLoanDebt() <= 0L) {
                long[] loans = {10000L, 50000L, 250000L};
                for (int i = 0; i != 3; i++) {
                    long amt = loans[i];
                    this.addRenderableWidget(Button.builder(Component.literal("Borrow " + (amt / 1000) + "k$"), b -> ModMessages.sendToServer(new LoanActionC2SPacket(true, amt))).bounds(x + 16 + i * 88, y + 130, 84, 22).build());
                }
            } else {
                this.addRenderableWidget(Button.builder(Component.literal("Pay 10,000$"), b -> ModMessages.sendToServer(new LoanActionC2SPacket(false, 10000L))).bounds(x + 16, y + 130, 122, 22).build());
                this.addRenderableWidget(Button.builder(Component.literal("Repay Full Loan"), b -> ModMessages.sendToServer(new LoanActionC2SPacket(false, ClientFinanceData.getLoanDebt()))).bounds(x + 148, y + 130, 122, 22).build());
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
            g.drawString(this.font, "Cash (Pocket): " + String.format("%,d$", ClientFinanceData.getCash()), x + 16, y + 38, 0x55FF55);
            g.drawString(this.font, "Bank Balance: " + String.format("%,d$", ClientFinanceData.getBankBalance()), x + 16, y + 54, 0x55FFFF);
            if (ClientFinanceData.getTaxDebt() > 0L) g.drawString(this.font, "OVERDUE TAX DEBT: " + String.format("%,d$", ClientFinanceData.getTaxDebt()), x + 16, y + 72, 0xFF5555);
            else g.drawString(this.font, "Deposit: +2%/week | Land Tax: 2%/week (>20k$)", x + 16, y + 72, 0xFFAA00);
        } else if (tab == 1) {
            IndustryPatent[] list = {IndustryPatent.FOOD_PROCESSING, IndustryPatent.HEAVY_MATERIALS, IndustryPatent.PRECISION_TECH, IndustryPatent.MACHINERY_TECH};
            for (int i = 0; i != 4; i++) {
                IndustryPatent pat = list[i]; int rowY = y + 32 + i * 46;
                g.drawString(this.font, pat.title, x + 12, rowY + 4, 0xFFFF55);
                if (ClientFinanceData.hasPatent(pat.id)) g.drawString(this.font, "[UNLOCKED]", x + 205, rowY + 4, 0x55FF55);
                for (int k = 0; k != Math.min(11, pat.previewItems.size()); k++) {
                    Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(pat.previewItems.get(k)));
                    if (it != null) {
                        ItemStack st = new ItemStack(it); int ix = x + 12 + k * 18; int iy = rowY + 20;
                        g.renderItem(st, ix, iy);
                        if (mx >= ix && mx <= ix + 16 && my >= iy && my <= iy + 16) hovered = st;
                    }
                }
            }
        } else if (tab == 2) {
            CorporationTier cur = CorporationTier.byLevel(ClientFinanceData.getCorpLevel());
            g.drawString(this.font, "Status: " + cur.title + " (Lvl " + (cur.level + 1) + "/5)", x + 16, y + 42, 0xFFFF55);
            g.drawString(this.font, "Max Industrial Plots: " + cur.maxPlots, x + 16, y + 60, 0xFFFFFF);
            g.drawString(this.font, "Sell Revenue Bonus: +" + (int)(cur.sellBonus * 100) + "%", x + 16, y + 78, 0x55FF55);
            if (cur.level < 4) {
                CorporationTier next = CorporationTier.byLevel(cur.level + 1);
                g.drawString(this.font, "Next: " + next.title + " (" + next.maxPlots + " plots, +" + (int)(next.sellBonus * 100) + "%)", x + 16, y + 115, 0x55FFFF);
            } else g.drawString(this.font, "Maximum Monopolist Status Reached!", x + 16, y + 115, 0xFFD700);
        } else {
            g.drawString(this.font, "Collateral Loan (Max 50% of Owned Plots)", x + 16, y + 40, 0xFFFF55);
            g.drawString(this.font, "Interest: +10% Fixed | Term: 14 Game Days", x + 16, y + 58, 0xAAAAAA);
            if (ClientFinanceData.getLoanDebt() > 0L) {
                g.drawString(this.font, "Active Loan Debt: " + String.format("%,d$", ClientFinanceData.getLoanDebt()), x + 16, y + 84, 0xFF5555);
                g.drawString(this.font, "Days Remaining: " + ClientFinanceData.getLoanDays(), x + 16, y + 102, 0xFFAA00);
            } else g.drawString(this.font, "No active loans. Select amount below:", x + 16, y + 90, 0x55FF55);
        }
        if (!hovered.isEmpty()) g.renderTooltip(this.font, hovered, mx, my); else this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
