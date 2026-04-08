package com.deathPunish.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public record PluginConfig(
        String version,
        boolean enableDeathPunish,
        List<String> enableWorlds,
        boolean autoSetRule,
        boolean doImmediateRespawn,
        boolean reduceMaxHealthOnDeath,
        double reduceHealthAmount,
        boolean reduceExpOnDeathEnable,
        double reduceExpValue,
        boolean reduceMoneyOnDeathEnable,
        int reduceMoneyMode,
        double reduceMoneyValue,
        boolean inventoryEnable,
        String inventoryMode,
        boolean inventoryClean,
        int inventoryMinAmount,
        int inventoryMaxAmount,
        List<String> inventoryWhitelist,
        boolean clearEnderchestOnDeath,
        List<String> deathMsg,
        boolean foodLevelSave,
        int foodLevelValue,
        boolean debuffEnable,
        List<String> debuffPotions,
        String skipPunishMsg,
        boolean banOnDeath,
        String banReason,
        int banDuration,
        boolean enableEpitaph,
        String epitaph,
        ItemConfig protectItem,
        ItemConfig enderProtectItem,
        HealItemConfig healItem
) {
    public static PluginConfig from(FileConfiguration config) {
        return new PluginConfig(
                config.getString("version", ""),
                config.getBoolean("punishOnDeath.enable"),
                List.copyOf(config.getStringList("punishOnDeath.enableWorlds")),
                config.getBoolean("autoSetRule"),
                config.getBoolean("doImmediateRespawn"),
                config.getBoolean("punishments.reduceMaxHealthOnDeath"),
                config.getDouble("punishments.reduceHealthAmount"),
                config.getBoolean("punishments.reduceExpOnDeath.enable"),
                config.getDouble("punishments.reduceExpOnDeath.value"),
                config.getBoolean("punishments.reduceMoneyOnDeath.enable"),
                config.getInt("punishments.reduceMoneyOnDeath.mode"),
                config.getDouble("punishments.reduceMoneyOnDeath.value"),
                config.getBoolean("punishments.Inventory.enable"),
                config.getString("punishments.Inventory.mode", "all"),
                config.getBoolean("punishments.Inventory.clean"),
                config.getInt("punishments.Inventory.amount.min"),
                config.getInt("punishments.Inventory.amount.max"),
                List.copyOf(config.getStringList("punishments.Inventory.whitelist")),
                config.getBoolean("punishments.clearEnderchestOnDeath"),
                List.copyOf(config.getStringList("punishments.deathMsg")),
                config.getBoolean("punishments.foodLevel.save"),
                config.getInt("punishments.foodLevel.value"),
                config.getBoolean("punishments.debuff.enable"),
                List.copyOf(config.getStringList("punishments.debuff.potions")),
                config.getString("punishments.skipPunishMsg"),
                config.getBoolean("punishments.banOnDeath"),
                config.getString("punishments.banReason"),
                config.getInt("punishments.banDuration"),
                config.getBoolean("punishments.enableEpitaph"),
                config.getString("punishments.epitaph"),
                ItemConfig.from(config, "customItems.protect_item"),
                ItemConfig.from(config, "customItems.ender_protect_item"),
                HealItemConfig.from(config, "customItems.heal_item")
        );
    }

    public record ItemConfig(String name, String material, List<String> lore) {
        public static ItemConfig from(FileConfiguration config, String path) {
            return new ItemConfig(
                    config.getString(path + ".name", ""),
                    config.getString(path + ".material", ""),
                    List.copyOf(config.getStringList(path + ".lore"))
            );
        }
    }

    public record HealItemConfig(
            String name,
            String material,
            List<String> lore,
            double maxHealth,
            double healAmount,
            List<String> potionEffects,
            String eatMsg,
            String eatWithoutHealMsg,
            List<String> shape,
            Map<String, String> ingredients
    ) {
        @SuppressWarnings("unchecked")
        public static HealItemConfig from(FileConfiguration config, String path) {
            var section = config.getConfigurationSection(path + ".ingredients");
            Map<String, String> ingredients = section == null
                    ? Map.of()
                    : section.getValues(false).entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            entry -> String.valueOf(entry.getValue())
                    ));

            return new HealItemConfig(
                    config.getString(path + ".name", ""),
                    config.getString(path + ".material", ""),
                    List.copyOf(config.getStringList(path + ".lore")),
                    config.getDouble(path + ".maxHealth"),
                    config.getDouble(path + ".heal_amount"),
                    List.copyOf(config.getStringList(path + ".potion_effects")),
                    config.getString(path + ".eatMsg", ""),
                    config.getString(path + ".eatWithoutHealMsg", ""),
                    List.of(
                            config.getString(path + ".shape1", "yxy"),
                            config.getString(path + ".shape2", "xbx"),
                            config.getString(path + ".shape3", "yxy")
                    ),
                    ingredients
            );
        }
    }
}
