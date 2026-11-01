# Iridium

Iridium 是一个面向 Minecraft **Fabric 1.20.1** 的轻量性能优化模组，同时支持**客户端**与**服务端**。

设计原则：**默认保留原版行为**，每一项优化都可独立开关，且只做"删除无用工作"，不改变游戏逻辑。

## 优化项

### 服务端（TPS）

| 配置项 | 默认 | 说明 |
|---|---|---|
| `blockEntityTickRange` | `0`（关闭） | 方块实体 tick 激活范围（格）。超出范围且远离所有玩家的方块实体跳过 tick |
| `randomTickChunkRange` | `0`（关闭） | 随机刻区块范围（区块数）。远处区块跳过随机刻（作物生长、火焰蔓延等） |

### 客户端（FPS）

| 配置项 | 默认 | 说明 |
|---|---|---|
| `blockEntityRenderDistance` | `96` | 超出距离的方块实体不再提交渲染（纯视觉，不影响逻辑） |
| `entityRenderDistance` | `128` | 超出距离的实体不再提交渲染（掉落物、生物、载具） |

### 诊断

| 配置项 | 默认 | 说明 |
|---|---|---|
| `performanceProfiling` | `true` | 每 30 秒输出一次被剔除的工作量统计 |
| `logicOptimizations` / `clientOptimizations` | `true` | 服务端 / 客户端优化总开关 |

## 配置文件

位于 `config/iridium.json`：

```json
{
  "logicOptimizations": true,
  "clientOptimizations": true,
  "blockEntityTickRange": 0,
  "blockEntityRenderDistance": 96,
  "randomTickChunkRange": 0,
  "entityRenderDistance": 128,
  "performanceProfiling": true
}
```

> 服务端优化项默认关闭，需显式设置大于 0 的值才会生效，以免意外改变玩法（例如玩家离开后农场停止运转）。

## 与其它模组的关系

- 与 **Sodium / Iris** 互补：Iridium 工作在逻辑与剔除层，不触碰渲染后端。
- **不提供游戏内 GUI**，配置通过 `config/iridium.json` 修改。Sodium 会替换原版视频设置界面，第三方无法向其中追加选项。

## 构建

```bash
gradle build
```

产物位于 `build/libs/`。

## 许可证

[AGPL-3.0](LICENSE)