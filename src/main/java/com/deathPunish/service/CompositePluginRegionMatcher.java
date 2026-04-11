package com.deathPunish.service;

import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public class CompositePluginRegionMatcher implements PluginRegionMatcher {
    private final List<PluginRegionMatcher> matchers;

    public CompositePluginRegionMatcher(PluginRegionMatcher... matchers) {
        this.matchers = Arrays.asList(matchers);
    }

    public CompositePluginRegionMatcher(List<PluginRegionMatcher> matchers) {
        this.matchers = List.copyOf(matchers);
    }

    @Override
    public boolean matches(Location location, List<String> configuredRegions) {
        return matchers.stream().anyMatch(matcher -> matcher.matches(location, configuredRegions));
    }

    public List<PluginRegionMatcher> matchers() {
        return List.copyOf(matchers);
    }
}
