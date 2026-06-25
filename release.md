# 2.10.31-ex120

欢迎来到制作和反馈清单。这里记录了下版本的计划和已知问题。

We'd apologize for our documentations are in Chinese. Please use translation.

## 已知问题

### 紧急

- 创造模式物品栏乱序（目前是按照注册顺序的，后续会调整为分类顺序）
- 末影珍珠粉材质更改
- 拟态板完全是坏的 `ItemObscurator`
- 日光灯不能用（仅限合成）
- 堆叠状态的能量水晶和兰波顿水晶贴图不对

### 不急

- 纳米剑 `nano_saber`: 右键激活不显示剑
- 自动弹出升级、自动抽入升级、撬棍：原版还没有配方
- 尚未添加：管道系统

### 要做

- 燃料棒（锂）和燃料棒（氚）：考虑后续版本的聚变堆

### 准备迁移的联动 Mod

- [Iridium Source](https://www.mcmod.cn/class/2588.html)
- [METS](https://www.mcmod.cn/class/2217.html)
- [Gravitation Suite](https://www.mcmod.cn/class/255.html)
- [ASP](https://www.mcmod.cn/class/23.html)

## 与原版的区别

### 小巧思

- 充电座：现在的无线充电机制不受电压等级限制，但充电速率仍然受限于充电座的规格；充电的顺序为：主手、副手、头盔、胸甲、护腿、靴子、快捷栏0~8、物品栏从左上到右下。
- 删除：旧版粉色粘球形态的 UU 物质、精炼铁锭
- 翻译：以中文为基准, 重做翻译。全部扁平化
- 矿脉生成，请使用 [JustEnoughResources](https://github.com/way2muchnoise/JustEnoughResources) 查看。

### 大特性

- IC2 电网目前支持 [Applied Energetics 2](https://modrinth.com/mod/ae2) 的能源接收器！
- 您可以安装 [Configured](https://modrinth.com/mod/configured) 来使用 GUI 调整 IC2 的配置文件！
- 高级采矿机现在支持采矿过滤卡来自定义更多的矿石名单！
