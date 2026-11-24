package com.industrialeconomy.economy;
import java.util.List;
public enum IndustryPatent {
    NONE(0, "None", 0L, List.of()),
    FOOD_PROCESSING(1, "Food & Bakery Patent", 65000L, List.of("minecraft:cake", "minecraft:pumpkin_pie", "minecraft:golden_carrot", "minecraft:golden_apple", "minecraft:honey_bottle", "minecraft:cookie", "create:wheat_flour", "create:dough", "create:bar_of_chocolate", "create:sweet_roll", "create:chocolate_glazed_berries", "create:honeyed_apple")),
    HEAVY_MATERIALS(2, "Metallurgy & Concrete Patent", 220000L, List.of("create:iron_sheet", "create:copper_sheet", "create:golden_sheet", "create:brass_sheet", "create:metal_girder", "minecraft:white_concrete", "minecraft:gray_concrete", "minecraft:black_concrete", "minecraft:red_concrete", "minecraft:yellow_concrete", "minecraft:blue_concrete", "minecraft:green_concrete", "minecraft:orange_concrete", "minecraft:glass", "minecraft:tinted_glass", "create:andesite_casing", "create:copper_casing", "create:brass_casing")),
    PRECISION_TECH(3, "Precision Engineering Patent", 650000L, List.of("create:electron_tube", "create:brass_hand", "create:sturdy_sheet", "create:precision_mechanism", "create:railway_casing", "create:blaze_cake", "minecraft:gunpowder", "minecraft:slime_ball")),
    MACHINERY_TECH(4, "Heavy Machinery Patent", 1500000L, List.of("create:mechanical_press", "create:mechanical_mixer", "create:millstone", "create:crushing_wheel", "create:mechanical_saw", "create:mechanical_drill", "create:mechanical_harvester", "create:mechanical_crafter", "create:encased_fan", "create:spout", "create:deployer", "create:steam_engine"));
    public final int id; public final String title; public final long price; public final List<String> previewItems;
    IndustryPatent(int id, String title, long price, List<String> preview) { this.id = id; this.title = title; this.price = price; this.previewItems = preview; }
    public static IndustryPatent byId(int id) { for (IndustryPatent p : values()) if (p.id == id) return p; return NONE; }
}
