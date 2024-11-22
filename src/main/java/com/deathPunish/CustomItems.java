package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CustomItems {

    public static final NamespacedKey heal_apple = new NamespacedKey("deathpunish", "heal_apple");

    public static ShapedRecipe createEnchantedGoldenApple() {
        ItemStack item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("��6������ʵ");
        meta.setLore(List.of("��7ʳ�ú�ָ��������ޣ�����ȡһЩ����Ч��"));
        item.setItemMeta(meta);
        return new ShapedRecipe(heal_apple, item)
                .shape("yxy", "xbx", "yxy")
                .setIngredient('x', Material.GOLD_BLOCK)
                .setIngredient('y', Material.DIAMOND_BLOCK)
                .setIngredient('b', Material.NETHER_STAR);
    }

}
