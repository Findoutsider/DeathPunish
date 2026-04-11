package com.deathPunish.service;

import org.bukkit.Location;

import java.util.List;

@FunctionalInterface
public interface PluginRegionMatcher {
    boolean matches(Location location, List<String> configuredRegions);
}
