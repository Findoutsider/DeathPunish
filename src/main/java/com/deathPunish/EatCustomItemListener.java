package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;

public class EatCustomItemListener implements Listener {

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        FileConfiguration config = DeathPunish.getPlugin(DeathPunish.class).getConfig();
        ItemStack item = event.getItem();
        List<String> potionEffects = config.getStringList("customItems.heal_item.potion_effects");
        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equalsIgnoreCase("§6生命果实")) {
                event.setCancelled(true); // 取消默认效果
                event.getPlayer().setMaxHealth(event.getPlayer().getMaxHealth() + config.getDouble("customItems.heal_item.heal_amount")); // 增加生命上限
                event.getPlayer().setHealth(event.getPlayer().getMaxHealth()); // 恢复生命值
                event.getPlayer().setFoodLevel(20);
                if (event.getPlayer().getMaxHealth() > config.getInt("customItems.heal_item.maxHealth")) {
                    event.getPlayer().setMaxHealth(20);
                    event.getPlayer().sendMessage(Objects.requireNonNull(config.getString("customItems.heal_item.eatWithoutHealMsg")));
                } else event.getPlayer().sendMessage(Objects.requireNonNull(config.getString("customItems.heal_item.eatMsg")));
                for (String effect : potionEffects) {
                    String[] parts = effect.split(" ");
                    PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(parts[0]));
                    int duration = Integer.parseInt(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);
                    event.getPlayer().addPotionEffect(new PotionEffect(type, duration, amplifier));
                }
                ItemStack mainItem = event.getPlayer().getInventory().getItemInMainHand();
                ItemStack offItem = event.getPlayer().getInventory().getItemInOffHand();
                ItemStack handItem = mainItem.isSimilar(item) ? mainItem : offItem;
                int amount = handItem.getAmount();
                if (amount > 1) {
                    handItem.setAmount(amount - 1);
                } else {
                    if (mainItem.isSimilar(item)) {
                        event.getPlayer().getInventory().setItemInMainHand(null);
                    } else {
                        event.getPlayer().getInventory().setItemInOffHand(null);
                    }
                }
            }
        }
    }
}
