package com.deathPunish.service;

import com.deathPunish.DeathPunish;
import com.deathPunish.config.PluginConfig;
import com.deathPunish.utils.EpitaphUtils;
import com.deathPunish.utils.SchedulerUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PunishmentService {
    private final DeathPunish plugin;
    private final CustomItemService customItemService;
    private final MessageService messageService;
    private final Map<UUID, PendingPunishment> pendingPunishments = new ConcurrentHashMap<>();

    public PunishmentService(DeathPunish plugin, CustomItemService customItemService, MessageService messageService) {
        this.plugin = plugin;
        this.customItemService = customItemService;
        this.messageService = messageService;
    }

    public void handleDeath(Player player) {
        var pluginConfig = plugin.getPluginConfig();
        if (!pluginConfig.enableDeathPunish() || !pluginConfig.enableWorlds().contains(player.getWorld().getName())) {
            return;
        }
        if (player.hasPermission("deathpunish.bypass")) {
            player.sendMessage(Objects.requireNonNull(pluginConfig.skipPunishMsg()));
            messageService.info("玩家 " + player.getName() + " 因为拥有 bypass 权限跳过死亡惩罚");
            return;
        }

        var maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {
            messageService.error("无法读取玩家 " + player.getName() + " 的最大生命值属性");
            return;
        }

        if (consumeProtectionItem(player, pluginConfig)) {
            return;
        }

        pendingPunishments.put(player.getUniqueId(), new PendingPunishment(maxHealthAttribute.getBaseValue(), player.getFoodLevel()));
        applyImmediatePunishments(player, pluginConfig);
        messageService.info("玩家 " + player.getName() + " 受到了死亡惩罚");

        if (SchedulerUtils.isFolia()) {
            SchedulerUtils.runTaskLater(plugin, () -> applyPendingPunishment(player), 5L);
        }
    }

    public void handleRespawn(Player player) {
        if (SchedulerUtils.isFolia()) {
            return;
        }
        applyPendingPunishment(player);
    }

    private void applyImmediatePunishments(Player player, PluginConfig pluginConfig) {
        if (pluginConfig.enableEpitaph()) {
            createEpitaph(player, pluginConfig);
        }

        var whitelist = resolveWhitelistedMaterials(pluginConfig);
        if (pluginConfig.inventoryEnable()) {
            if ("all".equalsIgnoreCase(pluginConfig.inventoryMode())) {
                clearInventory(player, whitelist, !pluginConfig.inventoryClean());
            } else if ("part".equalsIgnoreCase(pluginConfig.inventoryMode())) {
                int removeCount = pluginConfig.inventoryMinAmount() == pluginConfig.inventoryMaxAmount()
                        ? pluginConfig.inventoryMinAmount()
                        : ThreadLocalRandom.current().nextInt(pluginConfig.inventoryMinAmount(), pluginConfig.inventoryMaxAmount() + 1);
                removeRandomItems(player, whitelist, removeCount, !pluginConfig.inventoryClean());
            }
        }

        if (pluginConfig.clearEnderchestOnDeath()) {
            player.getEnderChest().clear();
        }

        if (pluginConfig.reduceExpOnDeathEnable()) {
            int newLevel = (int) (player.getLevel() * (1 - pluginConfig.reduceExpValue()));
            player.setTotalExperience(0);
            player.setLevel(Math.max(newLevel, 0));
        }

        if (pluginConfig.reduceMoneyOnDeathEnable()) {
            reduceMoney(player, pluginConfig);
        }
    }

    private void applyPendingPunishment(Player player) {
        var pluginConfig = plugin.getPluginConfig();
        var pending = pendingPunishments.remove(player.getUniqueId());
        if (pending == null) {
            return;
        }

        pluginConfig.deathMsg().forEach(player::sendMessage);

        if (pluginConfig.banOnDeath() && pending.maxHealth() <= 1.0D) {
            Date expiration = new Date(System.currentTimeMillis() + (long) pluginConfig.banDuration() * 60 * 1000);
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), pluginConfig.banReason(), expiration, "DeathPunish");
            player.kickPlayer(Objects.requireNonNull(pluginConfig.banReason()));
            return;
        }

        var maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null && pluginConfig.reduceMaxHealthOnDeath()) {
            double newMaxHealth = Math.max(pending.maxHealth() - pluginConfig.reduceHealthAmount(), 1.0D);
            maxHealthAttribute.setBaseValue(newMaxHealth);
            player.setHealth(newMaxHealth);
        }

        int targetFoodLevel = pluginConfig.foodLevelSave() ? pending.foodLevel() : pluginConfig.foodLevelValue();
        SchedulerUtils.runTaskLater(plugin, () -> player.setFoodLevel(targetFoodLevel), 1L);

        if (pluginConfig.debuffEnable()) {
            applyDebuffs(player, pluginConfig);
        }
    }

    private void applyDebuffs(Player player, PluginConfig pluginConfig) {
        for (String effect : pluginConfig.debuffPotions()) {
            var parts = effect.split(" ");
            if (parts.length != 3) {
                messageService.warn("无效的药水配置: " + effect);
                continue;
            }
            var type = PotionEffectType.getByKey(NamespacedKey.minecraft(parts[0]));
            if (type == null) {
                messageService.warn("无效的药水效果: " + parts[0]);
                continue;
            }
            try {
                int duration = Integer.parseInt(parts[1]);
                int amplifier = Integer.parseInt(parts[2]);
                if (SchedulerUtils.isFolia()) {
                    DeathPunish.getFoliaLib().getScheduler().runAtEntity(player, task ->
                            player.addPotionEffect(new PotionEffect(type, duration, amplifier)));
                } else {
                    player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                }
            } catch (NumberFormatException ex) {
                messageService.warn("无效的药水时长或等级: " + effect);
            }
        }
    }

    private void reduceMoney(Player player, PluginConfig pluginConfig) {
        if (!DeathPunish.enableEco || DeathPunish.econ == null) {
            messageService.warn("未启用经济系统，已跳过玩家 " + player.getName() + " 的扣费惩罚");
            return;
        }

        double amount;
        if (pluginConfig.reduceMoneyMode() == 1) {
            amount = DeathPunish.econ.getBalance(player) * pluginConfig.reduceMoneyValue();
        } else if (pluginConfig.reduceMoneyMode() == 2 && pluginConfig.reduceMoneyValue() >= 0) {
            amount = pluginConfig.reduceMoneyValue();
        } else {
            messageService.error("punishments.reduceMoneyOnDeath 配置错误，请检查 mode 和 value");
            return;
        }

        if (amount > 0) {
            DeathPunish.econ.withdrawPlayer(player, amount);
        }
    }

    private boolean consumeProtectionItem(Player player, PluginConfig pluginConfig) {
        return consumeConfiguredItem(player.getInventory(), CustomItemService.PROTECT_ITEM_PATH, player, "保护符", pluginConfig)
                || consumeConfiguredItem(player.getEnderChest(), CustomItemService.ENDER_PROTECT_ITEM_PATH, player, "末影保护符", pluginConfig);
    }

    private boolean consumeConfiguredItem(Inventory inventory, String configPath, Player player, String itemName, PluginConfig pluginConfig) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!customItemService.matchesConfiguredItem(item, configPath)) {
                continue;
            }
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                inventory.setItem(slot, null);
            }
            player.sendMessage(Objects.requireNonNull(pluginConfig.skipPunishMsg()));
            messageService.info("玩家 " + player.getName() + " 因为 " + itemName + " 跳过死亡惩罚");
            return true;
        }
        return false;
    }

    private void createEpitaph(Player player, PluginConfig pluginConfig) {
        Location location = player.getLocation();
        location.getBlock().setType(Material.BEDROCK);
        String text = Optional.ofNullable(pluginConfig.epitaph()).orElse("").replace("%player%", player.getName());
        EpitaphUtils.createFloatingText(location.clone().add(0, 1, 0), text);
    }

    private Set<Material> resolveWhitelistedMaterials(PluginConfig pluginConfig) {
        return pluginConfig.inventoryWhitelist().stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void clearInventory(Player player, Set<Material> whitelist, boolean dropItems) {
        collectRemovableItems(player.getInventory(), whitelist).forEach(item -> removeIndexedItem(player, item, dropItems));
    }

    private void removeRandomItems(Player player, Set<Material> whitelist, int count, boolean dropItems) {
        var removableItems = collectRemovableItems(player.getInventory(), whitelist);
        if (removableItems.isEmpty()) {
            return;
        }
        Collections.shuffle(removableItems);
        removableItems.stream().limit(count).forEach(item -> removeIndexedItem(player, item, dropItems));
    }

    private List<IndexedItem> collectRemovableItems(PlayerInventory inventory, Set<Material> whitelist) {
        List<IndexedItem> removableItems = new ArrayList<>();
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType().isAir() || whitelist.contains(item.getType())) {
                continue;
            }
            removableItems.add(new IndexedItem(slot, item.clone()));
        }
        return removableItems;
    }

    private void removeIndexedItem(Player player, IndexedItem indexedItem, boolean dropItems) {
        var inventory = player.getInventory();
        switch (indexedItem.slot()) {
            case 40 -> inventory.setItemInOffHand(null);
            case 39 -> inventory.setHelmet(null);
            case 38 -> inventory.setChestplate(null);
            case 37 -> inventory.setLeggings(null);
            case 36 -> inventory.setBoots(null);
            default -> inventory.setItem(indexedItem.slot(), null);
        }
        if (dropItems) {
            player.getWorld().dropItemNaturally(player.getLocation(), indexedItem.item());
        }
    }

    private record PendingPunishment(double maxHealth, int foodLevel) {}

    private record IndexedItem(int slot, ItemStack item) {}
}
