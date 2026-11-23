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
        list.add(makeArmor("helm", "Royal Helm VI", Items.NETHERITE_HELMET, 26000L, rnd, access, 0));
        list.add(makeArmor("chest", "Titan Chestplate VI", Items.NETHERITE_CHESTPLATE, 38000L, rnd, access, 1));
        list.add(makeArmor("legs", "Elite Leggings VI", Items.NETHERITE_LEGGINGS, 32000L, rnd, access, 2));
        list.add(makeArmor("boots", "Swift Boots VI", Items.NETHERITE_BOOTS, 25000L, rnd, access, 3));
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 7);
        sword.enchant(Enchantments.MOB_LOOTING, 5);
        sword.enchant(Enchantments.UNBREAKING, 5);
        sword.setHoverName(Component.literal("Executive Blade VII (Batch #" + batchNum + ")"));
        list.add(new BoutiqueOffer("sword", "Executive Blade VII", 34000L, sword));
        ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE);
        pick.enchant(Enchantments.BLOCK_EFFICIENCY, 7);
        pick.enchant(rnd.nextBoolean() ? Enchantments.BLOCK_FORTUNE : Enchantments.SILK_TOUCH, rnd.nextBoolean() ? 5 : 1);
        pick.enchant(Enchantments.UNBREAKING, 5);
        pick.setHoverName(Component.literal("Master Drill Pickaxe VII (Batch #" + batchNum + ")"));
        list.add(new BoutiqueOffer("pick", "Master Pickaxe VII", 36000L, pick));
        ItemStack elytra = new ItemStack(Items.ELYTRA);
        elytra.enchant(Enchantments.UNBREAKING, 6);
        elytra.enchant(Enchantments.MENDING, 1);
        elytra.setHoverName(Component.literal("VIP Wing Suit VI"));
        list.add(new BoutiqueOffer("elytra", "VIP Elytra VI", 55000L, elytra));
        return list;
    }
    private static BoutiqueOffer makeArmor(String id, String name, Item item, long price, Random rnd, RegistryAccess access, int slot) {
        ItemStack st = new ItemStack(item);
        st.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 6);
        st.enchant(Enchantments.UNBREAKING, 5);
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
