package com.deathPunish.service;

import com.deathPunish.DeathPunish;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Location;

import java.util.List;

public class WorldGuardPluginRegionMatcher implements PluginRegionMatcher {
    private final MessageService messageService;

    public WorldGuardPluginRegionMatcher(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        if (!DeathPunish.enableWorldGuard || configuredRegions.isEmpty() || location.getWorld() == null) {
            return false;
        }
        try {
            var regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            var regionManager = regionContainer.get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager == null) {
                return false;
            }
            var applicableRegions = regionManager.getApplicableRegions(BlockVector3.at(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            ));
            for (var region : applicableRegions) {
                String regionId = region.getId();
                for (String configuredRegion : configuredRegions) {
                    if (regionId.equalsIgnoreCase(configuredRegion)) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            messageService.warn("检查插件区域失败: " + ex.getMessage());
        }
        return false;
    }
}
