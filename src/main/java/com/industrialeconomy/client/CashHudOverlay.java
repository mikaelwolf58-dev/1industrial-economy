package com.industrialeconomy.client;
import com.industrialeconomy.IndustrialEconomy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = IndustrialEconomy.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CashHudOverlay {
    public static final IGuiOverlay HUD_CASH = (gui, g, pt, w, h) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.player.isSpectator()) return;
        String text = String.format("Money: %,d$", ClientFinanceData.getCash());
        Font font = mc.font;
        int x = (w - font.width(text)) / 2; int y = h - 49;
        g.drawString(font, text, x + 1, y + 1, 0x000000, false);
        g.drawString(font, text, x, y, 0x55FF55, false);
    };
    @SubscribeEvent public static void registerGuiOverlays(RegisterGuiOverlaysEvent e) { e.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "cash_hud", HUD_CASH); }
}
