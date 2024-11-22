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
                    sender.sendMessage("��c[��f�����ͷ����ָ�������c]");
                    sender.sendMessage("��cʹ�á�f\"/deathpunish help\"��c��ʾ��ҳ��");
                    sender.sendMessage("��c�����еġ�f\"deathpunish\"��c���滻Ϊ��e\"dp\"");
                    sender.sendMessage("��c/deathpunish ��fhelp��7: ��ʾ����ҳ��");
                    sender.sendMessage("��c/deathpunish ��freload��7: ���ز���������ļ�");
                    sender.sendMessage("��c/deathpunish ��fsetmaxhealth��7: �������Ѫ������");
                    return true;
                } else {
                    sender.sendMessage("��c���Ȩ�޲��㣡");
                    return false;
                }

            }
        }
//        if (args[0].equalsIgnoreCase("setmaxhealth"))
//        {
//            for (int i=0;i<=3;i++) {
//                if (args[i] == null&&i!=3) {
//                    sender.sendMessage("��c��������");
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
                    sender.sendMessage("��c���õ��������ֵ����Ϊ�����Ҳ���С��1��");
                    return false;
                }
                targetPlayer.setMaxHealth(Integer.parseInt(args[2]));
                boolean isHealth = false;
                if (args[3] != null) {
                    isHealth = args[3].equalsIgnoreCase("true");
                }
                if (isHealth) {
                    targetPlayer.setHealth(Integer.parseInt(args[2]));
                    sender.sendMessage("[DeathPunish] ��a��������� " + targetPlayer.getName() + " �������ֵΪ" + args[2] + "��Ϊ��ָ����������");
                } else {
                    sender.sendMessage("[DeathPunish] ��a��������� " + targetPlayer.getName() + " �������ֵΪ" + args[2]);
                }
                return true;
            } else {
                sender.sendMessage("[DeathPunish] ��c�Ҳ������ " + targetPlayer.getName());
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("reload") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
            pl.reloadConfig();
            sender.sendMessage("[DeathPunish] ��a�����ļ������ء�");
            return true;
        }

        sender.sendMessage("��cδ֪���");
        return false;
        }

        @Override
        public List<String> onTabComplete (@NotNull CommandSender commandSender, @NotNull Command
        command, @NotNull String s, @NotNull String[]args){
            if (args.length == 1) {
                // �������п��ܵ�����
                return new ArrayList<>(List.of("help", "reload", "setmaxhealth"));
            } else if (args.length == 3) {
                return new ArrayList<>(List.of("1", "10", "20"));
            } else if (args.length == 4) {
                return new ArrayList<>(List.of("true", "false"));
            }

            return null;
        }
    }
