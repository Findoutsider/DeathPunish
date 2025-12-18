package com.deathPunish.Listener;

import com.deathPunish.DeathPunish;
import com.deathPunish.utils.EpitaphUtils;
import com.deathPunish.utils.SchedulerUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.deathPunish.DeathPunish.*;
import static com.deathPunish.DeathPunish.log;
import static com.deathPunish.utils.manager.ConfigManager.*;


public class PlayerDeathListener implements Listener {
    private AttributeInstance playerMaxHealth;
    private final Random rand = new Random();
    private boolean isDeath = false;
    private final Plugin pl;
    private int food;
    private final List<Material> materials = new ArrayList<>();

    public PlayerDeathListener(DeathPunish plugin) {
        this.pl = plugin;
        for (String wl : inventoryWhitelist) {
            try {
                Material material = Material.valueOf(wl);
                materials.add(material);
            } catch (IllegalArgumentException e) {
                log.warn("§c无效的物品: " + wl);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (SchedulerUtils.isFolia()) return;
        var player = event.getPlayer();
        playerDeath(player);
    }

    @EventHandler
    public void isFoliaPlayerDeath(PlayerDeathEvent event) {
        if (!SchedulerUtils.isFolia()) return;
        Player player = event.getEntity();
        playerDeath(player);
    }

    private void playerDeath(Player player) {
        Runnable r = () -> {
            if (this.isDeath) {
                if (enableDeathPunish) {
                    List<String> deathMsg = Objects.requireNonNull(config.getStringList("punishments.deathMsg"));
                    for (String msg : deathMsg) {
                        player.sendMessage(msg);
                    }
                    // 读取当前玩家的最大生命值
                    double maxHealth = playerMaxHealth.getValue();
                    if (banOnDeath && maxHealth == 1) {
                        Date expiration = new Date(System.currentTimeMillis() + (long) banDuration * 60 * 1000);
                        Bukkit.getBanList(BanList.Type.NAME).addBan(
                                player.getName(),
                                banReason,
                                expiration,
                                "DeathPunish");
                        player.kickPlayer(Objects.requireNonNull(banReason));
                    }
                    // 减少最大生命值
                    if (reduceMaxHealthOnDeath) {
                        double newMaxHealth = Math.max(maxHealth - reduceHealthAmount, 1.0); // 最小值为1.0}
                        // 设置玩家的新最大生命值
                        playerMaxHealth.setBaseValue(newMaxHealth);
                        player.setHealth(newMaxHealth); // 重置当前生命值为新的最大值
                    }
                    if (foodLevelSave) {
                        SchedulerUtils.runTaskLater(pl,() -> player.setFoodLevel(food), 1L);
                    } else {
                        SchedulerUtils.runTaskLater(pl,() -> player.setFoodLevel(foodLevelValue), 1L);
                    }
                    if (debuffEnable) {
                        SchedulerUtils.runTask(pl, () -> {
                            for (String effect : debuffPotions) {
                                String[] parts = effect.split(" ");
                                if (parts.length == 3) {
                                    PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(parts[0]));
                                    int duration = Integer.parseInt(parts[1]);
                                    int amplifier = Integer.parseInt(parts[2]);
                                    if (type != null) {
                                        if (SchedulerUtils.isFolia()) {
                                            DeathPunish.getFoliaLib().getScheduler().runAtEntity(player, wrappedTask ->
                                                    player.addPotionEffect(new PotionEffect(type, duration, amplifier)));
                                        } else player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                                    } else {
                                        log.info("无效的药水效果: " + parts[0]);
                                    }
                                }
                            }
                        });
                    }
                    this.isDeath = false;
                }
            }
        };
        if (SchedulerUtils.isFolia()) {
            SchedulerUtils.runTaskLater(pl, r, 5L);
        } else r.run();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        World world = player.getWorld();
        if (!enableWorlds.contains(world.getName())) {
            log.info("§c玩家 " + player.getName() + " 在未启用死亡惩罚的 " + world.getName() + " 世界中死亡");
            return;
        }
        playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        String method;
        if (!player.hasPermission("deathpunish.bypass")) {

            // FileConfiguration epitaphConfig = plugin.getEpitaphConfig(); // 获取 epitaph.yml 配置
            if (enableDeathPunish) {
                @Nullable ItemStack[] contents = player.getInventory().getContents();
                @Nullable ItemStack[] contents1 = player.getEnderChest().getContents();
                Material material1 = Material.valueOf(config.getString("customItems.protect_item.material"));
                Material material2 = Material.valueOf(config.getString("customItems.ender_protect_item.material"));
                for (ItemStack item : contents) {

                    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
                        continue;
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        if (item.getType() == material1 && item.getItemMeta().getDisplayName().replace("§", "&").equalsIgnoreCase(config.getString("customItems.protect_item.name"))) {
                            item.setAmount(item.getAmount() - 1);
                            player.sendMessage(Objects.requireNonNull(skipPunishMsg));
                            method = item.getItemMeta().getDisplayName();
                            log.info("玩家 " + player.getName() + " 因为§a" + method + " §b跳过死亡惩罚");
                            return;
                        }
                    }
                }
                for (ItemStack item : contents1) {
                    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
                        continue;
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        if (item.getType() == material2 && item.getItemMeta()
                                .getDisplayName()
                                .replace("§", "&")
                                .equalsIgnoreCase(config.getString("customItems.ender_protect_item.name"))) {
                            item.setAmount(item.getAmount() - 1);
                            player.sendMessage(Objects.requireNonNull(skipPunishMsg));
                            method = item.getItemMeta().getDisplayName();
                            log.info("玩家 " + player.getName() + " 因为§a " + method + " §b跳过死亡惩罚");
                            return;
                        }
                    }
                }

                this.isDeath = true;
                this.food = player.getFoodLevel();
                log.info("§c玩家 §r" + player.getName() + "§c 受到了死亡惩罚");
                if (enableEpitaph) {
                    var position = player.getLocation();

                    position.getBlock().setType(Material.BEDROCK);

                    if (epitaph != null && epitaph.contains("%player%")) {
                        epitaph = epitaph.replace("%player%", player.getName());
                    }
                    EpitaphUtils.createFloatingText(position.clone().add(0, 1, 0), epitaph);
                }

                // 背包
                if (inventoryEnable) {
                    if (inventoryMode.equalsIgnoreCase("all")) {
                        clearInventoryContainOffhandAndArmor(player, materials, !inventoryClean);
                    }

                    if (Objects.requireNonNull(config.getString("punishments.Inventory.mode")).equalsIgnoreCase("part")) {
                        int min = config.getInt("punishments.Inventory.amount.min");
                        int max = config.getInt("punishments.Inventory.amount.max");
                        int dropAmount = rand.nextInt(max - min + 1) + min;
                        if (min == max) dropAmount = min;

                        removeItemFromInventoryContainOffhandAndArmor(player, materials, dropAmount, !inventoryClean);
                    }
                }
                if (clearEnderchestOnDeath) {
                    player.getEnderChest().clear();
                }

                if (reduceExpOnDeathEnable) {
                    player.setTotalExperience(0);
                    int level = player.getLevel();
                    player.setLevel((int) (level * (1 - reduceExpValue)));
                }

                if (reduceMoneyOnDeathEnable) {
                    double balance = econ.getBalance(player);
                    if (reduceMoneyMode == 1) {
                        econ.withdrawPlayer(player, balance * reduceMoneyValue);
                    } else if (reduceMoneyMode == 2 && reduceMoneyValue >= 0) {
                        econ.withdrawPlayer(player, reduceMoneyValue);
                    } else if (reduceMoneyMode == 2 && reduceMoneyValue < 0) {
                        log.err("你可能在punishments.reduceMoneyOnDeath.value 的值不能为负数");
                    } else {
                        log.err("punishments.reduceMoneyOnDeath.mode 配置错误，值应为1或2");
                    }
                }
            }
        } else {
            method = "拥有bypass权限";
            log.info("玩家 " + player.getName() + " 因为§a " + method + " §b跳过死亡惩罚");
            player.sendMessage(Objects.requireNonNull(skipPunishMsg));
        }
    }

    private void clearInventoryContainOffhandAndArmor(Player player, List<Material> filter, Boolean isDrop) {
        PlayerInventory inv = player.getInventory();

        List<ItemStack> items = new ArrayList<>(Arrays.asList(inv.getContents()));
        items.removeIf(Objects::isNull);
        items.removeIf(item -> filter.contains(item.getType()));

        ItemStack offhand = inv.getItemInOffHand();
        ItemStack helmet = inv.getHelmet();
        ItemStack chestplate = inv.getChestplate();
        ItemStack leggings = inv.getLeggings();
        ItemStack boots = inv.getBoots();

        for (ItemStack item : items) {
            if (Objects.equals(offhand, item)) { inv.setItemInOffHand(null); }
            if (helmet != null && helmet.equals(item)) { inv.setHelmet(null); }
            if (chestplate != null && chestplate.equals(item)) { inv.setChestplate(null); }
            if (leggings != null && leggings.equals(item)) { inv.setLeggings(null); }
            if (boots != null && boots.equals(item)) { inv.setBoots(null); }
            inv.remove(item);
            if (isDrop) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }

    private void removeItemFromInventoryContainOffhandAndArmor(Player player, List<Material> filter, Integer count, Boolean isDrop) {
        PlayerInventory inv = player.getInventory();

        // 创建一个包含索引信息的物品列表
        List<IndexedItem> indexedItems = new ArrayList<>();
        ItemStack[] contents = inv.getContents();
        
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && !filter.contains(item.getType())) {
                indexedItems.add(new IndexedItem(item, i));
            }
        }
        
        Collections.shuffle(indexedItems);
        if (indexedItems.isEmpty()) return;

        for (int removed = 0; removed < count && !indexedItems.isEmpty(); removed++) {
            IndexedItem indexedItem = indexedItems.get(0);
            ItemStack item = indexedItem.item;
            int index = indexedItem.index;

            if (index == 40) { // 副手槽位
                inv.setItemInOffHand(null);
            } else if (index == 39) { // 头盔槽位
                inv.setHelmet(null);
            } else if (index == 38) { // 胸甲槽位
                inv.setChestplate(null);
            } else if (index == 37) { // 护腿槽位
                inv.setLeggings(null);
            } else if (index == 36) { // 靴子槽位
                inv.setBoots(null);
            } else {
                inv.setItem(index, null);
            }
            
            if (isDrop) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            indexedItems.remove(0);
        }
    }

    private static class IndexedItem {
        final ItemStack item;
        final int index;
        
        IndexedItem(ItemStack item, int index) {
            this.item = item;
            this.index = index;
        }
    }

}