package com.deathPunish.commands;

import com.deathPunish.DeathPunish;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand implements SubCommand {
    private final CommandContext context;

    public ReloadSubCommand(CommandContext context) {
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        context.plugin().reloadConfig();
        context.plugin().refreshConfigState();
        context.managedItemService().load();
        DeathPunish.getWorldManger().setWorldRule();
        context.plugin().registerCustomRecipes();
        context.messageService().info(sender, "插件已重载");
        return true;
    }
}
