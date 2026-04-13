package com.deathPunish.commands;

import com.deathPunish.model.ManagedProtectItem;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ItemRemoveSubCommand implements SubCommand {
    private final CommandContext context;

    public ItemRemoveSubCommand(CommandContext context) {
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 4) {
            context.messageService().error(sender, "用法: /deathpunish item remove <heal|protect|ender> <id>");
            return false;
        }
        String type = normalizeType(args[2]);
        String id = args[3];
        boolean removed = switch (type) {
            case "heal" -> context.managedItemService().removeHealItem(id);
            case "protect" -> context.managedItemService().removeProtectItem(id, ManagedProtectItem.ProtectType.NORMAL);
            case "ender" -> context.managedItemService().removeProtectItem(id, ManagedProtectItem.ProtectType.ENDER);
            default -> false;
        };
        if (!removed) {
            context.messageService().error(sender, "找不到额外物品: " + id);
            return false;
        }
        context.messageService().info(sender, "已删除额外物品: " + id);
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            return List.of("heal", "protect", "ender");
        }
        if (args.length == 4) {
            return switch (normalizeType(args[2])) {
                case "heal", "protect", "ender" -> context.customItemService().getExtraItemIds(args[2]);
                default -> null;
            };
        }
        return null;
    }

    private String normalizeType(String input) {
        return switch (input.toLowerCase()) {
            case "ender_protect", "enderprotect", "ender" -> "ender";
            case "protect" -> "protect";
            case "heal" -> "heal";
            default -> input.toLowerCase();
        };
    }
}
