package com.industrialeconomy.client.screen;
import com.industrialeconomy.client.ClientFinanceData;
import com.industrialeconomy.menu.BankTerminalMenu;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.BankTransactionC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class BankTerminalScreen extends AbstractContainerScreen<BankTerminalMenu> {
    private EditBox amountBox;
    public BankTerminalScreen(BankTerminalMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 210; this.imageHeight = 150; }
    @Override protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        this.amountBox = new EditBox(this.font, x + 20, y + 65, 170, 20, Component.literal("Amount"));
        this.amountBox.setFilter(s -> s.matches("\\d*")); this.amountBox.setValue("100");
        this.addRenderableWidget(this.amountBox);
        this.addRenderableWidget(Button.builder(Component.literal("Deposit"), b -> { long v = parseAmount(); if (v > 0) ModMessages.sendToServer(new BankTransactionC2SPacket(true, v)); }).bounds(x + 20, y + 95, 82, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Withdraw"), b -> { long v = parseAmount(); if (v > 0) ModMessages.sendToServer(new BankTransactionC2SPacket(false, v)); }).bounds(x + 108, y + 95, 82, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Deposit All"), b -> { long a = ClientFinanceData.getCash(); if (a > 0) ModMessages.sendToServer(new BankTransactionC2SPacket(true, a)); }).bounds(x + 20, y + 120, 170, 20).build());
    }
    private long parseAmount() { try { return Long.parseLong(amountBox.getValue()); } catch (Exception e) { return 0L; } }
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xEE1E1E24); g.renderOutline(x, y, imageWidth, imageHeight, 0xFF4CAF50);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); super.render(g, mx, my, pt);
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.drawString(this.font, "ATM TERMINAL", x + 20, y + 12, 0x55FF55);
        g.drawString(this.font, String.format("Cash (Risk): %,d$", ClientFinanceData.getCash()), x + 20, y + 30, 0xFFFF55);
        g.drawString(this.font, String.format("Bank (Safe): %,d$", ClientFinanceData.getBank()), x + 20, y + 44, 0x55FFFF);
        this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
