package com.industrialeconomy.plot;
import com.industrialeconomy.economy.RecipeBlocker;
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
    public static boolean isTrackedMachine(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && RecipeBlocker.RESTRICTED_MACHINES.contains(id.toString());
    }
    public static void tickInspection(ServerLevel level) {
        PlotSavedData plots = PlotSavedData.get(level);
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) continue;
            List<BlockPos> illegalMachines = new ArrayList<>();
            BlockPos center = player.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-32, -16, -32), center.offset(32, 16, 32))) {
                if (isTrackedMachine(level.getBlockState(p)) && plots.getPlotAt(p) == null) illegalMachines.add(p.immutable());
            }
            int excess = illegalMachines.size() - 2;
            if (excess <= 0) { RISK_POINTS.put(player.getUUID(), Math.max(0, RISK_POINTS.getOrDefault(player.getUUID(), 0) - 2)); continue; }
            int risk = RISK_POINTS.getOrDefault(player.getUUID(), 0) + excess * 3;
            if (risk >= 30 && level.random.nextInt(100) < Math.min(85, risk)) {
                RISK_POINTS.put(player.getUUID(), 0);
                int confiscated = 0;
                for (int i = 2; i != illegalMachines.size(); i++) {
                    BlockPos target = illegalMachines.get(i);
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.02);
                    confiscated++;
                }
                long fine = confiscated * 1500L;
                player.getCapability(PlayerFinanceProvider.PLAYER_FINANCE).ifPresent(f -> {
                    long paid = Math.min(f.getCash(), fine); f.setCash(f.getCash() - paid);
                    long remFine = fine - paid; if (remFine > 0) f.setBankBalance(Math.max(0L, f.getBankBalance() - remFine));
                    FinanceEvents.syncToClient(player, f);
                });
                level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.6f);
                player.sendSystemMessage(Component.literal("[INDUSTRIAL INSPECTION] Raid! Confiscated " + confiscated + " illegal machines outside plots! Fine: " + String.format("%,d$", fine)).withStyle(ChatFormatting.RED));
            } else {
                RISK_POINTS.put(player.getUUID(), risk);
                if (risk == excess * 3) player.sendSystemMessage(Component.literal("[Inspection Warning] More than 2 machines outside a plot increases raid risk!").withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
