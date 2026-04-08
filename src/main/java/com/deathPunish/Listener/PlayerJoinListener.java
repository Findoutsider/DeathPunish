package com.deathPunish.Listener;

import com.deathPunish.DeathPunish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.deathPunish.DeathPunish.*;

public class PlayerJoinListener implements Listener {
    private final DeathPunish plugin;

    public PlayerJoinListener(DeathPunish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdminLogin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (player.isOp()) {
            player.sendMessage("");
            player.sendMessage("[deathpunish] §a当前插件版本为" + plugin.getPluginVersion());
            player.sendMessage("[deathpunish] §a配置文件版本为" + plugin.getPluginConfig().version());
            player.sendMessage("[deathpunish] §a若二者版本不同，请按最新配置模板检查并补齐配置项");
            player.sendMessage("[deathpunish] §a前往 https://github.com/Findoutsider/DeathPunish 获取更新");
            player.sendMessage("");
            if (updateAvailable && latestVersion != null) {
                player.sendMessage("§8[§bDeathPunish§8] §a检测到新版本: §6" + latestVersion + "§a，请前往§b https://github.com/Findoutsider/DeathPunish §a更新");
            }
        }
    }



}
