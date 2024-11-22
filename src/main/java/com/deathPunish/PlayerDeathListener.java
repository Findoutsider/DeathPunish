package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {

    public PlayerDeathListener(DeathPunish plugin) {
    }

    int foodLevel;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();

        var player = event.getPlayer();
        player.setMaxHealth(config.getDouble("defaultMaxHealth"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();
        if (config.getBoolean("punishOnDeath")) {
            // ��ȡ��Ҷ���
            var player = event.getEntity();

            // ��ȡ��ʳ��
            foodLevel = player.getFoodLevel();

            // �����ұ���
            if (config.getBoolean("clearInventoryOnDeath")) {
                player.getInventory().clear();
            }
            if (config.getBoolean("clearEnderchestOnDeath")) {
                player.getEnderChest().clear();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();
        if (config.getBoolean("punishOnDeath")) {
            player.sendMessage("��4��l�����ˣ������ʧȥ�ܵ��˳ͷ���");
            // ��ȡ��ǰ��ҵ��������ֵ
            double maxHealth = player.getMaxHealth();

            // �����������ֵ
            if (config.getBoolean("reduceMaxHealthOnDeath")) {
                double newMaxHealth = Math.max(maxHealth - 2.0, 1.0); // ��СֵΪ1.0}
                // ������ҵ����������ֵ
                player.setMaxHealth(newMaxHealth);
                player.setHealth(newMaxHealth); // ���õ�ǰ����ֵΪ�µ����ֵ
            }
            if (config.getBoolean("refillFoodLevelOnDeath")) {
                player.setFoodLevel(20);
            } else {
                player.setFoodLevel(foodLevel);
            }

            if (config.getBoolean("resetExpOnDeath")) {
                player.setExp(0);
            }

            if (config.getBoolean("banOnDeath") && maxHealth == 1) {
                player.banPlayer(config.getString("banReason"));
            }
        }
    }
}