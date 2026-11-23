package com.industrialeconomy.plot;
import com.industrialeconomy.economy.*;
import com.industrialeconomy.event.FinanceEvents;
import com.industrialeconomy.finance.PlayerFinanceProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
public class InspectionManager {
    private static final Map<UUID, Integer> RISK_POINTS = new HashMap<>();
    private static final String STEAM_ID = "create:steam_engine";
    public static String getTrackedId(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id != null && RecipeBlocker.RESTRICTED_MACHINES.contains(id.toString())) return id.toString();
        return null;
    }
    public static void tickInspection(ServerLevel level) {
        PlotSavedData plots = PlotSavedData.get(level);
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) continue;
            BlockPos center = player.blockPosition();
            if (Math.abs(center.getX()) > 1000000 || Math.abs(center.getZ()) > 1000000) continue;
            Map<String, List<BlockPos>> wildByType = new HashMap<>();
            Map<String, Map<String, List<BlockPos>>> plotByType = new HashMap<>();
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-48, -16, -48), center.offset(48, 16, 48))) {
                if (Math.abs(p.getX()) > 1000000 || Math.abs(p.getZ()) > 1000000) continue;
                String mId = getTrackedId(level.getBlockState(p));
                if (mId == null) continue;
                IndustrialPlot plot = plots.getPlotAt(p);
                if (plot == null) wildByType.computeIfAbsent(mId, k -> new ArrayList<>()).add(p.immutable());
                else if (player.getUUID().equals(plot.ownerId)) {
                    plotByType.computeIfAbsent(plot.id, k -> new HashMap<>()).computeIfAbsent(mId, k -> new ArrayList<>()).add(p.immutable());
                }
            }
            List<BlockPos> toConfiscate = new ArrayList<>();
            int riskWeight = 0;
            for (Map.Entry<String, List<BlockPos>> e : wildByType.entrySet()) {
                int allowed = e.getKey().equals(STEAM_ID) ? 0 : 2;
                int weight = e.getKey().equals(STEAM_ID) ? 8 : 1;
                List<BlockPos> list = e.getValue();
                for (int i = allowed; i < list.size(); i++) { toConfiscate.add(list.get(i)); riskWeight += weight; }
            }
            for (Map.Entry<String, Map<String, List<BlockPos>>> pe : plotByType.entrySet()) {
                IndustrialPlot plot = plots.getPlotById(pe.getKey());
                if (plot == null) continue;
                for (Map.Entry<String, List<BlockPos>> me : pe.getValue().entrySet()) {
                    int limit = me.getKey().equals(STEAM_ID) ? plot.tier.maxSteamEngines : plot.tier.maxMachinesPerType;
                    int weight = me.getKey().equals(STEAM_ID) ? 8 : 1;
                    List<BlockPos> list = me.getValue();
                    for (int i = limit; i < list.size(); i++) { toConfiscate.add(list.get(i)); riskWeight += weight; }
                }
            }
            if (toConfiscate.isEmpty()) { RISK_POINTS.put(player.getUUID(), Math.max(0, RISK_POINTS.getOrDefault(player.getUUID(), 0) - 2)); continue; }
            int risk = RISK_POINTS.getOrDefault(player.getUUID(), 0) + riskWeight * 3;
            if (risk >= 30 && level.random.nextInt(100) < Math.min(85, risk)) {
                RISK_POINTS.put(player.getUUID(), 0);
                long totalFine = 0L;
                for (BlockPos target : toConfiscate) {
                    String mId = getTrackedId(level.getBlockState(target));
                    MarketItemEntry entry = mId == null ? null : ItemCatalogManager.getItem(mId);
                    long unitPrice = entry != null ? entry.baseBuyPrice : 1000L;
                    totalFine += Math.round(unitPrice * 0.5);
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.02);
                }
                final long fine = totalFine; final int count = toConfiscate.size();
                player.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                    long fromCash = Math.min(f.getCash(), fine); f.setCash(f.getCash() - fromCash);
                    long remFine = fine - fromCash;
                    long fromBank = Math.min(f.getBankBalance(), remFine); f.setBankBalance(f.getBankBalance() - fromBank);
                    FinanceEvents.syncToClient(player, f);
                    player.sendSystemMessage(Component.literal("[INDUSTRIAL INSPECTION] Raid! Confiscated " + count + " illegal machines! Fine (50% value): " + String.format("%,d$", fine)).withStyle(ChatFormatting.RED));
                    if (fromBank > 0) player.sendSystemMessage(Component.literal("Debited " + String.format("%,d$", fromBank) + " from your bank account by Industrial Inspection!").withStyle(ChatFormatting.GOLD));
                    if (remFine > fromBank) player.sendSystemMessage(Component.literal("Cash & Bank zeroed due to unpaid debt. Fine settled.").withStyle(ChatFormatting.GRAY));
                });
                level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.6f);
            } else {
                RISK_POINTS.put(player.getUUID(), risk);
                if (risk == riskWeight * 3) player.sendSystemMessage(Component.literal("[Inspection Warning] Illegal machine placement detected! Raid risk is growing!").withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
