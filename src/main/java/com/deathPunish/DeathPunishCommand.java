package com.deathPunish;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;

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
                    sender.sendMessage("§c命令中的§f\"deathpunish\"§c可替换为§e\"dp\"");
                    sender.sendMessage("§c/deathpunish §fhelp§7: 显示帮助页面");
                    sender.sendMessage("§c/deathpunish §freload§7: 重载插件的配置文件");
                    sender.sendMessage("§c/deathpunish §fsetmaxhealth§7: 设置玩家血量上限");
                    return true;
                } else {
                    sender.sendMessage("§c你的权限不足！");
                    return false;
                }

            }
        }
//        if (args[0].equalsIgnoreCase("setmaxhealth"))
//        {
//            for (int i=0;i<=3;i++) {
//                if (args[i] == null&&i!=3) {
//                    sender.sendMessage("§c参数有误。");
//                    return false;
//                } else if (i==3&&args[i]==null) {
//                    args[3] = "false";
//                }
//            }
//        }
        if ((args[0].equalsIgnoreCase("setmaxhealth") || args[0].equalsIgnoreCase("smh")) && (sender.isOp() || sender instanceof ConsoleCommandSender)) {

            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer != null) {
                if (Integer.parseInt(args[2]) < 1) {
                    sender.sendMessage("§c设置的最大生命值必须为整数且不能小于1。");
                    return false;
                }
                targetPlayer.setMaxHealth(Integer.parseInt(args[2]));
                boolean isHealth = false;
                if (args[3] != null) {
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

        if (args[0].equalsIgnoreCase("reload") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
            pl.reloadConfig();
            sender.sendMessage("[DeathPunish] §a配置文件已重载。");
            return true;
        }

        sender.sendMessage("§c未知命令。");
        return false;
        }

        @Override
        public List<String> onTabComplete (@NotNull CommandSender commandSender, @NotNull Command
        command, @NotNull String s, @NotNull String[]args){
            if (args.length == 1) {
                // 返回所有可能的命令
                return new ArrayList<>(List.of("help", "reload", "setmaxhealth"));
            } else if (args.length == 3) {
                return new ArrayList<>(List.of("1", "10", "20"));
            } else if (args.length == 4) {
                return new ArrayList<>(List.of("true", "false"));
            }

            return null;
        }
    }
