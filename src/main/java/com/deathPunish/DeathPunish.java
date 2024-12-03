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
        say("[DeathPunish] ��a����Ѽ���");
         // ���������ļ�
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        // ע���Զ�����Ʒ���䷽
        registerCustomRecipes(config);
//        fileCreate();
        // ע���¼�������
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(), this);
        // ע������
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));



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
                if (!Objects.requireNonNull(config.getString("version")).equalsIgnoreCase("1.3.1")) {
                    configFile.delete();
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                }
            } catch (Exception e) {
                // �����ļ�����ʧ�ܣ�ɾ�����ļ�������д��Ĭ������
                configFile.delete();
                getConfig().options().copyDefaults(true);
                saveConfig();
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

