package com.industrialeconomy.finance;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class PlayerFinanceProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerFinanceData> PLAYER_FINANCE = CapabilityManager.get(new CapabilityToken<>() {});
    private PlayerFinanceData finance = null;
    private final LazyOptional<PlayerFinanceData> optional = LazyOptional.of(this::createFinanceData);
    private PlayerFinanceData createFinanceData() { if (this.finance == null) this.finance = new PlayerFinanceData(); return this.finance; }
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) { return cap == PLAYER_FINANCE ? optional.cast() : LazyOptional.empty(); }
    @Override public CompoundTag serializeNBT() { CompoundTag nbt = new CompoundTag(); createFinanceData().saveNBTData(nbt); return nbt; }
    @Override public void deserializeNBT(CompoundTag nbt) { createFinanceData().loadNBTData(nbt); }
}
