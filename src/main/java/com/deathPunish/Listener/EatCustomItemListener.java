package com.deathPunish.Listener;

import com.deathPunish.service.CustomItemService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class EatCustomItemListener implements Listener {
    private final CustomItemService customItemService;

    public EatCustomItemListener(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!customItemService.applyHealItem(event.getPlayer(), item)) {
            return;
        }
        event.setCancelled(true);
        consumeItem(event);
    }

    private void consumeItem(PlayerItemConsumeEvent event) {
        var inventory = event.getPlayer().getInventory();
        ItemStack mainHandItem = inventory.getItemInMainHand();
        ItemStack offHandItem = inventory.getItemInOffHand();
        boolean consumedFromOffHand = isMatchingStack(offHandItem, event.getItem()) && !isMatchingStack(mainHandItem, event.getItem());
        ItemStack consumedItem = consumedFromOffHand ? offHandItem : mainHandItem;
        if (consumedItem == null || consumedItem.getType().isAir()) {
            return;
        }
        if (consumedItem.getAmount() > 1) {
            consumedItem.setAmount(consumedItem.getAmount() - 1);
            return;
        }
        if (consumedFromOffHand) {
            inventory.setItemInOffHand(null);
        } else {
            inventory.setItemInMainHand(null);
        }
    }

    private boolean isMatchingStack(ItemStack handItem, ItemStack consumedItem) {
        return handItem != null && !handItem.getType().isAir() && handItem.isSimilar(consumedItem);
    }
}
