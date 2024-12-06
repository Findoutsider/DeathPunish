package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class DeathPunish extends JavaPlugin {

    public final static String VERSION = "1.3.4";
    private FileConfiguration epitaphConfig;
    public ShapedRecipe enchantedGoldenAppleRecipe;

    @Override
    public void onEnable() {
        say("[DeathPunish] ��a����Ѽ���");
         // ���������ļ�
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        // ע���Զ�����Ʒ���䷽
        registerCustomRecipes(config);
//        fileCreate();
        // ע���¼�������
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(), this);
        // ע������
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));
        this.getCommand("dp").setExecutor(new DeathPunishCommand(this));



    }

    @Override
    public void onDisable() {
        // �������ʱ�Ĵ���
        say("[DeathPunish] ��a�����ж��");
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

        // ��������ļ��Ƿ����
        if (!configFile.exists()) {
            // �����ļ������ڣ�д��Ĭ������
            getConfig().options().copyDefaults(true);
            saveConfig();
        } else {
            // �����ļ����ڣ����Լ�������
            try {
                FileConfiguration config = getConfig();
                // ��������ļ��Ƿ�����
                if (!Objects.requireNonNull(config.getString("version")).equalsIgnoreCase(VERSION)) {
                    configFile.delete();
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                    say("[DeathPunish] ��a�ѽ������ļ������� v" + VERSION);
                }
            } catch (Exception e) {
                // �����ļ�����ʧ�ܣ�ɾ�����ļ�������д��Ĭ������
                configFile.delete();
                getConfig().options().copyDefaults(true);
                saveConfig();
                say("[DeathPunish] ��c�����ļ�����ʧ�ܣ�������Ĭ������");
            }
        }
    }

//    public void fileCreate() {
//        // ���� message �ļ���
//        File messageFolder = new File(getDataFolder(), "message");
//        if (!messageFolder.exists()) {
//            messageFolder.mkdir();
//        }
//
//        // ���� epitaph.yml �ļ�
//    }
}

