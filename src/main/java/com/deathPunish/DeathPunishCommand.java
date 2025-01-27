package com.deathPunish;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.deathPunish.CustomItems.heal;
import static com.deathPunish.DeathPunish.config;

public class DeathPunishCommand implements CommandExecutor, TabExecutor {
    private final Plugin pl;
    AttributeInstance maxHealth;

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
                        sender.sendMessage("��c/deathpunish ��fgive��7: ��ȡ�Զ�����Ʒ");
                        sender.sendMessage("��c/deathpunish ��fset��7: �������Ѫ������");
                        sender.sendMessage("��c/deathpunish ��fadd��7: �������Ѫ������");
                        sender.sendMessage("��c/deathpunish ��fget��7: ��ȡ���Ѫ������");
                        sender.sendMessage("��c/deathpunish ��freload��7: ���ز���������ļ�");
                        sender.sendMessage("");
                        sender.sendMessage("��c��ǰ�����������ͷ��������У�");
                        for (String world : config.getStringList("punishOnDeath.enableWorlds")) {
                            sender.sendMessage("��f" + world);
                        }
                        return true;
                    } else {
                        sender.sendMessage("��c���Ȩ�޲��㣡");
                        return false;
                    }

                }
            }

            if (args[0].equalsIgnoreCase("set")) {
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
                    maxHealth = targetPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealth != null) {
                        maxHealth.setBaseValue(Integer.parseInt(args[2]));
                    }

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

            if (args[0].equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage("/deathpunish add <player> <health>");
                    return false;
                }
                if (args.length == 3) {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        try {
                            maxHealth = targetPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                            if (maxHealth != null) {
                                maxHealth.setBaseValue(maxHealth.getValue() + Integer.parseInt(args[2]));
                            }
                            sender.sendMessage("[DeathPunish] ��a��Ϊ��� " + targetPlayer.getName() + " ����Ѫ�����ޣ���ǰ����Ϊ" + maxHealth.getValue());
                            return true;
                        } catch (NumberFormatException e) {
                            sender.sendMessage("[DeathPunish] ��c����ֵ����Ϊ����");
                            return false;
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage("[DeathPunish] ��c���������Ѫ������С��1");
                            return false;
                        }
                    } else {
                        sender.sendMessage("[DeathPunish] ��c�Ҳ������ " + args[1]);
                        return false;
                    }
                }
            }

            if (args[0].equalsIgnoreCase("get")) {
                Player targetPlayer;
                if (args.length == 1) {
                    if (sender instanceof ConsoleCommandSender) {
                        sender.sendMessage("[DeathPunish] ��c/deathpunish get <player>");
                    } else {
                        targetPlayer = (Player) sender;
                        maxHealth = targetPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                        sender.sendMessage("[DeathPunish] ��a��� " + sender.getName() + " ��Ѫ������Ϊ" + maxHealth.getValue());
                        return true;
                    }
                } else {
                    targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        maxHealth = targetPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                        sender.sendMessage("[DeathPunish] ��a��� " + targetPlayer.getName() + " ��Ѫ������Ϊ" + maxHealth.getValue());
                        return true;
                    } else {
                        sender.sendMessage("[DeathPunish] ��c�Ҳ������ " + args[1]);
                        return false;
                    }
                }

            }

            if (args[0].equalsIgnoreCase("reload")) {
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
                    config = pl.getConfig();
                    sender.sendMessage("[DeathPunish] ��a���������");
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("give")) {
                if (args.length > 5 || args.length < 3) {
                    sender.sendMessage("/deathpunish give <player> <item> <amount>");
                    return false;
                }
                Player player = Bukkit.getPlayer(args[1]);
                int amount = (args.length == 4) ? Integer.parseInt(args[3]):1;
                if (player == null) {
                    sender.sendMessage("[DeathPunish] ��c�Ҳ������ " + args[1]);
                    return false;
                }
                String heal = config.getString("customItems.heal_item.name");
                String protect = config.getString("customItems.protect_item.name");
                String ender_protect = (config.getString("customItems.ender_protect_item.name"));
                ItemStack itemStack;
                ItemMeta meta;
                List<String> lore;
                if (args[2].equalsIgnoreCase(heal)) {
                    itemStack = new ItemStack(Material.valueOf(config.getString("customItems.heal_item.material")), amount);
                    lore = config.getStringList("customItems.heal_item.lore");
                    meta = itemStack.getItemMeta();
                    heal = heal.replace("&", "��");
                    meta.setDisplayName(heal);
                    lore = lore.stream().map(s -> s.replace("&", "��")).collect(Collectors.toList());
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    player.getInventory().addItem(itemStack);
                } else if (args[2].equalsIgnoreCase(protect)) {
                    itemStack = new ItemStack(Material.valueOf(config.getString("customItems.protect_item.material")), amount);
                    lore = config.getStringList("customItems.protect_item.lore");
                    protect = protect.replace("&", "��");
                    meta = itemStack.getItemMeta();
                    lore = lore.stream().map(s -> s.replace("&", "��")).collect(Collectors.toList());
                    meta.setDisplayName(protect);
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    player.getInventory().addItem(itemStack);
                } else if (args[2].equalsIgnoreCase(ender_protect)) {
                    itemStack = new ItemStack(Material.valueOf(config.getString("customItems.ender_protect_item.material")), amount);
                    lore = config.getStringList("customItems.ender_protect_item.lore");
                    ender_protect = ender_protect.replace("&", "��");
                    meta = itemStack.getItemMeta();
                    meta.setDisplayName(ender_protect);
                    lore = lore.stream().map(s -> s.replace("&", "��")).collect(Collectors.toList());
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    player.getInventory().addItem(itemStack);
                }
                return true;
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
        if (sender.hasPermission("deathpunish.command")) {
            if (args.length == 1) {
                // �������п��ܵ�����
                return new ArrayList<>(List.of("help", "give", "set", "add", "get", "reload"));
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) return new ArrayList<>(List.of("1", "10", "20"));
                else if (args[0].equalsIgnoreCase("give")) {
                    return new ArrayList<>(List.of(Objects.requireNonNull(config.getString("customItems.heal_item.name")),
                            Objects.requireNonNull(config.getString("customItems.protect_item.name")),
                            Objects.requireNonNull(config.getString("customItems.ender_protect_item.name"))));
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("give")) {
                    return new ArrayList<>(List.of("1", "5", "10", "32", "64"));
                } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) {
                    return new ArrayList<>(List.of("true", "false"));
                } else return null;
            }
        }
        return null;
    }

}
