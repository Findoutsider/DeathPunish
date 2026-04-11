package com.deathPunish.service.pluginRegionMatcher;

import java.util.List;

import org.bukkit.Location;

import com.deathPunish.service.MessageService;
import com.deathPunish.service.PluginRegionMatcher;

import cn.lunadeer.dominion.api.DominionAPI;

public class DominionRegionMatcher implements PluginRegionMatcher {
    private DominionAPI dominion;
    private MessageService messageService;

    public DominionRegionMatcher(MessageService messageService, DominionAPI dominion) {
        this.messageService = messageService;
        this.dominion = dominion;
    }
    
    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        if (dominion == null || configuredRegions.isEmpty() || location.getWorld() == null) {
            return false;
        }
        try {
            String regionName = dominion.getDominion(location).getName();
            for (String configuredRegion : configuredRegions) {
                if (regionName != null && regionName.equalsIgnoreCase(configuredRegion)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            messageService.warn("检查 Dominion 区域失败: " + ex.getMessage());
        }
        return false;
    }
    
}
