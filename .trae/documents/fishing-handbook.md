# 钓鱼手册：书与笔 UI + JSON 数据驱动条目

## Context

南山钓竿的钓鱼战利品目前硬编码在 [ModFishingHook.java](file:///c:/Users/15244/IdeaProjects/flavor_immersed_daily_argo/src/main/java/com/flavor_immersed_daily_argo/fishing/ModFishingHook.java#L58-L88)：淡水池 19 种、海洋池 7 种（按生物群系判定，海洋=9 种 ocean 群系），每种对应一个生鲜物品。玩家需要一个"钓鱼手册"物品：用**原版书与笔（writable book）**样式的 UI，一页展示一种战利品的贴图与属性（生活习性/生活水深/食性/挣扎拉力/警惕性/进食速度/活动时段），并支持按属性筛选；条目由 JSON 驱动，方便附属 mod 加内容。

## 一、数据层（JSON 驱动）

- 目录：`data/<namespace>/fishing_handbook/*.json`（标准 datapack 位置，附属 mod 在自己的 namespace 下放文件即可，无需改代码）。
- 加载：`SimpleJsonResourceReloadListener` + Gson 解析，在 `flavor_immersed_daily_argoClient.java` 的 `RegisterClientReloadListenersEvent`（MOD bus）中注册。
- 新建 `fishing/FishingHandbookEntry.java`：record + 枚举
  - `Habitat`: FRESH / OCEAN / BOTH
  - `Diet`: OMNIVORE(杂食) / HERBIVORE(食草) / CARNIVORE(食肉) / SCAVENGER(食腐) / LIVE(活体)
  - `Activity`: DAY / NIGHT / SUNNY / RAIN（组合，空=全天全气候）
  - `Alertness`: LOW / MEDIUM / HIGH；`FeedingSpeed`: SLOW / MEDIUM / FAST
- 新建 `fishing/FishingHandbookData.java`：单例 `SimpleJsonResourceReloadListener`，持有 `List<FishingHandbookEntry>`，提供"按筛选条件取条目"方法；条目通过 `BuiltInRegistries.ITEM` 解析 item id 得到图标 ItemStack。
- JSON schema（**所有字段可选**，缺省即默认值）：

```json
{
  "item": "flavor_immersed_daily_argo:raw_grass_carp",
  "name": "草鲩",                 // 缺省用物品显示名
  "habitat": "fresh",            // fresh/ocean/both，缺省 both
  "biomes": ["warm_ocean"],      // 特定生物群系（群系 path），缺省空
  "depth": "无限制",              // 自由文本，缺省"无限制"
  "diet": "herbivore",           // 缺省 omnivore
  "struggle_tension": 8,         // 缺省 8
  "alertness": "medium",         // 缺省 medium
  "feeding_speed": "medium",     // 缺省 medium
  "activity": ["day", "sunny"],  // 缺省空（全时间全气候）
  "description": "可选简介文字"
}
```

- 初始内容：对齐 ModFishingHook 两个鱼池生成 **19 个条目 JSON**（`data/flavor_immersed_daily_argo/fishing_handbook/`）：草鲩/丁鱥/鳜鱼/乌鳢/虾虎鱼/对虾/大闸蟹 = `both`，其余 = `fresh`；食性/活动时段按常见生物学填写（如清道夫=scavenger+夜行、鲶鱼/泥鳅/大闸蟹/对虾=夜行），水深默认无限制，拉力/警惕/进食全用默认值。数值只是初始内容，用户可随时改 JSON。

## 二、物品与注册

- 新建 `item/FishingHandbookItem.java`：`use()` 客户端 `Minecraft.getInstance().setScreen(new FishingHandbookScreen())`，返回 `sidedSuccess`。
- [ModItems.java](file:///c:/Users/15244/IdeaProjects/flavor_immersed_daily_argo/src/main/java/com/flavor_immersed_daily_argo/all/ModItems.java)：`REGISTRY.register("fishing_handbook", () -> new FishingHandbookItem(new Item.Properties()))`（放在钓竿注册附近）。
- [ModCreativeTabs.java](file:///c:/Users/15244/IdeaProjects/flavor_immersed_daily_argo/src/main/java/com/flavor_immersed_daily_argo/all/ModCreativeTabs.java)：加入"农牧渔产"标签。
- 物品模型：**手写** `assets/flavor_immersed_daily_argo/models/item/fishing_handbook.json`，parent=`minecraft:item/generated`，layer0=`minecraft:item/writable_book`（零贴图成本、样式贴合书与笔）。FIDItemModelProvider 是显式列条目的（钓竿/抄网也手写），**不把该物品加入 provider** 即可。

## 三、UI（书与笔风格，新文件 fishing/FishingHandbookScreen.java）

- 继承 `Screen`（纯客户端，无容器菜单）；背景用原版书与笔同款纹理 `minecraft:textures/gui/book.png`，固定放大比例（如 1.5 倍 → 384×288）居中绘制，双页感布局。
- **左页**：书名"南山钓竿钓获图鉴"；下方 5 行筛选（每行=类别标签+可点选项，点击切换高亮，同类别多选 OR、跨类别 AND、不选=不限）：
  - 水域：淡水 / 海水
  - 食性：杂食 / 食草 / 食肉 / 食腐 / 活体
  - 活动：全天 / 昼 / 夜 / 晴 / 雨
  - 警惕性：低 / 中 / 高
  - 进食速度：慢 / 中 / 快
- 左页底部：筛选后的战利品列表（行=迷你图标+名字，点击选中，条目多时滚轮滚动）。
- **右页**：选中条目详情——大图标（`GuiGraphics.renderItem`，如 32×32）、名字、7 行属性（生活习性[含生物群系]、生活水深、食性、挣扎拉力、警惕性、进食速度、活动时段）、可选简介自动换行；未选中显示"← 在左侧选择战利品"。
- 筛选状态存于 Screen 内存态，切换即实时刷新列表。

## 四、本地化

- `zh_cn.json`：物品名"钓鱼手册"、书名、筛选标签/选项、属性标签、"无限制/中等"等文案键。
- [ModLangs.java](file:///c:/Users/15244/IdeaProjects/flavor_immersed_daily_argo/src/main/java/com/flavor_immersed_daily_argo/datagen/lang/ModLangs.java) + 生成的 `en_us.json`：同步英文文案。

## 关键复用

- 物品注册 / 创意标签 / 语言生成沿用 ModItems / ModCreativeTabs / ModLangs 现有模式。
- 条目图标不额外做贴图，直接渲染物品原模型/贴图。
- Screen 参考 [FishingRodModScreen.java](file:///c:/Users/15244/IdeaProjects/flavor_immersed_daily_argo/src/main/java/com/flavor_immersed_daily_argo/fishing/FishingRodModScreen.java) 的自绘背景风格。

## 验证

1. `gradlew compileJava --offline` 编译通过。
2. `runClient`：手持钓鱼手册右键 → 打开书 UI；检查双页布局、点筛选实时过滤列表、点击条目右页属性齐全、滚轮滚动；用钓竿钓到的鱼与手册条目一一对应。
3. 附属 mod 兼容：在任意 `data/<ns>/fishing_handbook/` 放一个条目 JSON，F3+T 重载资源后手册应出现该条目。
