package com.industrialeconomy.commercial;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
public class CommercialSavedData extends SavedData {
    private final Map<String, CommercialPoint> points = new LinkedHashMap<>();
    public static CommercialSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(CommercialSavedData::load, CommercialSavedData::new, "industrial_economy_commercial");
    }
    public static CommercialSavedData load(CompoundTag tag) {
        CommercialSavedData d = new CommercialSavedData();
        ListTag list = tag.getList("Points", 10);
        for (int i = 0; i != list.size(); i++) { CommercialPoint c = CommercialPoint.load(list.getCompound(i)); d.points.put(c.id, c); }
        return d;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (CommercialPoint c : points.values()) list.add(c.save());
        tag.put("Points", list); return tag;
    }
    public void addPoint(CommercialPoint c) { points.put(c.id, c); setDirty(); }
    public int nextNumber() { return points.size() + 1; }
    public CommercialPoint getById(String id) { return points.get(id); }
    public CommercialPoint getAt(BlockPos pos) { for (CommercialPoint c : points.values()) if (c.contains(pos)) return c; return null; }
    public Collection<CommercialPoint> getAll() { return points.values(); }
}
