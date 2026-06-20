
# 工业时代 2：重构

<img src="https://img.shields.io/badge/Minecraft-1.20.1-brightgreen" alt="Minecraft 1.20.1">
<img src="https://img.shields.io/badge/Forge-47.4.20-orange" alt="Forge 47.4.20">
<img src="https://img.shields.io/badge/Version-2.10.22--ex120-blue" alt="Version 2.10.22-ex120">
<img src="https://img.shields.io/badge/License-ARR-red" alt="License ARR">

[English Readme](./README_EN.md)

本项目代码基于官方构建版本 `2.9.40-ex119` 反编译得到，并从 `2.8.222-ex112` 迁移了缺失与错误的功能。

## 写在前面

IC2 死了。没有官方回复，没有开源，继任开发者缺席逾年，一片死寂。IC2 Dev Team 曾明确声称：永远不会开源 IC2。

但社区的呼声从未消停。玩家们等待了太久——等待一个能在高版本运行的、原汁原味的 IC2。

于是有人站了出来。[IC2CR](https://github.com/yu1745/ic2-fabric) 率先开源，目标平台是 1.20.1 Fabric，声势不小。然而翻开一看：机制改了，数值改了，UI 也改了——甚至迁移工作大量依赖 AI 代劳。完成度或许尚可，但那已经不是 IC2 了。那是一个以 IC2 为名的别的什么东西。

我们不一样。我们的目标从一开始就只有一个：**在不改变原有功能与数值的前提下，将 IC2 完整地带到 1.20.1。**

最终，我们做到了。交到玩家手上的，是一份运行在 1.20.1 Forge / NeoForge 上的、完整的、可玩的 IC2——它还是那个你记忆中的 IC2。

## 与原版的差异

我们深知玩家选择本 mod 是为了还原体验，因此对原版的改动极为克制。以下是全部变更，无一隐瞒：

- **移除了物品形态的 UU 物质**（`uu_matter`，即粉色粘球）及其下游相关机器。IC2 Dev Team 早在 `2.8.222-ex112` 中便已删除物质生成机（`mass_fabricator`）的合成配方，
  遗留的 UU 物质复制功能已无存在意义。
- **移除了精炼铁锭**（`refined_iron_ingot`）。这是 IC2C 的物品，并非 IC2Exp 的组成部分——它的职能是替代钢锭。
- **调整了锡、铅、铀矿石的生成参数**（针对 `2.9.40-ex119`）。原版锡矿偏向高山生成，而玩家对锡的需求远高于铅、且更倾向于探索深层矿脉。我们在保持锡总量略多于铅的前提下，优化了其竖直分布，使游戏节奏更为合理。
- **调整了充电座的无线充电方式**。原版充电座只能为玩家身上不高于充电座电压等级的物品充电，且无法为玩家身上高于充电座电压等级的物品充电。我们改为：充电座可以为玩家身上任何电压等级的物品充电，但充电速度仍然受到充电座电压等级的限制。

## 版权声明

[IC2 Dev Team 引文](https://forum.industrial-craft.net/thread/9843-mc-1-7-ic%C2%B2-v-2-1-x-2-2-x-experimental/?postID=131008#post131008)

仓库许可证为 All Rights Reserved / No License。本项目的版权归原 IC2 Dev Team 所有。我们不拥有 IC2 的代码和资源文件版权，也不声称拥有 IC2 的代码和资源文件版权。我们只是对 IC2 进行反编译、修复、迁移和维护。

## 快速开始

请使用 Java 17 构建和编译本分支。

其他信息请查看 [Release](./release.md)。

```shell
gradlew build     # 构建 jar 包
gradlew runClient # 运行测试环境
gradlew runServer # 运行测试服务器
```
