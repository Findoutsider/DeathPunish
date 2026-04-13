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
        if (args.length < 3 || args.length > 5) {
            context.messageService().error(sender, "用法: /deathpunish give <player> <heal|protect|ender> [id] [amount]");
            return false;
        }

        var target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            context.messageService().error(sender, "找不到玩家 " + args[1]);
            return false;
        }

        String type = args[2];
        if (!context.customItemService().supportsType(type)) {
            context.messageService().error(sender, "未知物品类型，可用值: heal, protect, ender");
            return false;
        }

        String id = null;
        Integer amount = 1;
        if (args.length == 4) {
            if (context.customItemService().hasExtraItem(type, args[3])) {
                id = args[3];
            } else {
                Integer parsedAmount = parsePositiveInt(args[3]);
                if (parsedAmount != null) {
                    amount = parsedAmount;
                } else {
                    id = args[3];
                }
            }
        } else if (args.length == 5) {
            id = args[3];
            amount = parsePositiveInt(args[4]);
        }

        if (amount == null) {
            context.messageService().error(sender, "数量必须为正整数");
            return false;
        }

        if (id == null && isBuiltinDisabled(type)) {
            context.messageService().error(sender, "该类型的内置物品已禁用，请指定额外物品 ID");
            return false;
        }

        try {
            target.getInventory().addItem(id == null
                    ? context.customItemService().createBuiltinItem(type, amount)
                    : context.customItemService().createExtraItem(type, id, amount));
        } catch (IllegalArgumentException ex) {
            context.messageService().error(sender, ex.getMessage());
            return false;
        }

        context.messageService().info(sender, "已给予玩家 " + target.getName() + " " + amount + " 个物品");
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            return List.of("heal", "protect", "ender");
        }
        if (args.length == 4) {
            if (context.customItemService().supportsType(args[2])) {
                var ids = context.customItemService().getExtraItemIds(args[2]);
                if (!ids.isEmpty()) {
                    return ids;
                }
            }
            return List.of("1", "5", "10", "32", "64");
        }
        if (args.length == 5) {
            return List.of("1", "5", "10", "32", "64");
        }
        return null;
    }

    private boolean isBuiltinDisabled(String type) {
        String normalized = normalizeType(type);
        if ("heal".equals(normalized)) {
            return context.plugin().getPluginConfig().disableBuiltinHealItem();
        }
        return context.plugin().getPluginConfig().disableBuiltinProtectItems();
    }

    private String normalizeType(String input) {
        return switch (input.toLowerCase()) {
            case "ender_protect", "enderprotect", "ender" -> "ender";
            case "protect" -> "protect";
            case "heal" -> "heal";
            default -> input.toLowerCase();
        };
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
