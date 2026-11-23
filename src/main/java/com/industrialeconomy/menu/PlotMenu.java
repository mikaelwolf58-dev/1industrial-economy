package com.industrialeconomy.menu;
import com.industrialeconomy.plot.PlotTier;
import com.industrialeconomy.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
public class PlotMenu extends AbstractContainerMenu {
    public final String plotId; public final PlotTier tier; public final boolean hasOwner; public final boolean isMine; public final String ownerName;
    public PlotMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readUtf(), PlotTier.valueOf(buf.readUtf()), buf.readBoolean(), buf.readBoolean(), buf.readUtf());
    }
    public PlotMenu(int id, Inventory inv, String plotId, PlotTier tier, boolean hasOwner, boolean isMine, String ownerName) {
        super(ModMenuTypes.PLOT_MENU.get(), id);
        this.plotId = plotId; this.tier = tier; this.hasOwner = hasOwner; this.isMine = isMine; this.ownerName = ownerName;
    }
    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return true; }
}
