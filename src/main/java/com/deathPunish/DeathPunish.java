package com.deathPunish;

import net.milkbowl.vault.economy.Economy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

public final class DeathPunish extends JavaPlugin {

    public final static String VERSION = "1.3.6";
    public static Economy econ = null;
    public static boolean enableEco = false;
    private FileConfiguration epitaphConfig;
    public static FileConfiguration config;
    public ShapedRecipe enchantedGoldenAppleRecipe;
    public static LoggerUtils log;
    public static List<String> worlds;

    @Override
    public void onEnable() {
        log = new LoggerUtils();
        int pluginId = 24171;
        Metrics metrics = new Metrics(this, pluginId);
        setupEconomy();
        // 保存默认配置文件
        saveDefaultConfig();
        config = getConfig();
        // 注册自定义物品配方
        registerCustomRecipes(config);
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(), this);
        // 注册命令
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));
        setWorldRule();
        log.info("[DeathPunish] §a插件已启用");
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        // 插件禁用时的处理
        if (log != null) log.info("插件已禁用");
    }

    public void registerCustomRecipes(FileConfiguration config) {
        enchantedGoldenAppleRecipe = CustomItems.createEnchantedGoldenApple(config);
        getServer().resetRecipes(); // 重置配方
        getServer().addRecipe(enchantedGoldenAppleRecipe);
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        // 检查配置文件是否存在
        if (!configFile.exists()) {
            // 如果文件不存在，写入默认配置
            getConfig().options().copyDefaults(true);
            saveConfig();
        } else {
            // 如果文件存在，检查版本
            try {
                FileConfiguration config = getConfig();
                // 检查配置文件的版本
                if (!Objects.requireNonNull(config.getString("version")).equalsIgnoreCase(VERSION)) {
                    configFile.delete();
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                    log.info("[DeathPunish] §a已更新配置文件至 v" + VERSION);
                }
            } catch (Exception e) {
                // 如果配置文件读取失败，删除文件并写入默认配置
                configFile.delete();
                getConfig().options().copyDefaults(true);
                saveConfig();
                log.info("[DeathPunish] §c配置文件读取失败，已恢复默认配置");
            }
        }
    }

    private void setupEconomy() {
        boolean result = false;
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
                result = true;
            }
        }
        if (result) enableEco = true; log.info("[DeathPunish] §a经济内容已启动");
    }

    public static Economy getEconomy() {
        return econ;
    }

    public void setWorldRule() {
        if (config.getBoolean("autoSetRule") && config.getBoolean("punishOnDeath.enable")) {
            worlds = config.getStringList("punishOnDeath.enableWorlds");
            for (String world : worlds) {
                if (Boolean.FALSE.equals(Objects.requireNonNull(Bukkit.getWorld(world)).getGameRuleValue(GameRule.KEEP_INVENTORY))) {
                    Objects.requireNonNull(Bukkit.getWorld(world)).setGameRule(GameRule.KEEP_INVENTORY, true);
                    log.warn("发现启用死亡惩罚的世界未开启死亡不掉落");
                    log.warn("已自动设置世界 " + world + " 的游戏规则");
                }
            }

        }
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.github.com/repos/Findoutsider/DeathPunish/releases/latest")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
                    String latestVersion = (String) jsonObject.get("tag_name");
                    if (latestVersion != null && !latestVersion.equals("v" + VERSION)) {
                        log.info("[DeathPunish] 发现新版本: " + latestVersion);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.isOp()) {
                                player.sendMessage("[DeathPunish] §c发现新版本: " + latestVersion + "，请前往 https://github.com/Findoutsider/DeathPunish 更新插件");
                            }
                        }
                    } else {
                        log.info("[DeathPunish] 当前版本是最新的: v" + VERSION);
                    }
                } else {
                    log.err("[DeathPunish] 获取最新版本失败: " + response.code());
                }
            } catch (IOException | org.json.simple.parser.ParseException e) {
                log.err("[DeathPunish] 获取最新版本时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }



}
