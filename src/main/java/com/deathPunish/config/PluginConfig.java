package com.deathPunish.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

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
        String epitaph
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
                config.getString("punishments.epitaph")
        );
    }
}
