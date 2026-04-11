package com.deathPunish.service.pluginRegionMatcher;

import java.util.List;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.deathPunish.DeathPunish;
import com.deathPunish.service.MessageService;
import com.deathPunish.service.PluginRegionMatcher;

import static com.deathPunish.DeathPunish.residence;

public class ResidencePluginRegionMatcher implements PluginRegionMatcher {
    private final MessageService messageService;

    public ResidencePluginRegionMatcher(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        if (!DeathPunish.enableResidence || configuredRegions.isEmpty() || location.getWorld() == null) {
            return false;
        }
        try { 
            ClaimedResidence res = residence.getResidenceManager().getByLoc(location);
            if (res == null) {
                return false;
            }

            String residenceName = res.getName();
            String areaId = res.getAreaIDbyLoc(location);
            for (String configuredRegion : configuredRegions) {
                if ((residenceName != null && residenceName.equalsIgnoreCase(configuredRegion))
                        || (areaId != null && areaId.equalsIgnoreCase(configuredRegion))) {
                    return true;
                }
            }
        } catch (Exception ex) { 
            messageService.warn("检查 Residence 区域失败: " + ex.getMessage());
        }
        return false;
    }
    
}
