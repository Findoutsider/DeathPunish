package com.deathPunish;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {

    private final DeathPunish plugin;

    public PlayerDeathListener(DeathPunish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
         // ��ȡ��Ҷ���
        var player = event.getEntity();

        // �����ұ���
        player.getInventory().clear();

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // ��ȡ��Ҷ���
        var player = event.getPlayer();
        var playerName = player.getName();

        // ��ȡ�����ļ�
        FileConfiguration config = plugin.getConfig();

        // ��ȡ��ǰ��ҵ��������ֵ
        double maxHealth = config.getDouble(playerName + ".maxHealth", 20.0);

        // �����������ֵ
        double newMaxHealth = Math.max(maxHealth - 2.0, 1.0); // ��СֵΪ1.0

        // ���������ļ�
        config.set(playerName + ".maxHealth", newMaxHealth);
        plugin.saveConfig();

        // ������ҵ����������ֵ
        player.setMaxHealth(newMaxHealth);
        player.setHealth(newMaxHealth); // ���õ�ǰ����ֵΪ�µ����ֵ
    }
}