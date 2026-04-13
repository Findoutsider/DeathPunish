package com.deathPunish.model;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record ManagedHealItem(
        String id,
        ItemStack itemStack,
        double healAmount,
        double maxHealth,
        String eatMsg,
        String eatWithoutHealMsg,
        List<String> potionEffects
) {}
