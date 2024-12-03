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
        // FileConfiguration epitaphConfig = plugin.getEpitaphConfig(); // ��ȡ epitaph.yml ����
        if (config.getBoolean("punishOnDeath")) {
            // ��ȡ��Ҷ���
            var player = event.getEntity();
            if (config.getBoolean("enableEpitaph")) {
                var position = player.getLocation();

                position.getBlock().setType(Material.BEDROCK);
//                List<String> epitaphs = epitaphConfig.getStringList("defaultEpitaph");
//                if (!epitaphs.isEmpty()) {
//                    Random random = new Random();
//                    String selectedEpitaph = epitaphs.get(random.nextInt(epitaphs.size()));
//                    // ���������ı�
//                    NMSUtil.createFloatingText(position.clone().add(0, 1, 0), selectedEpitaph);
//                }
                Epitaph.createFloatingText(position.clone().add(0, 1, 0), "��4��l" + player.getName() + "���ڴ˵�");
            }
            // ��ȡ��ʳ��
            foodLevel = player.getFoodLevel();
            player.sendMessage(String.valueOf(foodLevel));

            // �����ұ���
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
                    player.setTotalExperience(0);
                }

                if (config.getBoolean("banOnDeath") && maxHealth == 1) {
                    player.banPlayer(config.getString("banReason"));
                }
            }
        }
    }


}