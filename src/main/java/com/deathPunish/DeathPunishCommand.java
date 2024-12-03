package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static com.deathPunish.CustomItems.heal_apple;

public class DeathPunishCommand implements CommandExecutor, TabExecutor {
    private final Plugin pl;

    public DeathPunishCommand(Plugin plugin) {
        this.pl = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (label.equalsIgnoreCase("deathpunish") || label.equalsIgnoreCase("dp")) {
            if ((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                if ((sender instanceof Player && sender.isOp()) || sender instanceof ConsoleCommandSender) {
                    sender.sendMessage("DeathPunish v" + pl.getDescription().getVersion());
                    sender.sendMessage("§c[§f死亡惩罚插件指令帮助§c]");
                    sender.sendMessage("§c使用§f\"/deathpunish help\"§c显示本页面");
                    sender.sendMessage("§c/deathpunish §fhelp§7: 显示帮助页面");
                    sender.sendMessage("§c/deathpunish §freload§7: 重载插件的配置文件");
                    sender.sendMessage("§c/deathpunish §fsetmaxhealth§7: 设置玩家血量上限");
                    sender.sendMessage("§c/deathpunish §fadd§7: 增加玩家血量上限");
                    sender.sendMessage("§c/deathpunish §fget§7: 获取玩家血量上限");
                    return true;
                } else {
                    sender.sendMessage("§c你的权限不足！");
                    return false;
                }

            }
        }

        if (args[0].equalsIgnoreCase("setmaxhealth") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
            boolean isHealth = false;
            if (args.length < 3) {
                sender.sendMessage("/deathpunish setmaxhealth <player> <health> <setHealth> <true/false>");
                return false;
            }
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer != null) {
                if (Integer.parseInt(args[2]) < 1) {
                    sender.sendMessage("§c设置的最大生命值必须为整数且不能小于1。");
                    return false;
                }
                targetPlayer.setMaxHealth(Integer.parseInt(args[2]));

                if (args.length >= 4) {
                    isHealth = args[3].equalsIgnoreCase("true");
                }
                if (isHealth) {
                    targetPlayer.setHealth(Integer.parseInt(args[2]));
                    sender.sendMessage("[DeathPunish] §a已设置玩家 " + targetPlayer.getName() + " 最大生命值为" + args[2] + "并为其恢复到最大生命");
                } else {
                    sender.sendMessage("[DeathPunish] §a已设置玩家 " + targetPlayer.getName() + " 最大生命值为" + args[2]);
                }
                return true;
            } else {
                sender.sendMessage("[DeathPunish] §c找不到玩家 " + targetPlayer.getName());
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("add") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
            if (args.length < 3) {
                sender.sendMessage("/deathpunish add <player> <health>");
                return false;
            }
            if (args.length == 3) {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer != null) {
                    targetPlayer.setMaxHealth((int) (targetPlayer.getMaxHealth() + Double.parseDouble(args[2])));
                    sender.sendMessage("[DeathPunish] §a已为玩家 " + targetPlayer.getName() + " 增加血量上限，当前上限为" + (int) targetPlayer.getMaxHealth());
                    return true;
                } else {
                    sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
                    return false;
                }
            }
        }

        if (args[0].equalsIgnoreCase("get") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
            Player targetPlayer;
            if (args.length == 1) {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage("[DeathPunish] §c/deathpunish get <player>");
                } else {
                    targetPlayer = (Player) sender;
                    sender.sendMessage("[DeathPunish] §a玩家 " + sender.getName() + " 的血量上限为" + (int) targetPlayer.getMaxHealth());
                    return true;
                }
            } else {
                targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer != null) {
                    sender.sendMessage("[DeathPunish] §a玩家 " + targetPlayer.getName() + " 的血量上限为" + (int) targetPlayer.getMaxHealth());
                    return true;
                } else {
                    sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
                    return false;
                }
            }

        }

        if (args[0].equalsIgnoreCase("reload") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
            if (args.length > 2) {
                sender.sendMessage("/deathpunish reload");
                return false;
            } else {
                try {
                    // 检查 heal_apple 是否已定义
                    pl.getServer().removeRecipe(heal_apple);
                } catch (Exception e) {
                    sender.sendMessage("[DeathPunish] §c重载失败: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
                pl.reloadConfig();
                registerCustomRecipes(pl.getConfig());
                sender.sendMessage("[DeathPunish] §a插件已重载");
                return true;
            }
        }


        sender.sendMessage("§c未知命令。");
        return false;
    }

    public void registerCustomRecipes(FileConfiguration config) {
        ShapedRecipe enchantedGoldenAppleRecipe = CustomItems.createEnchantedGoldenApple(config);
        pl.getServer().addRecipe(enchantedGoldenAppleRecipe);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String s, @NotNull String[] args) {
        if (sender.isOp() || sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                // 返回所有可能的命令
                return new ArrayList<>(List.of("help", "reload", "setmaxhealth", "add", "get"));
            } else if (args.length == 3) {
                return new ArrayList<>(List.of("1", "10", "20"));
            } else if (args.length == 4) {
                return new ArrayList<>(List.of("true", "false"));
            }
        }
        return null;
    }

}
