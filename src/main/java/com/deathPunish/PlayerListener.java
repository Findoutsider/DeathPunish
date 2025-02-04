package com.deathPunish;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import static com.deathPunish.DeathPunish.*;
import static com.deathPunish.DeathPunish.log;
import static com.deathPunish.DeathPunish.worlds;


public class PlayerListener implements Listener {

    private static final Set<Action> ACTIONS = Collections.unmodifiableSet(EnumSet.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK));
    private static String method;
    private boolean isDeath = false;
    private final Plugin pl;
    AttributeInstance playerMaxHealth;

    public PlayerListener(DeathPunish plugin) {
        this.pl = plugin;
    }

    @EventHandler
    public void onAdminLogin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (player.isOp()) {
            player.sendMessage("");
            player.sendMessage("[deathpunish] §a当前插件版本为" + VERSION);
            player.sendMessage("[deathpunish] §a配置文件版本为" + config.getString("version"));
            player.sendMessage("[deathpunish] §a若二者版本不同请手动删除配置文件后重启服务器");
            player.sendMessage("[deathpunish] §a前往 https://github.com/Findoutsider/DeathPunish 获取更新");
            player.sendMessage("");
            }
        }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        World world = player.getWorld();
        if (!worlds.contains(world.getName())) {
            log.info("§c玩家 " + player.getName() + " 在未启用死亡惩罚的 " + world.getName() + " 世界中死亡");
            return;
        }
        playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (!player.hasPermission("deathpunish.bypass")) {

            // FileConfiguration epitaphConfig = plugin.getEpitaphConfig(); // 获取 epitaph.yml 配置
            if (config.getBoolean("punishOnDeath.enable")) {
                @Nullable ItemStack[] contents = player.getInventory().getContents();
                @Nullable ItemStack[] contents1 = player.getEnderChest().getContents();
                Material material1 = Material.valueOf(config.getString("customItems.protect_item.material"));
                Material material2 = Material.valueOf(config.getString("customItems.ender_protect_item.material"));
                for (ItemStack item : contents) {
                    if (item == null) continue;
                    if (item.getType() == material1 && item.getItemMeta().getDisplayName().replace("§", "&").equalsIgnoreCase(config.getString("customItems.protect_item.name"))) {
                        item.setAmount(item.getAmount() - 1);
                        player.sendMessage(Objects.requireNonNull(config.getString("punishments.skipPunishMsg")));
                        method = item.getItemMeta().getDisplayName();
                        log.info("玩家 " + player.getName() + " 因为§a" + method + " §b跳过死亡惩罚");
                        return;
                    }
                }
                for (ItemStack item : contents1) {
                    if (item == null) continue;
                    if (item.getType() == material2 && item.getItemMeta().getDisplayName().replace("§", "&").equalsIgnoreCase(config.getString("customItems.ender_protect_item.name"))) {
                        item.setAmount(item.getAmount() - 1);
                        player.sendMessage(Objects.requireNonNull(config.getString("punishments.skipPunishMsg")));
                        method = item.getItemMeta().getDisplayName();
                        log.info("玩家 " + player.getName() + " 因为§a " + method + " §b跳过死亡惩罚");
                        return;
                    }
                }

                this.isDeath = true;
                log.info("§c玩家 §r" + player.getName() + "§c 受到了死亡惩罚");
                if (config.getBoolean("punishments.enableEpitaph")) {
                    var position = player.getLocation();

                    position.getBlock().setType(Material.BEDROCK);
//                List<String> epitaphs = epitaphConfig.getStringList("defaultEpitaph");
//                if (!epitaphs.isEmpty()) {
//                    Random random = new Random();
//                    String selectedEpitaph = epitaphs.get(random.nextInt(epitaphs.size()));
//                    // 创建悬浮文本
//                    NMSUtil.createFloatingText(position.clone().add(0, 1, 0), selectedEpitaph);
//                }
                    String epitaph = config.getString("punishments.epitaph");
                    if (epitaph != null && epitaph.contains("%player%")) {
                        epitaph = epitaph.replace("%player%", player.getName());
                    }
                    Epitaph.createFloatingText(position.clone().add(0, 1, 0), epitaph);
                }

                // 清除玩家背包
                if (config.getBoolean("punishments.clearInventoryOnDeath")) {
                    player.getInventory().clear();
                }
                if (config.getBoolean("punishments.clearEnderchestOnDeath")) {
                    player.getEnderChest().clear();
                }

                if (config.getBoolean("punishments.reduceExpOnDeath.enable")) {
                    player.setTotalExperience(0);
                    int level = player.getLevel();
                    // level按百分比减少
                    player.setLevel((int) (level * (1 - config.getDouble("punishments.reduceExpOnDeath.value"))));
                }

                if (config.getBoolean("punishments.reduceMoneyOnDeath.enable")) {
                    double balance = econ.getBalance(player);
                    if (config.getInt("punishments.reduceMoneyOnDeath.mode") == 1) {
                        econ.withdrawPlayer(player, balance * (1 - config.getDouble("punishments.reduceMoneyOnDeath.value")));
                    } else if (config.getInt("punishments.reduceMoneyOnDeath.mode") == 2) {
                        econ.withdrawPlayer(player, (config.getDouble("punishments.reduceMoneyOnDeath.value")));
                    } else {
                        log.err("punishments.reduceMoneyOnDeath.mode 配置错误，值应为1或2");
                    }
                }
            }
//            if (config.getBoolean("punishOnDeath.enable")) {
//                player.sendMessage(Objects.requireNonNull(config.getString("punishments.deathMsg")));
//                // 读取当前玩家的最大生命值
//                double maxHealth = playerMaxHealth.getValue();
//                double reduceHealthAmount = config.getDouble("punishments.reduceHealthAmount");
//                if (config.getBoolean("punishments.banOnDeath") && maxHealth == 1) {
//                    Date expiration = new Date(System.currentTimeMillis() + (long) config.getInt("punishments.banDuration") * 60 * 1000);
//                    Bukkit.getBanList(BanList.Type.NAME).addBan(
//                            player.getName(),
//                            config.getString("punishments.banReason"),
//                            expiration,
//                            null);
//                    player.kick(Component.text(Objects.requireNonNull(config.getString("punishments.banReason"))));
//                }
//                // 减少最大生命值
//                if (config.getBoolean("punishments.reduceMaxHealthOnDeath")) {
//                    double newMaxHealth = Math.max(maxHealth - reduceHealthAmount, 1.0); // 最小值为1.0}
//                    // 设置玩家的新最大生命值
//                    playerMaxHealth.setBaseValue(newMaxHealth);
//                    player.setHealth(newMaxHealth); // 重置当前生命值为新的最大值
//                }
//                if (config.getBoolean("punishments.debuff.enable")) {
//                    List<String> debuff = config.getStringList("punishments.debuff.potions");
//                    for (String effect : debuff) {
//                        String[] parts = effect.split(" ");
//                        if (parts.length == 3) {
//                            PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(parts[0]));
//                            int duration = Integer.parseInt(parts[1]);
//                            int amplifier = Integer.parseInt(parts[2]);
//                            if (type != null) {
//                                player.addPotionEffect(new PotionEffect(type, duration, amplifier));
//                            }
//                        }
//                    }
//                }
//
//                this.isDeath = false;
//        }
        }else {
            method = "拥有bypass权限";
            log.info("玩家 " +player.getName()+ " 因为§a " + method + " §b跳过死亡惩罚");
            player.sendMessage(Objects.requireNonNull(config.getString("punishments.skipPunishMsg")));
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        if (this.isDeath) {
            if (config.getBoolean("punishOnDeath.enable")) {
                player.sendMessage(Objects.requireNonNull(config.getString("punishments.deathMsg")));
                // 读取当前玩家的最大生命值
                double maxHealth = playerMaxHealth.getValue();
                double reduceHealthAmount = config.getDouble("punishments.reduceHealthAmount");
                if (config.getBoolean("punishments.banOnDeath") && maxHealth == 1) {
                    Date expiration = new Date(System.currentTimeMillis() + (long) config.getInt("punishments.banDuration") * 60 * 1000);
                    Bukkit.getBanList(BanList.Type.NAME).addBan(
                            player.getName(),
                            config.getString("punishments.banReason"),
                            expiration,
                            "DeathPunish");
                    player.kick(Component.text(Objects.requireNonNull(config.getString("punishments.banReason"))));
                }
                // 减少最大生命值
                if (config.getBoolean("punishments.reduceMaxHealthOnDeath")) {
                    double newMaxHealth = Math.max(maxHealth - reduceHealthAmount, 1.0); // 最小值为1.0}
                    // 设置玩家的新最大生命值
                    playerMaxHealth.setBaseValue(newMaxHealth);
                    player.setHealth(newMaxHealth); // 重置当前生命值为新的最大值
                }
                if (config.getBoolean("punishments.debuff.enable")) {
                    List<String> debuff = config.getStringList("punishments.debuff.potions");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (String effect : debuff) {
                                log.info(effect);
                                String[] parts = effect.split(" ");
                                if (parts.length == 3) {
                                    PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(parts[0]));
                                    int duration = Integer.parseInt(parts[1]);
                                    int amplifier = Integer.parseInt(parts[2]);
                                    if (type != null) {
                                        log.info(String.valueOf(duration));
                                        log.info(String.valueOf(amplifier));
                                        boolean res =  player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                                        log.info(String.valueOf(res));
                                    } else {
                                        log.info("无效的药水效果: " + parts[0]);
                                    }
                                }
                            }
                        }
                    }.runTaskLater(pl, 5L);
                }

                this.isDeath = false;
            }
        }
    }


    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (ACTIONS.contains(event.getAction())) {
            ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
            if ((itemInMainHand.getType() == Material.valueOf(config.getString("customItems.protect_item.material"))
                    && itemInMainHand.getItemMeta().getDisplayName().replace("§", "&").equalsIgnoreCase(config.getString("customItems.protect_item.name")) )
                    || (itemInMainHand.getType() == Material.valueOf(config.getString("customItems.ender_protect_item.material"))
                    && itemInMainHand.getItemMeta().getDisplayName().replace("§", "&").equalsIgnoreCase(config.getString("customItems.ender_protect_item.name")))) {
                event.setCancelled(true);
            }
        }
    }

}