package com.industrialeconomy.menu;
import com.industrialeconomy.commercial.CommercialPoint;
import com.industrialeconomy.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;
public class CommercialMenu extends AbstractContainerMenu {
    public final String pointId; public final String title; public final long basePrice; public final int level;
    public final boolean hasOwner; public final boolean isMine; public final String ownerName;
    public CommercialMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(ModMenus.COMMERCIAL_MENU.get(), id);
        this.pointId = buf.readUtf(); this.title = buf.readUtf(); this.basePrice = buf.readLong(); this.level = buf.readInt();
        this.hasOwner = buf.readBoolean(); this.isMine = buf.readBoolean(); this.ownerName = buf.readUtf();
    }
    public CommercialMenu(int id, Inventory inv, CommercialPoint cp, UUID viewer) {
        super(ModMenus.COMMERCIAL_MENU.get(), id);
        this.pointId = cp.id; this.title = cp.title; this.basePrice = cp.basePrice; this.level = cp.level;
        this.hasOwner = cp.ownerId != null;
        this.isMine = this.hasOwner && cp.ownerId.equals(viewer);
        this.ownerName = this.hasOwner ? (cp.ownerName == null ? "Player" : cp.ownerName) : "None";
    }
    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return true; }
}
