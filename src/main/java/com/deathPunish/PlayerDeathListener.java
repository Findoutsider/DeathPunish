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
         // 获取玩家对象
        var player = event.getEntity();

        // 清除玩家背包
        player.getInventory().clear();

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // 获取玩家对象
        var player = event.getPlayer();
        var playerName = player.getName();

        // 获取配置文件
        FileConfiguration config = plugin.getConfig();

        // 读取当前玩家的最大生命值
        double maxHealth = config.getDouble(playerName + ".maxHealth", 20.0);

        // 减少最大生命值
        double newMaxHealth = Math.max(maxHealth - 2.0, 1.0); // 最小值为1.0

        // 更新配置文件
        config.set(playerName + ".maxHealth", newMaxHealth);
        plugin.saveConfig();

        // 设置玩家的新最大生命值
        player.setMaxHealth(newMaxHealth);
        player.setHealth(newMaxHealth); // 重置当前生命值为新的最大值
    }
}