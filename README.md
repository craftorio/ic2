
# IndustrialCraft 2 重构项目

<img src="https://img.shields.io/badge/Minecraft-1.12.2-brightgreen" alt="Minecraft 1.12.2">
<img src="https://img.shields.io/badge/Forge-14.23.5.2864-orange" alt="Forge 14.23.5.2864">
<img src="https://img.shields.io/badge/Version-2.8.222--ex112-blue" alt="Version 2.8.222-ex112">
<img src="https://img.shields.io/badge/License-ARR-red" alt="License ARR">

## 关于

**IndustrialCraft 2** (IC²) 是 Minecraft 史上最具影响力的工业科技模组之一。从简单的电力系统到核反应堆工程，从金属成型到农作物杂交，IC² 为方块世界带来了（较为）完整的工业化体验。

本仓库基于 `2.8.222-ex112` 版本的 IC² 进行逆向工程和重构，旨在提供一个清晰、可维护的代码库，供开发者学习和扩展。我们完全保留了原版的核心机制，同时重构了代码结构，优化了性能，并不会添加一些现代化的特性。

本仓库是 **PRIVATE** 的，仅供授权开发者访问和贡献。我们欢迎对 IC² 经典机制感兴趣的开发者加入，共同维护这个工业时代的经典。

---

## 核心特性

### 能源网络

- **EU 能量系统** — 经典的电能单位，支持多级电压
- **电缆与变压器** — 铜缆、锡缆、金缆、HV 缆，逐级变压
- **能量存储** — 电池箱、MFE、MFSU、各类储能设备
- **发电机** — 地热、风力、水力、太阳能、核能、RTG

### 机器与自动化

- **基础机器** — 打粉机、压缩机、提取机、金属成型机
- **高级产线** — 回收机、感应炉、批量合成器
- **流体系统** — 蒸汽、蒸馏水、冷却液全流程
- **物品/流体分配器** — 精准的按权重分配

### 核能工程

- **核反应堆** — 经典 EU 式裂变，棋盘布局
- **燃料棒与控制** — 铀棒、MOX、反射板、散热片
- **流体反应堆** — 配合热交换机输出热冷却液

### 农业与资源

- **作物杂交** — 数十种作物，遗传改良系统
- **UU物质** — 从能量中创造物质，复制任意资源
- **废料系统** — 回收机产生废料，加速 UU 生产

### 工具与装备

- **电动工具** — 电钻、链锯、采矿镭射枪
- **量子套装** — 终极防护
- **纳米套装** — 中等防护，高移动性

---

## 快速开始

```bash

# 1. 克隆仓库
git clone <repo-url> && cd ic2

# 2. 配置开发环境
./gradlew setupDecompWorkspace

# 3. IDE 导入
#    IDEA: 直接 Open → 选择 build.gradle
#    Eclipse: ./gradlew eclipse

# 4. 运行客户端
./gradlew runClient

# 5. 构建
./gradlew build

```

---

## 项目结构

```plain
ic2/
├── src/main/java/ic2/
│   ├── core/               #  核心逻辑 — 电网、机器、GUI、配方
│   │   ├── block/          #    方块与 TileEntity
│   │   │   ├── generator/  #    发电机
│   │   │   ├── machine/    #    加工机器
│   │   │   ├── reactor/    #    核反应堆
│   │   │   ├── steam/      #    蒸汽动力
│   │   │   └── storage/    #    储能设备 (储电箱/储罐)
│   │   ├── audio/          #    音效系统
│   │   ├── gui/            #    GUI 框架
│   │   ├── network/        #    网络同步
│   │   └── profile/        #    纹理与样式
│   ├── bcIntegration/      #   BuildCraft 联动
│   └── jeiIntegration/     #   JEI 配方兼容
├── src/main/resources/     #   assets、lang、textures
├── build.gradle            #   ForgeGradle 构建脚本
└── README.md
```

---

## 开发进度

| 阶段 | 状态 | 说明 |
|:-----|:----:|:-----|
| 反编译 | ✅ | Vineflower 反编译完成 |
| 映射 | ✅ | 核心类名映射完成 |
| 编译 | ✅ | ForgeGradle 3.x 可构建 |
| 纹理 | ✅ | 纹理资源已添加 |
| 能源网络 | ✅ | EnergyNet 全局可读 |
| 游戏内运行 | ✅ | 可进入世界 |
| ~~~ | ... | 持续开发中 |

---

## 技术栈

<p align="center">
  <img src="https://img.shields.io/badge/Java-1.8-ED8B00?style=flat-square&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Gradle-3.x-02303A?style=flat-square&logo=gradle&logoColor=white">
  <img src="https://img.shields.io/badge/ForgeGradle-3.x-aa4400?style=flat-square">
  <img src="https://img.shields.io/badge/MCP-snapshot__20171003-555555?style=flat-square">
  <img src="https://img.shields.io/badge/BuildCraft-8.0.0-3355cc?style=flat-square">
  <img src="https://img.shields.io/badge/JEI-4.16.1-994488?style=flat-square">
</p>

---

## ⚠️ 声明

- 本项目为 **IndustrialCraft 2** 的逆向工程与重构版本
- 仅供学习和研究使用
- 原始 IC² 版权归 **IC² Dev Team** 所有

---

<br>

<p align="center">
  <sub>Made with ⚡ and ☢️  ·  forge/1.12.2  ·  1162 Java files</sub>
</p>