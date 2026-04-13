package com.deathPunish.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class HelpSubCommand implements SubCommand {
    private final CommandContext context;

    public HelpSubCommand(CommandContext context) {
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player && sender.isOp()) && !(sender instanceof ConsoleCommandSender)) {
            context.messageService().error(sender, "你的权限不足！");
            return false;
        }
        context.messageService().info(sender, "DeathPunish v" + context.plugin().getDescription().getVersion());
        sender.sendMessage("§c[§f死亡惩罚插件指令帮助§c]");
        sender.sendMessage("§c可使用 dp 替换 deathpunish");
        sender.sendMessage("§c/deathpunish §fhelp§7: 显示帮助页面");
        sender.sendMessage("§c/deathpunish §fgive <玩家> <heal|protect|ender> [额外物品ID] [数量]§7: 获取内置或额外物品");
        sender.sendMessage("§c/deathpunish §fitem add <heal|protect|ender> <id> ...§7: 添加额外物品到 items.yml");
        sender.sendMessage("§c/deathpunish §fitem list <heal|protect|ender>§7: 列出额外物品");
        sender.sendMessage("§c/deathpunish §fitem remove <heal|protect|ender> <id>§7: 删除额外物品");
        sender.sendMessage("§c/deathpunish §fset <玩家> <血量> [是否回满]§7: 设置玩家血量上限");
        sender.sendMessage("§c/deathpunish §fadd <玩家> <增量>§7: 增加玩家血量上限");
        sender.sendMessage("§c/deathpunish §fget [玩家]§7: 获取玩家血量上限");
        sender.sendMessage("§c/deathpunish §fmigrate <玩家>§7: 将玩家的血量上限迁移至新计算方式");
        sender.sendMessage("§c/deathpunish §freload§7: 重载插件配置");
        return true;
    }
}
