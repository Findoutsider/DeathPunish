package com.deathPunish.model;

import org.bukkit.inventory.ItemStack;

public record ManagedProtectItem(
        String id,
        ItemStack itemStack,
        ProtectType type
) {
    public enum ProtectType {
        NORMAL,
        ENDER
    }
}
