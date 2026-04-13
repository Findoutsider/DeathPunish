package com.deathPunish.commands;

import com.deathPunish.model.ManagedHealItem;
import com.deathPunish.model.ManagedProtectItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

public class ItemAddSubCommand implements SubCommand {
    private static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");
    private final CommandContext context;

    public ItemAddSubCommand(CommandContext context) {
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            context.messageService().error(sender, "只有玩家可以添加额外物品");
            return false;
        }
        if (args.length < 4 || args.length > 6) {
            context.messageService().error(sender, "用法: /deathpunish item add <heal|protect|ender> <id> [healAmount] [maxHealth]");
            return false;
        }

        String type = args[2];
        String id = args[3];
        if (!ID_PATTERN.matcher(id).matches()) {
            context.messageService().error(sender, "物品 ID 只能包含字母、数字、下划线和短横线");
            return false;
        }
        var handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType().isAir()) {
            context.messageService().error(sender, "请先将要添加的物品拿在主手");
            return false;
        }

        switch (normalizeType(type)) {
            case "heal" -> {
                if (args.length != 6) {
                    context.messageService().error(sender, "用法: /deathpunish item add heal <id> <healAmount> <maxHealth>");
                    return false;
                }
                if (context.managedItemService().containsHealItem(id)) {
                    context.messageService().error(sender, "治疗物品 ID 已存在: " + id);
                    return false;
                }
                Double healAmount = parseDouble(args[4]);
                Double maxHealth = parseDouble(args[5]);
                if (healAmount == null || maxHealth == null) {
                    context.messageService().error(sender, "healAmount 和 maxHealth 必须为数字");
                    return false;
                }
                var builtinHealItem = context.plugin().getPluginConfig().healItem();
                context.managedItemService().addHealItem(new ManagedHealItem(
                        id,
                        handItem,
                        healAmount,
                        maxHealth,
                        builtinHealItem.eatMsg(),
                        builtinHealItem.eatWithoutHealMsg(),
                        builtinHealItem.potionEffects()
                ));
                context.messageService().info(sender, "已添加额外治疗物品: " + id);
                return true;
            }
            case "protect", "ender" -> {
                if (args.length != 4) {
                    context.messageService().error(sender, "用法: /deathpunish item add <protect|ender> <id>");
                    return false;
                }
                ManagedProtectItem.ProtectType protectType = "ender".equals(normalizeType(type))
                        ? ManagedProtectItem.ProtectType.ENDER
                        : ManagedProtectItem.ProtectType.NORMAL;
                if (context.managedItemService().containsProtectItem(id, protectType)) {
                    context.messageService().error(sender, "保护符 ID 已存在: " + id);
                    return false;
                }
                context.managedItemService().addProtectItem(new ManagedProtectItem(id, handItem, protectType));
                context.messageService().info(sender, "已添加额外" + ("ender".equals(normalizeType(type)) ? "末影保护符" : "保护符") + ": " + id);
                return true;
            }
            default -> {
                context.messageService().error(sender, "未知物品类型，可用值: heal, protect, ender");
                return false;
            }
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            return List.of("heal", "protect", "ender");
        }
        return null;
    }

    private Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
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
