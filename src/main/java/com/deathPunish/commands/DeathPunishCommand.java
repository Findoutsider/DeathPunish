package com.deathPunish.commands;

import com.deathPunish.DeathPunish;
import com.deathPunish.utils.manager.CustomItems;
import com.deathPunish.utils.manager.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
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

import static com.deathPunish.DeathPunish.config;
import static com.deathPunish.utils.manager.ConfigManager.getAllConfigs;

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
                        sender.sendMessage("§c[§f死亡惩罚插件指令帮助§c]");
                        sender.sendMessage("§c可使用dp替换deathpunish");
                        sender.sendMessage("§c使用§f\"/deathpunish help\"§c显示本页面");
                        sender.sendMessage("§c/deathpunish §fhelp§7: 显示帮助页面");
                        sender.sendMessage("§c/deathpunish §fgive§7: 获取自定义物品");
                        sender.sendMessage("§c/deathpunish §fset§7: 设置玩家血量上限");
                        sender.sendMessage("§c/deathpunish §fadd§7: 增加玩家血量上限");
                        sender.sendMessage("§c/deathpunish §fget§7: 获取玩家血量上限");
                        sender.sendMessage("§c/deathpunish §freload§7: 重载插件的配置文件");
                        sender.sendMessage("");
                        sender.sendMessage("§c当前启用了死亡惩罚的世界有：");
                        for (String world : config.getStringList("punishOnDeath.enableWorlds")) {
                            sender.sendMessage("§f" + world);
                        }
                        return true;
                    } else {
                        sender.sendMessage("§c你的权限不足！");
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
                            sender.sendMessage("§c设置的最大生命值必须为整数且不能小于1。");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§c设置的最大生命值必须为整数且不能小于1。");
                        return false;
                    }
                    maxHealth = targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealth != null) {
                        maxHealth.setBaseValue(Integer.parseInt(args[2]));
                    }

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
                    sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
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
                            maxHealth = targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (maxHealth != null) {
                                maxHealth.setBaseValue(maxHealth.getValue() + Integer.parseInt(args[2]));
                            }
                            sender.sendMessage("[DeathPunish] §a已为玩家 " + targetPlayer.getName() + " 增加血量上限，当前上限为" + maxHealth.getValue());
                            return true;
                        } catch (NumberFormatException e) {
                            sender.sendMessage("[DeathPunish] §c生命值必须为整数");
                            return false;
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage("[DeathPunish] §c不能让玩家血量上限小于1");
                            return false;
                        }
                    } else {
                        sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
                        return false;
                    }
                }
            }

            if (args[0].equalsIgnoreCase("get")) {
                Player targetPlayer;
                if (args.length == 1) {
                    if (sender instanceof ConsoleCommandSender) {
                        sender.sendMessage("[DeathPunish] §c/deathpunish get <player>");
                    } else {
                        targetPlayer = (Player) sender;
                        maxHealth = targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        sender.sendMessage("[DeathPunish] §a玩家 " + sender.getName() + " 的血量上限为" + maxHealth.getValue());
                        return true;
                    }
                } else {
                    targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        maxHealth = targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        sender.sendMessage("[DeathPunish] §a玩家 " + targetPlayer.getName() + " 的血量上限为" + maxHealth.getValue());
                        return true;
                    } else {
                        sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
                        return false;
                    }
                }

            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (args.length > 2) {
                    sender.sendMessage("/deathpunish reload");
                    return false;
                } else {
                    pl.reloadConfig();
                    config = pl.getConfig();
                    getAllConfigs();
                    DeathPunish.getWorldManger().setWorldRule();
                    sender.sendMessage("[DeathPunish] §a插件已重载");
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
                    sender.sendMessage("[DeathPunish] §c找不到玩家 " + args[1]);
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
                    heal = heal.replace("&", "§");
                    meta.setDisplayName(heal);
                    lore = lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    player.getInventory().addItem(itemStack);
                } else if (args[2].equalsIgnoreCase(protect)) {
                    itemStack = new ItemStack(Material.valueOf(config.getString("customItems.protect_item.material")), amount);
                    lore = config.getStringList("customItems.protect_item.lore");
                    protect = protect.replace("&", "§");
                    meta = itemStack.getItemMeta();
                    lore = lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
                    meta.setDisplayName(protect);
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    player.getInventory().addItem(itemStack);
                } else if (args[2].equalsIgnoreCase(ender_protect)) {
                    itemStack = new ItemStack(Material.valueOf(config.getString("customItems.ender_protect_item.material")), amount);
                    lore = config.getStringList("customItems.ender_protect_item.lore");
                    ender_protect = ender_protect.replace("&", "§");
                    meta = itemStack.getItemMeta();
                    meta.setDisplayName(ender_protect);
                    lore = lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    player.getInventory().addItem(itemStack);
                }
                return true;
            }


            sender.sendMessage("§c未知命令。");
            return false;
        }
        sender.sendMessage("§c你没有权限！");
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
                // 返回所有可能的命令
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
