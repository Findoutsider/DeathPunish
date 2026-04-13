package com.deathPunish.commands;

import com.deathPunish.DeathPunish;
import com.deathPunish.service.CustomItemService;
import com.deathPunish.service.ManagedItemService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeathPunishCommand implements CommandExecutor, TabExecutor {
    private final SubCommand helpSubCommand;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public DeathPunishCommand(DeathPunish plugin, CustomItemService customItemService, ManagedItemService managedItemService) {
        var context = new CommandContext(plugin, customItemService, managedItemService, plugin.getMessageService());
        this.helpSubCommand = new HelpSubCommand(context);
        subCommands.put("help", helpSubCommand);
        subCommands.put("give", new GiveSubCommand(context));
        subCommands.put("item", new ItemSubCommand(context));
        subCommands.put("set", new HealthSubCommand(context, HealthSubCommand.Mode.SET));
        subCommands.put("add", new HealthSubCommand(context, HealthSubCommand.Mode.ADD));
        subCommands.put("get", new HealthSubCommand(context, HealthSubCommand.Mode.GET));
        subCommands.put("migrate", new MigrateSubCommand(context));
        subCommands.put("reload", new ReloadSubCommand(context));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("deathpunish.command")) {
            sender.sendMessage("§c你没有权限！");
            return true;
        }
        if (args.length == 0) {
            return helpSubCommand.execute(sender, args);
        }

        var subCommand = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subCommand == null) {
            sender.sendMessage("§c未知命令。");
            return false;
        }
        return subCommand.execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("deathpunish.command")) {
            return null;
        }
        if (args.length == 1) {
            return List.copyOf(subCommands.keySet());
        }

        var subCommand = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        return subCommand == null ? null : subCommand.complete(sender, args);
    }
}
