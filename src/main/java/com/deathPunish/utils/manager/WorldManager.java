package com.deathPunish.utils.manager;

import com.deathPunish.DeathPunish;
import com.deathPunish.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import java.util.List;

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
            List<String> worlds = pluginConfig.enableWorlds();
            if (pluginConfig.enableDeathPunish()) {
                for (String world : worlds) {
                    var bukkitWorld = Bukkit.getWorld(world);
                    if (bukkitWorld == null) {
                        log.warn("未找到世界 " + world + "，已跳过规则设置");
                        continue;
                    }
                    bukkitWorld.setGameRule(GameRule.KEEP_INVENTORY, pluginConfig.autoSetRule());
                    log.info("已设置世界 " + world + " 的 KEEP_INVENTORY=" + pluginConfig.autoSetRule());
                }
            }
            boolean immediateRespawn = SchedulerUtils.isFolia() || pluginConfig.doImmediateRespawn();
            if (pluginConfig.enableDeathPunish()) {
                for (String world : worlds) {
                    var bukkitWorld = Bukkit.getWorld(world);
                    if (bukkitWorld == null) {
                        continue;
                    }
                    bukkitWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, immediateRespawn);
                    log.info("已设置世界 " + world + " 的 DO_IMMEDIATE_RESPAWN=" + immediateRespawn);
                }
            }
        });
    }
}
