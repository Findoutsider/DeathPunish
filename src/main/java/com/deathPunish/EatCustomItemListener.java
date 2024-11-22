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
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equalsIgnoreCase("��6������ʵ")) {
                event.setCancelled(true); // ȡ��Ĭ��Ч��
                event.getPlayer().setHealth(event.getPlayer().getMaxHealth()); // �ָ�����ֵ
                event.getPlayer().setMaxHealth(event.getPlayer().getMaxHealth() + 2.0); // ������������
                event.getPlayer().setFoodLevel(20);
                if (event.getPlayer().getMaxHealth() > 20) {
                    event.getPlayer().setMaxHealth(20);
                    event.getPlayer().sendMessage("��a��ʳ����������ʵ����c���޷���������������ޣ�");
                } else event.getPlayer().sendMessage("��a��ʳ����������ʵ���ָ����������޲�����˶���Ч����");
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1)); // �������Ч��
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1)); // �������Ч��
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
