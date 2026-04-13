package com.deathPunish.commands;

import com.deathPunish.model.ManagedProtectItem;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemListSubCommand implements SubCommand {
    private final CommandContext context;

    public ItemListSubCommand(CommandContext context) {
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            context.messageService().error(sender, "用法: /deathpunish item list <heal|protect|ender>");
            return false;
        }

        switch (normalizeType(args[2])) {
            case "heal" -> {
                var items = context.managedItemService().getHealItems();
                if (items.isEmpty()) {
                    context.messageService().warn(sender, "当前没有额外治疗物品");
                    return true;
                }
                context.messageService().info(sender, "额外治疗物品列表:");
                items.forEach(item -> sendLine(sender, "heal", item.id(), item.itemStack()));
                return true;
            }
            case "protect" -> {
                var items = context.managedItemService().getProtectItems().stream()
                        .filter(item -> item.type() == ManagedProtectItem.ProtectType.NORMAL)
                        .toList();
                if (items.isEmpty()) {
                    context.messageService().warn(sender, "当前没有额外保护符");
                    return true;
                }
                context.messageService().info(sender, "额外保护符列表:");
                items.forEach(item -> sendLine(sender, "protect", item.id(), item.itemStack()));
                return true;
            }
            case "ender" -> {
                var items = context.managedItemService().getProtectItems().stream()
                        .filter(item -> item.type() == ManagedProtectItem.ProtectType.ENDER)
                        .toList();
                if (items.isEmpty()) {
                    context.messageService().warn(sender, "当前没有额外末影保护符");
                    return true;
                }
                context.messageService().info(sender, "额外末影保护符列表:");
                items.forEach(item -> sendLine(sender, "ender", item.id(), item.itemStack()));
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

    private void sendLine(CommandSender sender, String type, String id, ItemStack itemStack) {
        String itemName = resolveItemName(itemStack);
        String material = itemStack.getType().name();
        if (sender instanceof Player player) {
            TextComponent line = new TextComponent("§f- " + id + " §7[" + material + "] §f" + itemName + " ");
            TextComponent delete = new TextComponent("§c[删除]");
            delete.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/deathpunish item remove " + type + " " + id));
            delete.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                    net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("点击删除 " + id).create()
            ));
            line.addExtra(delete);
            player.spigot().sendMessage(line);
            return;
        }
        sender.sendMessage("§f- " + id + " §7[" + material + "] §f" + itemName);
    }

    private String resolveItemName(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }
        return itemStack.getType().name();
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
