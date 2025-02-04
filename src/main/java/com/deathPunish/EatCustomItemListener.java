package com.deathPunish;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;

import static com.deathPunish.DeathPunish.config;
import static com.deathPunish.DeathPunish.log;

public class EatCustomItemListener implements Listener {

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().replace("��", "&").equalsIgnoreCase(config.getString("customItems.heal_item.name"))) {
                event.setCancelled(true); // ȡ��Ĭ��Ч��
                List<String> potionEffects = config.getStringList("customItems.heal_item.potion_effects");
                double healAmount = config.getDouble("customItems.heal_item.heal_amount");
                double maxHealth = event.getPlayer().getMaxHealth() + healAmount;
                event.getPlayer().setMaxHealth(maxHealth); // �����������ֵ
                event.getPlayer().setHealth(maxHealth); // ���õ�ǰ����ֵΪ���ֵ
                event.getPlayer().setFoodLevel(20);

                int maxHealthCap = config.getInt("customItems.heal_item.maxHealth");
                if (maxHealth > maxHealthCap) {
                    event.getPlayer().setMaxHealth(maxHealthCap);
                    event.getPlayer().sendMessage(Objects.requireNonNull(config.getString("customItems.heal_item.eatWithoutHealMsg")));
                } else {
                    event.getPlayer().sendMessage(Objects.requireNonNull(config.getString("customItems.heal_item.eatMsg")));
                }

                for (String effect : potionEffects) {
                    String[] parts = effect.split(" ");

                    PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(parts[0]));
                    int duration = Integer.parseInt(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);
                    if (type != null) {
                        event.getPlayer().addPotionEffect(new PotionEffect(type, duration, amplifier));
                    }
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
                log.info("��� " + event.getPlayer().getName() + " ͨ�� " + config.getString("customItems.heal_item.name") + " �ָ����������ޣ���ǰ�������ޣ�" + event.getPlayer().getHealth());
            }
        }

    }
}
