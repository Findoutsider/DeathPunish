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
        // ����Ĭ�������ļ�
        saveDefaultConfig();
        config = getConfig();
        // ע���Զ�����Ʒ�䷽
        registerCustomRecipes(config);
        // ע���¼�������
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(), this);
        // ע������
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));
        setWorldRule();
        log.info("[DeathPunish] ��a���������");
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        // �������ʱ�Ĵ���
        if (log != null) log.info("����ѽ���");
    }

    public void registerCustomRecipes(FileConfiguration config) {
        enchantedGoldenAppleRecipe = CustomItems.createEnchantedGoldenApple(config);
        getServer().resetRecipes(); // �����䷽
        getServer().addRecipe(enchantedGoldenAppleRecipe);
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        // ��������ļ��Ƿ����
        if (!configFile.exists()) {
            // ����ļ������ڣ�д��Ĭ������
            getConfig().options().copyDefaults(true);
            saveConfig();
        } else {
            // ����ļ����ڣ����汾
            try {
                FileConfiguration config = getConfig();
                // ��������ļ��İ汾
                if (!Objects.requireNonNull(config.getString("version")).equalsIgnoreCase(VERSION)) {
                    configFile.delete();
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                    log.info("[DeathPunish] ��a�Ѹ��������ļ��� v" + VERSION);
                }
            } catch (Exception e) {
                // ��������ļ���ȡʧ�ܣ�ɾ���ļ���д��Ĭ������
                configFile.delete();
                getConfig().options().copyDefaults(true);
                saveConfig();
                log.info("[DeathPunish] ��c�����ļ���ȡʧ�ܣ��ѻָ�Ĭ������");
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
        if (result) enableEco = true; log.info("[DeathPunish] ��a��������������");
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
                    log.warn("�������������ͷ�������δ��������������");
                    log.warn("���Զ��������� " + world + " ����Ϸ����");
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
                        log.info("[DeathPunish] �����°汾: " + latestVersion);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.isOp()) {
                                player.sendMessage("[DeathPunish] ��c�����°汾: " + latestVersion + "����ǰ�� https://github.com/Findoutsider/DeathPunish ���²��");
                            }
                        }
                    } else {
                        log.info("[DeathPunish] ��ǰ�汾�����µ�: v" + VERSION);
                    }
                } else {
                    log.err("[DeathPunish] ��ȡ���°汾ʧ��: " + response.code());
                }
            } catch (IOException | org.json.simple.parser.ParseException e) {
                log.err("[DeathPunish] ��ȡ���°汾ʱ��������: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }



}
