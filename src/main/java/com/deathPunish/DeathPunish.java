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
        say("[DeathPunish] ��a����Ѽ���");
         // ���������ļ�
        saveDefaultConfig();
        // ע���¼�������
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EatCustomItemListener(), this);
        // ע������
        this.getCommand("deathpunish").setExecutor(new DeathPunishCommand(this));
        this.getCommand("deathpunish").setTabCompleter(new DeathPunishCommand(this));

        getServer().addRecipe(CustomItems.createEnchantedGoldenApple());
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
                // ��������ļ��Ƿ���Ч
                if (config.contains("version") && config.contains("punishOnDeath") && config.contains("defaultMaxHealth") 
                        && config.contains("refillFoolLevelOnDeath") && config.contains("resetExpOnDeath") && config.contains("clearInventoryOnDeath")
                        && config.contains("clearEnderchestOnDeath") && config.contains("banOnDeath") && config.contains("banReason")) {
                    // �����ļ���Ч������ȡ
                    return;
                } else if (!Objects.requireNonNull(config.getString("version")).equalsIgnoreCase("1.2.1")) {
                    configFile.delete();
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                } else {
                    // �����ļ���Ч��ɾ�����ļ�������д��Ĭ������
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
}

