package com.industrialeconomy.economy;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.*;
import net.minecraft.world.item.enchantment.Enchantments;
import java.util.*;
public class BoutiqueCatalog {
    public record BoutiqueOffer(String id, String title, long price, ItemStack displayStack) {}
    private static final long BATCH_TICKS = 48000L;
    private static final List<ResourceKey<TrimPattern>> PATTERNS = List.of(
        TrimPatterns.SILENCE, TrimPatterns.EYE, TrimPatterns.VEX, TrimPatterns.WARD, TrimPatterns.SPIRE, TrimPatterns.RIB
    );
    private static final List<ResourceKey<TrimMaterial>> MATERIALS = List.of(
        TrimMaterials.GOLD, TrimMaterials.EMERALD, TrimMaterials.DIAMOND, TrimMaterials.AMETHYST, TrimMaterials.REDSTONE, TrimMaterials.NETHERITE
    );
    public static int getDaysUntilNextBatch(long gameTime) {
        long remTicks = BATCH_TICKS - (gameTime % BATCH_TICKS);
        return (int) Math.ceil(remTicks / 24000.0);
    }
    public static List<BoutiqueOffer> getCurrentBatch(long gameTime, RegistryAccess access) {
        long batchNum = (gameTime / BATCH_TICKS) + 1L;
        Random rnd = new Random(batchNum * 7919L);
        List<BoutiqueOffer> list = new ArrayList<>();
        list.add(makeArmor("helm", "Royal Helm VI", Items.NETHERITE_HELMET, 42000L, rnd, access, 0));
        list.add(makeArmor("chest", "Titan Chestplate VI", Items.NETHERITE_CHESTPLATE, 65000L, rnd, access, 1));
        list.add(makeArmor("legs", "Elite Leggings VI", Items.NETHERITE_LEGGINGS, 55000L, rnd, access, 2));
        list.add(makeArmor("boots", "Swift Boots VI", Items.NETHERITE_BOOTS, 40000L, rnd, access, 3));
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 7); sword.enchant(Enchantments.MOB_LOOTING, 5); sword.enchant(Enchantments.UNBREAKING, 5);
        sword.setHoverName(Component.literal("Executive Blade VII"));
        list.add(new BoutiqueOffer("sword", "Executive Blade VII", 55000L, sword));
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 8); bow.enchant(Enchantments.INFINITY_ARROWS, 1); bow.enchant(Enchantments.FLAMING_ARROWS, 1); bow.enchant(Enchantments.UNBREAKING, 5);
        bow.setHoverName(Component.literal("Storm Hunter Bow VIII"));
        list.add(new BoutiqueOffer("bow", "Storm Bow VIII", 68000L, bow));
        ItemStack cross = new ItemStack(Items.CROSSBOW);
        cross.enchant(Enchantments.QUICK_CHARGE, 5); cross.enchant(Enchantments.MULTISHOT, 1); cross.enchant(Enchantments.PIERCING, 6); cross.enchant(Enchantments.UNBREAKING, 5);
        cross.setHoverName(Component.literal("Heavy Ballista VI"));
        list.add(new BoutiqueOffer("crossbow", "Heavy Ballista VI", 62000L, cross));
        ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE);
        pick.enchant(Enchantments.BLOCK_EFFICIENCY, 7); pick.enchant(rnd.nextBoolean() ? Enchantments.BLOCK_FORTUNE : Enchantments.SILK_TOUCH, rnd.nextBoolean() ? 5 : 1); pick.enchant(Enchantments.UNBREAKING, 5);
        pick.setHoverName(Component.literal("Tycoon Pickaxe VII"));
        list.add(new BoutiqueOffer("pick", "Tycoon Pickaxe VII", 58000L, pick));
        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        axe.enchant(Enchantments.BLOCK_EFFICIENCY, 7); axe.enchant(Enchantments.SHARPNESS, 7); axe.enchant(Enchantments.BLOCK_FORTUNE, 5); axe.enchant(Enchantments.UNBREAKING, 5);
        axe.setHoverName(Component.literal("Titan Lumber Axe VII"));
        list.add(new BoutiqueOffer("axe", "Titan Axe VII", 52000L, axe));
        ItemStack shovel = new ItemStack(Items.NETHERITE_SHOVEL);
        shovel.enchant(Enchantments.BLOCK_EFFICIENCY, 7); shovel.enchant(Enchantments.SILK_TOUCH, 1); shovel.enchant(Enchantments.UNBREAKING, 5);
        shovel.setHoverName(Component.literal("Quarry Shovel VII"));
        list.add(new BoutiqueOffer("shovel", "Quarry Shovel VII", 45000L, shovel));
        ItemStack elytra = new ItemStack(Items.ELYTRA);
        elytra.enchant(Enchantments.UNBREAKING, 6); elytra.enchant(Enchantments.MENDING, 1);
        elytra.setHoverName(Component.literal("VIP Wing Suit VI"));
        list.add(new BoutiqueOffer("elytra", "VIP Elytra VI", 110000L, elytra));
        return list;
    }
    private static BoutiqueOffer makeArmor(String id, String name, Item item, long price, Random rnd, RegistryAccess access, int slot) {
        ItemStack st = new ItemStack(item);
        st.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 6); st.enchant(Enchantments.UNBREAKING, 5);
        if (slot == 0) st.enchant(Enchantments.RESPIRATION, 5);
        if (slot == 3) st.enchant(Enchantments.FALL_PROTECTION, 6);
        if (access != null) {
            try {
                var patReg = access.registryOrThrow(Registries.TRIM_PATTERN);
                var matReg = access.registryOrThrow(Registries.TRIM_MATERIAL);
                var pat = patReg.getHolder(PATTERNS.get(rnd.nextInt(PATTERNS.size())));
                var mat = matReg.getHolder(MATERIALS.get(rnd.nextInt(MATERIALS.size())));
                if (pat.isPresent() && mat.isPresent()) ArmorTrim.setTrim(access, st, new ArmorTrim(mat.get(), pat.get()));
            } catch (Exception ignored) {}
        }
        st.setHoverName(Component.literal(name));
        return new BoutiqueOffer(id, name, price, st);
    }
}
