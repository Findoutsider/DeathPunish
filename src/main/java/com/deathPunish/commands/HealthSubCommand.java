package com.deathPunish.commands;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HealthSubCommand implements SubCommand {
    public enum Mode {
        SET, ADD, GET
    }

    private final Mode mode;

    public HealthSubCommand(Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        return switch (mode) {
            case SET -> handleSet(sender, args);
            case ADD -> handleAdd(sender, args);
            case GET -> handleGet(sender, args);
        };
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if ((mode == Mode.SET || mode == Mode.ADD) && args.length == 3) {
            return List.of("1", "10", "20");
        }
        if (mode == Mode.SET && args.length == 4) {
            return List.of("true", "false");
        }
        return null;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("/deathpunish set <player> <health> [true/false]");
            return false;
        }

        var target = Bukkit.getPlayer(args[1]);
        var value = parsePositiveDouble(args[2]);
        if (target == null) {
            sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
            return false;
        }
        if (value == null) {
            sender.sendMessage("§c设置的最大生命值必须为正数。");
            return false;
        }

        var maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            sender.sendMessage("§c无法读取目标玩家的最大生命值属性。");
            return false;
        }

        maxHealth.setBaseValue(value);
        if (args.length >= 4 && Boolean.parseBoolean(args[3])) {
            target.setHealth(value);
            sender.sendMessage("[DeathPunish] §a已设置玩家 " + target.getName() + " 最大生命值为 " + value + " 并为其恢复到最大生命");
        } else {
            sender.sendMessage("[DeathPunish] §a已设置玩家 " + target.getName() + " 最大生命值为 " + value);
        }
        return true;
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("/deathpunish add <player> <health>");
            return false;
        }

        var target = Bukkit.getPlayer(args[1]);
        var delta = parseDouble(args[2]);
        if (target == null) {
            sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
            return false;
        }
        if (delta == null) {
            sender.sendMessage("[DeathPunish] §c生命值必须为数字");
            return false;
        }

        var maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            sender.sendMessage("§c无法读取目标玩家的最大生命值属性。");
            return false;
        }

        double newValue = maxHealth.getBaseValue() + delta;
        if (newValue < 1.0D) {
            sender.sendMessage("[DeathPunish] §c不能让玩家血量上限小于 1");
            return false;
        }
        maxHealth.setBaseValue(newValue);
        sender.sendMessage("[DeathPunish] §a已为玩家 " + target.getName() + " 增加血量上限，当前上限为 " + newValue);
        return true;
    }

    private boolean handleGet(CommandSender sender, String[] args) {
        Player target;
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("[DeathPunish] §c/deathpunish get <player>");
                return false;
            }
            target = player;
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
                return false;
            }
        }

        AttributeInstance maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) {
            sender.sendMessage("§c无法读取目标玩家的最大生命值属性。");
            return false;
        }
        sender.sendMessage("[DeathPunish] §a玩家 " + target.getName() + " 的血量上限为 " + maxHealth.getBaseValue());
        return true;
    }

    private Double parsePositiveDouble(String raw) {
        Double value = parseDouble(raw);
        return value != null && value > 0 ? value : null;
    }

    private Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
