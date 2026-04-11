# DeathPunish

DeathPunish - 死亡惩罚插件，适合用于生存、RPG 或高风险玩法服务器。它可以在玩家死亡后扣除生命上限、清理物品、施加减益、生成墓碑，并提供保护符、豁免区域等配套机制。

---

## 画饼(TODO)

- 自定义合成物品有必要存在吗，应该可以用其他自定义物品的插件替代
- 保护符也是，可以移除了，使用命令添加手持物品为保护符替代
- 支持1.21.11+版本
- 支持更多能圈地的插件 ~~现在这些应该够了~~

## 支持环境

- 服务端核心：Paper 1.13 - 1.21.11
- Java：17
- 可选依赖：
  - Vault：启用金钱惩罚
  - WorldGuard
  - Residence
  - Lands
  - Towny
  - Dominion

## 主要功能

- 死亡后扣除生命上限，并支持设置扣除数值
- 死亡后清除背包、末影箱、经验、金钱
- 玩家生命上限降到 1 时可自动封禁
- 死亡原地生成墓碑与墓志铭
- 提供生命果实，用于恢复生命上限
- 提供保护符和末影保护符，用于免除死亡惩罚
- 支持指定世界启用惩罚
- 支持按世界、坐标范围、插件区域设置规则

## 快速开始

1. 将插件放入 `plugins` 目录并启动服务器。
2. 首次启动后关闭服务器，编辑 `plugins/DeathPunish/config.yml`。
3. 至少检查以下配置：
   - `punishOnDeath.enable`
   - `punishOnDeath.mode`
   - `punishments.reduceMaxHealthOnDeath`
   - `punishments.reduceHealthAmount`
   - `area.blacklist / area.whitelist`
4. 重新启动服务器，或使用 `/deathpunish reload` 重载配置。

## 常用配置说明

### 1. 总开关

```yml
punishOnDeath:
  enable: true
```

开启后，所有世界默认都会启用死亡惩罚。想排除某些世界时，请使用 `area.blacklist.world`。

### 2. 生命惩罚

```yml
punishments:
  reduceMaxHealthOnDeath: true
  reduceHealthAmount: 2
  minHealth: 1.0
```

开启后，玩家每次死亡会减少指定的生命上限，且不会低于 `minHealth`。

### 3. 区域模式

```yml
punishOnDeath:
  enable: true
  mode: "blacklist"

area:
  blacklist:
    world:
      - "world_the_end"
    coordinate:
      - "0 0 0 100 world"
    plugin_region:
      - "spawn"

  whitelist:
    world: []
    coordinate: []
    plugin_region: []
```

- `blacklist`：默认处罚，命中列表后跳过
- `whitelist`：默认不处罚，只有命中列表才处罚
- `world`：按整世界匹配
- `coordinate`：按坐标半径匹配，格式为 `x y z radius world`
- `plugin_region`：按圈地区域匹配，填写支持的插件的区域名

### 4. 跳过惩罚消息

```yml
punishments:
  skipPunishMsg: "§a你逃过了死亡惩罚！"
  bypassMsg: "§a你拥有 bypass 权限，已跳过死亡惩罚！"
  exemptionMsg: "§a你位于豁免区域，已跳过死亡惩罚！"
  protectItemMsg: "§a保护符生效，你逃过了死亡惩罚！"
  enderProtectItemMsg: "§a末影保护符生效，你逃过了死亡惩罚！"
```

不同的跳过来源可以分别设置提示消息。

## 命令

- `/deathpunish help`
- `/deathpunish reload`
- `/deathpunish give`
- `/deathpunish set`
- `/deathpunish add`
- `/deathpunish get`
- `/deathpunish migrate <玩家>`

别名：

- `/dp`

## 权限

- `deathpunish.command`
  - 使用 DeathPunish 管理命令
  - 默认：`op`
- `deathpunish.bypass`
  - 跳过死亡惩罚
  - 默认：`false`
- `deathpunish.craft`
  - 允许制作生命果实
  - 默认：`true`
- `deathpunish.protect`
  - 允许使用死亡保护物品
  - 默认：`true`

## 说明与注意事项

- 如果未安装 Vault，金钱惩罚不会生效。
- 至少要安装一个支持的区域插件， `plugin_region` 区域规则才会生效，目前支持的插件：
  - WorldGuard
  - Residence
  - Lands
  - Towny
  - Dominion
- 如果未配置跳过提示消息，会自动使用 `skipPunishMsg`。
- 从 `1.5.0-SNAPSHOT` 起，DeathPunish 改为使用 `AttributeModifier` 管理自身造成的生命上限变化，以提高与其他会修改生命值的插件的兼容性。
- 如果你从旧版本升级，且旧版本曾直接修改过玩家的 `GENERIC_MAX_HEALTH.baseValue`，建议对受影响玩家执行 `/deathpunish migrate <玩家>`。
- `migrate` 会将玩家当前最大生命的 `baseValue` 重置为 `20`，并把差值迁移为 DeathPunish 自己的 modifier。
- `migrate` 只建议用于修复旧版本遗留数据。若服务器中有其他插件主动修改 `baseValue` 且不是以 `20` 为基线，请先确认再执行。
- 本版本为 `1.5.0-SNAPSHOT`，功能仍在继续补充和调整。
