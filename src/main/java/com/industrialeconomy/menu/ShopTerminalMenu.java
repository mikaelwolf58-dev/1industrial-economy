package com.industrialeconomy.menu;
import com.industrialeconomy.economy.MarketCategory;
import com.industrialeconomy.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
public class ShopTerminalMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final MarketCategory category;
    public ShopTerminalMenu(int id, Inventory inv, FriendlyByteBuf data) { this(id, inv, data.readBlockPos(), MarketCategory.valueOf(data.readUtf())); }
    public ShopTerminalMenu(int id, Inventory inv, BlockPos pos, MarketCategory cat) {
        super(ModMenuTypes.SHOP_TERMINAL_MENU.get(), id); this.pos = pos; this.category = cat;
    }
    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return true; }
}
