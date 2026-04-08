package com.deathPunish.commands;

import com.deathPunish.DeathPunish;
import com.deathPunish.service.CustomItemService;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DeathPunishCommand implements CommandExecutor, TabExecutor {
    private final DeathPunish plugin;
    private final CustomItemService customItemService;

    public DeathPunishCommand(DeathPunish plugin, CustomItemService customItemService) {
        this.plugin = plugin;
        this.customItemService = customItemService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("deathpunish.command")) {
            sender.sendMessage("§c你没有权限！");
            return true;
        }
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            return sendHelp(sender);
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "set" -> handleSet(sender, args);
            case "add" -> handleAdd(sender, args);
            case "get" -> handleGet(sender, args);
            case "reload" -> handleReload(sender);
            case "give" -> handleGive(sender, args);
            default -> {
                sender.sendMessage("§c未知命令。");
                yield false;
            }
        };
    }

    private boolean sendHelp(CommandSender sender) {
        if (!(sender instanceof Player && sender.isOp()) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§c你的权限不足！");
            return false;
        }
        sender.sendMessage("DeathPunish v" + plugin.getDescription().getVersion());
        sender.sendMessage("§c[§f死亡惩罚插件指令帮助§c]");
        sender.sendMessage("§c可使用 dp 替换 deathpunish");
        sender.sendMessage("§c/deathpunish §fhelp§7: 显示帮助页面");
        sender.sendMessage("§c/deathpunish §fgive <玩家> <heal|protect|ender> [数量]§7: 获取自定义物品");
        sender.sendMessage("§c/deathpunish §fset <玩家> <血量> [是否回满]§7: 设置玩家血量上限");
        sender.sendMessage("§c/deathpunish §fadd <玩家> <增量>§7: 增加玩家血量上限");
        sender.sendMessage("§c/deathpunish §fget [玩家]§7: 获取玩家血量上限");
        sender.sendMessage("§c/deathpunish §freload§7: 重载插件配置");
        sender.sendMessage("");
        sender.sendMessage("§c当前启用了死亡惩罚的世界有：");
        plugin.getPluginConfig().enableWorlds().forEach(world -> sender.sendMessage("§f" + world));
        return true;
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

    private boolean handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.refreshConfigState();
        DeathPunish.getWorldManger().setWorldRule();
        plugin.registerCustomRecipes();
        sender.sendMessage("[DeathPunish] §a插件已重载");
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
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

        String itemPath = resolveItemPath(args[2]);
        if (itemPath == null) {
            sender.sendMessage("[DeathPunish] §c未知物品类型，可用值: heal, protect, ender");
            return false;
        }

        target.getInventory().addItem(customItemService.createConfiguredItem(itemPath, amount));
        sender.sendMessage("[DeathPunish] §a已给予玩家 " + target.getName() + " " + amount + " 个物品");
        return true;
    }

    private String resolveItemPath(String input) {
        return customItemService.resolveItemPath(input);
    }

    private Integer parsePositiveInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
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

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("deathpunish.command")) {
            return null;
        }

        if (args.length == 1) {
            return new ArrayList<>(List.of("help", "give", "set", "add", "get", "reload"));
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            return new ArrayList<>(List.of("heal", "protect", "ender"));
        }
        if (args.length == 3 && ("set".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0]))) {
            return new ArrayList<>(List.of("1", "10", "20"));
        }
        if (args.length == 4 && "give".equalsIgnoreCase(args[0])) {
            return new ArrayList<>(List.of("1", "5", "10", "32", "64"));
        }
        if (args.length == 4 && "set".equalsIgnoreCase(args[0])) {
            return new ArrayList<>(List.of("true", "false"));
        }
        return null;
    }
}
