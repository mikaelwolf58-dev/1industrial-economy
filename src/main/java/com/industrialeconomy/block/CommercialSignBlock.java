package com.industrialeconomy.block;
import com.industrialeconomy.commercial.*;
import com.industrialeconomy.menu.CommercialMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
public class CommercialSignBlock extends Block {
    public CommercialSignBlock(Properties props) { super(props); }
    @Override public InteractionResult use(BlockState st, Level lvl, BlockPos pos, Player pl, InteractionHand hand, BlockHitResult hit) {
        if (!lvl.isClientSide && pl instanceof ServerPlayer sp && lvl instanceof ServerLevel sl) {
            CommercialPoint cp = CommercialSavedData.get(sl).getAt(pos);
            if (cp == null) { pl.sendSystemMessage(Component.literal("No commercial point linked here.")); return InteractionResult.SUCCESS; }
            final CommercialPoint target = cp;
            NetworkHooks.openScreen(sp, new MenuProvider() {
                @Override public Component getDisplayName() { return Component.literal(target.title); }
                @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new CommercialMenu(id, inv, target, sp.getUUID()); }
            }, buf -> {
                buf.writeUtf(target.id); buf.writeUtf(target.title); buf.writeLong(target.basePrice); buf.writeInt(target.level);
                boolean hasOwner = target.ownerId != null;
                buf.writeBoolean(hasOwner);
                buf.writeBoolean(hasOwner && target.ownerId.equals(sp.getUUID()));
                buf.writeUtf(hasOwner ? (target.ownerName == null ? "Player" : target.ownerName) : "None");
            });
        }
        return InteractionResult.sidedSuccess(lvl.isClientSide);
    }
}
