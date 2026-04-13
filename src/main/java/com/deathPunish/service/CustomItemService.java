package com.deathPunish.service;

import com.deathPunish.DeathPunish;
import com.deathPunish.config.PluginConfig;
import com.deathPunish.model.ManagedHealItem;
import com.deathPunish.model.ManagedProtectItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomItemService {
    public static final String HEAL_ITEM_PATH = "customItems.heal_item";
    public static final String PROTECT_ITEM_PATH = "customItems.protect_item";
    public static final String ENDER_PROTECT_ITEM_PATH = "customItems.ender_protect_item";
    public static final NamespacedKey HEAL_RECIPE_KEY = new NamespacedKey("deathpunish", "heal");

    private final DeathPunish plugin;
    private final MessageService messageService;

    public CustomItemService(DeathPunish plugin) {
        this.plugin = plugin;
        this.messageService = plugin.getMessageService();
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
        return createItemFromConfig(getItemConfig(configPath), amount);
    }

    public ItemStack createBuiltinItem(String type, int amount) {
        String path = resolveBuiltinItemPath(type);
        if (path == null) {
            throw new IllegalArgumentException("未知物品类型: " + type);
        }
        return createConfiguredItem(path, amount);
    }

    public ItemStack createExtraItem(String type, String id, int amount) {
        return switch (normalizeType(type)) {
            case "heal" -> plugin.getManagedItemService().getHealItem(id)
                    .map(ManagedHealItem::itemStack)
                    .map(item -> cloneWithAmount(item, amount))
                    .orElseThrow(() -> new IllegalArgumentException("找不到额外治疗物品: " + id));
            case "protect", "ender" -> plugin.getManagedItemService().getProtectItem(id, toProtectType(type))
                    .map(ManagedProtectItem::itemStack)
                    .map(item -> cloneWithAmount(item, amount))
                    .orElseThrow(() -> new IllegalArgumentException("找不到额外保护符: " + id));
            default -> throw new IllegalArgumentException("未知物品类型: " + type);
        };
    }

    public boolean hasExtraItem(String type, String id) {
        return switch (normalizeType(type)) {
            case "heal" -> plugin.getManagedItemService().containsHealItem(id);
            case "protect", "ender" -> plugin.getManagedItemService().containsProtectItem(id, toProtectType(type));
            default -> false;
        };
    }

    public List<String> getExtraItemIds(String type) {
        return switch (normalizeType(type)) {
            case "heal" -> plugin.getManagedItemService().getHealItems().stream()
                    .map(ManagedHealItem::id)
                    .sorted()
                    .toList();
            case "protect", "ender" -> plugin.getManagedItemService().getProtectItems().stream()
                    .filter(item -> item.type() == toProtectType(type))
                    .map(ManagedProtectItem::id)
                    .sorted()
                    .toList();
            default -> List.of();
        };
    }

    public boolean supportsType(String input) {
        return resolveBuiltinItemPath(input) != null;
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

    public Optional<ManagedHealItem> findExtraHealItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return Optional.empty();
        }
        return plugin.getManagedItemService().getHealItems().stream()
                .filter(entry -> isSimilarIgnoringAmount(entry.itemStack(), item))
                .findFirst();
    }

    public Optional<ManagedProtectItem> findExtraProtectItem(ItemStack item, ManagedProtectItem.ProtectType type) {
        if (item == null || item.getType().isAir()) {
            return Optional.empty();
        }
        return plugin.getManagedItemService().getProtectItems().stream()
                .filter(entry -> entry.type() == type)
                .filter(entry -> isSimilarIgnoringAmount(entry.itemStack(), item))
                .findFirst();
    }

    public boolean matchesProtectItem(ItemStack item, ManagedProtectItem.ProtectType type) {
        if (!plugin.getPluginConfig().disableBuiltinProtectItems()) {
            String configPath = type == ManagedProtectItem.ProtectType.ENDER ? ENDER_PROTECT_ITEM_PATH : PROTECT_ITEM_PATH;
            if (matchesConfiguredItem(item, configPath)) {
                return true;
            }
        }
        return findExtraProtectItem(item, type).isPresent();
    }

    public boolean shouldBlockProtectItemInteraction(ItemStack item) {
        return matchesProtectItem(item, ManagedProtectItem.ProtectType.NORMAL)
                || matchesProtectItem(item, ManagedProtectItem.ProtectType.ENDER);
    }

    public boolean applyHealItem(Player player, ItemStack consumedItem) {
        ManagedHealItem managedHealItem = null;
        if (!plugin.getPluginConfig().disableBuiltinHealItem()) {
            if (consumedItem.getType() == Material.ENCHANTED_GOLDEN_APPLE && matchesConfiguredItem(consumedItem, HEAL_ITEM_PATH)) {
                return applyHeal(player, plugin.getPluginConfig().healItem());
            }
        }
        managedHealItem = findExtraHealItem(consumedItem).orElse(null);
        if (managedHealItem == null) {
            return false;
        }
        return applyHeal(player, managedHealItem);
    }

    public String resolveBuiltinItemPath(String input) {
        return switch (normalizeType(input)) {
            case "heal" -> HEAL_ITEM_PATH;
            case "protect" -> PROTECT_ITEM_PATH;
            case "ender" -> ENDER_PROTECT_ITEM_PATH;
            default -> null;
        };
    }

    private boolean applyHeal(Player player, PluginConfig.HealItemConfig healItem) {
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null) {
            messageService.error("无法读取玩家 " + player.getName() + " 的最大生命值属性");
            return false;
        }

        var maxHealthModifierService = plugin.getMaxHealthModifierService();
        Double currentMaxHealth = maxHealthModifierService.getEffectiveMaxHealth(player);
        Double externalMaxHealth = maxHealthModifierService.getExternalMaxHealth(player);
        if (currentMaxHealth == null || externalMaxHealth == null) {
            messageService.error("无法读取玩家 " + player.getName() + " 的最大生命值属性");
            return false;
        }

        double healCap = Math.max(healItem.maxHealth(), externalMaxHealth);
        double newMaxHealth = Math.min(currentMaxHealth + healItem.healAmount(), healCap);

        maxHealthModifierService.setEffectiveMaxHealth(player, newMaxHealth);
        player.setHealth(newMaxHealth);
        player.setFoodLevel(20);
        player.sendMessage(Objects.requireNonNull(currentMaxHealth + healItem.healAmount() > healCap
                ? healItem.eatWithoutHealMsg()
                : healItem.eatMsg()));

        for (String effect : healItem.potionEffects()) {
            applyPotionEffect(player, effect);
        }

        messageService.info("玩家 " + player.getName() + " 通过 " + healItem.name() + " 恢复了生命上限，当前生命上限：" + newMaxHealth);
        return true;
    }

    private boolean applyHeal(Player player, ManagedHealItem healItem) {
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null) {
            messageService.error("无法读取玩家 " + player.getName() + " 的最大生命值属性");
            return false;
        }

        var maxHealthModifierService = plugin.getMaxHealthModifierService();
        Double currentMaxHealth = maxHealthModifierService.getEffectiveMaxHealth(player);
        Double externalMaxHealth = maxHealthModifierService.getExternalMaxHealth(player);
        if (currentMaxHealth == null || externalMaxHealth == null) {
            messageService.error("无法读取玩家 " + player.getName() + " 的最大生命值属性");
            return false;
        }

        double healCap = Math.max(healItem.maxHealth(), externalMaxHealth);
        double newMaxHealth = Math.min(currentMaxHealth + healItem.healAmount(), healCap);

        maxHealthModifierService.setEffectiveMaxHealth(player, newMaxHealth);
        player.setHealth(newMaxHealth);
        player.setFoodLevel(20);
        player.sendMessage(Objects.requireNonNull(currentMaxHealth + healItem.healAmount() > healCap
                ? healItem.eatWithoutHealMsg()
                : healItem.eatMsg()));

        for (String effect : healItem.potionEffects()) {
            applyPotionEffect(player, effect);
        }

        String itemName = resolveDisplayName(healItem.itemStack());
        messageService.info("玩家 " + player.getName() + " 通过 " + itemName + " 恢复了生命上限，当前生命上限：" + newMaxHealth);
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

    private ItemStack createItemFromConfig(PluginConfig.ItemConfig itemConfig, int amount) {
        var material = Objects.requireNonNull(Material.matchMaterial(itemConfig.material()));
        var item = new ItemStack(material, amount);
        var meta = item.getItemMeta();
        meta.setDisplayName(colorize(itemConfig.name()));
        meta.setLore(colorize(itemConfig.lore()));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack cloneWithAmount(ItemStack itemStack, int amount) {
        ItemStack clone = itemStack.clone();
        clone.setAmount(amount);
        return clone;
    }

    private String normalizeType(String input) {
        return switch (input.toLowerCase()) {
            case "ender_protect", "enderprotect", "ender" -> "ender";
            case "protect" -> "protect";
            case "heal" -> "heal";
            default -> input.toLowerCase();
        };
    }

    private ManagedProtectItem.ProtectType toProtectType(String input) {
        return "ender".equals(normalizeType(input))
                ? ManagedProtectItem.ProtectType.ENDER
                : ManagedProtectItem.ProtectType.NORMAL;
    }

    private boolean isSimilarIgnoringAmount(ItemStack left, ItemStack right) {
        ItemStack normalizedLeft = left.clone();
        normalizedLeft.setAmount(1);
        ItemStack normalizedRight = right.clone();
        normalizedRight.setAmount(1);
        return normalizedLeft.isSimilar(normalizedRight);
    }

    private String resolveDisplayName(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }
        return itemStack.getType().name();
    }

    private void applyPotionEffect(Player player, String effect) {
        String[] parts = effect.split(" ");
        if (parts.length != 3) {
            messageService.warn("无效的药水配置: " + effect);
            return;
        }
        PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(parts[0]));
        if (type == null) {
            messageService.warn("无效的药水效果: " + parts[0]);
            return;
        }
        try {
            int duration = Integer.parseInt(parts[1]);
            int amplifier = Integer.parseInt(parts[2]);
            player.addPotionEffect(new PotionEffect(type, duration, amplifier));
        } catch (NumberFormatException ex) {
            messageService.warn("无效的药水时长或等级: " + effect);
        }
    }

    private List<String> colorize(List<String> lines) {
        return lines.stream().map(this::colorize).collect(Collectors.toList());
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }
}
