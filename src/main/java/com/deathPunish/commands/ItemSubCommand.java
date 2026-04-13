package com.deathPunish.commands;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemSubCommand implements SubCommand {
    private final CommandContext context;
    private final Map<String, SubCommand> subCommands;

    public ItemSubCommand(CommandContext context) {
        this.context = context;
        this.subCommands = Map.of(
                "add", new ItemAddSubCommand(context),
                "list", new ItemListSubCommand(context),
                "remove", new ItemRemoveSubCommand(context)
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            context.messageService().error(sender, "用法: /deathpunish item <add|list|remove> ...");
            return false;
        }
        var subCommand = subCommands.get(args[1].toLowerCase(Locale.ROOT));
        if (subCommand == null) {
            context.messageService().error(sender, "未知子命令，可用值: add, list, remove");
            return false;
        }
        return subCommand.execute(sender, args);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return List.of("add", "list", "remove");
        }
        var subCommand = args.length >= 2 ? subCommands.get(args[1].toLowerCase(Locale.ROOT)) : null;
        return subCommand == null ? null : subCommand.complete(sender, args);
    }
}
