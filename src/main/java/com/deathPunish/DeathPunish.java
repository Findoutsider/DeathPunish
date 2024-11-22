package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.util.Objects;

public final class DeathPunish extends JavaPlugin {

    @Override
    public void onEnable() {
        say("[DeathPunish] §a插件已加载");
         // 生成配置文件
        saveDefaultConfig();
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(), this);
        // 注册命令
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));

        getServer().addRecipe(CustomItems.createEnchantedGoldenApple());
    }

    @Override
    public void onDisable() {
        // 插件禁用时的处理
        say("[DeathPunish] §a插件已卸载");
    }

    public void say(String s) {
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(s);
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        // 检查配置文件是否存在
        if (!configFile.exists()) {
            // 配置文件不存在，写入默认配置
            getConfig().options().copyDefaults(true);
            saveConfig();
        } else {
            // 配置文件存在，尝试加载配置
            try {
                FileConfiguration config = getConfig();
                // 检查配置文件是否有效
                if (config.contains("version") && config.contains("punishOnDeath") && config.contains("defaultMaxHealth") 
                        && config.contains("refillFoolLevelOnDeath") && config.contains("resetExpOnDeath") && config.contains("clearInventoryOnDeath")
                        && config.contains("clearEnderchestOnDeath") && config.contains("banOnDeath") && config.contains("banReason")) {
                    // 配置文件有效，仅读取
                    return;
                } else if (!Objects.requireNonNull(config.getString("version")).equalsIgnoreCase("1.2.1")) {
                    configFile.delete();
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                } else {
                    // 配置文件无效，删除旧文件并重新写入默认配置
                    configFile.delete();
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                }
            } catch (Exception e) {
                // 配置文件加载失败，删除旧文件并重新写入默认配置
                configFile.delete();
                getConfig().options().copyDefaults(true);
                saveConfig();
            }
        }
    }
}

