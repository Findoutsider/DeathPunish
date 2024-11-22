package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EatCustomItemListener implements Listener {

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equalsIgnoreCase("§6生命果实")) {
                event.setCancelled(true); // 取消默认效果
                event.getPlayer().setHealth(event.getPlayer().getMaxHealth()); // 恢复生命值
                event.getPlayer().setMaxHealth(event.getPlayer().getMaxHealth() + 2.0); // 增加生命上限
                event.getPlayer().setFoodLevel(20);
                if (event.getPlayer().getMaxHealth() > 20) {
                    event.getPlayer().setMaxHealth(20);
                    event.getPlayer().sendMessage("§a你食用了生命果实！§c但无法继续提高生命上限！");
                } else event.getPlayer().sendMessage("§a你食用了生命果实，恢复了生命上限并获得了额外效果！");
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1)); // 添加吸收效果
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1)); // 添加再生效果
                ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
                if (handItem.isSimilar(item)) {
                    int amount = handItem.getAmount();
                    if (amount > 1) {
                        handItem.setAmount(amount - 1);
                    } else {
                        event.getPlayer().getInventory().setItemInMainHand(null);
                    }
                }
            }
        }
    }
}
