package com.deathPunish.utils.manager;

import com.deathPunish.DeathPunish;
import com.deathPunish.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;

import static com.deathPunish.DeathPunish.log;

public class WorldManager {
    private final DeathPunish pl;

    public WorldManager(DeathPunish pl) {
        this.pl = pl;
        setWorldRule();
    }

    public void setWorldRule() {
        SchedulerUtils.runTask(pl, () -> {
            var pluginConfig = pl.getPluginConfig();
            if (pluginConfig.enableDeathPunish()) {
                for (var bukkitWorld : Bukkit.getWorlds()) {
                    bukkitWorld.setGameRule(GameRule.KEEP_INVENTORY, pluginConfig.autoSetRule());
                    log.info("已设置世界 " + bukkitWorld.getName() + " 的 KEEP_INVENTORY=" + pluginConfig.autoSetRule());
                }
            }
            boolean immediateRespawn = SchedulerUtils.isFolia() || pluginConfig.doImmediateRespawn();
            if (pluginConfig.enableDeathPunish()) {
                for (var bukkitWorld : Bukkit.getWorlds()) {
                    bukkitWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, immediateRespawn);
                    log.info("已设置世界 " + bukkitWorld.getName() + " 的 DO_IMMEDIATE_RESPAWN=" + immediateRespawn);
                }
            }
        });
    }
}
