package com.deathPunish.service;

import com.deathPunish.model.ManagedHealItem;
import com.deathPunish.model.ManagedProtectItem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ManagedItemService {
    private final File file;
    private final Map<String, ManagedHealItem> healItems = new LinkedHashMap<>();
    private final Map<String, ManagedProtectItem> normalProtectItems = new LinkedHashMap<>();
    private final Map<String, ManagedProtectItem> enderProtectItems = new LinkedHashMap<>();

    public ManagedItemService(File file) {
        this.file = file;
    }

    public void load() {
        healItems.clear();
        normalProtectItems.clear();
        enderProtectItems.clear();
        ensureFileExists();

        var config = YamlConfiguration.loadConfiguration(file);
        loadHealItems(config);
        loadProtectItems(config);
    }

    public void save() {
        ensureFileExists();
        var config = new YamlConfiguration();
        for (var item : healItems.values()) {
            String path = "heal_items." + item.id();
            config.set(path + ".item", cloneSingle(item.itemStack()));
            config.set(path + ".healAmount", item.healAmount());
            config.set(path + ".maxHealth", item.maxHealth());
            config.set(path + ".eatMsg", item.eatMsg());
            config.set(path + ".eatWithoutHealMsg", item.eatWithoutHealMsg());
            config.set(path + ".potionEffects", item.potionEffects());
        }
        for (var item : normalProtectItems.values()) {
            writeProtectItem(config, item);
        }
        for (var item : enderProtectItems.values()) {
            writeProtectItem(config, item);
        }

        try {
            config.save(file);
        } catch (IOException ex) {
            throw new IllegalStateException("无法保存物品仓库: " + file.getAbsolutePath(), ex);
        }
    }

    public Collection<ManagedHealItem> getHealItems() {
        return List.copyOf(healItems.values());
    }

    public Collection<ManagedProtectItem> getProtectItems() {
        var items = new java.util.ArrayList<ManagedProtectItem>();
        items.addAll(normalProtectItems.values());
        items.addAll(enderProtectItems.values());
        return List.copyOf(items);
    }

    public Optional<ManagedHealItem> getHealItem(String id) {
        return Optional.ofNullable(healItems.get(id));
    }

    public Optional<ManagedProtectItem> getProtectItem(String id, ManagedProtectItem.ProtectType type) {
        return Optional.ofNullable(getProtectItemMap(type).get(id));
    }

    public boolean containsHealItem(String id) {
        return healItems.containsKey(id);
    }

    public boolean containsProtectItem(String id, ManagedProtectItem.ProtectType type) {
        return getProtectItemMap(type).containsKey(id);
    }

    public void addHealItem(ManagedHealItem item) {
        healItems.put(item.id(), sanitizeHealItem(item));
        save();
    }

    public void addProtectItem(ManagedProtectItem item) {
        getProtectItemMap(item.type()).put(item.id(), sanitizeProtectItem(item));
        save();
    }

    public boolean removeHealItem(String id) {
        if (healItems.remove(id) == null) {
            return false;
        }
        save();
        return true;
    }

    public boolean removeProtectItem(String id, ManagedProtectItem.ProtectType type) {
        if (getProtectItemMap(type).remove(id) == null) {
            return false;
        }
        save();
        return true;
    }

    private void loadHealItems(YamlConfiguration config) {
        var section = config.getConfigurationSection("heal_items");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            String path = "heal_items." + id;
            ItemStack itemStack = config.getItemStack(path + ".item");
            if (itemStack == null) {
                continue;
            }
            healItems.put(id, new ManagedHealItem(
                    id,
                    cloneSingle(itemStack),
                    config.getDouble(path + ".healAmount"),
                    config.getDouble(path + ".maxHealth"),
                    config.getString(path + ".eatMsg", ""),
                    config.getString(path + ".eatWithoutHealMsg", ""),
                    List.copyOf(config.getStringList(path + ".potionEffects"))
            ));
        }
    }

    private void loadProtectItems(YamlConfiguration config) {
        var section = config.getConfigurationSection("protect_items");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            String path = "protect_items." + id;
            ItemStack itemStack = config.getItemStack(path + ".item");
            if (itemStack == null) {
                continue;
            }
            ManagedProtectItem.ProtectType type = parseProtectType(config.getString(path + ".type", "NORMAL"));
            getProtectItemMap(type).put(id, new ManagedProtectItem(id, cloneSingle(itemStack), type));
        }
    }

    private void writeProtectItem(YamlConfiguration config, ManagedProtectItem item) {
        String path = "protect_items." + item.id();
        config.set(path + ".item", cloneSingle(item.itemStack()));
        config.set(path + ".type", item.type().name());
    }

    private ManagedHealItem sanitizeHealItem(ManagedHealItem item) {
        return new ManagedHealItem(
                item.id(),
                cloneSingle(item.itemStack()),
                item.healAmount(),
                item.maxHealth(),
                item.eatMsg(),
                item.eatWithoutHealMsg(),
                List.copyOf(item.potionEffects())
        );
    }

    private ManagedProtectItem sanitizeProtectItem(ManagedProtectItem item) {
        return new ManagedProtectItem(item.id(), cloneSingle(item.itemStack()), item.type());
    }

    private Map<String, ManagedProtectItem> getProtectItemMap(ManagedProtectItem.ProtectType type) {
        return type == ManagedProtectItem.ProtectType.ENDER ? enderProtectItems : normalProtectItems;
    }

    private ManagedProtectItem.ProtectType parseProtectType(String raw) {
        try {
            return ManagedProtectItem.ProtectType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ManagedProtectItem.ProtectType.NORMAL;
        }
    }

    private ItemStack cloneSingle(ItemStack itemStack) {
        ItemStack clone = Objects.requireNonNull(itemStack).clone();
        clone.setAmount(1);
        return clone;
    }

    private void ensureFileExists() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("无法创建目录: " + parent.getAbsolutePath());
            }
            if (!file.exists() && !file.createNewFile()) {
                throw new IllegalStateException("无法创建文件: " + file.getAbsolutePath());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("无法初始化物品仓库: " + file.getAbsolutePath(), ex);
        }
    }
}
