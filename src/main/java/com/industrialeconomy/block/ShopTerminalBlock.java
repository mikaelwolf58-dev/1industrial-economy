package com.industrialeconomy.block;
import com.industrialeconomy.economy.MarketCategory;
import com.industrialeconomy.menu.ShopTerminalMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
public class ShopTerminalBlock extends Block {
    private final MarketCategory category;
    public ShopTerminalBlock(Properties p, MarketCategory category) { super(p); this.category = category; }
    public MarketCategory getCategory() { return category; }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos pos, Player p, InteractionHand h, BlockHitResult hit) {
        if (!l.isClientSide && p instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, new SimpleMenuProvider((id, inv, pl) -> new ShopTerminalMenu(id, inv, pos, category), Component.literal("Shop: " + category.name())), buf -> {
                buf.writeBlockPos(pos); buf.writeUtf(category.name());
            });
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
