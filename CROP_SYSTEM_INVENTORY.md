# IC2 杂交系统完整清单 (基于 forge/1.12.2)

> 用于指导 forge/1.20.1 的杂交系统迁移工作

---

## 一、API 层 (`ic2.api.crops`)

### 核心类

| 类名 | 类型 | 职责 |
|------|------|------|
| `CropCard` | abstract class | 所有作物的基类，定义生长/杂交/收获/掉落等行为 |
| `Crops` | abstract class | 全局作物注册表单例，管理所有作物注册、基础种子映射、群系加成 |
| `ICropTile` | interface | 作物方块实体的接口，暴露所有状态读写方法 |
| `ICropSeed` | interface | 种子物品的接口，读写 NBT 中的作物、四维属性、扫描等级 |
| `CropProperties` | class (immutable) | 作物六维属性: tier, chemistry, consumable, defensive, colorful, weed |
| `BaseSeed` | class (POJO) | 将原版物品映射到作物卡片 + 初始大小和三维属性 |
| `CropSoilType` | enum | 土壤类型枚举 |
| `ExampleCropCard` | class | 供附属模组开发者参考的示例作物 |

### CropCard 关键方法

```
getId() / getOwner()          - 唯一标识 (owner:name)
getProperties()               - 六维属性
getAttributes()               - 字符串属性标签 (杂交权重计算关键)
getMaxSize()                  - 最大生长阶段
getGrowthDuration()           - 每阶段生长点数 (默认 tier * 200)
canGrow() / canCross()       - 能否生长 / 能否参与杂交
canBeHarvested()             - 是否可收获
getOptimalHarvestSize()      - 最佳收获大小
dropGainChance()             - 产物掉落概率 (基础 0.95^tier)
getGains() / getSeeds()      - 收获产物 / 种子袋
getSizeAfterHarvest()        - 收获后回退到的阶段
onRightClick() / onLeftClick() - 交互
onEntityCollision()          - 碰撞 (默认冲刺踩踏)
tick()                       - 每 tick 行为
isWeed()                     - 是否判定为杂草
getEmittedLight()            - 发光等级
getRootsLength()             - 根系深度
```

---

## 二、核心实现 (`ic2.core.crop`)

### 2.1 IC2Crops — 全局注册表

继承 `Crops`，管理:
- `cropMap`: `Map<owner, Map<id, CropCard>>` — 所有已注册作物
- `baseSeeds`: `Map<ItemStack, BaseSeed>` — 原版物品→作物映射
- 群系湿度/养分加成表

**初始化流程:**
1. `init()` → 设置群系加成 → `registerCrops()` → `registerBaseSeeds()`
2. 通过 `CropRegisterEvent` 允许附属注册额外作物

### 2.2 IC2CropCard — IC2 作物基类

owner 固定为 `"ic2"`，纹理路径 `ic2:blocks/crop/{id}_{size}`

### 2.3 TileEntityCrop (ICropTile 实现) — 核心逻辑

**状态字段 (NBT 持久化):**

| 字段 | 类型 | 范围 | 说明 |
|------|------|------|------|
| crop | CropCard | null/ref | 当前作物 |
| currentSize | byte | 1~maxSize | 当前生长阶段 |
| statGrowth | byte | 0~31 | Gr 属性 |
| statGain | byte | 0~31 | Ga 属性 |
| statResistance | byte | 0~31 | Re 属性 |
| storageNutrients | short | 0~100 | 养分存储 |
| storageWater | short | 0~200 | 水存储 |
| storageWeedEX | short | 0~150 | 除草剂存储 |
| growthPoints | short | 0~N | 生长点数 |
| scanLevel | byte | 0~4 | 扫描等级 |
| crossingBase | boolean | - | 是否为杂交架 (空 crop stick) |
| customData | NBTTagCompound | - | 自定义数据 |
| terrainHumidity | byte | - | 环境湿度 |
| terrainNutrients | byte | - | 环境养分 |
| terrainAirQuality | byte | - | 空气质量 |

**核心 Tick 逻辑 (每 256 tick 执行):**

1. **空闲处理**: 无作物时 → 尝试杂交 / 尝试蔓延 / 随机长杂草
2. **作物 tick**: 调用 `crop.tick(this)`
3. **生长计算**:
   - `baseGrowth = 3 + random(7) + statGrowth`
   - `minimumQuality = (tier-1)*4 + statGrowth + statGain + statResistance`
   - `providedQuality = weightInfluences(humidity, nutrients, air) * 5`
   - 质量足够 → 加速; 质量不足 → 惩罚 (甚至枯萎)
4. **资源消耗**: 每 tick 消耗 1 养分 + 1 水
5. **杂草扩散**: 如果是杂草，概率向邻居传播

**杂交算法:**

1. 收集 4 邻方可杂交邻居 (size≥3 的成熟作物)
2. 需要至少 2 个邻居
3. 对每个已注册作物计算权重:
   - 相同作物 → +500
   - 对 5 个属性维度取 `-|delta| + 2`
   - 每个匹配的 attribute 标签 → +5
   - tier 差 > 1 → -2*差; tier 差 < -3 → -差
4. 加权随机选出新作物
5. 子代属性 = 邻居平均值 + 随机变异 (-count ~ +count)，限制 0~31

**蔓延算法:**
- 仅有一个邻居时可能触发
- 基础概率 4/16，高 Gr/Re 加成

**杂草判定:**
- 当前作物是 weed，或 size≥2 且 statGrowth≥24

**交互:**
- 空手右击 → `crop.onRightClick()` (通常执行收获)
- 左键 → `crop.onLeftClick()` (通常拔除)
- 拿 crop stick 右击空地 → 放置杂交架
- 拿肥料右击 → +100 养分
- 拿水/除草剂容器右击 → 补充
- 拿种子右击 → 种植

---

## 三、全部作物清单 (52 种)

### 3.1 原版作物 (1 种 + CropVanilla 基类)

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 产物 | 备注 |
|---|----|------|------|------|------|------|------|---------|----------|--------|------|------|
| 1 | `weed` | 0 | 0 | 0 | 1 | 0 | 5 | 5 | Weed, Bad | IC2 Team | 无 | 杂草，不可收获 |

### 3.2 原版农业作物 (CropVanilla 基类)

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 产物 | BaseSeed |
|---|----|------|------|------|------|------|------|---------|----------|--------|------|----------|
| 2 | `wheat` | 1 | 0 | 4 | 0 | 0 | 2 | 7 | Yellow, Food, Wheat | Notch | 小麦 | 小麦种子 |
| 3 | `pumpkin` | 1 | 0 | 1 | 0 | 3 | 1 | 4 | Orange, Decoration, Stem | Notch | 南瓜 | 南瓜种子 |
| 4 | `melon` | 2 | 0 | 4 | 0 | 2 | 0 | 4 | Green, Food, Stem | Chao | 西瓜/西瓜片 | 西瓜种子 |
| 5 | `carrots` | 2 | 0 | 4 | 0 | 0 | 2 | 3 | Orange, Food, Carrots | Notch | 胡萝卜 | 胡萝卜 |
| 6 | `potato` | 2 | 0 | 4 | 0 | 0 | 2 | 4 | Yellow, Food, Potato | IC2 Team | 马铃薯/毒马铃薯 | 马铃薯 |
| 7 | `beetroots` | 1 | 0 | 4 | 0 | 1 | 2 | 3 | Red, Food, Beetroot | Notch | 甜菜根 | 甜菜种子 |

### 3.3 花卉作物 (CropColorFlower)

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 产物 | BaseSeed |
|---|----|------|------|------|------|------|------|---------|----------|--------|------|----------|
| 8 | `dandelion` | 2 | 1 | 1 | 0 | 5 | 1 | 4 | Yellow, Flower | Notch | 蒲公英黄 (11) | 蒲公英 |
| 9 | `rose` | 2 | 1 | 1 | 0 | 5 | 1 | 4 | Red, Flower, Rose | Notch | 玫瑰红 (1) | 虞美人 |
| 10 | `blackthorn` | 2 | 1 | 1 | 0 | 5 | 1 | 4 | Black, Flower, Rose | Alblaka | 墨囊 (0) | — |
| 11 | `tulip` | 2 | 1 | 1 | 0 | 5 | 1 | 4 | Purple, Flower, Tulip | Alblaka | 紫色染料 (5) | — |
| 12 | `cyazint` | 2 | 1 | 1 | 0 | 5 | 1 | 4 | Blue, Flower | Alblaka | 青色染料 (6) | — |

### 3.4 特殊植物作物

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 产物 | BaseSeed | 特殊机制 |
|---|----|------|------|------|------|------|------|---------|----------|--------|------|----------|----------|
| 13 | `reed` | 2 | 0 | 0 | 1 | 0 | 2 | 3 | Reed | Notch | 甘蔗 (size-1) | 甘蔗 | 无践踏碰撞 |
| 14 | `stickreed` | 4 | 2 | 0 | 1 | 0 | 1 | 4 | Reed, Resin | raa1337 | 甘蔗/树脂 | — | size=4 收树脂 |
| 15 | `cocoa` | 3 | 1 | 3 | 0 | 4 | 0 | 4 | Brown, Food, Stem | Notch | 可可豆 | 可可豆 | 需养分≥3 才生长 |
| 16 | `flax` | 2 | 1 | 1 | 2 | 0 | 1 | 4 | Silk, Vine, Addictive | Eloraam | 线 | — | |
| 17 | `venomilia` | 3 | 3 | 1 | 3 | 3 | 3 | 6 | Purple, Flower, Tulip, Poison | raGan | 紫色染料/Grin粉末 | — | size=5 有中毒碰撞; 右键/左键触发毒素 |

### 3.5 蘑菇作物 (CropBaseMushroom)

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 产物 | BaseSeed |
|---|----|------|------|------|------|------|------|---------|----------|------|----------|
| 18 | `red_mushroom` | 2 | 0 | 4 | 0 | 0 | 4 | 3 | Red, Food, Mushroom | 红蘑菇 | 红蘑菇 |
| 19 | `brown_mushroom` | 2 | 0 | 4 | 0 | 0 | 4 | 3 | Brown, Food, Mushroom | 棕蘑菇 | 棕蘑菇 |

### 3.6 下界/特殊维度作物

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 产物 | BaseSeed | 特殊机制 |
|---|----|------|------|------|------|------|------|---------|----------|--------|------|----------|----------|
| 20 | `nether_wart` | 5 | 4 | 2 | 0 | 2 | 1 | 3 | Red, Nether, Ingredient, Soulsand | Notch | 地狱疣 | 地狱疣 | 站在灵魂沙上加速; 站在雪上→terra_wart |
| 21 | `terra_wart` | 5 | 2 | 4 | 0 | 3 | 0 | 3 | Blue, Aether, Consumable, Snow | IC2 Team | Terra Wart | terra_wart | 站在雪上加速; 站在灵魂沙上→nether_wart |

### 3.7 树苗作物 (CropBaseSapling)

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 产物 | BaseSeed |
|---|----|------|------|------|------|------|------|---------|----------|--------|------|----------|
| 22 | `oak_sapling` | 3 | 1 | 0 | 4 | 4 | 0 | 5 | Leaves, Sapling, Green | Speiger | 橡木 (+苹果 25%) | 橡树苗 |
| 23 | `spruce_sapling` | 3 | 1 | 0 | 4 | 4 | 0 | 5 | Leaves, Sapling, Green | Speiger | 云杉木 | 云杉树苗 |
| 24 | `birch_sapling` | 3 | 1 | 0 | 4 | 4 | 0 | 5 | Leaves, Sapling, Green | Speiger | 白桦木 | 白桦树苗 |
| 25 | `jungle_sapling` | 3 | 1 | 0 | 4 | 4 | 0 | 5 | Leaves, Sapling, Green | Speiger | 丛林木 | 丛林树苗 |
| 26 | `acacia_sapling` | 3 | 1 | 0 | 4 | 4 | 0 | 5 | Leaves, Sapling, Green | Speiger | 金合欢木 | 金合欢树苗 |
| 27 | `dark_oak_sapling` | 3 | 1 | 0 | 4 | 4 | 0 | 5 | Leaves, Sapling, Green | Speiger | 深色橡木 | 深色橡树苗 |

### 3.8 金属作物 (CropBaseMetalCommon/Uncommon)

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 产物 | 根系需求 |
|---|----|------|------|------|------|------|------|---------|----------|------|----------|
| 28 | `ferru` | 6 | 2 | 0 | 0 | 1 | 0 | 4 | Gray, Leaves, Metal | 小撮铁粉 | oreIron / blockIron |
| 29 | `cyprium` | 6 | 2 | 0 | 0 | 1 | 0 | 4 | Orange, Leaves, Metal | 小撮铜粉 | oreCopper / blockCopper |
| 30 | `stagnium` | 6 | 2 | 0 | 0 | 1 | 0 | 4 | Shiny, Leaves, Metal | 小撮锡粉 | oreTin / blockTin |
| 31 | `plumbiscus` | 6 | 2 | 0 | 0 | 1 | 0 | 4 | Dense, Leaves, Metal | 小撮铅粉 | oreLead / blockLead |
| 32 | `aurelia` | 6 | 2 | 0 | 0 | 2 | 0 | **5** | Gold, Leaves, Metal | 小撮金粉 | oreGold / blockGold |
| 33 | `shining` | 6 | 2 | 0 | 0 | 2 | 0 | **5** | Silver, Leaves, Metal | 小撮银粉 | oreSilver / blockSilver |

**金属作物特殊机制:**
- Common (铁/铜/锡/铅): size 3→4 需要下方有对应矿石/块
- Uncommon (金/银): size 4→5 需要下方有对应矿石/块，maxSize=5
- 收获必定回退到 size=2
- 产物掉落率折半

### 3.9 高级IC2作物

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 产物 | 特殊机制 |
|---|----|------|------|------|------|------|------|---------|----------|--------|------|----------|
| 34 | `redwheat` | 6 | 3 | 0 | 0 | 2 | 0 | 7 | Red, Redstone, Wheat | raa1337 | 红石/小麦 | 需光照5~10; size=7发光7; 被红石信号激活收小麦 |
| 35 | `coffee` | 7 | 1 | 4 | 1 | 2 | 0 | 5 | Leaves, Ingredient, Beans | Snoochy | 咖啡豆 | 需光照≥9; 不同阶段生长速度不同 |
| 36 | `hops` | 5 | 2 | 2 | 0 | 1 | 1 | 7 | Green, Ingredient, Wheat | IC2 Team | 啤酒花 | 需光照≥9 |
| 37 | `eatingplant` | 6 | 1 | 1 | 3 | 1 | 4 | 6 | Bad, Food | Hasudako | 仙人掌 | size>1 吞噬附近生物; size<3 需光照>10; size≥3 需下方岩浆 |

### 3.10 动态注册作物 (GenericCropCard)

| # | ID | Tier | 化学 | 食用 | 防御 | 彩色 | 杂草 | MaxSize | 属性标签 | 发现者 | 主要产物 | 特殊掉落 | 特殊机制 |
|---|----|------|------|------|------|------|------|---------|----------|--------|----------|----------|----------|
| 38 | `blazereed` | 6 | 0 | 4 | 1 | 0 | 0 | 4 | Fire, Blaze, Reed, Sulfur | Mr. Brain | 烈焰粉 | 烈焰棒, 硫粉 | 收后回退到1 |
| 39 | `bobs_yer_uncle_ranks_berries` | 11 | 4 | 0 | 8 | 2 | 9 | 4 | Shiny, Vine, Emerald, Berylium, Crystal | GenerikB | BYUR berry | 绿宝石 | |
| 40 | `corium` | 6 | 0 | 2 | 3 | 1 | 0 | 4 | Cow, Silk, Vine | Gregorius Techneticies | 皮革 | — | |
| 41 | `corpse_plant` | 5 | 0 | 2 | 1 | 0 | 3 | 4 | Toxic, Undead, Vine, Edible, Rotten | Mr. Kenny | 腐肉 | 骨头, 骨粉×2 | |
| 42 | `creeper_weed` | 7 | 3 | 0 | 5 | 1 | 3 | 4 | Creeper, Vine, Explosive, Fire, Sulfur, Saltpeter, Coal | General Spaz | 火药 | — | |
| 43 | `diareed` | 12 | 5 | 0 | 10 | 2 | 10 | 4 | Fire, Shiny, Reed, Coal, Diamond, Crystal | Diareed | 小撮钻石粉 | 钻石 | |
| 44 | `egg_plant` | 6 | 0 | 4 | 1 | 0 | 0 | 3 | Chicken, Egg, Edible, Feather, Flower, Addictive | Link | 鸡蛋 | 鸡肉, 羽毛×3 | 生长加速(900); 收后回退到2 |
| 45 | `ender_blossom` | 10 | 5 | 0 | 2 | 1 | 6 | 4 | Ender, Flower, Shiny | RichardG | 末影珍珠粉 | 末影珍珠×2, 末影之眼 | |
| 46 | `meat_rose` | 7 | 0 | 4 | 1 | 3 | 0 | 4 | Edible, Flower, Cow, Chicken, Pig, Sheep | VintageBeef | 粉色染料 | 牛肉/猪排/鸡肉/羊肉 | 生长减速(1500) |
| 47 | `milk_wart` | 6 | 0 | 3 | 0 | 1 | 0 | 3 | Edible, Milk, Cow | Mr. Brain | Milk Wart | — | 生长加速(900); 自带 BaseSeed |
| 48 | `oil_berries` | 9 | 6 | 1 | 2 | 1 | 12 | 3 | Fire, Dark, Reed, Rotten, Coal, Oil | Spacetoad | Oil Berry | — | |
| 49 | `slime_plant` | 6 | 3 | 0 | 0 | 0 | 2 | 4 | Slime, Bouncy, Sticky, Bush | Neowulf | 粘液球 | — | 收后回退到3 |
| 50 | `spidernip` | 4 | 2 | 1 | 4 | 1 | 3 | 4 | Toxic, Silk, Spider, Flower, Ingredient, Addictive | Mr. Kenny | 线 | 蜘蛛眼, 蜘蛛网 | 生长加速(600) |
| 51 | `tearstalks` | 8 | 1 | 2 | 0 | 0 | 0 | 4 | Healing, Nether, Ingredient, Reed, Ghast | Neowulf | 恶魂之泪 | — | |
| 52 | `withereed` | 8 | 2 | 0 | 4 | 1 | 3 | 4 | Fire, Undead, Reed, Coal, Rotten, Wither | CovertJaguar | 煤粉 | 煤炭×2 | |

---

## 四、机器

### 4.1 Cropmatron (作物监管机)

**文件:** `TileEntityCropmatron.java`
**功能:** 自动对 9×3×9 范围内的作物施加肥料、水和除草剂
**参数:**
- 能量: 10,000 EU, 1 tier
- 水槽: 2,000 mB
- 除草剂槽: 2,000 mB
- 肥料槽: 7 格 (只接受 fertilizer 物品)
- 升级槽: 4 格
- 扫描范围: X(-4~4), Y(-1~1), Z(-4~4)
- 每 10 tick 扫描一格
- 基础能耗: 1 EU/格 + 10 EU/操作
- 还会自动润湿耕地

**GUI:** 两个流体槽 + 肥料槽 + 电池槽 + 升级槽
**经典版本:** `TileEntityClassicCropmatron`

### 4.2 Crop Harvester (作物收割机)

**文件:** `TileEntityCropHarvester.java`
**功能:** 自动收获范围内作物的产物
**参数:**
- 能量: 10,000 EU, 1 tier
- 输出槽: 15 格
- 升级槽: 4 格
- 扫描范围: X(-4~4), Y(-1~1), Z(-4~4)
- 在最佳收获大小或最大大小时触发收获
- 能耗: 1 EU/扫描 + 20 EU/收获物

**注:** `@NotClassic` — 仅在非经典版本注册

### 4.3 机器升级属性

| 机器 | 升级属性 |
|------|----------|
| Cropmatron | Transformer, EnergyStorage, ItemConsuming, FluidConsuming |
| Crop Harvester | Transformer, EnergyStorage, ItemProducing |

---

## 五、工具与物品

### 5.1 Cropnalyzer (作物分析仪)

**文件:** `ItemCropnalyzer.java` / `HandHeldCropnalyzer.java`
**功能:**
- 手持式 GUI 设备，1 slot 输入 + 1 slot 输出 + 1 电池槽
- 扫描种子袋: 消耗能量逐步提升扫描等级 (0→1→2→3→4)
  - Level 0→1: 10 EU
  - Level 1→2: 90 EU
  - Level 2→3: 900 EU
  - Level 3→4: 9000 EU
- 对作物方块使用: 显示作物名/发现者/大小/养分/水/除草剂/生长点
- 存储: 100,000 EU, 128 EU/t, tier 2
- 扫描等级显示:
  - 0: "未知种子"
  - 1: 作物名称
  - 2: 属性标签
  - 3: Tier
  - 4: Gr/Ga/Re 具体数值

### 5.2 Crop Stick (作物架)

**文件:** `ItemCrop.java`
- 在耕地上放置作物架 (TileEntityCrop)
- 左键空间棒 → 放置杂交架
- 可存放在工具箱

### 5.3 Seed Bag (种子袋)

**文件:** `ItemCropSeed.java` (implements ICropSeed)
- 单个物品 (maxStackSize=1)
- NBT 数据: `{owner, id, growth, gain, resistance, scan}`
- 在作物架上右键 → 种植
- 属性继承: 种子袋的四维属性带入作物

### 5.4 CropResItemType (作物资源物品)

| 枚举名 | ID | 用途 |
|--------|-----|------|
| `coffee_beans` | 0 | 咖啡豆 (咖啡作物产物) |
| `coffee_powder` | 1 | 咖啡粉 |
| `fertilizer` | 2 | 肥料 (右击作物 +100 养分) |
| `grin_powder` | 3 | Grin 粉末 (Venomilia 特殊产物) |
| `hops` | 4 | 啤酒花 (Hops 作物产物) |
| `weed` | 5 | Weed (杂草相关) |
| `milk_wart` | 6 | Milk Wart (milk_wart 作物产物/BaseSeed) |
| `oil_berry` | 7 | Oil Berry (oil_berries 作物产物) |
| `bobs_yer_uncle_ranks_berry` | 8 | BYUR Berry |

---

## 六、核心机制详解

### 6.1 属性系统

**六维 CropProperties:** `(tier, chemistry, consumable, defensive, colorful, weed)`

每个维度在杂交权重计算中都有作用。两个作物属性越接近，杂交出新作物的概率越高。

**三维作物属性 (stat):**
- **Growth (Gr)** 0-31: 影响生长速度
- **Gain (Ga)** 0-31: 影响产物掉落数量/概率
- **Resistance (Re)** 0-31: 抗践踏、抗杂草、抗环境恶化枯萎

**Attribute 标签:**
- 纯字符串匹配 (大小写不敏感)
- 匹配的标签在杂交权重中 +5 分
- 描述作物的"基因"特征

### 6.2 生长机制

```
growthPoints += baseGrowth * (100 + (providedQuality - minimumQuality)) / 100
```
- 质量足够 → 加速
- 质量不足 → 减速 (甚至 reset 枯萎)
- 每 tick (`256 ticks`) 累积一次
- 当 growthPoints >= growthDuration → 生长一个阶段

### 6.3 环境系统

**Terrain Humidity:** 群系 + 耕地湿润 + 水存储
**Terrain Nutrients:** 群系加成 + 下方泥土层数 + 养分存储
**Terrain Air Quality:** 海拔 + 周围空气方块 + 露天

### 6.4 杂草系统

- **判定:** 作物=weed 或 (size≥2 且 Gr≥24)
- **扩散:** 每 tick 有概率随机感染邻居 (Re 抵抗)
- **除草剂 (Weed-EX):** 存储最多 150，消耗 5 阻挡一次感染
- **Gr≥24的作物也会变成杂草!**

### 6.5 BaseSeed 系统

将原版物品映射到作物卡片，玩家可直接用原版物品在作物架上种植:
- 小麦种子 → wheat
- 南瓜种子 → pumpkin
- 西瓜种子 → melon
- 地狱疣 → nether_wart
- 咖啡豆 → coffee
- 甘蔗 → reed (Gr=3, Ga=0, Re=2)
- 可可豆 → cocoa (Gr=0, Ga=0, Re=0)
- 虞美人 → rose
- 蒲公英 → dandelion
- 胡萝卜 → carrots
- 马铃薯 → potato
- 两种蘑菇 → red/brown_mushroom
- 仙人掌 → eatingplant
- 甜菜种子 → beetroots
- 6 种树苗 → 对应 sapling 作物

### 6.6 群系加成

| 群系类型 | 湿度加成 | 养分加成 |
|----------|---------|---------|
| WATER | +10 | — |
| WET | +10 | — |
| DRY | -10 | — |
| JUNGLE | — | +10 |
| SWAMP | — | +10 |
| MUSHROOM | — | +5 |
| FOREST | — | +5 |
| RIVER | — | +2 |
| PLAINS | — | 0 |
| SAVANNA | — | -2 |
| HILLS | — | -5 |
| MOUNTAIN | — | -5 |
| WASTELAND | — | -8 |
| END | — | -10 |
| NETHER | — | -10 |
| DEAD | — | -10 |

---

## 七、禁用作物配置

`ConfigUtil.getString(MainConfig.get(), "agriculture/disabledCrops")`

格式: `owner:id` 列表，例如 `ic2:weed,ic2:ferru`

被禁用的作物:
- 不会注册到 cropMap
- 其 BaseSeed 也不会注册
- weed 作物永远不被禁用

---

## 八、文件统计

| 类别 | 文件数 | 位置 |
|------|--------|------|
| API 接口/类 | 8 | `ic2.api.crops` |
| 核心实现 | 6 | `ic2.core.crop` |
| 作物卡片 | 24 | `ic2.core.crop.cropcard` |
| 机器 TE | 2 | `ic2.core.block.machine.tileentity` |
| 机器 Container | 2 | `ic2.core.block.machine.container` |
| 机器 GUI | 2 (非经典+1) | `ic2.core.block.machine.gui` |
| 工具 | 4 | `ic2.core.item.tool` |
| 物品 | 2 | `ic2.core.item` + `ic2.core.crop` |
| 物品类型 | 1 | `ic2.core.item.type` |
| 方块状态/模型 | 60+ | `assets/ic2/blockstates/`, `models/block/crop/`, `textures/` |
| 战利品表 | 50+ | `data/ic2/loot_tables/blocks/` |

---

## 九、1.20.1 迁移注意事项

1. **52 种作物全部需要迁移** — 包括 1 种杂草、37 种硬编码 + 14 种 GenericCropCard
2. **TileEntityCrop → BlockEntityCrop** — 迁移到 1.20.1 的 BlockEntity 体系
3. **BlockState 体系重构** — 1.12.2 使用 `renderStateProperty` 自定义属性，1.20.1 需改用 blockstates JSON
4. **ICropTile 接口** — 需要适配 1.20.1 的 API
5. **作物纹理** — 从 1.12.2 以 crop_owner 为前缀到 1.20.1 需检查纹理路径变更
6. **流体 API** — 从 Forge Fluid 迁移到 1.20.1 的 Fluid API
7. **GUI** — 从 GuiScreen 迁移到 Screen 体系
8. **NBT** — `NBTTagCompound` → `CompoundTag`
9. **Config** — 配置系统完全变化
10. **能量 API** — 检查 IC2 能量系统迁移状态
