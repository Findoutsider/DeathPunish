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

import static com.deathPunish.CustomItems.heal;

public class DeathPunishCommand implements CommandExecutor, TabExecutor {
    private final Plugin pl;

    public DeathPunishCommand(Plugin plugin) {
        this.pl = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender.hasPermission("deathpunish.command")) {
            if (label.equalsIgnoreCase("deathpunish") || label.equalsIgnoreCase("dp")) {
                if ((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                    if ((sender instanceof Player && sender.isOp()) || sender instanceof ConsoleCommandSender) {
                        sender.sendMessage("DeathPunish v" + pl.getDescription().getVersion());
                        sender.sendMessage("��c[��f�����ͷ����ָ�������c]");
                        sender.sendMessage("��c��ʹ��dp�滻deathpunish");
                        sender.sendMessage("��cʹ�á�f\"/deathpunish help\"��c��ʾ��ҳ��");
                        sender.sendMessage("��c/deathpunish ��fhelp��7: ��ʾ����ҳ��");
                        sender.sendMessage("��c/deathpunish ��freload��7: ���ز���������ļ�");
                        sender.sendMessage("��c/deathpunish ��fsetmaxhealth��7: �������Ѫ������");
                        sender.sendMessage("��c/deathpunish ��fadd��7: �������Ѫ������");
                        sender.sendMessage("��c/deathpunish ��fget��7: ��ȡ���Ѫ������");
                        return true;
                    } else {
                        sender.sendMessage("��c���Ȩ�޲��㣡");
                        return false;
                    }

                }
            }

            if (args[0].equalsIgnoreCase("set") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
                boolean isHealth = false;
                if (args.length < 3) {
                    sender.sendMessage("/deathpunish set <player> <health> <setHealth> <true/false>");
                    return false;
                }
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer != null) {
                    try {
                        if (Integer.parseInt(args[2]) < 1) {
                            sender.sendMessage("��c���õ��������ֵ����Ϊ�����Ҳ���С��1��");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("��c���õ��������ֵ����Ϊ�����Ҳ���С��1��");
                        return false;
                    }
                    targetPlayer.setMaxHealth(Integer.parseInt(args[2]));

                    if (args.length >= 4) {
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

            if (args[0].equalsIgnoreCase("add") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
                if (args.length < 3) {
                    sender.sendMessage("/deathpunish add <player> <health>");
                    return false;
                }
                if (args.length == 3) {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        try{
                        targetPlayer.setMaxHealth((int) (targetPlayer.getMaxHealth() + Integer.parseInt(args[2])));
                        sender.sendMessage("[DeathPunish] ��a��Ϊ��� " + targetPlayer.getName() + " ����Ѫ�����ޣ���ǰ����Ϊ" + (int) targetPlayer.getMaxHealth());
                        return true;}catch (NumberFormatException e) {
                            sender.sendMessage("[DeathPunish] ��c����ֵ����Ϊ����");
                            return false;
                        }catch (IllegalArgumentException e) {
                            sender.sendMessage("[DeathPunish] ��c���������Ѫ������С��1");
                            return false;
                        }
                    } else {
                        sender.sendMessage("[DeathPunish] ��c�Ҳ������ " + args[1]);
                        return false;
                    }
                }
            }

            if (args[0].equalsIgnoreCase("get") && (sender.isOp() || sender instanceof ConsoleCommandSender)) {
                Player targetPlayer;
                if (args.length == 1) {
                    if (sender instanceof ConsoleCommandSender) {
                        sender.sendMessage("[DeathPunish] ��c/deathpunish get <player>");
                    } else {
                        targetPlayer = (Player) sender;
                        sender.sendMessage("[DeathPunish] ��a��� " + sender.getName() + " ��Ѫ������Ϊ" + (int) targetPlayer.getMaxHealth());
                        return true;
                    }
                } else {
                    targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        sender.sendMessage("[DeathPunish] ��a��� " + targetPlayer.getName() + " ��Ѫ������Ϊ" + (int) targetPlayer.getMaxHealth());
                        return true;
                    } else {
                        sender.sendMessage("[DeathPunish] ��c�Ҳ������ " + args[1]);
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
                        pl.getServer().removeRecipe(heal);
                    } catch (Exception e) {
                        sender.sendMessage("[DeathPunish] ��c����ʧ��: " + e.getMessage());
                        e.printStackTrace();
                        return false;
                    }
                    pl.reloadConfig();
                    registerCustomRecipes(pl.getConfig());
                    sender.sendMessage("[DeathPunish] ��a���������");
                    return true;
                }
            }


            sender.sendMessage("��cδ֪���");
            return false;
        }
        sender.sendMessage("��c��û��Ȩ�ޣ�");
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
                // �������п��ܵ�����
                return new ArrayList<>(List.of("help", "reload", "set", "add", "get"));
            } else if (args.length == 3) {
                return new ArrayList<>(List.of("1", "10", "20"));
            } else if (args.length == 4) {
                return new ArrayList<>(List.of("true", "false"));
            }
        }
        return null;
    }

}
