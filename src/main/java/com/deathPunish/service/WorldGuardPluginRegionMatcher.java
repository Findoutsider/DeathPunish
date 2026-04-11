package com.deathPunish.service;

import com.deathPunish.DeathPunish;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.Location;

import java.util.List;

import static com.deathPunish.DeathPunish.worldGuard;

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
            RegionContainer regionContainer = worldGuard.getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager == null) {
                return false;
            }
            ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(BlockVector3.at(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            ));
            for (ProtectedRegion region : applicableRegions) {
                String regionId = region.getId();
                for (String configuredRegion : configuredRegions) {
                    if (regionId.equalsIgnoreCase(configuredRegion)) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            messageService.warn("检查 WorldGuard 区域失败: " + ex.getMessage());
        }
        return false;
    }
}
