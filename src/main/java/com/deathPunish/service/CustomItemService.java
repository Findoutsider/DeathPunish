package com.deathPunish.service;

import com.deathPunish.DeathPunish;
import com.deathPunish.config.PluginConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomItemService {
    public static final String HEAL_ITEM_PATH = "customItems.heal_item";
    public static final String PROTECT_ITEM_PATH = "customItems.protect_item";
    public static final String ENDER_PROTECT_ITEM_PATH = "customItems.ender_protect_item";
    public static final NamespacedKey HEAL_RECIPE_KEY = new NamespacedKey("deathpunish", "heal");

    private final DeathPunish plugin;

    public CustomItemService(DeathPunish plugin) {
        this.plugin = plugin;
    }

    public ShapedRecipe createHealRecipe() {
        var healItem = plugin.getPluginConfig().healItem();
        String materialItem = healItem.material();
        String displayName = colorize(healItem.name());
        List<String> lore = colorize(healItem.lore());

        ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(materialItem)));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);

        var recipe = new ShapedRecipe(HEAL_RECIPE_KEY, item).shape(
                healItem.shape().get(0),
                healItem.shape().get(1),
                healItem.shape().get(2)
        );
        for (var entry : healItem.ingredients().entrySet()) {
            Material material = Material.matchMaterial(entry.getValue());
            if (material != null) {
                recipe.setIngredient(entry.getKey().charAt(0), material);
            }
        }
        return recipe;
    }

    public ItemStack createConfiguredItem(String configPath, int amount) {
        var itemConfig = getItemConfig(configPath);
        var material = Objects.requireNonNull(Material.matchMaterial(itemConfig.material()));
        var item = new ItemStack(material, amount);
        var meta = item.getItemMeta();
        meta.setDisplayName(colorize(itemConfig.name()));
        meta.setLore(colorize(itemConfig.lore()));
        item.setItemMeta(meta);
        return item;
    }

    public boolean matchesConfiguredItem(ItemStack item, String configPath) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        var itemConfig = getItemConfig(configPath);
        var expectedMaterial = Material.matchMaterial(itemConfig.material());
        var meta = item.getItemMeta();
        if (expectedMaterial == null || meta == null || !meta.hasDisplayName()) {
            return false;
        }
        return item.getType() == expectedMaterial && colorize(itemConfig.name()).equals(meta.getDisplayName());
    }

    public String resolveItemPath(String input) {
        return switch (input.toLowerCase()) {
            case "heal" -> HEAL_ITEM_PATH;
            case "protect" -> PROTECT_ITEM_PATH;
            case "ender", "ender_protect", "enderprotect" -> ENDER_PROTECT_ITEM_PATH;
            default -> {
                if (input.equalsIgnoreCase(plugin.getPluginConfig().healItem().name())) {
                    yield HEAL_ITEM_PATH;
                }
                if (input.equalsIgnoreCase(plugin.getPluginConfig().protectItem().name())) {
                    yield PROTECT_ITEM_PATH;
                }
                if (input.equalsIgnoreCase(plugin.getPluginConfig().enderProtectItem().name())) {
                    yield ENDER_PROTECT_ITEM_PATH;
                }
                yield null;
            }
        };
    }

    public boolean applyHealItem(Player player, ItemStack consumedItem) {
        if (consumedItem.getType() != Material.ENCHANTED_GOLDEN_APPLE || !matchesConfiguredItem(consumedItem, HEAL_ITEM_PATH)) {
            return false;
        }

        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {
            DeathPunish.log.err("无法读取玩家 " + player.getName() + " 的最大生命值属性");
            return false;
        }

        var healItem = plugin.getPluginConfig().healItem();
        double currentMaxHealth = maxHealthAttribute.getBaseValue();
        double newMaxHealth = Math.min(currentMaxHealth + healItem.healAmount(), healItem.maxHealth());

        maxHealthAttribute.setBaseValue(newMaxHealth);
        player.setHealth(newMaxHealth);
        player.setFoodLevel(20);
        player.sendMessage(Objects.requireNonNull(currentMaxHealth + healItem.healAmount() > healItem.maxHealth()
                ? healItem.eatWithoutHealMsg()
                : healItem.eatMsg()));

        for (String effect : healItem.potionEffects()) {
            applyPotionEffect(player, effect);
        }

        DeathPunish.log.info("玩家 " + player.getName() + " 通过 " + healItem.name() + " 恢复了生命上限，当前生命上限：" + newMaxHealth);
        return true;
    }

    private PluginConfig.ItemConfig getItemConfig(String configPath) {
        return switch (configPath) {
            case PROTECT_ITEM_PATH -> plugin.getPluginConfig().protectItem();
            case ENDER_PROTECT_ITEM_PATH -> plugin.getPluginConfig().enderProtectItem();
            case HEAL_ITEM_PATH -> {
                var healItem = plugin.getPluginConfig().healItem();
                yield new PluginConfig.ItemConfig(healItem.name(), healItem.material(), healItem.lore());
            }
            default -> throw new IllegalArgumentException("未知物品配置路径: " + configPath);
        };
    }

    private void applyPotionEffect(Player player, String effect) {
        String[] parts = effect.split(" ");
        if (parts.length != 3) {
            DeathPunish.log.warn("无效的药水配置: " + effect);
            return;
        }
        PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(parts[0]));
        if (type == null) {
            DeathPunish.log.warn("无效的药水效果: " + parts[0]);
            return;
        }
        try {
            int duration = Integer.parseInt(parts[1]);
            int amplifier = Integer.parseInt(parts[2]);
            player.addPotionEffect(new PotionEffect(type, duration, amplifier));
        } catch (NumberFormatException ex) {
            DeathPunish.log.warn("无效的药水时长或等级: " + effect);
        }
    }

    private List<String> colorize(List<String> lines) {
        return lines.stream().map(this::colorize).collect(Collectors.toList());
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }
}
