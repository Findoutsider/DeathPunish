package com.deathPunish.service;

import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public class CompositePluginRegionMatcher implements PluginRegionMatcher {
    private final List<PluginRegionMatcher> matchers;

    public CompositePluginRegionMatcher(PluginRegionMatcher... matchers) {
        this.matchers = Arrays.asList(matchers);
    }

    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        return matchers.stream().anyMatch(matcher -> matcher.matches(location, configuredRegions));
    }
}
