package com.deathPunish.service;

import cn.lunadeer.dominion.api.DominionAPI;
import com.bekvon.bukkit.residence.Residence;
import com.deathPunish.DeathPunish;
import com.deathPunish.service.pluginRegionMatcher.DominionRegionMatcher;
import com.deathPunish.service.pluginRegionMatcher.LandsRegionMatcher;
import com.deathPunish.service.pluginRegionMatcher.ResidenceRegionMatcher;
import com.deathPunish.service.pluginRegionMatcher.TownyRegionMatcher;
import com.deathPunish.service.pluginRegionMatcher.WorldGuardRegionMatcher;
import com.palmergames.bukkit.towny.TownyAPI;
import com.sk89q.worldguard.WorldGuard;
import me.angeschossen.lands.api.LandsIntegration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class SoftDependencyContext {
    private final Economy economy;
    private final WorldGuard worldGuard;
    private final Residence residence;
    private final DominionAPI dominionAPI;
    private final TownyAPI townyAPI;
    private final LandsIntegration landsIntegration;

    public SoftDependencyContext(Economy economy, WorldGuard worldGuard, Residence residence, DominionAPI dominionAPI, TownyAPI townyAPI, LandsIntegration landsIntegration) {
        this.economy = economy;
        this.worldGuard = worldGuard;
        this.residence = residence;
        this.dominionAPI = dominionAPI;
        this.townyAPI = townyAPI;
        this.landsIntegration = landsIntegration;
    }

    public static SoftDependencyContext initialize(DeathPunish plugin, MessageService messageService) {
        Economy economy = resolveEconomy(plugin);
        WorldGuard worldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null
                ? WorldGuard.getInstance()
                : null;
        Residence residence = plugin.getServer().getPluginManager().getPlugin("Residence") != null
                ? Residence.getInstance()
                : null;
        DominionAPI dominionAPI = plugin.getServer().getPluginManager().getPlugin("Dominion") != null
                ? DominionAPI.getInstance()
                : null;
        TownyAPI townyAPI = plugin.getServer().getPluginManager().getPlugin("Towny") != null
                ? TownyAPI.getInstance()
                : null;
        LandsIntegration landsIntegration = plugin.getServer().getPluginManager().getPlugin("Lands") != null
                ? LandsIntegration.of(plugin)
                : null;

        SoftDependencyContext context = new SoftDependencyContext(
                economy,
                worldGuard,
                residence,
                dominionAPI,
                townyAPI,
                landsIntegration
        );
        context.logEnabledDependencies(messageService);
        return context;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public boolean hasWorldGuard() {
        return worldGuard != null;
    }

    public boolean hasResidence() {
        return residence != null;
    }

    public boolean hasDominion() {
        return dominionAPI != null;
    }

    public boolean hasTowny() {
        return townyAPI != null;
    }

    public boolean hasLands() {
        return landsIntegration != null;
    }

    public Economy economy() {
        return economy;
    }

    public WorldGuard worldGuard() {
        return worldGuard;
    }

    public Residence residence() {
        return residence;
    }

    public DominionAPI dominion() {
        return dominionAPI;
    }

    public TownyAPI towny() {
        return townyAPI;
    }

    public LandsIntegration lands() {
        return landsIntegration;
    }

    public PluginRegionMatcher createPluginRegionMatcher(MessageService messageService) {
        List<PluginRegionMatcher> matchers = new ArrayList<>();
        if (hasWorldGuard()) {
            matchers.add(new WorldGuardRegionMatcher(messageService, worldGuard));
        }
        if (hasResidence()) {
            matchers.add(new ResidenceRegionMatcher(messageService, residence));
        }
        if (hasDominion()) {
            matchers.add(new DominionRegionMatcher(messageService, dominionAPI));
        }
        if (hasTowny()) {
            matchers.add(new TownyRegionMatcher(messageService, townyAPI));
        }
        if (hasLands()) {
            matchers.add(new LandsRegionMatcher(messageService, landsIntegration));
        }
        return new CompositePluginRegionMatcher(matchers);
    }

    private static Economy resolveEconomy(DeathPunish plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> registration = plugin.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    private void logEnabledDependencies(MessageService messageService) {
        StringBuilder builder = new StringBuilder("\n=================================\n\n已启用依赖：\n");
        if (hasEconomy()) {
            builder.append("Vault \n");
        }
        if (hasWorldGuard()) {
            builder.append("WorldGuard \n");
        }
        if (hasResidence()) {
            builder.append("Residence \n");
        }
        if (hasDominion()) {
            builder.append("Dominion \n");
        }
        if (hasTowny()) {
            builder.append("Towny \n");
        }
        if (hasLands()) {
            builder.append("Lands \n");
        }
        builder.append("\n=================================\n");
        messageService.info(builder.toString());
    }
}
