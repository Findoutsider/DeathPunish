package com.deathPunish.utils.manager;

import com.deathPunish.DeathPunish;
import com.deathPunish.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;

import static com.deathPunish.DeathPunish.log;
import static com.deathPunish.utils.manager.ConfigManager.*;

public class WorldManager {
    private final DeathPunish pl;

    public WorldManager(DeathPunish pl) {
        this.pl = pl;
        setWorldRule();
    }

    public void setWorldRule() {
        SchedulerUtils.runTask(pl, () -> {
            List<String> worlds = enableWorlds;
            // 死亡不掉落
            if (enableDeathPunish) {
                for (String world : worlds) {
                    Objects.requireNonNull(Bukkit.getWorld(world)).setGameRule(GameRule.KEEP_INVENTORY, autoSetRule);
                    log.warn("发现启用死亡惩罚的世界未开启死亡不掉落");
                    log.warn("已自动设置世界 " + world + " 的游戏规则为" + autoSetRule);
                }
            }
            // 立即重生
            boolean y = SchedulerUtils.isFolia() || doImmediateRespawn;
            if (enableDeathPunish) {
                for (String world : worlds) {
                    Objects.requireNonNull(Bukkit.getWorld(world)).setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, y);
                    log.warn("发现启用死亡惩罚的世界未开启立即重生");
                    log.warn("已自动设置世界 " + world + " 的游戏规则为" + y);
                }
            }
        });
    }
}
