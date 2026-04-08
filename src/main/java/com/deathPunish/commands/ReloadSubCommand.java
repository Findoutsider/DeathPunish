package com.deathPunish.commands;

import com.deathPunish.DeathPunish;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand implements SubCommand {
    private final DeathPunish plugin;

    public ReloadSubCommand(DeathPunish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        plugin.refreshConfigState();
        DeathPunish.getWorldManger().setWorldRule();
        plugin.registerCustomRecipes();
        sender.sendMessage("[DeathPunish] §a插件已重载");
        return true;
    }
}
