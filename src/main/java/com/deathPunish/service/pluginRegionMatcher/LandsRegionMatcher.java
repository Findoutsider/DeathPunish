package com.deathPunish.service.pluginRegionMatcher;

import java.util.List;

import org.bukkit.Location;

import com.deathPunish.service.MessageService;
import com.deathPunish.service.PluginRegionMatcher;

import me.angeschossen.lands.api.LandsIntegration;

public class LandsRegionMatcher implements PluginRegionMatcher {
    private MessageService messageService;
    private LandsIntegration lands;

    public LandsRegionMatcher(MessageService messageService, LandsIntegration lands) {
        this.messageService = messageService;
        this.lands = lands;
    }

    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        if (lands == null || configuredRegions.isEmpty() || location.getWorld() == null) {
            return false;
        }
        try {
            String regionName = lands.getArea(location).getName();
            for (String configuredRegion : configuredRegions) {
                if (regionName != null && regionName.equalsIgnoreCase(configuredRegion)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            messageService.warn("检查 Lands 区域失败: " + ex.getMessage());
        }
        return false;
    }
}
