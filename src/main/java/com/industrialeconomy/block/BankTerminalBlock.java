package com.industrialeconomy.block;
import com.industrialeconomy.menu.BankTerminalMenu;
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
public class BankTerminalBlock extends Block {
    public BankTerminalBlock(Properties p) { super(p); }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos pos, Player p, InteractionHand h, BlockHitResult hit) {
        if (!l.isClientSide && p instanceof ServerPlayer sp) NetworkHooks.openScreen(sp, new SimpleMenuProvider((id, inv, pl) -> new BankTerminalMenu(id, inv), Component.literal("Bank Terminal")));
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
