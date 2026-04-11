package com.deathPunish.service.pluginRegionMatcher;

import java.util.List;

import org.bukkit.Location;

import com.deathPunish.service.MessageService;
import com.deathPunish.service.PluginRegionMatcher;
import com.palmergames.bukkit.towny.TownyAPI;

public class TownyRegionMatcher implements PluginRegionMatcher {
    private final MessageService messageService;
    private final TownyAPI towny;

    public TownyRegionMatcher(MessageService messageService, TownyAPI towny) {
        this.messageService = messageService;
        this.towny = towny;
    }

    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        if (towny == null || configuredRegions.isEmpty() || location.getWorld() == null) {
            return false;
        }
        try { 
            if (towny.isWilderness(location)) {
                return false;
            }
            String townName = towny.getTown(location).getName();
            for (String configuredRegion : configuredRegions) {
                if (townName != null && townName.equalsIgnoreCase(configuredRegion)) {
                    return true;
                }
            }
        } catch (Exception ex) { 
            messageService.warn("检查 Towny 区域失败:  " + ex.getMessage());
        }
        return false;
    }
    
}
