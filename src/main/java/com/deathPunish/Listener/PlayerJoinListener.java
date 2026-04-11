package com.deathPunish.Listener;

import com.deathPunish.DeathPunish;
import com.deathPunish.service.MessageService;

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
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        plugin.getMaxHealthModifierService().syncPlayer(player);
        if (player.isOp()) {
            player.sendMessage("");
            player.sendMessage(MessageService.PLUGIN_PREFIX + " §a当前插件版本为" + plugin.getPluginVersion());
            player.sendMessage(MessageService.PLUGIN_PREFIX + " §a配置文件版本为" + plugin.getPluginConfig().version());
            player.sendMessage(MessageService.PLUGIN_PREFIX + " §a若二者版本不同，请按最新配置模板检查并补齐配置项");
            player.sendMessage(MessageService.PLUGIN_PREFIX + " §a若从旧版本升级且玩家生命值异常，可使用 §f/deathpunish migrate <玩家> §a迁移旧数据");
            player.sendMessage(MessageService.PLUGIN_PREFIX + " §a前往 https://github.com/Findoutsider/DeathPunish 获取更新");
            player.sendMessage("");
            if (updateAvailable && latestVersion != null) {
                player.sendMessage(MessageService.PLUGIN_PREFIX + " §a检测到新版本: §6" + latestVersion + "§a，请前往§b https://github.com/Findoutsider/DeathPunish §a更新");
            }
        }
    }



}
