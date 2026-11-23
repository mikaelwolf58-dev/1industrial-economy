package com.industrialeconomy.world;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
public class WorldHubSavedData extends SavedData {
    public record HubInfo(String type, int x, int y, int z, int radius) {}
    private final List<HubInfo> hubs = new ArrayList<>();
    private final Set<Long> checkedRegions = new HashSet<>();
    public static WorldHubSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(WorldHubSavedData::load, WorldHubSavedData::new, "industrial_economy_hubs");
    }
    public static WorldHubSavedData load(CompoundTag tag) {
        WorldHubSavedData d = new WorldHubSavedData();
        ListTag list = tag.getList("Hubs", 10);
        for (int i = 0; i != list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            d.hubs.add(new HubInfo(c.getString("T"), c.getInt("X"), c.getInt("Y"), c.getInt("Z"), c.getInt("R")));
        }
        for (long k : tag.getLongArray("Regions")) d.checkedRegions.add(k);
        return d;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (HubInfo h : hubs) {
            CompoundTag c = new CompoundTag();
            c.putString("T", h.type); c.putInt("X", h.x); c.putInt("Y", h.y); c.putInt("Z", h.z); c.putInt("R", h.radius);
            list.add(c);
        }
        tag.put("Hubs", list);
        tag.putLongArray("Regions", checkedRegions.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }
    public boolean markRegionChecked(int rx, int rz) {
        long key = (((long) rx) << 32) | (rz & 0xFFFFFFFFL);
        if (!checkedRegions.add(key)) return false;
        setDirty(); return true;
    }
    public void addHub(String type, BlockPos pos, int radius) {
        hubs.add(new HubInfo(type, pos.getX(), pos.getY(), pos.getZ(), radius));
        setDirty();
    }
    public boolean isProtected(BlockPos pos) {
        for (HubInfo h : hubs) {
            if (Math.abs(pos.getX() - h.x) <= h.radius && Math.abs(pos.getZ() - h.z) <= h.radius && Math.abs(pos.getY() - h.y) <= 12) return true;
        }
        return false;
    }
    public List<HubInfo> getHubs() { return hubs; }
}
