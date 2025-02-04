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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

public final class DeathPunish extends JavaPlugin {

    public final static String VERSION = "1.3.8";
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
        log.info("���������");
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
//                    log.info("[DeathPunish] ��a�Ѹ��������ļ��� v" + VERSION);
                }
            } catch (Exception e) {
                // ��������ļ���ȡʧ�ܣ�ɾ���ļ���д��Ĭ������
                configFile.delete();
                getConfig().options().copyDefaults(true);
                saveConfig();
                log.err("�����ļ���ȡʧ�ܣ��ѻָ�Ĭ������");
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
        if (result) enableEco = true; log.info("��������������");
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
            try {
                URL url = new URL("https://api.github.com/repos/Findoutsider/DeathPunish/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonObject = getJsonObject(connection);
                    String latestVersion = (String) jsonObject.get("tag_name");
                    String info = (String) jsonObject.get("body");

                    if (latestVersion != null && !latestVersion.equalsIgnoreCase("v" + VERSION)) {
                        log.info("��⵽�°汾: " + latestVersion + "����ǰ�� https://github.com/Findoutsider/DeathPunish ����");
                        if (!info.equalsIgnoreCase("")) {
                            log.info("�°汾��Ϣ: " + info);
                        }
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.isOp()) {
                                player.sendMessage("��8[��bDeathPunish��8] ��r��⵽�°汾: ��a" + latestVersion
                                        + "��r����ǰ�� https://github.com/Findoutsider/DeathPunish ����");
                                if (!info.equalsIgnoreCase("")) {
                                    player.sendMessage("��8[��bDeathPunish��8] ��r�°汾��Ϣ: ��a" + info);
                                }
                            }
                        }
                    } else {
                        log.info("��ǰ�汾��������: v" + VERSION);
                    }
                } else {
                    log.err("��ȡ���°汾ʧ��: " + responseCode);
                }
            } catch (IOException | org.json.simple.parser.ParseException e) {
                log.err("��ȡ���°汾ʱ�����쳣: " + e.getMessage());
                e.printStackTrace();
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
