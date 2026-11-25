package com.industrialeconomy.commercial;
import com.industrialeconomy.economy.MarketCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import java.util.UUID;
public class CommercialPoint {
    public String id; public String title; public MarketCategory category;
    public int originX, originY, originZ; public Rotation rotation;
    public long basePrice; public int level = 1;
    public UUID ownerId; public String ownerName;
    public CommercialPoint(String id, String title, MarketCategory cat, BlockPos o, Rotation rot) {
        this.id = id; this.title = title; this.category = cat;
        this.originX = o.getX(); this.originY = o.getY(); this.originZ = o.getZ();
        this.rotation = rot; this.basePrice = getCategoryPrice(cat);
    }
    public static long getCategoryPrice(MarketCategory c) {
        return switch (c) {
            case FUEL, AGRICULTURE -> 150000L;
            case WOOD -> 220000L;
            case BUILDING, FOOD -> 350000L;
            case METALS -> 450000L;
            case EQUIPMENT, RARE -> 650000L;
            default -> 0L;
        };
    }
    public BlockPos getOrigin() { return new BlockPos(originX, originY, originZ); }
    public boolean contains(BlockPos pos) {
        return pos.getX() >= originX - 2 && pos.getX() <= originX + 18 && pos.getZ() >= originZ - 2 && pos.getZ() <= originZ + 18 && pos.getY() >= originY - 3 && pos.getY() <= originY + 24;
    }
    public long getUpgradeCost() { return level == 1 ? basePrice : (level == 2 ? basePrice * 2L : 0L); }
    public double getSellCashback() { return ownerId == null ? 0.0 : (level == 1 ? 0.15 : (level == 2 ? 0.25 : 0.40)); }
    public long getWeeklyPassiveIncome() {
        if (ownerId == null) return 0L;
        double mult = level == 1 ? 1.0 : (level == 2 ? 1.8 : 3.24);
        return Math.round(basePrice * 0.07 * mult);
    }
    public long getTotalInvested() { return basePrice + (level >= 2 ? basePrice : 0L) + (level >= 3 ? basePrice * 2L : 0L); }
    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putString("Id", id); t.putString("Title", title); t.putString("Cat", category.name());
        t.putInt("OX", originX); t.putInt("OY", originY); t.putInt("OZ", originZ);
        t.putString("Rot", rotation.name()); t.putLong("Price", basePrice); t.putInt("Lvl", level);
        if (ownerId != null) { t.putUUID("OwnerId", ownerId); t.putString("OwnerName", ownerName == null ? "Player" : ownerName); }
        return t;
    }
    public static CommercialPoint load(CompoundTag t) {
        CommercialPoint c = new CommercialPoint(t.getString("Id"), t.getString("Title"), MarketCategory.valueOf(t.getString("Cat")), new BlockPos(t.getInt("OX"), t.getInt("OY"), t.getInt("OZ")), Rotation.valueOf(t.getString("Rot")));
        if (t.contains("Price")) c.basePrice = t.getLong("Price");
        c.level = Math.max(1, Math.min(3, t.getInt("Lvl")));
        if (t.hasUUID("OwnerId")) { c.ownerId = t.getUUID("OwnerId"); c.ownerName = t.getString("OwnerName"); }
        return c;
    }
}
