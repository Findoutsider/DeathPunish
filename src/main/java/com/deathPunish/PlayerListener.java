package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Objects;

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
            player.sendMessage("[deathpunish] ��a��ǰ����汾Ϊ" + VERSION);
            player.sendMessage("[deathpunish] ��a�����ļ��汾Ϊ" + config.getString("version"));
            player.sendMessage("[deathpunish] ��a�����߰汾��ͬ���ֶ�ɾ�������ļ�������������");
            player.sendMessage("");
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        if (!player.hasPermission("deathpunish.bypass")) {

            // FileConfiguration epitaphConfig = plugin.getEpitaphConfig(); // ��ȡ epitaph.yml ����
            if (config.getBoolean("punishments.punishOnDeath")) {

                if (config.getBoolean("punishments.enableEpitaph")) {
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

                // �����ұ���
                if (config.getBoolean("punishments.clearInventoryOnDeath")) {
                    player.getInventory().clear();
                }
                if (config.getBoolean("punishments.clearEnderchestOnDeath")) {
                    player.getEnderChest().clear();
                }

                if (config.getBoolean("punishments.resetExpOnDeath")) {
                    player.setLevel(0);
                    player.setTotalExperience(0);
                }

                this.isdeath = true;
            }
        }else {
            player.sendMessage(Objects.requireNonNull(config.getString("punishments.skipPunishMsg")));
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();
        if (this.isdeath) {
            if (config.getBoolean("punishments.punishOnDeath")) {
                player.sendMessage(Objects.requireNonNull(config.getString("punishments.deathMsg")));
                // ��ȡ��ǰ��ҵ��������ֵ
                double maxHealth = player.getMaxHealth();
                double reduceHealthAmount = config.getDouble("punishments.reduceHealthAmount");
                // �����������ֵ
                if (config.getBoolean("punishments.reduceMaxHealthOnDeath")) {
                    double newMaxHealth = Math.max(maxHealth - reduceHealthAmount, 1.0); // ��СֵΪ1.0}
                    // ������ҵ����������ֵ
                    player.setMaxHealth(newMaxHealth);
                    player.setHealth(newMaxHealth); // ���õ�ǰ����ֵΪ�µ����ֵ
                }

                if (config.getBoolean("punishments.banOnDeath") && maxHealth == 1) {
                    player.banPlayer(config.getString("punishments.banReason"));

                }

                this.isdeath = false;
            }
        }
    }


}