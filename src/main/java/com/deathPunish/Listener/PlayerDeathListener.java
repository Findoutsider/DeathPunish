package com.deathPunish.Listener;

import com.deathPunish.service.PunishmentService;
import com.deathPunish.utils.SchedulerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {
    private final PunishmentService punishmentService;

    public PlayerDeathListener(PunishmentService punishmentService) {
        this.punishmentService = punishmentService;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (SchedulerUtils.isFolia()) {
            return;
        }
        punishmentService.handleRespawn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        punishmentService.handleDeath(event.getEntity());
    }
}
