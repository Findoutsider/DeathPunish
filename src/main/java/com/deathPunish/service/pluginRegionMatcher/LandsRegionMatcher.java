package com.deathPunish.service.pluginRegionMatcher;

import com.deathPunish.service.MessageService;
import com.deathPunish.service.PluginRegionMatcher;
import me.angeschossen.lands.api.LandsIntegration;
import org.bukkit.Location;

import java.util.List;

public class LandsRegionMatcher implements PluginRegionMatcher {
    private final MessageService messageService;
    private final LandsIntegration lands;

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
            var area = lands.getArea(location);
            if (area == null) {
                return false;
            }
            String regionName = area.getName();
            if (regionName == null) {
                return false;
            }
            for (String configuredRegion : configuredRegions) {
                if (regionName.equalsIgnoreCase(configuredRegion)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            messageService.warn("检查 Lands 区域失败: " + ex.getMessage());
        }
        return false;
    }
}
