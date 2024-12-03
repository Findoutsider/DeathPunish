package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {

    private int foodLevel = 20;
    private boolean isdeath = false;

    public PlayerDeathListener(DeathPunish plugin) {
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();
        // FileConfiguration epitaphConfig = plugin.getEpitaphConfig(); // 获取 epitaph.yml 配置
        if (config.getBoolean("punishOnDeath")) {
            // 获取玩家对象
            var player = event.getEntity();
            if (config.getBoolean("enableEpitaph")) {
                var position = player.getLocation();

                position.getBlock().setType(Material.BEDROCK);
//                List<String> epitaphs = epitaphConfig.getStringList("defaultEpitaph");
//                if (!epitaphs.isEmpty()) {
//                    Random random = new Random();
//                    String selectedEpitaph = epitaphs.get(random.nextInt(epitaphs.size()));
//                    // 创建悬浮文本
//                    NMSUtil.createFloatingText(position.clone().add(0, 1, 0), selectedEpitaph);
//                }
                Epitaph.createFloatingText(position.clone().add(0, 1, 0), "§4§l" + player.getName() + "死于此地");
            }
            // 获取饱食度
            foodLevel = player.getFoodLevel();
            player.sendMessage(String.valueOf(foodLevel));

            // 清除玩家背包
            if (config.getBoolean("clearInventoryOnDeath")) {
                player.getInventory().clear();
            }
            if (config.getBoolean("clearEnderchestOnDeath")) {
                player.getEnderChest().clear();
            }

            this.isdeath = true;
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();
        if (this.isdeath) {
            if (config.getBoolean("punishOnDeath")) {
                player.sendMessage("§4§l你死了，你因此失去受到了惩罚！");
                // 读取当前玩家的最大生命值
                double maxHealth = player.getMaxHealth();

                // 减少最大生命值
                if (config.getBoolean("reduceMaxHealthOnDeath")) {
                    double newMaxHealth = Math.max(maxHealth - 2.0, 1.0); // 最小值为1.0}
                    // 设置玩家的新最大生命值
                    player.setMaxHealth(newMaxHealth);
                    player.setHealth(newMaxHealth); // 重置当前生命值为新的最大值
                }
                if (config.getBoolean("refillFoodLevelOnDeath")) {
                    player.setFoodLevel(20);
                } else {
                    player.setFoodLevel(foodLevel);
                }

                if (config.getBoolean("resetExpOnDeath")) {
                    player.setTotalExperience(0);
                }

                if (config.getBoolean("banOnDeath") && maxHealth == 1) {
                    player.banPlayer(config.getString("banReason"));
                }
            }
        }
    }


}