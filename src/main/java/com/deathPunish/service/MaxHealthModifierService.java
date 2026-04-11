package com.deathPunish.service;

import com.deathPunish.DeathPunish;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class MaxHealthModifierService {
    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("0f4f0e4c-47dd-4b55-9e54-3c57b9b54d62");
    private static final String MAX_HEALTH_MODIFIER_NAME = "deathpunish_max_health";

    private final NamespacedKey modifierAmountKey;

    public MaxHealthModifierService(DeathPunish plugin) {
        this.modifierAmountKey = new NamespacedKey(plugin, "max_health_delta");
    }

    public Double getEffectiveMaxHealth(Player player) {
        var attribute = getMaxHealthAttribute(player);
        return attribute == null ? null : attribute.getValue();
    }

    public Double getExternalMaxHealth(Player player) {
        var attribute = getMaxHealthAttribute(player);
        if (attribute == null) {
            return null;
        }
        return attribute.getValue() - getAppliedModifierAmount(attribute);
    }

    public void setEffectiveMaxHealth(Player player, double targetEffectiveMaxHealth) {
        var attribute = getMaxHealthAttribute(player);
        if (attribute == null) {
            return;
        }

        double externalMaxHealth = attribute.getValue() - getAppliedModifierAmount(attribute);
        double newModifierAmount = targetEffectiveMaxHealth - externalMaxHealth;
        persistModifierAmount(player.getPersistentDataContainer(), newModifierAmount);
        applyModifier(attribute, newModifierAmount);
    }

    public boolean migrateBaseValue(Player player, double targetBaseValue) {
        var attribute = getMaxHealthAttribute(player);
        if (attribute == null) {
            return false;
        }

        double currentModifierAmount = getAppliedModifierAmount(attribute);
        double newModifierAmount = attribute.getBaseValue() + currentModifierAmount - targetBaseValue;
        attribute.setBaseValue(targetBaseValue);
        persistModifierAmount(player.getPersistentDataContainer(), newModifierAmount);
        applyModifier(attribute, newModifierAmount);
        return true;
    }

    public void syncPlayer(Player player) {
        var attribute = getMaxHealthAttribute(player);
        if (attribute == null) {
            return;
        }

        double storedModifierAmount = getStoredModifierAmount(player.getPersistentDataContainer());
        applyModifier(attribute, storedModifierAmount);
        Double effectiveMaxHealth = getEffectiveMaxHealth(player);
        if (effectiveMaxHealth != null && player.getHealth() > effectiveMaxHealth) {
            player.setHealth(effectiveMaxHealth);
        }
    }

    public void clearModifier(Player player) {
        var attribute = getMaxHealthAttribute(player);
        if (attribute == null) {
            return;
        }
        applyModifier(attribute, 0.0D);
    }

    private AttributeInstance getMaxHealthAttribute(Player player) {
        return player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    }

    private double getAppliedModifierAmount(AttributeInstance attribute) {
        return attribute.getModifiers().stream()
                .filter(modifier -> modifier.getUniqueId().equals(MAX_HEALTH_MODIFIER_ID))
                .mapToDouble(AttributeModifier::getAmount)
                .findFirst()
                .orElse(0.0D);
    }

    private void applyModifier(AttributeInstance attribute, double amount) {
        attribute.getModifiers().stream()
                .filter(modifier -> modifier.getUniqueId().equals(MAX_HEALTH_MODIFIER_ID))
                .forEach(attribute::removeModifier);

        if (Math.abs(amount) < 1.0E-9) {
            return;
        }

        attribute.addModifier(new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID,
                MAX_HEALTH_MODIFIER_NAME,
                amount,
                AttributeModifier.Operation.ADD_NUMBER
        ));
    }

    private double getStoredModifierAmount(PersistentDataContainer dataContainer) {
        Double storedAmount = dataContainer.get(modifierAmountKey, PersistentDataType.DOUBLE);
        return storedAmount == null ? 0.0D : storedAmount;
    }

    private void persistModifierAmount(PersistentDataContainer dataContainer, double amount) {
        if (Math.abs(amount) < 1.0E-9) {
            dataContainer.remove(modifierAmountKey);
            return;
        }
        dataContainer.set(modifierAmountKey, PersistentDataType.DOUBLE, amount);
    }
}
