package com.deathPunish.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class MigrateSubCommand implements SubCommand {
    private static final double TARGET_BASE_HEALTH = 20.0D;

    private final CommandContext context;

    public MigrateSubCommand(CommandContext context) {
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player && sender.isOp()) && !(sender instanceof ConsoleCommandSender)) {
            context.messageService().error(sender, "你的权限不足！");
            return false;
        }
        if (args.length < 2) {
            context.messageService().error(sender, "用法: /deathpunish migrate <player>");
            return false;
        }

        var target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            context.messageService().error(sender, "找不到玩家 " + args[1]);
            return false;
        }

        if (!context.plugin().getMaxHealthModifierService().migrateBaseValue(target, TARGET_BASE_HEALTH)) {
            context.messageService().error(sender, "无法迁移玩家 " + target.getName() + " 的生命值数据。");
            return false;
        }

        context.messageService().info(sender, "已将玩家 " + target.getName() + " 的生命值迁移至新计算方式。");
        return true;
    }
}
