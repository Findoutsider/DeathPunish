package com.deathPunish.service;

import com.deathPunish.DeathPunish;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
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
        var config = plugin.getConfig();
        String materialItem = config.getString(HEAL_ITEM_PATH + ".material", "ENCHANTED_GOLDEN_APPLE");
        String displayName = colorize(config.getString(HEAL_ITEM_PATH + ".name", "&6生命果实"));
        List<String> lore = colorize(config.getStringList(HEAL_ITEM_PATH + ".lore"));
        String shape1 = config.getString(HEAL_ITEM_PATH + ".shape1", "yxy");
        String shape2 = config.getString(HEAL_ITEM_PATH + ".shape2", "xbx");
        String shape3 = config.getString(HEAL_ITEM_PATH + ".shape3", "yxy");
        Map<String, Object> ingredients = Objects.requireNonNull(config.getConfigurationSection(HEAL_ITEM_PATH + ".ingredients")).getValues(false);

        ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(materialItem)));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);

        var recipe = new ShapedRecipe(HEAL_RECIPE_KEY, item).shape(shape1, shape2, shape3);
        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            Material material = Material.matchMaterial((String) entry.getValue());
            if (material != null) {
                recipe.setIngredient(entry.getKey().charAt(0), material);
            }
        }
        return recipe;
    }

    public ItemStack createConfiguredItem(String configPath, int amount) {
        var config = plugin.getConfig();
        var material = Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(config.getString(configPath + ".material"))));
        var item = new ItemStack(material, amount);
        var meta = item.getItemMeta();
        meta.setDisplayName(colorize(config.getString(configPath + ".name", "")));
        meta.setLore(colorize(config.getStringList(configPath + ".lore")));
        item.setItemMeta(meta);
        return item;
    }

    public boolean matchesConfiguredItem(ItemStack item, String configPath) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        var config = plugin.getConfig();
        var expectedMaterial = Material.matchMaterial(config.getString(configPath + ".material", ""));
        var meta = item.getItemMeta();
        if (expectedMaterial == null || meta == null || !meta.hasDisplayName()) {
            return false;
        }
        return item.getType() == expectedMaterial && colorize(config.getString(configPath + ".name", "")).equals(meta.getDisplayName());
    }

    public String resolveItemPath(String input) {
        return switch (input.toLowerCase()) {
            case "heal" -> HEAL_ITEM_PATH;
            case "protect" -> PROTECT_ITEM_PATH;
            case "ender", "ender_protect", "enderprotect" -> ENDER_PROTECT_ITEM_PATH;
            default -> {
                if (input.equalsIgnoreCase(Objects.requireNonNull(plugin.getConfig().getString(HEAL_ITEM_PATH + ".name")))) {
                    yield HEAL_ITEM_PATH;
                }
                if (input.equalsIgnoreCase(Objects.requireNonNull(plugin.getConfig().getString(PROTECT_ITEM_PATH + ".name")))) {
                    yield PROTECT_ITEM_PATH;
                }
                if (input.equalsIgnoreCase(Objects.requireNonNull(plugin.getConfig().getString(ENDER_PROTECT_ITEM_PATH + ".name")))) {
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

        var config = plugin.getConfig();
        double currentMaxHealth = maxHealthAttribute.getBaseValue();
        double healAmount = config.getDouble(HEAL_ITEM_PATH + ".heal_amount");
        double maxHealthCap = config.getDouble(HEAL_ITEM_PATH + ".maxHealth");
        double newMaxHealth = Math.min(currentMaxHealth + healAmount, maxHealthCap);

        maxHealthAttribute.setBaseValue(newMaxHealth);
        player.setHealth(newMaxHealth);
        player.setFoodLevel(20);
        player.sendMessage(Objects.requireNonNull(currentMaxHealth + healAmount > maxHealthCap
                ? config.getString(HEAL_ITEM_PATH + ".eatWithoutHealMsg")
                : config.getString(HEAL_ITEM_PATH + ".eatMsg")));

        for (String effect : config.getStringList(HEAL_ITEM_PATH + ".potion_effects")) {
            applyPotionEffect(player, effect);
        }

        DeathPunish.log.info("玩家 " + player.getName() + " 通过 " + config.getString(HEAL_ITEM_PATH + ".name") + " 恢复了生命上限，当前生命上限：" + newMaxHealth);
        return true;
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
