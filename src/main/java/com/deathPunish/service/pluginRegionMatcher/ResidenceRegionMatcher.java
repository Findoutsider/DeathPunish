package com.deathPunish.service.pluginRegionMatcher;

import java.util.List;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.Residence;
import com.deathPunish.service.MessageService;
import com.deathPunish.service.PluginRegionMatcher;

public class ResidenceRegionMatcher implements PluginRegionMatcher {
    private final MessageService messageService;
    private final Residence residence;

    public ResidenceRegionMatcher(MessageService messageService, Residence residence) {
        this.messageService = messageService;
        this.residence = residence;
    }

    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        if (residence == null || configuredRegions.isEmpty() || location.getWorld() == null) {
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
