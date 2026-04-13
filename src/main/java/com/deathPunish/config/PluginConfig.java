package com.deathPunish.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public record PluginConfig(
        String version,
        boolean enableDeathPunish,
        String punishMode,
        CustomItemManagerConfig customItemManager,
        boolean autoSetRule,
        boolean doImmediateRespawn,
        boolean reduceMaxHealthOnDeath,
        double reduceHealthAmount,
        double minHealthAfterDeath,
        boolean reduceExpOnDeathEnable,
        double reduceExpValue,
        boolean reduceMoneyOnDeathEnable,
        int reduceMoneyMode,
        double reduceMoneyValue,
        boolean inventoryEnable,
        String inventoryMode,
        boolean inventoryClean,
        int inventoryMinAmount,
        int inventoryMaxAmount,
        List<String> inventoryWhitelist,
        boolean clearEnderchestOnDeath,
        List<String> deathMsg,
        boolean foodLevelSave,
        int foodLevelValue,
        boolean debuffEnable,
        List<String> debuffPotions,
        String skipPunishMsg,
        String bypassMsg,
        String exemptionMsg,
        String protectItemMsg,
        String enderProtectItemMsg,
        boolean banOnDeath,
        String banReason,
        int banDuration,
        boolean enableEpitaph,
        String epitaph,
        ItemConfig protectItem,
        ItemConfig enderProtectItem,
        HealItemConfig healItem,
        KillStealConfig killSteal,
        AreaSettings areaSettings
) {
    public static PluginConfig from(FileConfiguration config) {
        return new PluginConfig(
                config.getString("version", ""),
                config.getBoolean("punishOnDeath.enable"),
                config.getString("punishOnDeath.mode", "blacklist").toLowerCase(),
                new CustomItemManagerConfig(
                        new ToggleConfig(config.getBoolean("customItemManager.healItem.disableBuiltin", false)),
                        new ToggleConfig(config.getBoolean("customItemManager.protectItem.disableBuiltin", false))
                ),
                config.getBoolean("autoSetRule"),
                config.getBoolean("doImmediateRespawn"),
                config.getBoolean("punishments.reduceMaxHealthOnDeath"),
                config.getDouble("punishments.reduceHealthAmount"),
                resolveMinHealthAfterDeath(config),
                config.getBoolean("punishments.reduceExpOnDeath.enable"),
                config.getDouble("punishments.reduceExpOnDeath.value"),
                config.getBoolean("punishments.reduceMoneyOnDeath.enable"),
                config.getInt("punishments.reduceMoneyOnDeath.mode"),
                config.getDouble("punishments.reduceMoneyOnDeath.value"),
                config.getBoolean("punishments.Inventory.enable"),
                config.getString("punishments.Inventory.mode", "all"),
                config.getBoolean("punishments.Inventory.clean"),
                config.getInt("punishments.Inventory.amount.min"),
                config.getInt("punishments.Inventory.amount.max"),
                List.copyOf(config.getStringList("punishments.Inventory.whitelist")),
                config.getBoolean("punishments.clearEnderchestOnDeath"),
                List.copyOf(config.getStringList("punishments.deathMsg")),
                config.getBoolean("punishments.foodLevel.save"),
                config.getInt("punishments.foodLevel.value"),
                config.getBoolean("punishments.debuff.enable"),
                List.copyOf(config.getStringList("punishments.debuff.potions")),
                config.getString("punishments.skipPunishMsg"),
                config.getString("punishments.bypassMsg", config.getString("punishments.skipPunishMsg", "")),
                config.getString("punishments.exemptionMsg", config.getString("punishments.skipPunishMsg", "")),
                config.getString("punishments.protectItemMsg", config.getString("punishments.skipPunishMsg", "")),
                config.getString("punishments.enderProtectItemMsg", config.getString("punishments.skipPunishMsg", "")),
                config.getBoolean("punishments.banOnDeath"),
                config.getString("punishments.banReason"),
                config.getInt("punishments.banDuration"),
                config.getBoolean("punishments.enableEpitaph"),
                config.getString("punishments.epitaph"),
                ItemConfig.from(config, "customItems.protect_item"),
                ItemConfig.from(config, "customItems.ender_protect_item"),
                HealItemConfig.from(config, "customItems.heal_item"),
                KillStealConfig.from(config, "killSteal"),
                AreaSettings.from(config)
        );
    }

    public boolean disableBuiltinHealItem() {
        return customItemManager.healItem().disableBuiltin();
    }

    public boolean disableBuiltinProtectItems() {
        return customItemManager.protectItem().disableBuiltin();
    }

    private static double resolveMinHealthAfterDeath(FileConfiguration config) {
        return config.contains("punishments.minHealth")
                ? config.getDouble("punishments.minHealth")
                : config.getDouble("punishments.Health.min", 1.0D);
    }

    public record ItemConfig(String name, String material, List<String> lore) {
        public static ItemConfig from(FileConfiguration config, String path) {
            return new ItemConfig(
                    config.getString(path + ".name", ""),
                    config.getString(path + ".material", ""),
                    List.copyOf(config.getStringList(path + ".lore"))
            );
        }
    }

    public record CustomItemManagerConfig(
            ToggleConfig healItem,
            ToggleConfig protectItem
    ) {}

    public record ToggleConfig(boolean disableBuiltin) {}

    public record HealItemConfig(
            String name,
            String material,
            List<String> lore,
            double maxHealth,
            double healAmount,
            List<String> potionEffects,
            String eatMsg,
            String eatWithoutHealMsg,
            List<String> shape,
            Map<String, String> ingredients
    ) {
        @SuppressWarnings("unchecked")
        public static HealItemConfig from(FileConfiguration config, String path) {
            var section = config.getConfigurationSection(path + ".ingredients");
            Map<String, String> ingredients = section == null
                    ? Map.of()
                    : section.getValues(false).entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    entry -> String.valueOf(entry.getValue())
            ));

            return new HealItemConfig(
                    config.getString(path + ".name", ""),
                    config.getString(path + ".material", ""),
                    List.copyOf(config.getStringList(path + ".lore")),
                    config.getDouble(path + ".maxHealth"),
                    config.getDouble(path + ".heal_amount"),
                    List.copyOf(config.getStringList(path + ".potion_effects")),
                    config.getString(path + ".eatMsg", ""),
                    config.getString(path + ".eatWithoutHealMsg", ""),
                    List.of(
                            config.getString(path + ".shape1", "yxy"),
                            config.getString(path + ".shape2", "xbx"),
                            config.getString(path + ".shape3", "yxy")
                    ),
                    ingredients
            );
        }
    }

    public record KillStealConfig(
            StealConfig lifeSteal,
            StealConfig expSteal
    ) {
        public static KillStealConfig from(FileConfiguration config, String path) {
            return new KillStealConfig(
                    StealConfig.from(config, path + ".lifeSteal"),
                    StealConfig.from(config, path + ".expSteal")
            );
        }
    }

    public record StealConfig(
            boolean enable,
            double rate
    ) {
        public static StealConfig from(FileConfiguration config, String path) {
            return new StealConfig(
                    config.getBoolean(path + ".enable"),
                    config.getDouble(path + ".rate")
            );
        }
    }

    public record AreaSettings(
            AreaRuleSet blacklist,
            AreaRuleSet whitelist
    ) {
        public static AreaSettings from(FileConfiguration config) {
            boolean hasAreaConfig = config.contains("area.blacklist") || config.contains("area.whitelist");
            AreaRuleSet blacklist = hasAreaConfig
                    ? AreaRuleSet.from(config, "area.blacklist")
                    : AreaRuleSet.from(config, "punishments.exemption");
            AreaRuleSet whitelist = hasAreaConfig
                    ? AreaRuleSet.from(config, "area.whitelist")
                    : AreaRuleSet.empty();
            return new AreaSettings(blacklist, whitelist);
        }
    }

    public record AreaRuleSet(
            List<String> worlds,
            List<ExemptionCoordinate> coordinates,
            List<String> pluginRegions
    ) {
        public static AreaRuleSet from(FileConfiguration config, String path) {
            List<String> pluginRegions = config.contains(path + ".plugin_region")
                    ? List.copyOf(config.getStringList(path + ".plugin_region"))
                    : List.copyOf(config.getStringList(path + ".worldguard_region"));
            return new AreaRuleSet(
                    List.copyOf(config.getStringList(path + ".world")),
                    config.getStringList(path + ".coordinate").stream()
                            .map(ExemptionCoordinate::from)
                            .filter(java.util.Objects::nonNull)
                            .toList(),
                    pluginRegions
            );
        }

        public static AreaRuleSet empty() {
            return new AreaRuleSet(List.of(), List.of(), List.of());
        }
    }

    public record ExemptionCoordinate(
            double x,
            double y,
            double z,
            double radius,
            String world
    ) {
        public static ExemptionCoordinate from(String value) {
            String[] parts = value.trim().split("\\s+");
            if (parts.length != 5) {
                return null;
            }
            try {
                return new ExemptionCoordinate(
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3]),
                        parts[4]
                );
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
