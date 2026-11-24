package com.industrialeconomy.plot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;
public class IndustrialPlot {
    public String id; public PlotTier tier; public int minX, minZ, maxX, maxZ, baseY;
    public UUID ownerId; public String ownerName;
    public IndustrialPlot(String id, PlotTier tier, int minX, int minZ, int baseY) {
        this.id = id; this.tier = tier; this.minX = minX; this.minZ = minZ;
        this.maxX = minX + tier.size - 1; this.maxZ = minZ + tier.size - 1; this.baseY = baseY;
    }
    public boolean contains(BlockPos pos) {
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ && pos.getY() >= baseY - 5 && pos.getY() <= baseY + 32;
    }
    public long getWeeklyTax() {
        if (ownerId == null || tier.price <= 20000L) return 0L;
        return Math.round(tier.price * 0.02);
    }
    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putString("Id", id); t.putString("Tier", tier.name());
        t.putInt("MinX", minX); t.putInt("MinZ", minZ); t.putInt("BaseY", baseY);
        if (ownerId != null) { t.putUUID("OwnerId", ownerId); t.putString("OwnerName", ownerName == null ? "Player" : ownerName); }
        return t;
    }
    public static IndustrialPlot load(CompoundTag t) {
        IndustrialPlot p = new IndustrialPlot(t.getString("Id"), PlotTier.valueOf(t.getString("Tier")), t.getInt("MinX"), t.getInt("MinZ"), t.getInt("BaseY"));
        if (t.hasUUID("OwnerId")) { p.ownerId = t.getUUID("OwnerId"); p.ownerName = t.getString("OwnerName"); }
        return p;
    }
}
