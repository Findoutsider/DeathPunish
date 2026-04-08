package com.deathPunish.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GiveSubCommand implements SubCommand {
    private final CommandContext context;

    public GiveSubCommand(CommandContext context) {
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage("/deathpunish give <player> <heal|protect|ender> [amount]");
            return false;
        }

        var target = Bukkit.getPlayer(args[1]);
        Integer amount = args.length == 4 ? parsePositiveInt(args[3]) : 1;
        if (target == null) {
            sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
            return false;
        }
        if (amount == null) {
            sender.sendMessage("[DeathPunish] §c数量必须为正整数");
            return false;
        }

        String itemPath = context.customItemService().resolveItemPath(args[2]);
        if (itemPath == null) {
            sender.sendMessage("[DeathPunish] §c未知物品类型，可用值: heal, protect, ender");
            return false;
        }

        target.getInventory().addItem(context.customItemService().createConfiguredItem(itemPath, amount));
        sender.sendMessage("[DeathPunish] §a已给予玩家 " + target.getName() + " " + amount + " 个物品");
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            return List.of("heal", "protect", "ender");
        }
        if (args.length == 4) {
            return List.of("1", "5", "10", "32", "64");
        }
        return null;
    }

    private Integer parsePositiveInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
