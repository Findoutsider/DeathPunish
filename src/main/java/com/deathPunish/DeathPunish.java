package com.deathPunish;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public final class DeathPunish extends JavaPlugin {

    public final static String VERSION = "1.4.0";
    public static Map<Boolean, String> ifNeedUpdate = new HashMap<>();;
    public static Economy econ = null;
    public static boolean enableEco = false;
    private FileConfiguration epitaphConfig;
    public static FileConfiguration config;
    public ShapedRecipe enchantedGoldenAppleRecipe;
    public static com.deathPunish.Utils.LoggerUtils log;
    public static List<String> worlds;

    @Override
    public void onEnable() {
        log = new com.deathPunish.Utils.LoggerUtils();
        int pluginId = 24171;
        com.deathPunish.Utils.Metrics metrics = new com.deathPunish.Utils.Metrics(this, pluginId);
        setupEconomy();
        // 保存默认配置文件
        saveDefaultConfig();
        config = getConfig();
        // 注册自定义物品配方
        registerCustomRecipes(config);
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new com.deathPunish.Listener.PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new com.deathPunish.Listener.EatCustomItemListener(), this);
        // 注册命令
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));
        setWorldRule();
        log.info("插件已启用");
        checkForUpdates();
    }

    @Override
    public void onDisable() {
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
//                    log.info("[DeathPunish] §a已更新配置文件至 v" + VERSION);
                }
            } catch (Exception e) {
                // 如果配置文件读取失败，删除文件并写入默认配置
                configFile.delete();
                getConfig().options().copyDefaults(true);
                saveConfig();
                log.err("配置文件读取失败，已恢复默认配置");
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
        if (result) {
            enableEco = true;
            log.info("经济内容已启动");
        } else {
            log.err("未检测到Vault，经济相关功能无法使用");
        }
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
            try {
                URL url = new URL("https://api.github.com/repos/Findoutsider/DeathPunish/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonObject = getJsonObject(connection);
                    String latestVersion = (String) jsonObject.get("tag_name");
                    String info = (String) jsonObject.get("body");
                    if (latestVersion != null) {
                        int latestVersionInt = Integer.parseInt(latestVersion.replace("v", "").replace(".", ""));
                        int currentVersion = Integer.parseInt(VERSION.replace(".", ""));
                        if (latestVersionInt > currentVersion) {
                            log.info("检测到新版本: " + latestVersion + "，请前往 https://github.com/Findoutsider/DeathPunish 更新");

                            ifNeedUpdate.put(true, latestVersion);

                            if (!info.equalsIgnoreCase("")) {
                                log.info("新版本信息: " + info);
                            }
                        } else if (latestVersionInt < currentVersion) {
                            log.info("你正在使用开发版本！v" + VERSION);
                        } else {
                            log.info("当前版本已是最新: v" + VERSION);
                            ifNeedUpdate.put(false, null);
                        }
                    }
                } else {
                    log.err("获取最新版本失败: " + responseCode);
                }
            } catch (IOException | org.json.simple.parser.ParseException e) {
                log.err("获取最新版本时发生异常: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            }
        });
    }

    private static JSONObject getJsonObject(HttpURLConnection connection) throws IOException, org.json.simple.parser.ParseException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(content.toString());
    }

}
