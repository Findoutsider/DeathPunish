package com.deathPunish.service;

import java.util.List;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.deathPunish.DeathPunish;

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

            // TODO: 不知道Name和ID哪个对，先这样写吧
            if (configuredRegions.contains(res.getName()) || configuredRegions.contains(res.getAreaIDbyLoc(location))) {
                return true;
            }
        } catch (Exception ex) { 
            messageService.warn("检查插件区域失败: " + ex.getMessage());
        }
        return false;
    }
    
}
