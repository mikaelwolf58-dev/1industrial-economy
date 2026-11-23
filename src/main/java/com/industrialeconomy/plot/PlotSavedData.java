package com.industrialeconomy.plot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
public class PlotSavedData extends SavedData {
    private final Map<String, IndustrialPlot> plots = new LinkedHashMap<>();
    public static PlotSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(PlotSavedData::load, PlotSavedData::new, "industrial_economy_plots");
    }
    public static PlotSavedData load(CompoundTag tag) {
        PlotSavedData d = new PlotSavedData();
        ListTag list = tag.getList("Plots", 10);
        for (int i = 0; i != list.size(); i++) { IndustrialPlot p = IndustrialPlot.load(list.getCompound(i)); d.plots.put(p.id, p); }
        return d;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (IndustrialPlot p : plots.values()) list.add(p.save());
        tag.put("Plots", list); return tag;
    }
    public void addPlot(IndustrialPlot p) { plots.put(p.id, p); setDirty(); }
    public int nextNumber() { return plots.size() + 1; }
    public IndustrialPlot getPlotAt(BlockPos pos) { for (IndustrialPlot p : plots.values()) if (p.contains(pos)) return p; return null; }
    public IndustrialPlot getPlotById(String id) { return plots.get(id); }
    public Collection<IndustrialPlot> getAll() { return plots.values(); }
}
