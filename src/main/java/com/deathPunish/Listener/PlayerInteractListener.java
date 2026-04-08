package com.deathPunish.Listener;

import com.deathPunish.service.CustomItemService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class PlayerInteractListener implements Listener {
    private static final Set<Action> ACTIONS = Collections.unmodifiableSet(EnumSet.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK));
    private final CustomItemService customItemService;

    public PlayerInteractListener(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (!ACTIONS.contains(event.getAction()) || event.getItem() == null) {
            return;
        }
        if (customItemService.matchesConfiguredItem(event.getItem(), CustomItemService.PROTECT_ITEM_PATH)
                || customItemService.matchesConfiguredItem(event.getItem(), CustomItemService.ENDER_PROTECT_ITEM_PATH)) {
            event.setCancelled(true);
        }
    }
}
