package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import static com.deathPunish.DeathPunish.VERSION;


public class PlayerListener implements Listener {

    private boolean isdeath = false;
    FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();

    public PlayerListener(DeathPunish plugin) {
    }

    @EventHandler
    public void onAdminLogin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (player.isOp()) {
            player.sendMessage("");
            player.sendMessage("[deathpunish] §a当前插件版本为" + VERSION);
            player.sendMessage("[deathpunish] §a配置文件版本为" + config.getString("version"));
            player.sendMessage("[deathpunish] §a若二者版本不同请手动删除配置文件后重启服务器");
            player.sendMessage("");
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        if (!player.hasPermission("deathpunish.bypass")) {

            // FileConfiguration epitaphConfig = plugin.getEpitaphConfig(); // 获取 epitaph.yml 配置
            if (config.getBoolean("punishOnDeath")) {

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

                // 清除玩家背包
                if (config.getBoolean("clearInventoryOnDeath")) {
                    player.getInventory().clear();
                }
                if (config.getBoolean("clearEnderchestOnDeath")) {
                    player.getEnderChest().clear();
                }

                if (config.getBoolean("resetExpOnDeath")) {
                    player.setLevel(0);
                    player.setTotalExperience(0);
                    player.sendMessage("resetHealth");
                }

                this.isdeath = true;
            }
        }else {
            player.sendMessage("§a你逃过了死亡惩罚！");
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();
        if (this.isdeath) {
            if (config.getBoolean("punishOnDeath")) {
                player.sendMessage("§4§l你死了，你因此失去受到了惩罚！");
                // 读取当前玩家的最大生命值
                double maxHealth = player.getMaxHealth();
                double reduceHealthAmount = config.getDouble("reduceHealthAmount");
                // 减少最大生命值
                if (config.getBoolean("reduceMaxHealthOnDeath")) {
                    double newMaxHealth = Math.max(maxHealth - reduceHealthAmount, 1.0); // 最小值为1.0}
                    // 设置玩家的新最大生命值
                    player.setMaxHealth(newMaxHealth);
                    player.setHealth(newMaxHealth); // 重置当前生命值为新的最大值
                }

                if (config.getBoolean("banOnDeath") && maxHealth == 1) {
                    player.banPlayer(config.getString("banReason"));

                }
            }
        }
    }


}