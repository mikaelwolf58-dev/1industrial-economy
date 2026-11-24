package com.industrialeconomy.client.screen;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.economy.IndustryPatent;
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
    public BankTerminalScreen(BankTerminalMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 280; this.imageHeight = 200; }
    @Override protected void init() { super.init(); rebuildButtons(); }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Account / Cash"), b -> { tab = 0; rebuildButtons(); }).bounds(x + 12, y + 8, 125, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Patents (Licenses)"), b -> { tab = 1; rebuildButtons(); }).bounds(x + 143, y + 8, 125, 18).build());
        if (tab == 0) {
            long[] amts = {100L, 1000L, 10000L};
            for (int i = 0; i != 3; i++) {
                long a = amts[i]; int bx = x + 16 + i * 86;
                this.addRenderableWidget(Button.builder(Component.literal("Dep " + a + "$"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(true, a))).bounds(bx, y + 95, 82, 20).build());
                this.addRenderableWidget(Button.builder(Component.literal("Wit " + a + "$"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(false, a))).bounds(bx, y + 122, 82, 20).build());
            }
            this.addRenderableWidget(Button.builder(Component.literal("Deposit ALL"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(true, ClientFinanceData.getCash()))).bounds(x + 16, y + 152, 120, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Withdraw ALL"), b -> ModMessages.sendToServer(new BankTransactionC2SPacket(false, Math.max(0L, ClientFinanceData.getBankBalance())))).bounds(x + 144, y + 152, 120, 20).build());
        } else {
            IndustryPatent[] list = {IndustryPatent.FOOD_PROCESSING, IndustryPatent.HEAVY_MATERIALS, IndustryPatent.PRECISION_TECH};
            for (int i = 0; i != 3; i++) {
                IndustryPatent pat = list[i]; int rowY = y + 36 + i * 52;
                if (!ClientFinanceData.hasPatent(pat.id)) {
                    this.addRenderableWidget(Button.builder(Component.literal("Buy (" + String.format("%,d$", pat.price) + ")"), b -> ModMessages.sendToServer(new BuyPatentC2SPacket(pat.id))).bounds(x + 170, rowY + 2, 98, 18).build());
                }
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
            g.drawString(this.font, "Cash (Pocket): " + String.format("%,d$", ClientFinanceData.getCash()), x + 16, y + 42, 0x55FF55);
            g.drawString(this.font, "Bank Balance: " + String.format("%,d$", ClientFinanceData.getBankBalance()), x + 16, y + 60, 0x55FFFF);
        } else {
            IndustryPatent[] list = {IndustryPatent.FOOD_PROCESSING, IndustryPatent.HEAVY_MATERIALS, IndustryPatent.PRECISION_TECH};
            for (int i = 0; i != 3; i++) {
                IndustryPatent pat = list[i]; int rowY = y + 36 + i * 52;
                g.drawString(this.font, pat.title, x + 12, rowY + 4, 0xFFFF55);
                if (ClientFinanceData.hasPatent(pat.id)) g.drawString(this.font, "[UNLOCKED]", x + 195, rowY + 4, 0x55FF55);
                for (int k = 0; k != Math.min(11, pat.previewItems.size()); k++) {
                    Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(pat.previewItems.get(k)));
                    if (it != null) {
                        ItemStack st = new ItemStack(it); int ix = x + 12 + k * 18; int iy = rowY + 22;
                        g.renderItem(st, ix, iy);
                        if (mx >= ix && mx <= ix + 16 && my >= iy && my <= iy + 16) hovered = st;
                    }
                }
            }
        }
        if (!hovered.isEmpty()) g.renderTooltip(this.font, hovered, mx, my); else this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
