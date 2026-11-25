package com.industrialeconomy.economy;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import java.util.*;
public class BoutiqueRotationManager {
    public record OverlimitLot(String id, String title, ItemStack stack, long price) {}
    private static final List<OverlimitLot> POOL = new ArrayList<>();
    static {
        ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE);
        pick.enchant(Enchantments.BLOCK_EFFICIENCY, 8); pick.enchant(Enchantments.BLOCK_FORTUNE, 5); pick.enchant(Enchantments.UNBREAKING, 5); pick.enchant(Enchantments.MENDING, 1);
        pick.setHoverName(Component.literal("[OVERLIMIT] Titan Drill Pickaxe").withStyle(ChatFormatting.LIGHT_PURPLE));
        POOL.add(new OverlimitLot("ov_pick", "Titan Drill Pickaxe", pick, 450000L));
        ItemStack chest = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chest.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 7); chest.enchant(Enchantments.UNBREAKING, 5); chest.enchant(Enchantments.MENDING, 1);
        chest.setHoverName(Component.literal("[OVERLIMIT] Aegis Chestplate").withStyle(ChatFormatting.LIGHT_PURPLE));
        POOL.add(new OverlimitLot("ov_chest", "Aegis Chestplate", chest, 600000L));
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 8); sword.enchant(Enchantments.MOB_LOOTING, 5); sword.enchant(Enchantments.UNBREAKING, 5); sword.enchant(Enchantments.MENDING, 1);
        sword.setHoverName(Component.literal("[OVERLIMIT] Sovereign Blade").withStyle(ChatFormatting.LIGHT_PURPLE));
        POOL.add(new OverlimitLot("ov_sword", "Sovereign Blade", sword, 500000L));
        ItemStack helm = new ItemStack(Items.NETHERITE_HELMET);
        helm.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 7); helm.enchant(Enchantments.AQUA_AFFINITY, 1); helm.enchant(Enchantments.RESPIRATION, 5); helm.enchant(Enchantments.MENDING, 1);
        helm.setHoverName(Component.literal("[OVERLIMIT] Crown of Depth").withStyle(ChatFormatting.LIGHT_PURPLE));
        POOL.add(new OverlimitLot("ov_helm", "Crown of Depth", helm, 400000L));
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 7); boots.enchant(Enchantments.FALL_PROTECTION, 6); boots.enchant(Enchantments.DEPTH_STRIDER, 5); boots.enchant(Enchantments.MENDING, 1);
        boots.setHoverName(Component.literal("[OVERLIMIT] Strider Boots").withStyle(ChatFormatting.LIGHT_PURPLE));
        POOL.add(new OverlimitLot("ov_boots", "Strider Boots", boots, 420000L));
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 8); bow.enchant(Enchantments.INFINITY_ARROWS, 1); bow.enchant(Enchantments.UNBREAKING, 5);
        bow.setHoverName(Component.literal("[OVERLIMIT] Railgun Longbow").withStyle(ChatFormatting.LIGHT_PURPLE));
        POOL.add(new OverlimitLot("ov_bow", "Railgun Longbow", bow, 380000L));
        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(Enchantments.IMPALING, 8); trident.enchant(Enchantments.LOYALTY, 5); trident.enchant(Enchantments.CHANNELING, 1); trident.enchant(Enchantments.MENDING, 1);
        trident.setHoverName(Component.literal("[OVERLIMIT] Poseidon Trident").withStyle(ChatFormatting.LIGHT_PURPLE));
        POOL.add(new OverlimitLot("ov_trident", "Poseidon Trident", trident, 550000L));
    }
    public static List<OverlimitLot> getCurrentLots(long dayTime) {
        long period = Math.max(0L, dayTime / 48000L);
        int start = (int) ((period * 3L) % POOL.size());
        List<OverlimitLot> res = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) res.add(POOL.get((start + i) % POOL.size()));
        return res;
    }
}
