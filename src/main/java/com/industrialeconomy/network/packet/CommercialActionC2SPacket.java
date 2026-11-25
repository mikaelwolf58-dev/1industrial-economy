package com.industrialeconomy.network.packet;
import com.industrialeconomy.commercial.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import com.industrialeconomy.world.CustomStructurePlacer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class CommercialActionC2SPacket {
    private final String pointId; private final int action;
    public CommercialActionC2SPacket(String pointId, int action) { this.pointId = pointId; this.action = action; }
    public CommercialActionC2SPacket(FriendlyByteBuf buf) { this.pointId = buf.readUtf(); this.action = buf.readInt(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(pointId); buf.writeInt(action); }
    public boolean handle(Supplier<NetworkEvent.Context> sup) {
        sup.get().enqueueWork(() -> {
            ServerPlayer sp = sup.get().getSender(); if (sp == null) return;
            ServerLevel sl = sp.serverLevel(); CommercialSavedData data = CommercialSavedData.get(sl);
            CommercialPoint cp = data.getById(pointId); if (cp == null) return;
            sp.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                if (action == 0 && cp.ownerId == null) {
                    if (!f.spendBank(cp.basePrice) && !f.spendCash(cp.basePrice)) {
                        sp.sendSystemMessage(Component.literal("Not enough funds (" + String.format("%,d$", cp.basePrice) + ")!").withStyle(ChatFormatting.RED)); return;
                    }
                    cp.ownerId = sp.getUUID(); cp.ownerName = sp.getGameProfile().getName(); cp.level = 1; data.setDirty();
                    FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Purchased " + cp.title + " (#" + cp.id + ")!").withStyle(ChatFormatting.GREEN));
                } else if (action == 1 && sp.getUUID().equals(cp.ownerId) && cp.level < 3) {
                    long cost = cp.getUpgradeCost();
                    if (!f.spendBank(cost) && !f.spendCash(cost)) {
                        sp.sendSystemMessage(Component.literal("Not enough funds (" + String.format("%,d$", cost) + ")!").withStyle(ChatFormatting.RED)); return;
                    }
                    cp.level++; data.setDirty();
                    if (cp.level == 3) {
                        CustomStructurePlacer.placeCustom(sl, cp.getOrigin(), "commercial_warehouse", cp.rotation, cp.category);
                        sp.sendSystemMessage(Component.literal("Upgraded to Level 3: Commercial Warehouse constructed!").withStyle(ChatFormatting.GOLD));
                    } else {
                        sp.sendSystemMessage(Component.literal("Upgraded " + cp.title + " to Level " + cp.level + "!").withStyle(ChatFormatting.GREEN));
                    }
                    FinanceEvents.syncToClient(sp, f);
                } else if (action == 2 && sp.getUUID().equals(cp.ownerId)) {
                    long refund = cp.getTotalInvested() / 2L;
                    cp.ownerId = null; cp.ownerName = null; cp.level = 1; data.setDirty();
                    f.addBank(refund); FinanceEvents.syncToClient(sp, f);
                    sp.sendSystemMessage(Component.literal("Sold " + cp.title + " for 50% (" + String.format("%,d$", refund) + ")").withStyle(ChatFormatting.YELLOW));
                }
            });
        });
        return true;
    }
}
