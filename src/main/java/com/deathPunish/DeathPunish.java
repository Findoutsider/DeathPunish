package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public final class DeathPunish extends JavaPlugin {

    private FileConfiguration epitaphConfig;
    public ShapedRecipe enchantedGoldenAppleRecipe;

    @Override
    public void onEnable() {
        say("[DeathPunish] §a插件已加载");
         // 生成配置文件
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        // 注册自定义物品的配方
        registerCustomRecipes(config);
//        fileCreate();
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(), this);
        // 注册命令
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));



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

    public void registerCustomRecipes(FileConfiguration config) {
        enchantedGoldenAppleRecipe = CustomItems.createEnchantedGoldenApple(config);
        getServer().addRecipe(enchantedGoldenAppleRecipe);
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
                // 检查配置文件是否最新
                if (!Objects.requireNonNull(config.getString("version")).equalsIgnoreCase("1.3.1")) {
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

//    public void fileCreate() {
//        // 创建 message 文件夹
//        File messageFolder = new File(getDataFolder(), "message");
//        if (!messageFolder.exists()) {
//            messageFolder.mkdir();
//        }
//
//        // 创建 epitaph.yml 文件
//    }
}

