package com.industrialeconomy.client.screen;
import com.industrialeconomy.menu.CommercialMenu;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.CommercialActionC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class CommercialScreen extends AbstractContainerScreen<CommercialMenu> {
    public CommercialScreen(CommercialMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 260; this.imageHeight = 185; }
    @Override protected void init() { super.init(); rebuildButtons(); }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        if (!menu.hasOwner) {
            this.addRenderableWidget(Button.builder(Component.literal("Buy Point (" + String.format("%,d$", menu.basePrice) + ")"), b -> { ModMessages.sendToServer(new CommercialActionC2SPacket(menu.pointId, 0)); this.onClose(); }).bounds(x + 20, y + 145, 220, 22).build());
        } else if (menu.isMine) {
            if (menu.level < 3) {
                long upCost = menu.level == 1 ? menu.basePrice : menu.basePrice * 2L;
                String upLabel = menu.level == 1 ? "Upgrade Lvl 2" : "Upgrade Lvl 3 (Warehouse)";
                this.addRenderableWidget(Button.builder(Component.literal(upLabel + " (" + String.format("%,d$", upCost) + ")"), b -> { ModMessages.sendToServer(new CommercialActionC2SPacket(menu.pointId, 1)); this.onClose(); }).bounds(x + 15, y + 120, 230, 22).build());
            }
            long total = menu.basePrice + (menu.level >= 2 ? menu.basePrice : 0L) + (menu.level >= 3 ? menu.basePrice * 2L : 0L);
            this.addRenderableWidget(Button.builder(Component.literal("Sell Point (50% = " + String.format("%,d$", total / 2L) + ")"), b -> { ModMessages.sendToServer(new CommercialActionC2SPacket(menu.pointId, 2)); this.onClose(); }).bounds(x + 15, y + 148, 230, 22).build());
        }
    }
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xEE181C22); g.renderOutline(x, y, imageWidth, imageHeight, 0xFF00BCD4);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); super.render(g, mx, my, pt);
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.drawString(this.font, menu.title + " (#" + menu.pointId + ")", x + 16, y + 14, 0xFFFF55);
        g.drawString(this.font, "Status: " + (!menu.hasOwner ? "FOR SALE" : "Owner: " + menu.ownerName), x + 16, y + 34, !menu.hasOwner ? 0x55FF55 : 0xAAAAAA);
        g.drawString(this.font, "Level: " + menu.level + " / 3" + (menu.level == 3 ? " [WAREHOUSE]" : ""), x + 16, y + 52, 0x55FFFF);
        int cb = menu.level == 1 ? 15 : (menu.level == 2 ? 25 : 40);
        double mult = menu.level == 1 ? 1.0 : (menu.level == 2 ? 1.8 : 3.24);
        long weekly = Math.round(menu.basePrice * 0.07 * mult);
        g.drawString(this.font, "Owner Sell Cashback: +" + cb + "%", x + 16, y + 72, 0x55FF55);
        g.drawString(this.font, "Weekly Retail Income: +" + String.format("%,d$/week", weekly), x + 16, y + 88, 0xFFAA00);
        this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
