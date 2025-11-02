package com.deathPunish.utils.manager;

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

    public static final NamespacedKey heal = new NamespacedKey("deathpunish", "heal");
    public static ShapedRecipe recipe;

    public static ShapedRecipe createEnchantedGoldenApple(FileConfiguration config) {
        // 读取配置文件中的物品信息
        String materialItem = config.getString("customItems.heal_item.material", "ENCHANTED_GOLDEN_APPLE");
        String displayName = config.getString("customItems.heal_item.displayName", "§6生命果实");
        displayName = displayName.replace("&", "§");
        List<String> lore = config.getStringList("customItems.heal_item.lore");
        String shape1 = config.getString("customItems.heal_item.shape1", "yxy");
        String shape2 = config.getString("customItems.heal_item.shape2", "xbx");
        String shape3 = config.getString("customItems.heal_item.shape3", "yxy");
        @NotNull Map<String, Object> ingredients = Objects.requireNonNull(config.getConfigurationSection("customItems.heal_item.ingredients")).getValues(false);

        // 创建物品
        ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(materialItem)));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);

        // 创建配方
        recipe = new ShapedRecipe(heal, item)
                .shape(shape1, shape2, shape3);

        // 设置配方成分
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
