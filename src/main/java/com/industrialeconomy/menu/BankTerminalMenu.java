package com.industrialeconomy.menu;
import com.industrialeconomy.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
public class BankTerminalMenu extends AbstractContainerMenu {
    public BankTerminalMenu(int id, Inventory inv, FriendlyByteBuf data) { this(id, inv); }
    public BankTerminalMenu(int id, Inventory inv) { super(ModMenuTypes.BANK_TERMINAL_MENU.get(), id); }
    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return true; }
}
