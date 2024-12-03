package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomItems {

    public static final NamespacedKey heal_apple = new NamespacedKey("deathpunish", "heal_apple");

    public static ShapedRecipe createEnchantedGoldenApple(FileConfiguration config) {
        // ��ȡ�����ļ��е���Ʒ��Ϣ
        String displayName = config.getString("customItems.heal_apple.displayName", "��6������ʵ");
        List<String> lore = config.getStringList("customItems.heal_apple.lore");
        String shape1 = config.getString("customItems.heal_apple.shape1", "yxy");
        String shape2 = config.getString("customItems.heal_apple.shape2", "xbx");
        String shape3 = config.getString("customItems.heal_apple.shape3", "yxy");
        @NotNull Map<String, Object> ingredients = Objects.requireNonNull(config.getConfigurationSection("customItems.heal_apple.ingredients")).getValues(false);

        // ������Ʒ
        ItemStack item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);

        // �����䷽
        ShapedRecipe recipe = new ShapedRecipe(heal_apple, item)
                .shape(shape1, shape2, shape3);

        // �����䷽�ɷ�
        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            String key = entry.getKey();
            String materialName = (String) entry.getValue();
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                recipe.setIngredient(key.charAt(0), material);
            }
        }

        return recipe;
    }

}
