package com.industrialeconomy.client.screen;
import com.industrialeconomy.menu.PlotMenu;
import com.industrialeconomy.network.ModMessages;
import com.industrialeconomy.network.packet.PlotActionC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class PlotScreen extends AbstractContainerScreen<PlotMenu> {
    private boolean confirmSell = false;
    public PlotScreen(PlotMenu m, Inventory inv, Component t) { super(m, inv, t); this.imageWidth = 240; this.imageHeight = 165; }
    @Override protected void init() { super.init(); rebuildButtons(); }
    private void rebuildButtons() {
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        if (!menu.hasOwner) {
            String label = "Buy Plot (" + String.format("%,d$", menu.tier.price) + ")";
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> { ModMessages.sendToServer(new PlotActionC2SPacket(menu.plotId, false)); this.onClose(); }).bounds(x + 20, y + 128, 200, 22).build());
        } else if (menu.isMine) {
            if (!confirmSell) {
                String label = "Sell Plot (50% = " + String.format("%,d$", menu.tier.price / 2L) + ")";
                this.addRenderableWidget(Button.builder(Component.literal(label), b -> { confirmSell = true; rebuildButtons(); }).bounds(x + 20, y + 128, 200, 22).build());
            } else {
                this.addRenderableWidget(Button.builder(Component.literal("CONFIRM SELL (CLEAR ALL)"), b -> { ModMessages.sendToServer(new PlotActionC2SPacket(menu.plotId, true)); this.onClose(); }).bounds(x + 20, y + 128, 145, 22).build());
                this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> { confirmSell = false; rebuildButtons(); }).bounds(x + 170, y + 128, 50, 22).build());
            }
        }
    }
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xEE181C22); g.renderOutline(x, y, imageWidth, imageHeight, 0xFFFF9800);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g); super.render(g, mx, my, pt);
        int x = (this.width - this.imageWidth) / 2; int y = (this.height - this.imageHeight) / 2;
        g.drawString(this.font, "INDUSTRIAL PLOT #" + menu.plotId, x + 16, y + 12, 0xFFFF55);
        g.drawString(this.font, "Size: " + menu.tier.size + "x" + menu.tier.size + " (" + menu.tier.name() + ")", x + 16, y + 32, 0xFFFFFF);
        String mLim = menu.tier.maxMachinesPerType >= 900 ? "Unlimited" : String.valueOf(menu.tier.maxMachinesPerType);
        String sLim = menu.tier.maxSteamEngines >= 900 ? "Unlimited" : String.valueOf(menu.tier.maxSteamEngines);
        g.drawString(this.font, "Machines limit (per type): " + mLim, x + 16, y + 48, 0x55FFFF);
        g.drawString(this.font, "Steam Engines limit: " + sLim, x + 16, y + 64, 0x55FFFF);
        String st = !menu.hasOwner ? "Status: FOR SALE" : "Owner: " + menu.ownerName;
        g.drawString(this.font, st, x + 16, y + 82, !menu.hasOwner ? 0x55FF55 : 0xAAAAAA);
        if (confirmSell) {
            g.drawString(this.font, "WARNING: All blocks & items", x + 16, y + 100, 0xFF5555);
            g.drawString(this.font, "inside plot will be DELETED!", x + 16, y + 111, 0xFF5555);
        }
        this.renderTooltip(g, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
}
