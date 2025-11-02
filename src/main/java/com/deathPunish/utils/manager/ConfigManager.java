package com.deathPunish.utils.manager;

import com.deathPunish.DeathPunish;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

import static com.deathPunish.DeathPunish.config;

public class ConfigManager {
    // 基础配置
    public static boolean enableDeathPunish;
    public static List<String> enableWorlds;
    public static boolean autoSetRule;
    public static boolean doImmediateRespawn;

    // 惩罚相关配置
    public static boolean reduceMaxHealthOnDeath;
    public static double reduceHealthAmount;

    // 经验减少配置
    public static boolean reduceExpOnDeathEnable;
    public static double reduceExpValue;

    // 金钱减少配置
    public static boolean reduceMoneyOnDeathEnable;
    public static int reduceMoneyMode;
    public static double reduceMoneyValue;

    // 背包操作配置
    public static boolean inventoryEnable;
    public static String inventoryMode;
    public static boolean inventoryClean;
    public static int inventoryMinAmount;
    public static int inventoryMaxAmount;
    public static List<String> inventoryWhitelist;

    // 其他惩罚配置
    public static boolean clearEnderchestOnDeath;
    public static List<String> deathMsg;

    // 饱食度配置
    public static boolean foodLevelSave;
    public static int foodLevelValue;

    // Debuff配置
    public static boolean debuffEnable;
    public static List<String> debuffPotions;

    // 跳过惩罚信息
    public static String skipPunishMsg;

    // 封禁配置
    public static boolean banOnDeath;
    public static String banReason;
    public static int banDuration;

    // 墓碑配置
    public static boolean enableEpitaph;
    public static String epitaph;

    // 自定义物品配置 - 生命果实
    public static String healItemName;
    public static String healItemMaterial;
    public static List<String> healItemLore;
    public static int healItemMaxHealth;
    public static int healItemHealAmount;
    public static List<String> healItemPotionEffects;
    public static String healItemEatMsg;
    public static String healItemEatWithoutHealMsg;

    // 保护符配置
    public static String protectItemName;
    public static String protectItemMaterial;
    public static List<String> protectItemLore;

    // 末影保护符配置
    public static String enderProtectItemName;
    public static String enderProtectItemMaterial;
    public static List<String> enderProtectItemLore;


    public static void getAllConfigs() {
        // 基础配置
        enableDeathPunish = config.getBoolean("punishOnDeath.enable");
        enableWorlds = config.getStringList("punishOnDeath.enableWorlds");
        autoSetRule = config.getBoolean("autoSetRule");
        doImmediateRespawn = config.getBoolean("doImmediateRespawn");

        // 惩罚相关配置
        reduceMaxHealthOnDeath = config.getBoolean("punishments.reduceMaxHealthOnDeath");
        reduceHealthAmount = config.getDouble("punishments.reduceHealthAmount");

        // 经验减少配置
        reduceExpOnDeathEnable = config.getBoolean("punishments.reduceExpOnDeath.enable");
        reduceExpValue = config.getDouble("punishments.reduceExpOnDeath.value");

        // 金钱减少配置
        reduceMoneyOnDeathEnable = config.getBoolean("punishments.reduceMoneyOnDeath.enable");
        reduceMoneyMode = config.getInt("punishments.reduceMoneyOnDeath.mode");
        reduceMoneyValue = config.getDouble("punishments.reduceMoneyOnDeath.value");

        // 背包操作配置
        inventoryEnable = config.getBoolean("punishments.Inventory.enable");
        inventoryMode = config.getString("punishments.Inventory.mode");
        inventoryClean = config.getBoolean("punishments.Inventory.clean");
        inventoryMinAmount = config.getInt("punishments.Inventory.amount.min");
        inventoryMaxAmount = config.getInt("punishments.Inventory.amount.max");
        inventoryWhitelist = config.getStringList("punishments.Inventory.whitelist");

        // 其他惩罚配置
        clearEnderchestOnDeath = config.getBoolean("punishments.clearEnderchestOnDeath");
        deathMsg = config.getStringList("punishments.deathMsg");

        // 饱食度配置
        foodLevelSave = config.getBoolean("punishments.foodLevel.save");
        foodLevelValue = config.getInt("punishments.foodLevel.value");

        // Debuff配置
        debuffEnable = config.getBoolean("punishments.debuff.enable");
        debuffPotions = config.getStringList("punishments.debuff.potions");

        // 跳过惩罚信息
        skipPunishMsg = config.getString("punishments.skipPunishMsg");

        // 封禁配置
        banOnDeath = config.getBoolean("punishments.banOnDeath");
        banReason = config.getString("punishments.banReason");
        banDuration = config.getInt("punishments.banDuration");

        // 墓碑配置
        enableEpitaph = config.getBoolean("punishments.enableEpitaph");
        epitaph = config.getString("punishments.epitaph");

        // 自定义物品配置
        healItemName = config.getString("customItems.heal_item.name");
        healItemMaterial = config.getString("customItems.heal_item.material");
        healItemLore = config.getStringList("customItems.heal_item.lore");
        healItemMaxHealth = config.getInt("customItems.heal_item.maxHealth");
        healItemHealAmount = config.getInt("customItems.heal_item.heal_amount");
        healItemPotionEffects = config.getStringList("customItems.heal_item.potion_effects");
        healItemEatMsg = config.getString("customItems.heal_item.eatMsg");
        healItemEatWithoutHealMsg = config.getString("customItems.heal_item.eatWithoutHealMsg");

        // 保护符配置
        protectItemName = config.getString("customItems.protect_item.name");
        protectItemMaterial = config.getString("customItems.protect_item.material");
        protectItemLore = config.getStringList("customItems.protect_item.lore");

        // 末影保护符配置
        enderProtectItemName = config.getString("customItems.ender_protect_item.name");
        enderProtectItemMaterial = config.getString("customItems.ender_protect_item.material");
        enderProtectItemLore = config.getStringList("customItems.ender_protect_item.lore");
    }

}
