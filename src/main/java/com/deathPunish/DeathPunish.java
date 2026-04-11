package com.deathPunish;

import com.deathPunish.config.PluginConfig;
import com.bekvon.bukkit.residence.Residence;
import com.deathPunish.Listener.EatCustomItemListener;
import com.deathPunish.Listener.PlayerDeathListener;
import com.deathPunish.Listener.PlayerInteractListener;
import com.deathPunish.Listener.PlayerJoinListener;
import com.deathPunish.service.CustomItemService;
import com.deathPunish.service.MaxHealthModifierService;
import com.deathPunish.service.MessageService;
import com.deathPunish.service.PunishmentService;
import com.deathPunish.utils.LoggerUtils;
import com.deathPunish.utils.Metrics;
import com.deathPunish.utils.SchedulerUtils;
import com.deathPunish.utils.manager.WorldManager;
import com.sk89q.worldguard.WorldGuard;
import com.deathPunish.commands.DeathPunishCommand;
import com.tcoded.folialib.FoliaLib;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class DeathPunish extends JavaPlugin {

    private static FoliaLib foliaLib;
    private static WorldManager worldManager;
    public static FileConfiguration config;
    public static LoggerUtils log;
    private PluginConfig pluginConfig;
    private CustomItemService customItemService;
    private MaxHealthModifierService maxHealthModifierService;
    private PunishmentService punishmentService;
    private MessageService messageService;

    public static volatile String latestVersion;
    public static volatile boolean updateAvailable;

    public static Economy econ = null;
    public static WorldGuard worldGuard = null;
    public static Residence residence = null;

    public static boolean enableEco = false;
    public static boolean enableWorldGuard = false;
    public static boolean enableResidence = false;
    public ShapedRecipe enchantedGoldenAppleRecipe;

    public static FoliaLib getFoliaLib() { return foliaLib; }
    public static WorldManager getWorldManger() { return worldManager; }
    public PluginConfig getPluginConfig() { return pluginConfig; }
    public CustomItemService getCustomItemService() { return customItemService; }
    public MaxHealthModifierService getMaxHealthModifierService() { return maxHealthModifierService; }
    public PunishmentService getPunishmentService() { return punishmentService; }
    public MessageService getMessageService() { return messageService; }
    public String getPluginVersion() { return getDescription().getVersion(); }

    @Override
    public void onEnable() {
        log = new LoggerUtils();
        messageService = new MessageService(log);
        saveDefaultConfig();
        reloadConfig();
        refreshConfigState();
        maxHealthModifierService = new MaxHealthModifierService(this);
        customItemService = new CustomItemService(this);
        punishmentService = new PunishmentService(this, customItemService, messageService, maxHealthModifierService);
        foliaLib = new FoliaLib(this);
        worldManager = new WorldManager(this);
        new Metrics(this, 24171);
        setupSoftDependency();
        registerCustomRecipes();

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(punishmentService), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(customItemService), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(customItemService), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getOnlinePlayers().forEach(maxHealthModifierService::syncPlayer);

        var deathPunishCommand = new DeathPunishCommand(this, customItemService);
        Objects.requireNonNull(getCommand("deathpunish"), "deathpunish command not defined").setExecutor(deathPunishCommand);
        Objects.requireNonNull(getCommand("deathpunish"), "deathpunish command not defined").setTabCompleter(deathPunishCommand);
        messageService.info("插件已启用");
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (maxHealthModifierService != null) {
            getServer().getOnlinePlayers().forEach(maxHealthModifierService::clearModifier);
        }
        if (messageService != null) messageService.info("插件已禁用");
    }

    public void registerCustomRecipes() {
        enchantedGoldenAppleRecipe = customItemService.createHealRecipe();
        getServer().removeRecipe(enchantedGoldenAppleRecipe.getKey());
        getServer().addRecipe(enchantedGoldenAppleRecipe);
    }

    public void refreshConfigState() {
        config = getConfig();
        pluginConfig = PluginConfig.from(config);
        warnIfConfigOutdated(pluginConfig);
    }

    private void warnIfConfigOutdated(PluginConfig currentConfig) {
        var configVersion = currentConfig.version();
        var pluginVersion = getPluginVersion();
        if (!pluginVersion.equalsIgnoreCase(configVersion == null ? "" : configVersion)) {
            messageService.warn("配置文件版本为 " + configVersion + "，插件版本为 " + pluginVersion + "，建议同步更新配置");
        }
    }

    private void setupSoftDependency() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
                enableEco = true;
            }
        }
            
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            enableWorldGuard = true;
            worldGuard = WorldGuard.getInstance();
        }

        if (getServer().getPluginManager().getPlugin("Residence") != null) {
            enableResidence = true;
            residence = Residence.getInstance();
        }

        messageService.info("\n=================================\n\n已启用依赖：\n" + (enableEco ? "Vault \n" : "") + (enableWorldGuard ? "WorldGuard \n" : "") + (enableResidence ? "Residence \n" : "") + "\n=================================\n");
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
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "DeathPunish/" + getPluginVersion());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonObject = getJsonObject(connection);
                    String latestVersion = (String) jsonObject.get("tag_name");
                    String info = (String) jsonObject.get("body");
                    if (latestVersion != null) {
                        String pluginVersion = getPluginVersion();
                        int compareResult = compareVersion(pluginVersion, latestVersion);
                        if (compareResult < 0) {
                            messageService.info("检测到新版本: " + latestVersion + "，请前往 https://github.com/Findoutsider/DeathPunish 更新");
                            DeathPunish.latestVersion = latestVersion;
                            updateAvailable = true;
                            if (info != null && !info.isBlank()) {
                                messageService.info("新版本信息: " + info);
                            }
                        } else if (compareResult > 0) {
                            messageService.info("你正在使用开发版本: v" + pluginVersion);
                        } else {
                            messageService.info("当前版本已是最新: v" + pluginVersion);
                            DeathPunish.latestVersion = null;
                            updateAvailable = false;
                        }
                    }
                } else {
                    messageService.error("获取最新版本失败: " + responseCode);
                }
            } catch (IOException | org.json.simple.parser.ParseException e) {
                messageService.error("获取最新版本时发生异常: " + e.getMessage());
            }
        });
    }

    private static JSONObject getJsonObject(HttpURLConnection connection) throws IOException, org.json.simple.parser.ParseException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(content.toString());
    }

    private static int compareVersion(String left, String right) {
        var leftParts = normalizeVersion(left).split("\\.");
        var rightParts = normalizeVersion(right).split("\\.");
        var maxLength = Math.max(leftParts.length, rightParts.length);
        for (int i = 0; i < maxLength; i++) {
            var leftValue = i < leftParts.length ? Integer.parseInt(leftParts[i]) : 0;
            var rightValue = i < rightParts.length ? Integer.parseInt(rightParts[i]) : 0;
            if (leftValue != rightValue) {
                return Integer.compare(leftValue, rightValue);
            }
        }
        return 0;
    }

    private static String normalizeVersion(String version) {
        return version.replaceFirst("^[vV]", "").split("[-+]")[0];
    }

}
