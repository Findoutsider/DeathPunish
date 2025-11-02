package com.deathPunish;

import com.deathPunish.Listener.PlayerDeathListener;
import com.deathPunish.utils.LoggerUtils;
import com.deathPunish.utils.Metrics;
import com.deathPunish.utils.SchedulerUtils;
import com.deathPunish.utils.manager.CustomItems;
import com.deathPunish.utils.manager.WorldManager;
import com.deathPunish.commands.DeathPunishCommand;
import com.tcoded.folialib.FoliaLib;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;
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

import static com.deathPunish.utils.manager.ConfigManager.getAllConfigs;

public final class DeathPunish extends JavaPlugin {

    private static FoliaLib foliaLib;
    private static WorldManager worldManager;
    public static FileConfiguration config;
    public static LoggerUtils log;

    public final static String VERSION = "1.4.3";
    public static Map<Boolean, String> ifNeedUpdate = new HashMap<>();;
    public static Economy econ = null;
    public static boolean enableEco = false;
    public ShapedRecipe enchantedGoldenAppleRecipe;

    public static FoliaLib getFoliaLib() { return foliaLib; }
    public static WorldManager getWorldManger() { return worldManager; }

    @Override
    public void onEnable() {
        // 初始化
        log = new LoggerUtils();
        config = getConfig();
        getAllConfigs();
        foliaLib = new FoliaLib(this);
        worldManager = new WorldManager(this);

        // stats
        new Metrics(this, 24171);
        setupEconomy();

        // 保存默认配置文件
        saveDefaultConfig();

        // 注册自定义物品配方
        registerCustomRecipes(config);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new com.deathPunish.Listener.EatCustomItemListener(), this);

        // 注册命令
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));
        log.info("插件已启用");
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (log != null) log.info("插件已禁用");
    }

    public void registerCustomRecipes(FileConfiguration config) {
        enchantedGoldenAppleRecipe = CustomItems.createEnchantedGoldenApple(config);
        if (!SchedulerUtils.isFolia()) getServer().resetRecipes(); // 重置配方
        
        // 检查配方是否已经存在
        boolean recipeExists = false;
        for (org.bukkit.inventory.Recipe recipe : getServer().getRecipesFor(enchantedGoldenAppleRecipe.getResult())) {
            if (recipe instanceof ShapedRecipe && 
                ((ShapedRecipe) recipe).getKey().equals(enchantedGoldenAppleRecipe.getKey())) {
                recipeExists = true;
                break;
            }
        }
        
        // 如果配方不存在，则添加
        if (!recipeExists) {
            getServer().addRecipe(enchantedGoldenAppleRecipe);
        }
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

    private void checkForUpdates() {
        SchedulerUtils.runTaskAsynchronously(this, () -> {
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
